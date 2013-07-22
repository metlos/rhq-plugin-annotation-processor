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

package org.rhq.plugin.annotation;

import java.util.List;

import org.rhq.plugin.annotation.configuration.ResourceConfiguration;
import org.rhq.plugin.annotation.metric.Metric;
import org.rhq.plugin.annotation.operation.Operation;

/**
 * @author Lukas Krejci
 * @since 4.9
 */
@PojoResourceComponent(category = PojoResourceComponent.Category.SERVER, discovery = Discovery.class)
public class Resource {

    private PluginConfiguration config;

    public Resource(PluginConfiguration config) {
        this.config = config;
    }

    @Metric
    public int getMetric1() {
        return 0;
    }

    @Operation
    public double operation1() {
        return 0.0;
    }

    @Operation
    public List<ComplexResultRow> operation2() {
        return null;
    }

    @ResourceConfiguration
    public ManagedResourceConfiguration getConfig() {
        return null;
    }
}
