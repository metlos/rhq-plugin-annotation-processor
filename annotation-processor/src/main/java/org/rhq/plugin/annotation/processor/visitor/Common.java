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

import java.lang.annotation.Annotation;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import org.rhq.plugin.annotation.common.Description;
import org.rhq.plugin.annotation.common.DisplayName;
import org.rhq.plugin.annotation.common.Help;
import org.rhq.plugin.annotation.common.Name;

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

        return name == null ? defaultName : name.value();
    }

    public static String getAnnotatedDisplayName(Element element) {
        DisplayName name = element.getAnnotation(DisplayName.class);

        return name == null ? null : name.value();
    }

    public static String getAnnotatedDescription(Element element) {
        Description descr = element.getAnnotation(Description.class);

        return descr == null ? null : descr.value();
    }

    public static String getAnnotatedHelp(Element element) {
        Help descr = element.getAnnotation(Help.class);

        return descr == null ? null : descr.value();
    }

}
