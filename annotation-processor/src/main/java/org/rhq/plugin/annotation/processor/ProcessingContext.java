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

import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import org.rhq.core.clientapi.descriptor.plugin.PluginDescriptor;
import org.rhq.plugin.annotation.processor.visitor.util.ResourceDescriptorAndDiscovery;

/**
 * @author Lukas Krejci
 * @since 4.9
 */
public class ProcessingContext {

    private PluginDescriptor pluginDescriptor;
    private final Map<String, ResourceDescriptorAndDiscovery> resourceTypes = new HashMap<String, ResourceDescriptorAndDiscovery>();

    private final ProcessingEnvironment processingEnvironment;

    public ProcessingContext(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
    }

    public ProcessingEnvironment getProcessingEnvironment() {
        return processingEnvironment;
    }

    public PluginDescriptor getPluginDescriptor() {
        return pluginDescriptor;
    }

    public void setPluginDescriptor(PluginDescriptor pluginDescriptor) {
        this.pluginDescriptor = pluginDescriptor;
    }

    /**
     * @return a map of so far defined resource types. The keys are the names of the resource types.
     */
    public Map<String, ResourceDescriptorAndDiscovery> getResourceTypes() {
        return resourceTypes;
    }

    public AnnotationValueExtractor getValueExtractor(AnnotationMirror annotation) {
        return new AnnotationValueExtractor(annotation, this);
    }
}
