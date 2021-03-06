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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.rhq.core.clientapi.descriptor.plugin.PlatformDescriptor;
import org.rhq.core.clientapi.descriptor.plugin.PluginDescriptor;
import org.rhq.core.clientapi.descriptor.plugin.ResourceDescriptor;
import org.rhq.core.clientapi.descriptor.plugin.ServerDescriptor;
import org.rhq.core.clientapi.descriptor.plugin.ServiceDescriptor;
import org.rhq.plugin.annotation.processor.ProcessingContext;
import org.rhq.plugin.annotation.processor.visitor.Visitors;
import org.rhq.plugin.annotation.processor.visitor.util.ResourceDescriptorAndDiscovery;

/**
 * An annotation processor to generate a plugin out of a Jar with annotated classes at compile time.
 *
 * @author Lukas Krejci
 * @since 4.9
 */
@SupportedAnnotationTypes({"org.rhq.plugin.annotation.PojoResourceComponent", "org.rhq.plugin.annotation.AgentPlugin", "org.rhq.plugin.annotation.DiscoveryFor"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class Processor extends AbstractProcessor {

    private ProcessingContext processingContext;
    private Visitors visitors = new Visitors();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            validate();
            writeOut();
        }

        for (TypeElement annotation : annotations) {
            for (Element e : roundEnv.getElementsAnnotatedWith(annotation)) {
                e.accept(visitors, getProcessingContext());
            }
        }

        return true;
    }

    private ProcessingContext getProcessingContext() {
        if (processingContext == null) {
            processingContext = new ProcessingContext(processingEnv);
        }

        return processingContext;
    }
    private void validate() {
    }

    private void writeOut() {
        PluginDescriptor descriptor = processingContext.getPluginDescriptor();
        if (descriptor == null) {
            //no agent plugin here
            return;
        }

        for(ResourceDescriptorAndDiscovery rdadc : processingContext.getResourceTypes().values()) {
            ResourceDescriptor r = rdadc.finalizeDescriptor();
            if (r instanceof PlatformDescriptor)  {
                descriptor.getPlatforms().add((PlatformDescriptor) r);
            } else if (r instanceof ServerDescriptor) {
                descriptor.getServers().add((ServerDescriptor) r);
            } else if (r instanceof ServiceDescriptor) {
                descriptor.getServices().add((ServiceDescriptor) r);
            }
        }

        //TODO merge the descriptor with a potential pre-existing one

        JAXBContext context = null;
        OutputStream out = null;
        try {
            context = JAXBContext.newInstance(PluginDescriptor.class);
            Marshaller marshaller = context.createMarshaller();
            FileObject rhqPluginXml = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, "",
                "META-INF/rhq-plugin.xml", null);
            out = rhqPluginXml.openOutputStream();

            marshaller.marshal(descriptor, out);
        } catch (JAXBException e) {
            throw new IllegalStateException("Failed to create the plugin descriptor", e);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create the plugin descriptor", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // ignored
                }
            }
        }
    }
}
