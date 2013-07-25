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

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.PackageElement;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import javax.lang.model.util.SimpleElementVisitor6;

import org.rhq.core.clientapi.descriptor.plugin.PluginDescriptor;
import org.rhq.plugin.annotation.AgentPlugin;
import org.rhq.plugin.annotation.processor.AgentPluginDescriptorException;
import org.rhq.plugin.annotation.processor.AnnotationValueExtractor;
import org.rhq.plugin.annotation.processor.ProcessingContext;
import org.rhq.plugin.annotation.processor.Util;

/**
 * @author Lukas Krejci
 * @since 4.9
 */
public class PluginVisitor extends SimpleElementVisitor6<Void, ProcessingContext> {

    private static SimpleAnnotationValueVisitor6<PluginDescriptor.Depends, ProcessingContext> dependsExtractor = new SimpleAnnotationValueVisitor6<PluginDescriptor.Depends, ProcessingContext>() {
        @Override
        public PluginDescriptor.Depends visitAnnotation(AnnotationMirror a, ProcessingContext context) {
            final PluginDescriptor.Depends depends = new PluginDescriptor.Depends();

            AnnotationValueExtractor extractor = context.getValueExtractor(a);

            depends.setPlugin(extractor.extractValue("pluginName", String.class, false));
            depends.setUseClasses(extractor.extractValue("useClasses", Boolean.class, false));

            return  depends;
        }
    };

    @Override
    public Void visitPackage(final PackageElement e, final ProcessingContext context) {
        AnnotationMirror agentPlugin = Util.findAnnotation(e, AgentPlugin.class);

        if (agentPlugin == null) {
            return null;
        } else if (context.getPluginDescriptor() != null) {
            throw new AgentPluginDescriptorException("Only 1 agent plugin allowed.");
        }

        AnnotationValueExtractor extractor = context.getValueExtractor(agentPlugin);
        final PluginDescriptor descriptor = new PluginDescriptor();

        descriptor.setName(Common.getAnnotatedName(e));
        descriptor.setDisplayName(Common.getAnnotatedDisplayName(e));
        descriptor.setDescription(Common.getAnnotatedDescription(e));
        descriptor.setHelp(Common.getAnnotatedHelp(e));
        descriptor.setAmpsVersion(extractor.extractValue("ampsVersion", String.class, false));
        descriptor.setPluginLifecycleListener(extractor.extractValue("pluginLifecycleListener", String.class, false));
        descriptor.setVersion(extractor.extractValue("version", String.class, false));

        List<PluginDescriptor.Depends> depends = extractor.extractArrayValue("dependencies", false, dependsExtractor);
        if (Util.notEmpty(depends)) {
            descriptor.getDepends().addAll(depends);
        }

        context.setPluginDescriptor(descriptor);
        return null;
    }
}
