/*
 * RHQ Management Platform
 * Copyright (C) 2013 Red Hat, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.rhq.plugin.annotation.processor;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import javax.lang.model.util.SimpleTypeVisitor6;

/**
 * @author Lukas Krejci
 * @since 4.9
 */
public class AnnotationValueExtractor {

    private final ProcessingContext context;
    private final AnnotationMirror annotation;

    //This is brutally unsafe, but I hate writing stuff multiple times ;)

    @SuppressWarnings("unchecked")
    private static class Extractor<T> extends SimpleAnnotationValueVisitor6<T, ProcessingContext> {

        private final Class<?> type;

        public Extractor(Class<?> type) {
            this.type = type;
        }

        @Override
        public T visitArray(List<? extends AnnotationValue> vals, ProcessingContext context) {
            T ret = (T) Array.newInstance(type.getComponentType(), vals.size());

            int i = 0;
            for(AnnotationValue v : vals) {
                Array.set(ret, i++, v.accept(this, context));
            }
            return ret;
        }

        @Override
        public T visitBoolean(boolean b, ProcessingContext context) {
            return (T) (Boolean) b;
        }

        @Override
        public T visitByte(byte b, ProcessingContext context) {
            return (T) (Byte) b;
        }

        @Override
        public T visitChar(char c, ProcessingContext context) {
            return (T) (Character) c;
        }

        @Override
        public T visitDouble(double d, ProcessingContext context) {
            return (T) (Double) d;
        }

        @Override
        public T visitFloat(float f, ProcessingContext context) {
            return (T) (Float) f;
        }

        @Override
        public T visitInt(int i, ProcessingContext context) {
            return (T) (Integer) i;
        }

        @Override
        public T visitLong(long i, ProcessingContext context) {
            return (T) (Long) i;
        }

        @Override
        public T visitShort(short s, ProcessingContext context) {
            return (T) (Short) s;
        }

        @Override
        public T visitString(String s, ProcessingContext context) {
            return (T) s;
        }

        @Override
        public T visitEnumConstant(VariableElement c, ProcessingContext nothing) {
            return (T) c.getSimpleName().toString();
        }

        @Override
        public T visitType(TypeMirror t, ProcessingContext anything) {
            return t.accept(new SimpleTypeVisitor6<T, ProcessingContext>() {
                @Override
                public T visitDeclared(DeclaredType t, ProcessingContext anything) {
                    return (T) ((TypeElement) t.asElement()).getQualifiedName().toString();
                }
            }, anything);
        }
    }

    public AnnotationValueExtractor(AnnotationMirror annotation,
        ProcessingContext context) {

        this.annotation = annotation;
        this.context = context;
    }

    /**
     * Returns a value of a field as given type.
     * Note that this works in the realm of type mirrors so really only primitive types and enums are supported here.
     * <p/>
     * In particular, the only valid return type for annotation fields with some class type is "string" and will return
     * the fully qualified class name of that type - this is because most probably such type is available in the
     * annotation processor's classpath.
     * <p/>
     * Note that this method is *NOT* usable with fields with some annotation type. These have to processed differently.
     *
     * @param fieldName the name of the field
     * @param cls the required return type (use String.class for annotation fields of class type)
     * @param <T> the type parameter of the class
     * @return the value
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> T getValue(String fieldName, Class<T> cls) {
        AnnotationValue value = Util.findAnnotationFieldWithoutDefault(annotation, fieldName, context);
        if (value == null) {
            return null;
        }

        Object ret = value.accept(new Extractor<Object>(cls), context);

        if (cls.isEnum()) {
            return (T) Enum.valueOf((Class<Enum>)cls, (String) ret);
        } else {
            return (T) ret;
        }
    }

    /**
     * Use for extracting the value of the annotation contained in given field.
     * @param fieldName the name of the field with annotation value
     * @param visitor the visitor to use to extract the value from the annotation
     * @param <T> the return type representing the value of the annotation
     * @return the value representing the annotation as extracted by the visitors
     */
    public <T> T getAnnotationValue(String fieldName, AnnotationValueVisitor<T, ProcessingContext> visitor) {
        AnnotationValue value = Util.findAnnotationFieldWithoutDefault(annotation, fieldName, context);
        if (value == null) {
            return null;
        }

        return value.accept(visitor, context);
    }

    /**
     * A helper to extract the value out of the fields with arrays of annotations.
     *
     * @param fieldName the name of the field
     * @param elementVisitor the visitor able to extract the annotation of which the field contains an array
     * @param <T> the type representing the annotation as extracted by the visitor
     * @return a list of values representing the array of annotations
     */
    public <T> List<T> getAnnotationArrayValue(String fieldName, final AnnotationValueVisitor<T, ProcessingContext> elementVisitor) {
        AnnotationValue value = Util.findAnnotationFieldWithoutDefault(annotation, fieldName, context);
        if (value != null) {
            final List<T> ret = new ArrayList<T>();
            value.accept(new SimpleAnnotationValueVisitor6<Void, Void>(){
                @Override
                public Void visitArray(List<? extends AnnotationValue> vals, Void nothing) {
                    for(AnnotationValue v : vals) {
                        T element = v.accept(elementVisitor, context);
                        ret.add(element);
                    }

                    return null;
                }
            }, null);

            return ret;
        }

        return null;
    }

    /**
     * Use this convenience method in cases where {@link #getValue(String, Class)} does not support (i.e. extract
     * annotation values, etc).
     *
     * @param fieldName the name of the field on the annotation
     * @param visitor the visitor to use to extract the value from the field
     * @param <T> the return type
     * @return the value extracted using the visitor
     */
    public <T> T extractField(String fieldName, AnnotationValueVisitor<T, ProcessingContext> visitor) {
        AnnotationValue value = Util.findAnnotationFieldWithoutDefault(annotation, fieldName, context);
        if (value == null) {
            return null;
        }

        return value.accept(visitor, context);
    }
}
