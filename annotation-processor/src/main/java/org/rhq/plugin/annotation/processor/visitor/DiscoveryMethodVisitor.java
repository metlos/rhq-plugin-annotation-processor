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

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.SimpleElementVisitor6;

import org.rhq.plugin.annotation.DiscoveryFor;
import org.rhq.plugin.annotation.processor.AnnotationValueExtractor;
import org.rhq.plugin.annotation.processor.ProcessingContext;
import org.rhq.plugin.annotation.processor.Util;
import org.rhq.plugin.annotation.processor.visitor.util.ResourceDescriptorAndDiscovery;

/**
 * @author Lukas Krejci
 * @since 4.9
 */
public class DiscoveryMethodVisitor extends SimpleElementVisitor6<Void, ProcessingContext> {
    @Override
    public Void visitExecutable(ExecutableElement e, ProcessingContext processingContext) {
        AnnotationMirror annotation = Util.findAnnotation(e, DiscoveryFor.class);
        if (annotation == null) {
            return null;
        }

        AnnotationValueExtractor extractor = processingContext.getValueExtractor(annotation);

        String[] resourceTypes = extractor.extractValue("types", String[].class, false);
        Boolean manual = extractor.extractValue("manual", Boolean.class, false);

        for(String resourceType : resourceTypes) {
            ResourceDescriptorAndDiscovery descriptor = processingContext.getResourceTypes().get(resourceType);
            if (descriptor == null) {
                descriptor = new ResourceDescriptorAndDiscovery();
            }

            //TODO this is not right, this should be the synthesized class
            String cls = ((TypeElement)e.getEnclosingElement()).getQualifiedName().toString();

            if (manual == null || !manual) {
                descriptor.setDiscoveryClass(cls);
                descriptor.setDiscoveryMethod(e);
            } else {
                descriptor.setManualDiscoveryClass(cls);
                descriptor.setManualDiscoveryMethod(e);
            }

            processingContext.getResourceTypes().put(resourceType, descriptor);
        }

        return null;
    }
}
