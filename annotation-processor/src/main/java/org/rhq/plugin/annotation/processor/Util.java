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
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;

/**
 * @author Lukas Krejci
 * @since 4.9
 */
public class Util {

    private Util() {

    }

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

    public static <Ctx extends Context> void visitAnnotation(AnnotationMirror annotation, AnnotationVisitor<Ctx> visitor,
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

    public static String getNameStoredInAnnotations(Class<?> cls) {
        return cls.getName().replace('$', '.');
    }

    public static String getTypeName(DeclaredType type) {
        return ((TypeElement) type.asElement()).getQualifiedName().toString();
    }

    public static AnnotationValueVisitor<Object, Context> PRIMITIVE_VALUE_EXTRACTOR = new SimpleAnnotationValueVisitor6<Object, Context>() {
        @Override
        public Object visitBoolean(boolean b, Context context) {
            return b;
        }

        @Override
        public Object visitByte(byte b, Context context) {
            return b;
        }

        @Override
        public Object visitChar(char c, Context context) {
            return c;
        }

        @Override
        public Object visitDouble(double d, Context context) {
            return d;
        }

        @Override
        public Object visitFloat(float f, Context context) {
            return f;
        }

        @Override
        public Object visitInt(int i, Context context) {
            return i;
        }

        @Override
        public Object visitLong(long i, Context context) {
            return i;
        }

        @Override
        public Object visitShort(short s, Context context) {
            return s;
        }

        @Override
        public Object visitString(String s, Context context) {
            return s;
        }
    };
}
