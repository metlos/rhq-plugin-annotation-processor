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
 * The constraint on the value of a property. For simplicity we only have a single constraint annotation that can express
 * all kinds of constraints on various data types.
 * <p/>
 * If the type of the field that this annotation is defined on is an integral number, the constraint requires
 * "minimum" and "maximum" to have whole number values. If the field is a real number (float or double) the minimum and
 * maximum can have real values, too.
 * <p/>
 * The {@code expr} is only considered with string values and is a regular expression that the values need to match.
 *
 * @author Lukas Krejci
 * @since 4.9
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface Constraint {
    double minimum() default 0;
    double maximum() default 0;

    String expr() default "";
}
