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

package org.rhq.plugin.annotation.processor.visitor;

import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.AbstractElementVisitor6;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import javax.lang.model.util.SimpleTypeVisitor6;

import org.rhq.core.clientapi.descriptor.plugin.Help;
import org.rhq.core.clientapi.descriptor.plugin.PluginDescriptor;
import org.rhq.plugin.annotation.AgentPlugin;
import org.rhq.plugin.annotation.processor.AgentPluginDescriptorException;
import org.rhq.plugin.annotation.processor.AnnotationVisitor;
import org.rhq.plugin.annotation.processor.Context;
import org.rhq.plugin.annotation.processor.Util;

/**
 * @author Lukas Krejci
 * @since 4.9
 */
public class PluginVisitor extends AbstractElementVisitor6<Void, Context> {

    private static class PluginDescriptorContext extends Context {
        private final PluginDescriptor descriptor;
        private final Context context;

        private PluginDescriptorContext(PluginDescriptor descriptor,
            Context processingContext) {
            super(processingContext.getProcessingEnvironment());
            this.descriptor = descriptor;
            this.context = processingContext;
        }

        @Override
        public PluginDescriptor getPluginDescriptor() {
            return context.getPluginDescriptor();
        }

        @Override
        public ProcessingEnvironment getProcessingEnvironment() {
            return context.getProcessingEnvironment();
        }

        @Override
        public void setPluginDescriptor(PluginDescriptor pluginDescriptor) {
            context.setPluginDescriptor(pluginDescriptor);
        }

        public PluginDescriptor getWorkingPluginDescriptor() {
            return descriptor;
        }
    }

    private SimpleAnnotationValueVisitor6<String, Void> classNameExtractor = new SimpleAnnotationValueVisitor6<String, Void>() {
        @Override
        public String visitType(TypeMirror t, Void ctx) {
            return t.accept(new SimpleTypeVisitor6<String, Void>() {
                @Override
                public String visitDeclared(DeclaredType t, Void context) {
                    return ((TypeElement) t.asElement()).getQualifiedName().toString();
                }
            }, ctx);
        }
    };

    private SimpleAnnotationValueVisitor6<PluginDescriptor.Depends, PluginDescriptorContext> dependsExtractor = new SimpleAnnotationValueVisitor6<PluginDescriptor.Depends, PluginDescriptorContext>() {
        @Override
        public PluginDescriptor.Depends visitAnnotation(AnnotationMirror a, PluginDescriptorContext context) {
            final PluginDescriptor.Depends depends = new PluginDescriptor.Depends();

            Util.visitAnnotation(a, new AnnotationVisitor<Context>() {
                @Override
                public void visitField(TypeMirror fieldType, String fieldName, AnnotationValue value,
                    Context context) {

                    if ("pluginName".equals(fieldName)) {
                        depends.setPlugin((String) value.accept(Util.PRIMITIVE_VALUE_EXTRACTOR, context));
                    } else if ("useClasses".equals(fieldName)) {
                        depends.setUseClasses((Boolean) value.accept(Util.PRIMITIVE_VALUE_EXTRACTOR, context));
                    }
                }
            }, context);

            return  depends;
        }
    };

    private SimpleAnnotationValueVisitor6<Void, PluginDescriptorContext> dependenciesExtractor = new SimpleAnnotationValueVisitor6<Void, PluginDescriptorContext>() {
        @Override
        public Void visitArray(List<? extends AnnotationValue> vals, PluginDescriptorContext context) {
            for (AnnotationValue dep : vals) {
                PluginDescriptor.Depends depends = dep.accept(dependsExtractor, context);
                context.getWorkingPluginDescriptor().getDepends().add(depends);
            }
            return null;
        }
    };

    @Override
    public Void visitPackage(final PackageElement e, final Context context) {
        AnnotationMirror agentPlugin = Util.findAnnotation(e, AgentPlugin.class);

        if (agentPlugin != null && context.getPluginDescriptor() != null) {
            throw new AgentPluginDescriptorException("Only 1 agent plugin allowed.");
        }

        final PluginDescriptor descriptor = new PluginDescriptor();

        descriptor.setName(Common.getAnnotatedName(e));
        descriptor.setDisplayName(Common.getAnnotatedDisplayName(e));
        descriptor.setDescription(Common.getAnnotatedDescription(e));

        String helpText = Common.getAnnotatedHelp(e);
        if (helpText != null) {
            Help help = new Help();
            help.setContentType("text/html");
            help.getContent().add(helpText);
            descriptor.setHelp(help);
        }

        Util.visitAnnotation(agentPlugin, new AnnotationVisitor<Context>() {
            @Override
            public void visitField(TypeMirror fieldType, String fieldName, AnnotationValue value, Context parameter) {
                if ("ampsVersion".equals(fieldName)) {
                    descriptor.setAmpsVersion((String) value.accept(Util.PRIMITIVE_VALUE_EXTRACTOR, context));
                } else if ("pluginLifecycleListener".equals(fieldName)) {
                    String className = value.accept(classNameExtractor, null);
                    if (!Util.getNameStoredInAnnotations(AgentPlugin.NoopLifecycleListener.class).equals(className)) {
                        descriptor.setPluginLifecycleListener(className);
                    }
                } else if ("version".equals(fieldName)) {
                    descriptor.setVersion((String) value.accept(Util.PRIMITIVE_VALUE_EXTRACTOR, context));
                } else if ("dependencies".equals(fieldName)) {
                    value.accept(dependenciesExtractor, new PluginDescriptorContext(descriptor, context));
                }
            }
        }, context);

        context.setPluginDescriptor(descriptor);
        return null;
    }

    @Override
    public Void visitType(TypeElement e, Context context) {
        return null;
    }

    @Override
    public Void visitVariable(VariableElement e, Context context) {
        return null;
    }

    @Override
    public Void visitExecutable(ExecutableElement e, Context context) {
        return null;
    }

    @Override
    public Void visitTypeParameter(TypeParameterElement e, Context context) {
        return null;
    }

    @Override
    public Void visitUnknown(Element e, Context context) {
        return null;
    }
}
