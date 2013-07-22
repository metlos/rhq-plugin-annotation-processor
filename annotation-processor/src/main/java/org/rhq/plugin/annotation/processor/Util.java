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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import javax.lang.model.util.SimpleTypeVisitor6;

/**
 * @author Lukas Krejci
 * @since 4.9
 */
public class Util {

    private Util() {

    }

    private static ThreadLocal<Boolean> gettingClassName = new ThreadLocal<Boolean>();
    private static ThreadLocal<String> foundClassName = new ThreadLocal<String>();
    static {
        gettingClassName.set(false);
    }

    private static AnnotationValueVisitor<String, Void> classNameExtractor = new SimpleAnnotationValueVisitor6<String, Void>() {
        @Override
        public String visitType(TypeMirror t, Void aVoid) {
            return t.accept(new SimpleTypeVisitor6<String, Void>() {
                @Override
                public String visitDeclared(DeclaredType t, Void aVoid) {
                    return ((TypeElement) t.asElement()).getQualifiedName().toString();
                }
            }, null);
        }
    };

    public static <T extends Annotation> T asAnnotation(AnnotationMirror mirror, final Class<T> annotationClass, Context context) {
        final Map<? extends ExecutableElement, ? extends AnnotationValue> annotationValues = context
            .getProcessingEnvironment().getElementUtils().getElementValuesWithDefaults(mirror);

        return annotationClass.cast(Proxy
            .newProxyInstance(annotationClass.getClassLoader(), new Class[]{annotationClass}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    for(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e : annotationValues.entrySet()) {
                        if (e.getKey().getSimpleName().toString().equals(method.getName())) {
                            if (gettingClassName.get()) {
                                String className = e.getValue().accept(classNameExtractor, null);
                                foundClassName.set(className);
                                return null;
                            } else {
                                return e.getValue().getValue();
                            }
                        }
                    }

                    throw new IllegalArgumentException("Method " + method + " not found on annotation " + annotationClass);
                }
            }));
    }

    public static <T extends Annotation> T findAnnotation(Element e, Class<T> annotation, Context context) {
        AnnotationMirror mirror = findAnnotation(e, annotation);
        if (mirror == null) {
            return null;
        }

        return asAnnotation(mirror, annotation, context);
    }

    public static AnnotationMirror findAnnotation(Element e, Class<? extends Annotation> annotation) {
        String name = annotation.getName();
        for(AnnotationMirror a : e.getAnnotationMirrors()) {
            TypeElement el = (TypeElement) a.getAnnotationType().asElement();
            if (name.equals(el.getQualifiedName().toString())) {
                return a;
            }
        }

        return null;
    }

    public static String classNameOf(Callable<Class<?>> statement) {
        try {
            gettingClassName.set(true);
            statement.call();
            return foundClassName.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            gettingClassName.set(false);
        }
    }

    public static String getNameStoredInAnnotations(Class<?> cls) {
        return cls.getName().replace('$', '.');
    }
}
