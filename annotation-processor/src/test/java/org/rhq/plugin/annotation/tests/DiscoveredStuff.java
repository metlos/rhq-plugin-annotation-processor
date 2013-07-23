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

package org.rhq.plugin.annotation.tests;

import org.rhq.plugin.annotation.discovery.ResourceKey;
import org.rhq.plugin.annotation.discovery.ResourceName;
import org.rhq.plugin.annotation.discovery.Version;

/**
 * @author Lukas Krejci
 * @since 4.9
 */
public class DiscoveredStuff {

    @ResourceKey
    private String key;

    @ResourceName
    private String name;

    @Version
    private String version;

    @org.rhq.plugin.annotation.discovery.PluginConfiguration
    private PluginConfiguration config;

    public PluginConfiguration getConfig() {
        return config;
    }

    public void setConfig(PluginConfiguration config) {
        this.config = config;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
