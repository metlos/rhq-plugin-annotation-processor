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

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import javax.lang.model.util.SimpleElementVisitor6;

import org.rhq.core.clientapi.descriptor.plugin.BundleTargetDescriptor;
import org.rhq.core.clientapi.descriptor.plugin.ParentResourceType;
import org.rhq.core.clientapi.descriptor.plugin.PlatformDescriptor;
import org.rhq.core.clientapi.descriptor.plugin.ResourceDescriptor;
import org.rhq.core.clientapi.descriptor.plugin.RunsInsideType;
import org.rhq.core.clientapi.descriptor.plugin.ServerDescriptor;
import org.rhq.core.clientapi.descriptor.plugin.ServiceDescriptor;
import org.rhq.core.clientapi.descriptor.plugin.SubCategoryDescriptor;
import org.rhq.plugin.annotation.processor.visitor.util.ResourceDescriptorAndDiscovery;
import org.rhq.plugin.annotation.type.ClassLoaderType;
import org.rhq.plugin.annotation.PojoResourceComponent;
import org.rhq.plugin.annotation.processor.AnnotationValueExtractor;
import org.rhq.plugin.annotation.processor.ProcessingContext;
import org.rhq.plugin.annotation.processor.Util;

/**
 * @author Lukas Krejci
 * @since 4.9
 */
public class ResourceTypeVisitor extends SimpleElementVisitor6<Void, ProcessingContext> {

    private static SimpleAnnotationValueVisitor6<BundleTargetDescriptor.DestinationBaseDir, ProcessingContext> fileSystemDestinationExtractor = new SimpleAnnotationValueVisitor6<BundleTargetDescriptor.DestinationBaseDir, ProcessingContext>() {
        @Override
        public BundleTargetDescriptor.DestinationBaseDir visitAnnotation(AnnotationMirror a, ProcessingContext context) {
            AnnotationValueExtractor extractor = context.getValueExtractor(a);
            String path = extractor.getValue("path", String.class);

            BundleTargetDescriptor.DestinationBaseDir baseDir = extractor.extractField("destination",
                Util.DESTINATION_BASE_DIR_EXTRACTOR);
            baseDir.setValueContext("fileSystem");
            baseDir.setValueName(path);

            return baseDir;
        }
    };

    private static SimpleAnnotationValueVisitor6<BundleTargetDescriptor, ProcessingContext> bundleTargetExtractor = new SimpleAnnotationValueVisitor6<BundleTargetDescriptor, ProcessingContext>() {
        @Override
        public BundleTargetDescriptor visitArray(List<? extends AnnotationValue> vals, ProcessingContext context) {
            if (vals.isEmpty()) {
                return null;
            }

            final BundleTargetDescriptor descriptor = new BundleTargetDescriptor();

            for (AnnotationValue av : vals) {
                BundleTargetDescriptor.DestinationBaseDir baseDir = av.accept(fileSystemDestinationExtractor, context);
                baseDir.setValueContext("fileSystem");

                descriptor.getDestinationBaseDir().add(baseDir);
            }
            return descriptor;
        }
    };

    private static SimpleAnnotationValueVisitor6<ParentResourceType, ProcessingContext> parentResourceTypeExtractor = new SimpleAnnotationValueVisitor6<ParentResourceType, ProcessingContext>() {
        @Override
        public ParentResourceType visitAnnotation(AnnotationMirror a, ProcessingContext processingContext) {
            AnnotationValueExtractor extractor = processingContext.getValueExtractor(a);

            ParentResourceType ret = new ParentResourceType();
            ret.setName(extractor.getValue("name", String.class));
            ret.setPlugin(extractor.getValue("plugin", String.class));

            return ret;
        }
    };

    private static SimpleAnnotationValueVisitor6<SubCategoryDescriptor, ProcessingContext> subcategoryExtractor = new SimpleAnnotationValueVisitor6<SubCategoryDescriptor, ProcessingContext>() {
        @Override
        public SubCategoryDescriptor visitAnnotation(AnnotationMirror a, ProcessingContext processingContext) {
            SubCategoryDescriptor ret = new SubCategoryDescriptor();

            AnnotationValueExtractor extractor = processingContext.getValueExtractor(a);

            ret.setName(extractor.getValue("name", String.class));
            ret.setDisplayName(extractor.getValue("displayName", String.class));
            ret.setDescription(extractor.getValue("description", String.class));

            return ret;
        }
    };

    @Override
    public Void visitType(TypeElement e, ProcessingContext context) {
        AnnotationMirror annotation = Util.findAnnotation(e, PojoResourceComponent.class);

        if (annotation == null) {
            return null;
        }

        AnnotationValueExtractor extractor = new AnnotationValueExtractor(annotation, context);

        String name = Common.getAnnotatedName(e);

        PojoResourceComponent.Category category = extractor.getValue("category", PojoResourceComponent.Category.class);

        ResourceDescriptorAndDiscovery descriptorAndDiscovery = context.getResourceTypes().get(name);
        if (descriptorAndDiscovery == null) {
            descriptorAndDiscovery = new ResourceDescriptorAndDiscovery();
            descriptorAndDiscovery.setResourceComponent(e);
        }

        ResourceDescriptor descriptor = descriptorAndDiscovery.getDescriptor();
        if (descriptor == null) {
            switch (category) {
            case PLATFORM:
                descriptor = new PlatformDescriptor();
                break;
            case SERVER:
                descriptor = new ServerDescriptor();
                break;
            case SERVICE:
                descriptor = new ServiceDescriptor();
                break;
            default:
                throw new IllegalStateException("Unsupported resource category: " + category);
            }

            descriptorAndDiscovery.setDescriptor(descriptor);
        }

        descriptor.setBundleTarget(extractor.getAnnotationValue("fileSystemBundleDestinations", bundleTargetExtractor));
        descriptor.setClassLoader(Util.safeToString(extractor.getValue("classLoader", ClassLoaderType.class)));

        //TODO this is not right, this should be the synthetized class name
        descriptor.setClazz(e.getQualifiedName().toString());

        descriptor.setDescription(Common.getAnnotatedDescription(e));
        descriptor.setHelp(Common.getAnnotatedHelp(e));
        descriptor.setName(name);

        //TODO implement configuration definition extraction
        descriptor.setPluginConfiguration(null);
        descriptor.setResourceConfiguration(null);

        List<ParentResourceType> parents = extractor.getAnnotationArrayValue("parents", parentResourceTypeExtractor);
        if (Util.notEmpty(parents)) {
            RunsInsideType runsInside = new RunsInsideType();
            runsInside.getParentResourceType().addAll(parents);
            descriptor.setRunsInside(runsInside);
        }

        descriptor.setSingleton(extractor.getValue("singleton", boolean.class));

        //TODO implement tree-structure of subcategories
        List<SubCategoryDescriptor> subcatList = extractor.getAnnotationArrayValue("subcategories", subcategoryExtractor);
        if (Util.notEmpty(subcatList)) {
            ResourceDescriptor.Subcategories subcategories = new ResourceDescriptor.Subcategories();
            subcategories.getSubcategory().addAll(subcatList);
            descriptor.setSubcategories(subcategories);
        }

        descriptor.setSubCategory(extractor.getValue("subcategory", String.class));

        //TODO not supported
        //descriptor.setVersion();

        context.getResourceTypes().put(name, descriptorAndDiscovery);

        //TODO visit the type and examine the metrics et al.
        List<? extends Element> members = context.getProcessingEnvironment().getElementUtils().getAllMembers(e);

        return null;
    }
}
