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

package org.rhq.plugin.annotation.drift;

/**
 * A drift base directory can be specified explicitly (i.e. not be based on the value of some other configuration property
 * or metric value). In such cases, one can use this annotation to describe such drifts.
 *
 * @see org.rhq.plugin.annotation.PojoResourceComponent#fileSystemDrifts()
 *
 * @author Lukas Krejci
 * @since 4.9
 */
public @interface FileSystemDrift {
    /**
     * The filesystem path to consider for drift detection.
     */
    String path();

    /**
     * The definition of the drift.
     */
    Drift definition();
}
