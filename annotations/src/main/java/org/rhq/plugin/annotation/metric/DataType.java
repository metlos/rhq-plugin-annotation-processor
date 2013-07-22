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

import org.rhq.plugin.annotation.configuration.ResourceConfiguration;

/**
 * DataType.
 *
 * @author Galder Zamarre�o
 * @since 4.0
 */
public enum DataType {
   MEASUREMENT, TRAIT, CALLTIME;

   @Override
   @ResourceConfiguration
   public String toString() {
      return super.toString().toLowerCase();
   }

}
