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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When a field represented a configuration property or a method representing a metric is annotated with this annotation
 * the field's value / method's return value is used as the "base directory" for the drift detection.
 *
 * @author Lukas Krejci
 * @since 4.9
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface Drift {

    public enum HandlingMode {
        NORMAL, PLANNED_CHANGES
    }

    public @interface Filter {
        /**
         * The path under the base directory to consider
         */
        String path();

        /**
         * Regex to match the files under the path
         */
        String pattern() default "";
    }

    /**
     * The name of the drift base directory
     */
    String name();

    String description() default "";

    /**
     * The default interval between drift detection in seconds.
     */
    long interval() default 1800;

    /**
     * The default handling mode of the drift detection.
     */
    HandlingMode handlingMode() default HandlingMode.NORMAL;

    /**
     * Filters for files to include in the drift detection.
     */
    Filter[] includes() default {};

    /**
     * Filters for files to exclude from the drift detection.
     */
    Filter[] excludes() default {};
}

