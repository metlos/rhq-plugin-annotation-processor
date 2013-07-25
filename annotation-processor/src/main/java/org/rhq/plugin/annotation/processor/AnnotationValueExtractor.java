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
            for (AnnotationValue v : vals) {
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
     * Returns a value of a field as given type. Note that this works in the realm of type mirrors so really only
     * primitive types, arrays and enums are supported here.
     * <p/>
     * In particular, the only valid return type for annotation fields with some class type is "string" and will return
     * the fully qualified class name of that type - this is because most probably such type is not available in the
     * annotation processor's classpath.
     * <p/>
     * Note that this method is *NOT* usable with fields with some annotation type. These have to processed
     * differently.
     *
     * @param fieldName      the name of the field
     * @param cls            the required return type (use String.class for annotation fields of class type)
     * @param returnDefaults whether to return the default value of the field or not
     *
     * @return the value or null if such field doesn't exist or has the default value
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> T extractValue(String fieldName, Class<T> cls, boolean returnDefaults) {
        AnnotationValue value = returnDefaults ? Util.findAnnotationField(annotation, fieldName, context) :
            Util.findAnnotationFieldWithoutDefault(annotation, fieldName, context);
        if (value == null) {
            return null;
        }

        Object ret = value.accept(new Extractor<Object>(cls), context);

        if (cls.isEnum()) {
            return (T) Enum.valueOf((Class<Enum>) cls, (String) ret);
        } else {
            return (T) ret;
        }
    }

    /**
     * Use this convenience method in cases where {@link #extractValue(String, Class, boolean)} does not support (i.e.
     * extract annotation values, etc).
     *
     * @param fieldName      the name of the field on the annotation
     * @param returnDefaults whether to return the default value of the field or not
     * @param visitor        the visitor to use to extract the value from the field  @return the value extracted using
     *                       the visitor
     */
    public <T> T extractValue(String fieldName, boolean returnDefaults,
        AnnotationValueVisitor<T, ProcessingContext> visitor) {
        AnnotationValue value = returnDefaults ? Util.findAnnotationField(annotation, fieldName, context) :
            Util.findAnnotationFieldWithoutDefault(annotation, fieldName, context);
        if (value == null) {
            return null;
        }

        return value.accept(visitor, context);
    }

    /**
     * A helper to extract the value out of the fields with arrays of annotations.
     *
     * @param fieldName      the name of the field
     * @param returnDefaults whether to return the default value of the field or not
     * @param elementVisitor the visitor able to extract the annotation of which the field contains an array  @return a
     *                       list of values representing the array of annotations
     */
    public <T> List<T> extractArrayValue(String fieldName,
        boolean returnDefaults, final AnnotationValueVisitor<T, ProcessingContext> elementVisitor) {

        AnnotationValue value = returnDefaults ? Util.findAnnotationField(annotation, fieldName, context) :
            Util.findAnnotationFieldWithoutDefault(annotation, fieldName, context);
        if (value != null) {
            final List<T> ret = new ArrayList<T>();
            value.accept(new SimpleAnnotationValueVisitor6<Void, Void>() {
                @Override
                public Void visitArray(List<? extends AnnotationValue> vals, Void nothing) {
                    for (AnnotationValue v : vals) {
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

    public AnnotationMirror extractAnnotationValue(String fieldName, boolean returnDefaults) {
        AnnotationValue value = returnDefaults ? Util.findAnnotationField(annotation, fieldName, context) :
            Util.findAnnotationFieldWithoutDefault(annotation, fieldName, context);
        if (value == null) {
            return null;
        }

        return value.accept(new SimpleAnnotationValueVisitor6<AnnotationMirror, Void>() {
            @Override
            public AnnotationMirror visitAnnotation(AnnotationMirror a, Void nothing) {
                return a;
            }
        }, null);
    }

    public List<AnnotationMirror> extractAnnotationArrayValue(String fieldName, boolean returnDefaults) {
        AnnotationValue value = returnDefaults ? Util.findAnnotationField(annotation, fieldName, context) :
            Util.findAnnotationFieldWithoutDefault(annotation, fieldName, context);
        if (value == null) {
            return null;
        }

        return value.accept(new SimpleAnnotationValueVisitor6<List<AnnotationMirror>, Void>() {
            @Override
            public List<AnnotationMirror> visitArray(List<? extends AnnotationValue> vals, Void aVoid) {
                final List<AnnotationMirror> ret = new ArrayList<AnnotationMirror>();
                for (AnnotationValue v : vals) {
                    v.accept(new SimpleAnnotationValueVisitor6<Void, Void>() {
                        @Override
                        public Void visitAnnotation(AnnotationMirror a, Void nothing) {
                            ret.add(a);
                            return null;
                        }
                    }, null);
                }
                return ret;
            }
        }, null);
    }
}
