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

import java.util.concurrent.Callable;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.AbstractElementVisitor6;

import org.rhq.core.clientapi.descriptor.plugin.Help;
import org.rhq.core.clientapi.descriptor.plugin.PluginDescriptor;
import org.rhq.plugin.annotation.AgentPlugin;
import org.rhq.plugin.annotation.processor.AgentPluginDescriptorException;
import org.rhq.plugin.annotation.processor.Context;
import org.rhq.plugin.annotation.processor.Util;

/**
 * @author Lukas Krejci
 * @since 4.9
 */
public class PluginVisitor extends AbstractElementVisitor6<Void, Context> {

    @Override
    public Void visitPackage(PackageElement e, Context context) {
        final AgentPlugin annotation = Util.findAnnotation(e, AgentPlugin.class, context);
        if (annotation == null) {
            return null;
        }

        if (context.getPluginDescriptor() != null) {
            throw new AgentPluginDescriptorException("Only 1 agent definition allowed in a single compilation.");
        }

        PluginDescriptor descriptor = new PluginDescriptor();
        descriptor.setAmpsVersion(annotation.ampsVersion());
        descriptor.setDescription(Common.getAnnotatedDescription(e));
        descriptor.setDisplayName(Common.getAnnotatedDisplayName(e));
        Help help = new Help();
        help.setContentType("text/html");
        help.getContent().add(Common.getAnnotatedHelp(e));
        descriptor.setName(Common.getAnnotatedName(e));
        descriptor.setPackage(e.getQualifiedName().toString());
        descriptor.setPluginLifecycleListener(Util.classNameOf(new Callable<Class<?>>() {
            @Override
            public Class<?> call() throws Exception {
                return annotation.pluginLifecycleListener();
            }
        }));

        if (Util.getNameStoredInAnnotations(AgentPlugin.NoopLifecycleListener.class).equals(descriptor.getPluginLifecycleListener())) {
            descriptor.setPluginLifecycleListener(null);
        }

        descriptor.setVersion(annotation.version());

        //TODO extract the dependencies

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
