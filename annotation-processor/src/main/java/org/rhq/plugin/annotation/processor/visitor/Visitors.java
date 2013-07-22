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

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.AbstractElementVisitor6;

import org.rhq.plugin.annotation.processor.Context;

/**
 * @author Lukas Krejci
 * @since 4.9
 */
public class Visitors extends AbstractElementVisitor6<Void, Context> {

    private List<ElementVisitor<?, Context>> visitors = new ArrayList<ElementVisitor<?, Context>>();
    {
        visitors.add(new PluginVisitor());
        visitors.add(new ResourceTypeVisitor());
    }


    @Override
    public Void visitPackage(PackageElement e, Context context) {
        for(ElementVisitor<?, Context> v : visitors) {
            v.visitPackage(e, context);
        }
        return null;
    }

    @Override
    public Void visitType(TypeElement e, Context context) {
        for(ElementVisitor<?, Context> v : visitors) {
            v.visitType(e, context);
        }
        return null;
    }

    @Override
    public Void visitVariable(VariableElement e, Context context) {
        for(ElementVisitor<?, Context> v : visitors) {
            v.visitVariable(e, context);
        }
        return null;
    }

    @Override
    public Void visitExecutable(ExecutableElement e, Context context) {
        for(ElementVisitor<?, Context> v : visitors) {
            v.visitExecutable(e, context);
        }
        return null;
    }

    @Override
    public Void visitTypeParameter(TypeParameterElement e, Context context) {
        for(ElementVisitor<?, Context> v : visitors) {
            v.visitTypeParameter(e, context);
        }
        return null;
    }

    @Override
    public Void visitUnknown(Element e, Context context) {
        return null;
    }
}
