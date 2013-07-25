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

package org.rhq.plugin.annotation.processor.visitor;

import javax.lang.model.element.Element;

import org.rhq.core.clientapi.descriptor.configuration.MeasurementUnitsDescriptor;
import org.rhq.plugin.annotation.common.Description;
import org.rhq.plugin.annotation.common.DisplayName;
import org.rhq.plugin.annotation.common.Help;
import org.rhq.plugin.annotation.common.Name;
import org.rhq.plugin.annotation.common.Unit;
import org.rhq.plugin.annotation.processor.Util;

/**
 * @author Lukas Krejci
 * @since 4.9
 */
public class Common {
    private Common() {

    }

    public static String getAnnotatedName(Element element) {
        Name name = element.getAnnotation(Name.class);
        String defaultName = element.getSimpleName().toString();

        return name == null ? defaultName : Util.nullify(name.value());
    }

    public static String getAnnotatedDisplayName(Element element) {
        DisplayName name = element.getAnnotation(DisplayName.class);

        return name == null ? null : Util.nullify(name.value());
    }

    public static String getAnnotatedDescription(Element element) {
        Description descr = element.getAnnotation(Description.class);

        return descr == null ? null : Util.nullify(descr.value());
    }

    public static MeasurementUnitsDescriptor getAnnotatedUnit(Element element) {
        Unit unit = element.getAnnotation(Unit.class);

        return unit == null ? null : MeasurementUnitsDescriptor.fromValue(unit.value().toString());
    }

    public static org.rhq.core.clientapi.descriptor.plugin.Help getAnnotatedHelp(Element element) {
        Help descr = element.getAnnotation(Help.class);

        String text = descr == null ? null : Util.nullify(descr.value());

        if (text == null) {
            return null;
        } else {
            org.rhq.core.clientapi.descriptor.plugin.Help help = new org.rhq.core.clientapi.descriptor.plugin.Help();
            help.setContentType("text/html");
            help.getContent().add(text);

            return help;
        }
    }

}
