package com.amazonaws.ssm.association.translator.request;

import com.amazonaws.ssm.association.ResourceModel;
import com.amazonaws.ssm.association.translator.property.InstanceAssociationOutputLocationTranslator;
import com.amazonaws.ssm.association.translator.property.TargetsListTranslator;
import com.amazonaws.ssm.association.util.SimpleTypeValidator;
import software.amazon.awssdk.services.ssm.model.UpdateAssociationRequest;

/**
 * Translates ResourceModel objects into AWS SDK's UpdateAssociation requests.
 */
public class UpdateAssociationTranslator implements RequestTranslator<UpdateAssociationRequest> {

    private final SimpleTypeValidator simpleTypeValidator;
    private final InstanceAssociationOutputLocationTranslator instanceAssociationOutputLocationTranslator;
    private final TargetsListTranslator targetsListTranslator;

    /**
     * Constructor that initializes all required fields.
     */
    public UpdateAssociationTranslator() {
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
    UpdateAssociationTranslator(final SimpleTypeValidator simpleTypeValidator,
                                       final InstanceAssociationOutputLocationTranslator instanceAssociationOutputLocationTranslator,
                                       final TargetsListTranslator targetsListTranslator) {
        this.simpleTypeValidator = simpleTypeValidator;
        this.instanceAssociationOutputLocationTranslator = instanceAssociationOutputLocationTranslator;
        this.targetsListTranslator = targetsListTranslator;
    }

    /**
     * Converts ResourceModel objects into UpdateAssociationRequests to be used for API calls.
     *
     * @param model ResourceModel to convert into an UpdateAssociationRequest.
     * @return UpdateAssociationRequest with parameters present on the model.
     */
    @Override
    public UpdateAssociationRequest resourceModelToRequest(final ResourceModel model) {
        final UpdateAssociationRequest.Builder updateAssociationRequestBuilder =
            UpdateAssociationRequest.builder()
                .associationId(model.getAssociationId())
                .applyOnlyAtCronInterval(model.getApplyOnlyAtCronInterval());

        simpleTypeValidator.getValidatedString(model.getName())
            .ifPresent(updateAssociationRequestBuilder::name);

        simpleTypeValidator.getValidatedString(model.getAssociationName())
            .ifPresent(updateAssociationRequestBuilder::associationName);

        simpleTypeValidator.getValidatedString(model.getDocumentVersion())
            .ifPresent(updateAssociationRequestBuilder::documentVersion);

        simpleTypeValidator.getValidatedMap(model.getParameters())
            .ifPresent(updateAssociationRequestBuilder::parameters);

        simpleTypeValidator.getValidatedString(model.getScheduleExpression())
            .ifPresent(updateAssociationRequestBuilder::scheduleExpression);

        targetsListTranslator.resourceModelPropertyToServiceModel(model.getTargets())
            .ifPresent(updateAssociationRequestBuilder::targets);

        instanceAssociationOutputLocationTranslator.resourceModelPropertyToServiceModel(model.getOutputLocation())
            .ifPresent(updateAssociationRequestBuilder::outputLocation);

        simpleTypeValidator.getValidatedString(model.getAutomationTargetParameterName())
            .ifPresent(updateAssociationRequestBuilder::automationTargetParameterName);

        simpleTypeValidator.getValidatedString(model.getMaxErrors())
            .ifPresent(updateAssociationRequestBuilder::maxErrors);

        simpleTypeValidator.getValidatedString(model.getMaxConcurrency())
            .ifPresent(updateAssociationRequestBuilder::maxConcurrency);

        simpleTypeValidator.getValidatedString(model.getComplianceSeverity())
            .ifPresent(updateAssociationRequestBuilder::complianceSeverity);

        simpleTypeValidator.getValidatedString(model.getSyncCompliance())
            .ifPresent(updateAssociationRequestBuilder::syncCompliance);

        return updateAssociationRequestBuilder.build();
    }
}
