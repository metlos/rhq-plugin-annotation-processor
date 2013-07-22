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
import java.lang.annotation.Target;

/**
 * Use this annotation on a field representing a configuration property if you need the values of the property to
 * correspond to values offered by other properties elsewhere in the inventory.
 *
 * @author Lukas Krejci
 * @since 4.9
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface OptionSource {
    public enum Target {
        PLUGIN, RESOURCE_TYPE, RESOURCE, CONFIGURATION
    }

    /**
     * The type of the target this option source points to.
     */
    Target target();

    /**
     * A clue to the UI whether to show a link to the target resource or not.
     */
    boolean linkToTarget() default false;

    /**
     * A filter to narrow down search results. Example: *.jdbc
     */
    String filter() default "";

    /**
     * An expression that defines a path to the target item(s).
     * Expressions are written in the syntax of the search bar.
     */
    String expr();
}
