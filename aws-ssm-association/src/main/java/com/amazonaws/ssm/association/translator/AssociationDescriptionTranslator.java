package com.amazonaws.ssm.association.translator;

import com.amazonaws.ssm.association.ResourceModel;
import com.amazonaws.ssm.association.translator.property.InstanceAssociationOutputLocationTranslator;
import com.amazonaws.ssm.association.translator.property.ParametersTranslator;
import com.amazonaws.ssm.association.translator.property.TargetsListTranslator;
import com.amazonaws.ssm.association.util.SimpleTypeValidator;
import software.amazon.awssdk.services.ssm.model.AssociationDescription;

/**
 * Object translator for converting State Manager's AssociationDescription to ResourceModel object.
 */
public class AssociationDescriptionTranslator {

    private final SimpleTypeValidator simpleTypeValidator;
    private final InstanceAssociationOutputLocationTranslator instanceAssociationOutputLocationTranslator;
    private final TargetsListTranslator targetsListTranslator;
    private final ParametersTranslator parametersTranslator;

    /**
     * Constructor that initializes all required fields.
     */
    public AssociationDescriptionTranslator() {
        this.simpleTypeValidator = new SimpleTypeValidator();
        this.instanceAssociationOutputLocationTranslator = new InstanceAssociationOutputLocationTranslator();
        this.targetsListTranslator = new TargetsListTranslator();
        this.parametersTranslator = new ParametersTranslator();
    }

    /**
     * Used for unit tests.
     *
     * @param simpleTypeValidator Validator for simple data types.
     * @param instanceAssociationOutputLocationTranslator PropertyTranslator for InstanceAssociationOutputLocation property.
     * @param targetsListTranslator PropertyTranslator for Targets List property.
     */
    public AssociationDescriptionTranslator(final SimpleTypeValidator simpleTypeValidator,
                                            final InstanceAssociationOutputLocationTranslator instanceAssociationOutputLocationTranslator,
                                            final TargetsListTranslator targetsListTranslator,
                                            final ParametersTranslator parametersTranslator) {
        this.simpleTypeValidator = simpleTypeValidator;
        this.instanceAssociationOutputLocationTranslator = instanceAssociationOutputLocationTranslator;
        this.targetsListTranslator = targetsListTranslator;
        this.parametersTranslator = parametersTranslator;
    }

    /**
     * Converts State Manager's AssociationDescription service object to ResourceModel.
     *
     * @param association AssociationDescription representing association to convert to the model.
     * @return ResourceModel representation of the association.
     */
    public ResourceModel associationDescriptionToResourceModel(
        final AssociationDescription association) {

        final ResourceModel model = new ResourceModel();

        model.setAssociationId(association.associationId());
        model.setName(association.name());

        simpleTypeValidator.getValidatedString(association.associationName())
            .ifPresent(model::setAssociationName);

        simpleTypeValidator.getValidatedString(association.documentVersion())
            .ifPresent(model::setDocumentVersion);

        simpleTypeValidator.getValidatedString(association.instanceId())
            .ifPresent(model::setInstanceId);

        parametersTranslator.serviceModelPropertyToResourceModel(association.parameters())
            .ifPresent(model::setParameters);

        simpleTypeValidator.getValidatedString(association.scheduleExpression())
            .ifPresent(model::setScheduleExpression);

        targetsListTranslator.serviceModelPropertyToResourceModel(association.targets())
            .ifPresent(model::setTargets);

        instanceAssociationOutputLocationTranslator.serviceModelPropertyToResourceModel(association.outputLocation())
            .ifPresent(model::setOutputLocation);

        simpleTypeValidator.getValidatedString(association.automationTargetParameterName())
            .ifPresent(model::setAutomationTargetParameterName);

        simpleTypeValidator.getValidatedString(association.maxErrors())
            .ifPresent(model::setMaxErrors);

        simpleTypeValidator.getValidatedString(association.maxConcurrency())
            .ifPresent(model::setMaxConcurrency);

        simpleTypeValidator.getValidatedString(association.complianceSeverityAsString())
            .ifPresent(model::setComplianceSeverity);

        return model;
    }
}
