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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import javax.lang.model.util.SimpleTypeVisitor6;
import javax.lang.model.util.Types;

import org.rhq.core.clientapi.descriptor.plugin.BundleTargetDescriptor;

/**
 * @author Lukas Krejci
 * @since 4.9
 */
public class Util {

    /**
     * Extracts DestinationBaseDir out of @BundleDestination. Only name and description are set, the rest needs to be
     * filled in depending on the context in which the annotation appears.
     */
    public static SimpleAnnotationValueVisitor6<BundleTargetDescriptor.DestinationBaseDir, ProcessingContext> DESTINATION_BASE_DIR_EXTRACTOR = new SimpleAnnotationValueVisitor6<BundleTargetDescriptor.DestinationBaseDir, ProcessingContext>() {
        @Override
        public BundleTargetDescriptor.DestinationBaseDir visitAnnotation(AnnotationMirror a, ProcessingContext context) {
            BundleTargetDescriptor.DestinationBaseDir baseDir = new BundleTargetDescriptor.DestinationBaseDir();

            String name = (String) findAnnotationField(a, "name", context).accept(PRIMITIVE_VALUE_EXTRACTOR, context);
            String description = (String) findAnnotationField(a, "description", context).accept(PRIMITIVE_VALUE_EXTRACTOR, context);

            baseDir.setName(name);
            baseDir.setDescription(description);

            return baseDir;
        }
    };

    private Util() {

    }

    public static AnnotationValueVisitor<Object, ProcessingContext> PRIMITIVE_VALUE_EXTRACTOR = new SimpleAnnotationValueVisitor6<Object, ProcessingContext>() {
        @Override
        public Object visitBoolean(boolean b, ProcessingContext context) {
            return b;
        }

        @Override
        public Object visitByte(byte b, ProcessingContext context) {
            return b;
        }

        @Override
        public Object visitChar(char c, ProcessingContext context) {
            return c;
        }

        @Override
        public Object visitDouble(double d, ProcessingContext context) {
            return d;
        }

        @Override
        public Object visitFloat(float f, ProcessingContext context) {
            return f;
        }

        @Override
        public Object visitInt(int i, ProcessingContext context) {
            return i;
        }

        @Override
        public Object visitLong(long i, ProcessingContext context) {
            return i;
        }

        @Override
        public Object visitShort(short s, ProcessingContext context) {
            return s;
        }

        @Override
        public Object visitString(String s, ProcessingContext context) {
            return s;
        }
    };

    public static AnnotationValueVisitor<String, ProcessingContext> ENUM_VALUE_EXTRACTOR = new SimpleAnnotationValueVisitor6<String, ProcessingContext>() {
        @Override
        public String visitEnumConstant(VariableElement c, ProcessingContext nothing) {
            return (String) c.getSimpleName().toString();
        }
    };

    public static SimpleAnnotationValueVisitor6<String, ProcessingContext> CLASS_NAME_EXTRACTOR = new SimpleAnnotationValueVisitor6<String, ProcessingContext>() {
        @Override
        public String visitType(TypeMirror t, ProcessingContext anything) {
            return t.accept(new SimpleTypeVisitor6<String, ProcessingContext>() {
                @Override
                public String visitDeclared(DeclaredType t, ProcessingContext anything) {
                    return ((TypeElement) t.asElement()).getQualifiedName().toString();
                }
            }, anything);
        }
    };

    public static AnnotationMirror findAnnotation(Element e, Class<? extends Annotation> annotation) {
        String name = annotation.getName();
        for (AnnotationMirror a : e.getAnnotationMirrors()) {
            TypeElement el = (TypeElement) a.getAnnotationType().asElement();
            if (name.equals(el.getQualifiedName().toString())) {
                return a;
            }
        }

        return null;
    }

    public static AnnotationValue findAnnotationField(AnnotationMirror annotation, String fieldName, ProcessingContext context) {
        Map<? extends ExecutableElement, ? extends AnnotationValue> elements = context.getProcessingEnvironment()
            .getElementUtils().getElementValuesWithDefaults(
                annotation);
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e : elements.entrySet()) {
            TypeMirror fieldType = e.getKey().getReturnType();
            String curField = e.getKey().getSimpleName().toString();
            if (fieldName.equals(curField)) {
                return e.getValue();
            }
        }

        return null;
    }

    public static AnnotationValue findAnnotationFieldWithoutDefault(AnnotationMirror annotation, String fieldName, ProcessingContext context) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e : annotation.getElementValues().entrySet()) {
            String curField = e.getKey().getSimpleName().toString();
            if (fieldName.equals(curField)) {
                return e.getValue();
            }
        }

        return null;
    }

    public static <Ctx extends ProcessingContext> void visitAnnotation(AnnotationMirror annotation, AnnotationVisitor<Ctx> visitor,
        Ctx context) {

        if (annotation == null) {
            return;
        }

        Map<? extends ExecutableElement, ? extends AnnotationValue> elements = context.getProcessingEnvironment()
            .getElementUtils().getElementValuesWithDefaults(
                annotation);
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e : elements.entrySet()) {
            TypeMirror fieldType = e.getKey().getReturnType();
            String fieldName = e.getKey().getSimpleName().toString();
            visitor.visitField(fieldType, fieldName, e.getValue(), context);
        }
    }

    public static <R, Ctx extends ProcessingContext> R getAnnotationFieldValue(AnnotationMirror annotation, String fieldName, AnnotationValueVisitor<R, Ctx> visitor, Ctx context) {
        AnnotationValue val = findAnnotationField(annotation, fieldName, context);
        if (val != null) {
            return val.accept(visitor, context);
        }

        return null;
    }

    public static String getNameStoredInAnnotations(Class<?> cls) {
        return cls.getName().replace('$', '.');
    }

    public static String getTypeName(DeclaredType type) {
        return ((TypeElement) type.asElement()).getQualifiedName().toString();
    }

    public static <T extends Enum<T>> T getEnum(AnnotationValue value, Class<T> enumClass) {
        String name = value.accept(ENUM_VALUE_EXTRACTOR, null);
        return Enum.valueOf(enumClass, name);
    }

    public static String nullify(String value) {
        if (value == null || value.length() == 0) {
            return null;
        }

        return value;
    }

    public static String safeToString(Object value) {
        return value == null ? null : value.toString();
    }

    public static boolean notEmpty(Collection<?> col) {
        return col != null && !col.isEmpty();
    }

    public static List<TypeElement> getAllInterfaces(TypeElement element, ProcessingContext context) {
        List<TypeElement> ret = new ArrayList<TypeElement>();

        Types types = context.getProcessingEnvironment().getTypeUtils();
        List<TypeMirror> typesToProcess = new ArrayList<TypeMirror>();
        List<TypeMirror> toAdd = new ArrayList<TypeMirror>();
        typesToProcess.add(element.asType());
        while (!typesToProcess.isEmpty()) {
            Iterator<TypeMirror> it = typesToProcess.iterator();
            while(it.hasNext()) {
                TypeMirror tm = it.next();
                it.remove();

                TypeElement e = (TypeElement) types.asElement(tm);

                for(TypeMirror t : e.getInterfaces()) {
                    TypeElement x = (TypeElement) types.asElement(t);
                    ret.add(x);
                }

                TypeMirror superClass = e.getSuperclass();
                if (superClass.getKind() != TypeKind.NONE) {
                    toAdd.add(e.getSuperclass());
                }
                toAdd.addAll(e.getInterfaces());
            }

            typesToProcess.addAll(toAdd);
            toAdd.clear();
        }

        return ret;
    }
}
