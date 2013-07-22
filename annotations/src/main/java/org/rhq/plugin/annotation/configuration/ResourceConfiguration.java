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

package org.rhq.plugin.annotation.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * The annotated method is used to load the configuration of the managed resource.
 * The return type of the method can be any type. Such type is inspected to generate a rich configuration definition
 * available in RHQ - use the other annotations from this package to annotate the parts of such type to provide additional
 * metadata to RHQ.
 * <p/>
 * The algorithm by which the arbitrary type is converted into a configuration definition is quite simple:
 *
 * <ol>
 *
 * <li>Fields with primitive type are converted to simple properties</li>
 *
 * <li>Fields with {@link java.util.List} type are considered list properties (with the generic parameter used
 * for further inspection of the format of the list memebers)</li>
 *
 * <li>Fields with other types are considered map properties with fields representing the parts of the map</li>
 *
 * </ol>
 *
 * @author Lukas Krejci
 * @since 4.9
 */
@Retention(RetentionPolicy.SOURCE)
@java.lang.annotation.Target(ElementType.METHOD)
public @interface ResourceConfiguration {
}
