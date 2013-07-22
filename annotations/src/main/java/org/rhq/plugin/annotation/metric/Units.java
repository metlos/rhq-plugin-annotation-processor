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
package org.rhq.plugin.annotation.metric;

/**
 * Metric Units.
 *
 * @author Galder Zamarre�o
 * @since 4.0
 */
public enum Units {
   NONE, PERCENTAGE, BITS, KILOBITS, MEGABITS, GIGABITS, TERABITS, PETABITS, BYTES, KILOBYTES, MEGABYTES, GIGABYTES,
    TERABYTES, PETABYTES, EPOCH_MILLISECONDS, EPOCH_SECONDS, JIFFYS, NANOSECONDS, MICROSECONDS, MILLISECONDS, SECONDS,
    MINUTES, HOURS, DAYS, KELVIN, CELSIUS, FAHRENHEIT;

   @Override
   public String toString() {
      return super.toString().toLowerCase();
   }
}
