package com.amazonaws.ssm.association;

import com.amazonaws.ssm.association.translator.AssociationDescriptionTranslator;
import com.amazonaws.ssm.association.translator.ExceptionTranslator;
import com.amazonaws.ssm.association.translator.request.UpdateAssociationTranslator;
import com.amazonaws.ssm.association.util.SsmClientBuilder;
import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.AssociationDescription;
import software.amazon.awssdk.services.ssm.model.UpdateAssociationRequest;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Optional;

/**
 * Handles update requests on a given resource.
 */
public class UpdateHandler extends BaseHandler<CallbackContext> {

    private static final SsmClient SSM_CLIENT = SsmClientBuilder.getClient();

    private final UpdateAssociationTranslator updateAssociationTranslator;
    private final AssociationDescriptionTranslator associationDescriptionTranslator;
    private final ExceptionTranslator exceptionTranslator;

    /**
     * Constructor to use by dependencies. Processes Update requests.
     */
    UpdateHandler() {
        this.updateAssociationTranslator = new UpdateAssociationTranslator();
        this.associationDescriptionTranslator = new AssociationDescriptionTranslator();
        this.exceptionTranslator = new ExceptionTranslator();
    }

    /**
     * Used for unit tests.
     *
     * @param updateAssociationTranslator Translates ResourceModel objects into UpdateAssociation requests.
     * @param associationDescriptionTranslator Translates AssociationDescription into ResourceModel objects.
     * @param exceptionTranslator Translates service model exceptions.
     */
    UpdateHandler(final UpdateAssociationTranslator updateAssociationTranslator,
                  final AssociationDescriptionTranslator associationDescriptionTranslator,
                  final ExceptionTranslator exceptionTranslator) {
        this.updateAssociationTranslator = updateAssociationTranslator;
        this.associationDescriptionTranslator = associationDescriptionTranslator;
        this.exceptionTranslator = exceptionTranslator;
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        logger.log(String.format("Processing UpdateHandler request %s", request));

        final ResourceModel requestModel = request.getDesiredResourceState();

        final ProgressEvent<ResourceModel, CallbackContext> progressEvent = new ProgressEvent<>();
        progressEvent.setResourceModel(request.getPreviousResourceState());
        progressEvent.setStatus(OperationStatus.FAILED);

        final String associationId = requestModel.getAssociationId();

        if (StringUtils.isNullOrEmpty(associationId)) {
            progressEvent.setErrorCode(HandlerErrorCode.InvalidRequest);
            progressEvent.setMessage("AssociationId must be present to update the existing association.");
            return progressEvent;
        }

        final UpdateAssociationRequest updateAssociationRequest =
            updateAssociationTranslator.resourceModelToRequest(requestModel);

        try {
            final AssociationDescription association =
                proxy.injectCredentialsAndInvokeV2(updateAssociationRequest, SSM_CLIENT::updateAssociation)
                    .associationDescription();

            final ResourceModel updatedModel =
                associationDescriptionTranslator.associationDescriptionToResourceModel(association);

            progressEvent.setResourceModel(updatedModel);
            progressEvent.setStatus(OperationStatus.SUCCESS);

        } catch (Exception e) {
            final BaseHandlerException cfnException = exceptionTranslator
                .translateFromServiceException(e, updateAssociationRequest, Optional.of(associationId));

            logger.log(cfnException.getCause().getMessage());

            throw cfnException;
        }

        return progressEvent;
    }
}
