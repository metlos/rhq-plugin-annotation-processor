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

package org.rhq.plugin.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.rhq.plugin.annotation.drift.FileSystemDrift;
import org.rhq.plugin.annotation.event.Event;

/**
 * A class annotated with the annotation is considered an RHQ's resource component. That is it is scanned for other
 * definitions to compose a full "picture" of a resource in RHQ and is used by the generated glue code to perform the
 * actual actions.
 * <p/>
 * The annotated class is permitted to either have no-arg, 1 arg or 2 arg constructor.
 *
 * In case of the constructor with 1 argument, the type of the argument is understood to represent a plugin
 * configuration of the resource component (see {@link org.rhq.plugin.annotation.configuration.ResourceConfiguration}
 * for description of the how the type is converted into a plugin configuration definition). If the type of the argument
 * is {@link org.rhq.core.pluginapi.inventory.ResourceContext} it does not go through that conversion. Instead, the
 * resource is considered to have no plugin configuration and the when instantiated the constructor is given the
 * appropriate resource context (see for example
 * {@link org.rhq.core.pluginapi.inventory.ResourceComponent#start(org.rhq.core.pluginapi.inventory.ResourceContext)}
 * for what a resource context is).
 *
 * The constructor with 2 arguments can, in addition to a type representing its plugin configuration, have an argument
 * with the {@link org.rhq.core.pluginapi.inventory.ResourceContext} type. This is for occassions when a resource
 * needs to have a plugin configuration AND also access to the resource context.
 *
 * @author Lukas Krejci
 * @since 4.9
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface PojoResourceComponent {
    public enum Category {
        PLATFORM, SERVER, SERVICE
    }

    /**
     * The category of the resource, i.e. platform, server or service.
     */
    Category category();

    /**
     * A PIQL query to discover processes this resource should be managing.
     */
    String processScan() default "";

    /**
     * TODO this is not right.
     */
    String subCategories() default "";

    /**
     * Lists the events handled. Because event handling is handled in background worker threads, we purposely enforce
     * splitting the handling into separate classes, so that the implementors are more careful and aware of
     * the intricacies.
     *
     * @see Event
     */
    Event[] events() default {};

    /**
     * Configuration drifts are usually declared at the configuration properties or metrics that define the base
     * directories. If a drift definition should be based on a filesystem path not deduced from other variables in the
     * plugin, this is the place to do that.
     */
    FileSystemDrift[] fileSystemDrifts() default {};

    /**
     * Lists the bundle destinations defined directly by the filesystem paths. Similarly to drifts, bundle destinations
     * are usually defined at the respective configuration properties / metrics. But if a bundle destination is an
     * actual path not derived from a value of another variable in the plugin, this is the place to define it.
     */
    String[] fileSystemBundleDestinations() default {};


    /**
     * Detailed documentation and help text.
     */
    String help() default "";

}
