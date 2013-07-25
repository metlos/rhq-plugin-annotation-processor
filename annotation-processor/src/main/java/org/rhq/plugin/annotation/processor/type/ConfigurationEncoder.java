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

package org.rhq.plugin.annotation.processor.type;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import javax.lang.model.util.Types;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.rhq.core.clientapi.descriptor.configuration.ConfigurationDescriptor;
import org.rhq.core.clientapi.descriptor.configuration.ConfigurationProperty;
import org.rhq.core.clientapi.descriptor.configuration.ConstraintType;
import org.rhq.core.clientapi.descriptor.configuration.ListProperty;
import org.rhq.core.clientapi.descriptor.configuration.MapProperty;
import org.rhq.core.clientapi.descriptor.configuration.Option;
import org.rhq.core.clientapi.descriptor.configuration.PropertyType;
import org.rhq.core.clientapi.descriptor.configuration.RegexConstraintType;
import org.rhq.core.clientapi.descriptor.configuration.SimpleProperty;
import org.rhq.core.clientapi.descriptor.configuration.SourceType;
import org.rhq.plugin.annotation.common.Summary;
import org.rhq.plugin.annotation.configuration.Constraints;
import org.rhq.plugin.annotation.configuration.InitialValue;
import org.rhq.plugin.annotation.configuration.Directory;
import org.rhq.plugin.annotation.configuration.ListMemberDefinition;
import org.rhq.plugin.annotation.configuration.OptionSource;
import org.rhq.plugin.annotation.configuration.Options;
import org.rhq.plugin.annotation.configuration.Password;
import org.rhq.plugin.annotation.configuration.ReadOnly;
import org.rhq.plugin.annotation.configuration.Required;
import org.rhq.plugin.annotation.processor.AgentPluginDescriptorException;
import org.rhq.plugin.annotation.processor.AnnotationValueExtractor;
import org.rhq.plugin.annotation.processor.ProcessingContext;
import org.rhq.plugin.annotation.processor.Util;
import org.rhq.plugin.annotation.processor.visitor.Common;

/**
 * @author Lukas Krejci
 * @since 4.9
 */
public class ConfigurationEncoder {

    private final ProcessingContext context;

    public ConfigurationEncoder(ProcessingContext context) {
        this.context = context;
    }

    private enum PropertyInstanceType {
        SIMPLE("simple-property"), LIST("list-property"), MAP("map-property");

        private String xmlName;

        private PropertyInstanceType(String xmlName) {
            this.xmlName = xmlName;
        }

        private String getXmlName() {
            return xmlName;
        }

        public static PropertyInstanceType detect(TypeElement element, ProcessingContext context) {
            if (isSimple(element, context)) {
                return SIMPLE;
            } else if (Util.implementsInterface(element, "java.util.List", context)) {
                return LIST;
            } else {
                return MAP;
            }
        }

        public static PropertyInstanceType detect(ConfigurationProperty prop) {
            if (prop instanceof SimpleProperty) {
                return SIMPLE;
            } else if (prop instanceof ListProperty) {
                return LIST;
            } else if (prop instanceof MapProperty) {
                return MAP;
            } else {
                throw new IllegalStateException("Unknown type of configuration property: " + prop.getClass());
            }
        }

        private static boolean isSimple(TypeElement el, ProcessingContext context) {
            Types types = context.getProcessingEnvironment().getTypeUtils();
            if (el.asType().getKind().isPrimitive()) {
                return true;
            } else {
                try {
                    types.unboxedType(el.asType());
                    return true;
                } catch (IllegalArgumentException e) {
                    return "java.lang.String".equals(el.getQualifiedName().toString());
                }
            }
        }
    }

    public <T extends ConfigurationProperty> ConfigurationDescriptor encode(TypeElement cls) {
        ConfigurationDescriptor ret = new ConfigurationDescriptor();

        List<VariableElement> fields = ElementFilter.fieldsIn(
            context.getProcessingEnvironment().getElementUtils().getAllMembers(cls));

        for (VariableElement field : fields) {
            ConfigurationProperty prop = encodeField(field);

            JAXBElement<T> el = new JAXBElement<T>(
                new QName("urn:rhq-configuration", PropertyInstanceType.detect(prop).getXmlName()),
                (Class<T>) prop.getClass(), (T) prop);

            ret.getConfigurationProperty().add(el);
        }

        //TODO support for templates

        return ret;
    }

    private ConfigurationProperty encodeField(VariableElement field) {
        TypeElement fieldType = Util.getTypeElement(field.asType(), context);

        String fieldName = field.getSimpleName().toString();
        String customName = Common.getAnnotatedName(field);
        String displayName = Common.getAnnotatedDisplayName(field);
        String description = Common.getAnnotatedDescription(field);
        Boolean required = getAnnotationValue(field, Required.class, Boolean.class);
        Boolean readOnly = getAnnotationValue(field, ReadOnly.class, Boolean.class);
        boolean summary = hasAnnotation(field, Summary.class);

        PropertyInstanceType type = PropertyInstanceType.detect(fieldType, context);

        ConfigurationProperty ret;
        switch (type) {
        case SIMPLE:
            ret = new SimpleProperty();
            break;
        case MAP:
            ret = new MapProperty();
            break;
        case LIST:
            ret = new ListProperty();
            break;
        default:
            throw new IllegalStateException("Unknown configuration property type: " + type);
        }

        ret.setDescription(description);
        ret.setDisplayName(displayName);
        ret.setName(customName == null ? fieldName : customName);
        ret.setReadOnly(readOnly);
        ret.setRequired(required);
        ret.setSummary(summary);

        switch (type) {
        case SIMPLE:
            encodeSimpleProperty((SimpleProperty) ret, field, fieldType);
            break;
        case MAP:
            encodeMapProperty((MapProperty) ret, fieldType);
            break;
        case LIST:
            encodeListProperty((ListProperty) ret, field, fieldType);
        }

        return ret;
    }

    private void encodeListProperty(ListProperty prop, VariableElement field, TypeElement fieldType) {
        List<? extends TypeParameterElement> typeParams = fieldType.getTypeParameters();
        if (typeParams.isEmpty()) {
            throw new IllegalArgumentException("A list property must be declared with a generic signature so that the" +
                " type of the list members can be deduced. Field '" + field.getSimpleName() + "' in type '" +
                ((TypeElement) field.getEnclosingElement()).getQualifiedName().toString() + "'.");
        }

        TypeElement memberType = Util.getTypeElement(typeParams.get(0).asType(), context);
        PropertyInstanceType propertyType = PropertyInstanceType.detect(memberType, context);

        ConfigurationProperty ret;
        switch (propertyType) {
        case SIMPLE:
            ret = new SimpleProperty();
            ret.setName(ListMemberDefinition.DEFAULT_NAME);
            break;
        case MAP:
            ret = new MapProperty();
            encodeMapProperty((MapProperty) ret, memberType);
            break;
        case LIST:
            throw new AgentPluginDescriptorException(
                "Lists of lists are unsupported. Found one on field '" + field.getSimpleName() + "' in type '" +
                    ((TypeElement) field.getEnclosingElement()).getQualifiedName().toString() + "'.");
        default:
            throw new IllegalStateException("Unknown configuration property type: " + propertyType);
        }

        AnnotationMirror memberDef = Util.findAnnotation(field, ListMemberDefinition.class);
        if (memberDef != null) {
            AnnotationValueExtractor extractor = context.getValueExtractor(memberDef);

            ret.setName(extractor.extractValue("name", String.class, true));
            ret.setDisplayName(extractor.extractValue("displayName", String.class, false));
            ret.setDescription(extractor.extractValue("description", String.class, false));
            ret.setReadOnly(extractor.extractValue("readOnly", Boolean.class, false));
            ret.setRequired(extractor.extractValue("required", Boolean.class, false));

            if (propertyType == PropertyInstanceType.SIMPLE) {
                SimpleProperty sp = (SimpleProperty) ret;
                setOptionSource(sp, extractor.extractAnnotationValue("optionSource", false));
                setOptions(sp, extractor.extractAnnotationValue("options", false), memberType);

                setConstraints(sp, field, memberType, extractor.extractAnnotationArrayValue("constraints", true));

                determinePropertyType(sp, null, fieldType);
                if (sp.getType() == PropertyType.STRING && extractor.extractValue("password", boolean.class, true)) {
                    sp.setType(PropertyType.PASSWORD);
                }

                if (sp.getType() == PropertyType.FILE && extractor.extractValue("directory", boolean.class, true)) {
                    sp.setType(PropertyType.DIRECTORY);
                }

                sp.setInitialValue(extractor.extractValue("initialValue", String.class, false));
            }
        }
    }

    private <T extends ConfigurationProperty> void encodeMapProperty(MapProperty prop,
        TypeElement fieldType) {
        List<VariableElement> mapMemberFields = ElementFilter.fieldsIn(fieldType.getEnclosedElements());

        for (VariableElement f : mapMemberFields) {
            ConfigurationProperty mapProp = encodeField(f);

            JAXBElement<T> el = new JAXBElement<T>(
                new QName("urn:rhq-configuration", PropertyInstanceType.detect(mapProp).getXmlName()),
                (Class<T>) mapProp.getClass(), (T) mapProp);

            prop.getConfigurationProperty().add(el);
        }
    }

    private void encodeSimpleProperty(SimpleProperty prop, VariableElement field, TypeElement fieldType) {
        String defaultValue = getAnnotationValue(field, InitialValue.class, String.class);

        prop.setInitialValue(defaultValue);

        AnnotationMirror optionSource = Util.findAnnotation(field, OptionSource.class);
        setOptionSource(prop, optionSource);

        AnnotationMirror optionsAnnotation = Util.findAnnotation(field, Options.class);
        setOptions(prop, optionsAnnotation, fieldType);

        setConstraints(prop, field, fieldType, Util.findAnnotation(field, Constraints.class));

        determinePropertyType(prop, field, fieldType);
        prop.setUnits(Common.getAnnotatedUnit(field));
    }

    private void setOptionSource(SimpleProperty prop, AnnotationMirror optionSource) {
        if (optionSource == null) {
            return;
        }

        AnnotationValueExtractor extractor = context.getValueExtractor(optionSource);
        OptionSource.Target target = extractor.extractValue("target", OptionSource.Target.class, false);
        if (target != OptionSource.Target.NONE) {
            Boolean linkToTarget = extractor.extractValue("linkToTarget", Boolean.class, false);
            String filter = extractor.extractValue("filter", String.class, false);
            String expr = extractor.extractValue("expression", String.class, false);

            org.rhq.core.clientapi.descriptor.configuration.OptionSource os = new org.rhq.core.clientapi.descriptor.configuration.OptionSource();
            os.setExpression(expr);
            os.setFilter(filter);
            os.setLinkToTarget(linkToTarget);
            os.setTarget(Enum.valueOf(SourceType.class, target.name()));

            prop.setOptionSource(os);
        }
    }

    private void setOptions(SimpleProperty prop, AnnotationMirror optionsAnnotation, TypeElement fieldType) {
        if (optionsAnnotation != null) {
            setPropertyOptionsFromAnnotation(prop, optionsAnnotation);
        } else if (fieldType.getKind() == ElementKind.ENUM) {
            setPropertyOptionsFromEnum(prop, fieldType);
        }
    }

    private void setPropertyOptionsFromEnum(SimpleProperty prop, TypeElement fieldType) {
        List<VariableElement> enumFields = ElementFilter.fieldsIn(fieldType.getEnclosedElements());
        for (VariableElement enumField : enumFields) {
            if (enumField.getKind() == ElementKind.ENUM_CONSTANT) {
                String name = enumField.getSimpleName().toString();
                String customName = Common.getAnnotatedName(enumField);

                Option option = new Option();

                if (customName != null) {
                    option.setName(customName);
                }
                option.setValue(name);
                prop.getPropertyOptions().getOption().add(option);
            }
        }
    }

    private void setPropertyOptionsFromAnnotation(SimpleProperty prop, AnnotationMirror optionsAnnotation) {
        List<Option> options = context.getValueExtractor(optionsAnnotation)
            .extractArrayValue("value", false, new SimpleAnnotationValueVisitor6<Option, ProcessingContext>() {
                @Override
                public Option visitAnnotation(AnnotationMirror a, ProcessingContext processingContext) {
                    Option o = new Option();

                    AnnotationValueExtractor extractor = context.getValueExtractor(a);

                    o.setName(extractor.extractValue("name", String.class, false));
                    o.setValue(extractor.extractValue("value", String.class, false));

                    return o;
                }
            });

        if (options != null) {
            prop.getPropertyOptions().getOption().addAll(options);
        }
    }

    private <T> T getAnnotationValue(VariableElement field, Class<? extends Annotation> annotation,
        Class<T> valueType) {
        AnnotationMirror a = Util.findAnnotation(field, annotation);
        if (a == null) {
            return null;
        }

        return context.getValueExtractor(a).extractValue("value", valueType, false);
    }

    private boolean hasAnnotation(VariableElement field, Class<? extends Annotation> annotation) {
        return Util.findAnnotation(field, annotation) != null;
    }

    private void determinePropertyType(SimpleProperty prop, VariableElement field, TypeElement fieldType) {
        PropertyType type;

        //try to unbox here
        TypeMirror mirror = fieldType.asType();
        try {
            mirror = context.getProcessingEnvironment().getTypeUtils().unboxedType(mirror);
        } catch (IllegalArgumentException e) {
            //well, this wasn't a boxed type
        }

        switch (mirror.getKind()) {
        case BOOLEAN:
            type = PropertyType.BOOLEAN;
            break;
        case BYTE:
            type = PropertyType.INTEGER;
            break;
        case CHAR:
            type = PropertyType.STRING;
            ConstraintType c = new ConstraintType();
            RegexConstraintType constraint = new RegexConstraintType();
            constraint.setExpression(".?");
            c.getIntegerConstraintOrFloatConstraintOrRegexConstraint().add(constraint);
            prop.getConstraint().add(c);
            break;
        case DOUBLE:
            type = PropertyType.DOUBLE;
            break;
        case FLOAT:
            type = PropertyType.FLOAT;
            break;
        case INT:
            type = PropertyType.INTEGER;
            break;
        case LONG:
            type = PropertyType.LONG;
            break;
        case SHORT:
            type = PropertyType.INTEGER;
            break;
        case DECLARED:
            if ("java.lang.String".equals(fieldType.getQualifiedName().toString())) {
                type = PropertyType.STRING;
                if (field != null && hasAnnotation(field, Password.class)) {
                    type = PropertyType.PASSWORD;
                }
            } else if ("java.io.File".equals(fieldType.getQualifiedName().toString())) {
                type = PropertyType.FILE;
                if (field != null && hasAnnotation(field, Directory.class)) {
                    type = PropertyType.DIRECTORY;
                }
            } else {
                throw new IllegalArgumentException("Field '" + field.getSimpleName() + "' on type '" +
                    ((TypeElement) field.getEnclosingElement()).getQualifiedName().toString() +
                    "' does not have a supported type for a simple property.");
            }
            break;
        default:
            return;
        }

        prop.setType(type);
    }

    private void setConstraints(SimpleProperty prop, VariableElement declaringField, TypeElement fieldType,
        AnnotationMirror constraintsAnnotation) {

        if (constraintsAnnotation == null) {
            return;
        }

        List<AnnotationMirror> constraints = context.getValueExtractor(constraintsAnnotation)
            .extractAnnotationArrayValue("value", true);
        setConstraints(prop, declaringField, fieldType, constraints);
    }

    private void setConstraints(SimpleProperty prop, VariableElement declaringField, TypeElement fieldType,
        List<AnnotationMirror> constraints) {
        if (constraints == null || constraints.isEmpty()) {
            return;
        }

        for (AnnotationMirror c : constraints) {
            AnnotationValueExtractor ex = context.getValueExtractor(c);

            Double min = ex.extractValue("minimum", Double.class, false);
            Double max = ex.extractValue("maximum", Double.class, false);
            String expr = ex.extractValue("expression", String.class, false);

            switch (fieldType.asType().getKind()) {
            case BYTE:
                if (min == null || min != min.byteValue()) {
                    throw constraintTypeException("Minimum", declaringField, fieldType);
                }
                if (max == null || max != max.byteValue()) {
                    throw constraintTypeException("Maximum", declaringField, fieldType);
                }
                if (expr != null) {
                    throw expressionNotSupportedException(declaringField);
                }
                break;
            case DOUBLE:
                if (expr != null) {
                    throw expressionNotSupportedException(declaringField);
                }
                break;
            case FLOAT:
                if (min == null || min != min.floatValue()) {
                    throw constraintTypeException("Minimum", declaringField, fieldType);
                }
                if (max == null || max != max.floatValue()) {
                    throw constraintTypeException("Maximum", declaringField, fieldType);
                }
                if (expr != null) {
                    throw expressionNotSupportedException(declaringField);
                }
                break;
            case INT:
                if (min == null || min != min.intValue()) {
                    throw constraintTypeException("Minimum", declaringField, fieldType);
                }
                if (max == null || max != max.intValue()) {
                    throw constraintTypeException("Maximum", declaringField, fieldType);
                }
                if (expr != null) {
                    throw expressionNotSupportedException(declaringField);
                }
                break;
            case LONG:
                if (min == null || min != min.longValue()) {
                    throw constraintTypeException("Minimum", declaringField, fieldType);
                }
                if (max == null || max != max.longValue()) {
                    throw constraintTypeException("Maximum", declaringField, fieldType);
                }
                if (expr != null) {
                    throw expressionNotSupportedException(declaringField);
                }
                break;
            case SHORT:
                if (min == null || min != min.shortValue()) {
                    throw constraintTypeException("Minimum", declaringField, fieldType);
                }
                if (max == null || max != max.shortValue()) {
                    throw constraintTypeException("Maximum", declaringField, fieldType);
                }
                if (expr != null) {
                    throw expressionNotSupportedException(declaringField);
                }
                break;
            case DECLARED:
                if ("java.lang.String".equals(fieldType.getQualifiedName().toString())) {
                    if (expr == null) {
                        throw expressionRequiredException(declaringField);
                    }
                    if (min != null || max != null) {
                        throw minMaxNotSupportedException(declaringField);
                    }

                    try {
                        Pattern.compile(expr);
                    } catch (PatternSyntaxException e) {
                        AgentPluginDescriptorException x = invalidExpressionException(declaringField);
                        x.initCause(e);
                        throw x;
                    }
                }
            }
        }
    }

    private AgentPluginDescriptorException constraintTypeException(String constraintElement,
        VariableElement declaringField,
        TypeElement fieldType) {
        TypeElement fieldOwnerType = (TypeElement) declaringField.getEnclosingElement();
        return new AgentPluginDescriptorException(
            constraintElement + " value on a constraint on field '" + declaringField.getSimpleName().toString() +
                "' in type '" +
                fieldOwnerType.getQualifiedName().toString() +
                "' is not a value convertible to '" + fieldType.getQualifiedName().toString() + "'.");
    }

    private AgentPluginDescriptorException expressionNotSupportedException(VariableElement declaringField) {
        TypeElement fieldOwnerType = (TypeElement) declaringField.getEnclosingElement();
        return new AgentPluginDescriptorException(
            "Expressions constraint not supported on field '" + declaringField.getSimpleName().toString() +
                "' in type '" +
                fieldOwnerType.getQualifiedName().toString() +
                "'. Expressions are supported only on strings but the field is numeric.");
    }

    private AgentPluginDescriptorException invalidExpressionException(VariableElement declaringField) {
        TypeElement fieldOwnerType = (TypeElement) declaringField.getEnclosingElement();
        return new AgentPluginDescriptorException(
            "Expression constraint is not valid on field '" + declaringField.getSimpleName().toString() +
                "' in type '" + fieldOwnerType.getQualifiedName().toString() + "'.");
    }

    private AgentPluginDescriptorException minMaxNotSupportedException(VariableElement declaringField) {
        TypeElement fieldOwnerType = (TypeElement) declaringField.getEnclosingElement();
        return new AgentPluginDescriptorException(
            "Minimum/maximum constraint is not valid on field '" + declaringField.getSimpleName().toString() +
                "' in type '" + fieldOwnerType.getQualifiedName().toString() + "'. It is only valid for numeric types.");
    }

    private AgentPluginDescriptorException expressionRequiredException(VariableElement declaringField) {
        TypeElement fieldOwnerType = (TypeElement) declaringField.getEnclosingElement();
        return new AgentPluginDescriptorException(
            "Expression constraint is required on field '" + declaringField.getSimpleName().toString() +
                "' in type '" + fieldOwnerType.getQualifiedName().toString() + "'.");
    }

    private AgentPluginDescriptorException constraintsNotSupportedException(VariableElement declaringField) {
        TypeElement fieldOwnerType = (TypeElement) declaringField.getEnclosingElement();
        return new AgentPluginDescriptorException(
            "Constraints are not supported on field '" + declaringField.getSimpleName().toString() +
                "' in type '" + fieldOwnerType.getQualifiedName().toString() + "'. String or numeric types are required.");
    }
}
