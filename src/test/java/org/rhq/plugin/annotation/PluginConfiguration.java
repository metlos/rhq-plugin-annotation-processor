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

import org.rhq.plugin.annotation.bundle.BundleDestination;
import org.rhq.plugin.annotation.common.Description;
import org.rhq.plugin.annotation.common.DisplayName;
import org.rhq.plugin.annotation.configuration.Password;
import org.rhq.plugin.annotation.configuration.Required;
import org.rhq.plugin.annotation.drift.Drift;

/**
 * @author Lukas Krejci
 * @since 4.9
 */
public class PluginConfiguration {

    @DisplayName("Super path")
    @Description("Jada jada")
    @Required
    @Drift(name = "Installation path")
    @BundleDestination(name = "Installation path")
    private String path;

    @Password
    private String password;

    //no setter, so this is readonly
    private int number;

    public int getNumber() {
        return number;
    }

    public String getPassword() {
        return password;
    }

    public String getPath() {
        return path;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
