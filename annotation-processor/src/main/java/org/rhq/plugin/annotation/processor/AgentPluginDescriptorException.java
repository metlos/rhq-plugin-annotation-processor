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

/**
 * @author Lukas Krejci
 * @since 4.9
 */
public class AgentPluginDescriptorException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AgentPluginDescriptorException() {
    }

    public AgentPluginDescriptorException(Throwable cause) {
        super(cause);
    }

    public AgentPluginDescriptorException(String message) {
        super(message);
    }

    public AgentPluginDescriptorException(String message, Throwable cause) {
        super(message, cause);
    }
}
