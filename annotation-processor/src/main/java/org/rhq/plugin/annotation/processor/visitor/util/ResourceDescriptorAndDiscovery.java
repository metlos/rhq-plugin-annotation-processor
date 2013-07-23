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

package org.rhq.plugin.annotation.processor.visitor.util;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import org.rhq.core.clientapi.descriptor.plugin.ResourceDescriptor;

/**
 * @author Lukas Krejci
 * @since 4.9
 */
public class ResourceDescriptorAndDiscovery {
    private String discoveryClass;
    private ExecutableElement discoveryMethod;
    private String manualDiscoveryClass;
    private ExecutableElement manualDiscoveryMethod;
    private ResourceDescriptor descriptor;
    private TypeElement resourceComponent;

    public ResourceDescriptor getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(ResourceDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public String getDiscoveryClass() {
        return discoveryClass;
    }

    public void setDiscoveryClass(String discoveryClass) {
        this.discoveryClass = discoveryClass;
    }

    public boolean isSupportsManualAdd() {
        return manualDiscoveryClass != null && manualDiscoveryMethod != null;
    }

    public ExecutableElement getDiscoveryMethod() {
        return discoveryMethod;
    }

    public void setDiscoveryMethod(ExecutableElement discoveryMethod) {
        this.discoveryMethod = discoveryMethod;
    }

    public String getManualDiscoveryClass() {
        return manualDiscoveryClass;
    }

    public void setManualDiscoveryClass(String manualDiscoveryClass) {
        this.manualDiscoveryClass = manualDiscoveryClass;
    }

    public ExecutableElement getManualDiscoveryMethod() {
        return manualDiscoveryMethod;
    }

    public void setManualDiscoveryMethod(ExecutableElement manualDiscoveryMethod) {
        this.manualDiscoveryMethod = manualDiscoveryMethod;
    }

    public ResourceDescriptor finalizeDescriptor() {
        //TODO this is not right, this should be the synthetized class name
        descriptor.setDiscovery(discoveryClass);
        descriptor.setSupportsManualAdd(isSupportsManualAdd());

        return descriptor;
    }

    public TypeElement getResourceComponent() {
        return resourceComponent;
    }

    public void setResourceComponent(TypeElement resourceComponent) {
        this.resourceComponent = resourceComponent;
    }
}
