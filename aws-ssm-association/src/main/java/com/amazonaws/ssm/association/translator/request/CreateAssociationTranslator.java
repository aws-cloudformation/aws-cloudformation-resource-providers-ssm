package com.amazonaws.ssm.association.translator.request;

import com.amazonaws.ssm.association.ResourceModel;
import com.amazonaws.ssm.association.translator.property.InstanceAssociationOutputLocationTranslator;
import com.amazonaws.ssm.association.translator.property.TargetsListTranslator;
import com.amazonaws.ssm.association.util.SimpleTypeValidator;
import software.amazon.awssdk.services.ssm.model.CreateAssociationRequest;

/**
 * Translates ResourceModel objects into AWS SDK's CreateAssociation requests.
 */
public class CreateAssociationTranslator implements RequestTranslator<CreateAssociationRequest> {

    private final SimpleTypeValidator simpleTypeValidator;
    private final InstanceAssociationOutputLocationTranslator instanceAssociationOutputLocationTranslator;
    private final TargetsListTranslator targetsListTranslator;

    /**
     * Constructor that initializes all required fields.
     */
    public CreateAssociationTranslator() {
        this.simpleTypeValidator = new SimpleTypeValidator();
        this.instanceAssociationOutputLocationTranslator = new InstanceAssociationOutputLocationTranslator();
        this.targetsListTranslator = new TargetsListTranslator();
    }

    /**
     * Used for unit tests.
     *
     * @param simpleTypeValidator Validator for simple data types.
     * @param instanceAssociationOutputLocationTranslator PropertyTranslator for InstanceAssociationOutputLocation property.
     * @param targetsListTranslator PropertyTranslator for Targets List property.
     */
    public CreateAssociationTranslator(final SimpleTypeValidator simpleTypeValidator,
                                       final InstanceAssociationOutputLocationTranslator instanceAssociationOutputLocationTranslator,
                                       final TargetsListTranslator targetsListTranslator) {
        this.simpleTypeValidator = simpleTypeValidator;
        this.instanceAssociationOutputLocationTranslator = instanceAssociationOutputLocationTranslator;
        this.targetsListTranslator = targetsListTranslator;
    }

    /**
     * Converts ResourceModel object into CreateAssociationRequest.
     *
     * @param model ResourceModel to convert into a CreateAssociationRequest.
     * @return CreateAssociationRequest with properties present on the model.
     */
    @Override
    public CreateAssociationRequest resourceModelToRequest(final ResourceModel model) {
        final CreateAssociationRequest.Builder createAssociationRequestBuilder =
            CreateAssociationRequest.builder()
                .name(model.getName());

        simpleTypeValidator.getValidatedString(model.getAssociationName())
            .ifPresent(createAssociationRequestBuilder::associationName);

        simpleTypeValidator.getValidatedString(model.getDocumentVersion())
            .ifPresent(createAssociationRequestBuilder::documentVersion);

        simpleTypeValidator.getValidatedString(model.getInstanceId())
            .ifPresent(createAssociationRequestBuilder::instanceId);

        simpleTypeValidator.getValidatedMap(model.getParameters())
            .ifPresent(createAssociationRequestBuilder::parameters);

        simpleTypeValidator.getValidatedString(model.getScheduleExpression())
            .ifPresent(createAssociationRequestBuilder::scheduleExpression);

        targetsListTranslator.resourceModelPropertyToServiceModel(model.getTargets())
            .ifPresent(createAssociationRequestBuilder::targets);

        instanceAssociationOutputLocationTranslator.resourceModelPropertyToServiceModel(model.getOutputLocation())
            .ifPresent(createAssociationRequestBuilder::outputLocation);

        simpleTypeValidator.getValidatedString(model.getAutomationTargetParameterName())
            .ifPresent(createAssociationRequestBuilder::automationTargetParameterName);

        simpleTypeValidator.getValidatedString(model.getMaxErrors())
            .ifPresent(createAssociationRequestBuilder::maxErrors);

        simpleTypeValidator.getValidatedString(model.getMaxConcurrency())
            .ifPresent(createAssociationRequestBuilder::maxConcurrency);

        simpleTypeValidator.getValidatedString(model.getComplianceSeverity())
            .ifPresent(createAssociationRequestBuilder::complianceSeverity);
        
        simpleTypeValidator.getValidatedString(model.getSyncCompliance())
            .ifPresent(createAssociationRequestBuilder::syncCompliance);

        return createAssociationRequestBuilder.build();
    }
}
