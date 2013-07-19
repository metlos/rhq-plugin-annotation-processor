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

/**
 * A field in a type that represents a configuration doesn't have to have this annotation to be understood as a
 * property of that configuration.
 * <p/>
 * Use this annotation if you need to either override the name (by default equal to the name of the annotated field)
 * or display name (by default "decamelized" name (e.g. "myCoolProperty" -> "My Cool Property")) or provide description
 * to the property.
 *
 * @author Lukas Krejci
 * @since 4.9
 */
public @interface Property {

    /**
     * The name of the property to be used in the generated configuration definition if the name of the annotated
     * field should not be used.
     */
    String name() default "";

    /**
     * Human readable name. By default this is the "decamelized" name.
     * E.g. "myCoolProperty" -> "My Cool Property".
     */
    String displayName() default "";

    /**
     * The description of the property.
     */
    String description() default "";

    /**
     * If true, this property is marked as part of the summary information for the parent
     * object.
     */
    boolean summary() default false;
}
