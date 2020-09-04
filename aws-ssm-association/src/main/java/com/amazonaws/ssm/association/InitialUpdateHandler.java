package com.amazonaws.ssm.association;

import com.amazonaws.ssm.association.translator.AssociationDescriptionTranslator;
import com.amazonaws.ssm.association.translator.ExceptionTranslator;
import com.amazonaws.ssm.association.translator.request.UpdateAssociationTranslator;
import com.amazonaws.util.StringUtils;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.AssociationDescription;
import software.amazon.awssdk.services.ssm.model.UpdateAssociationRequest;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

/**
 * Handles initial update requests on a given resource.
 */
public class InitialUpdateHandler extends BaseHandler<CallbackContext> {

    private final SsmClient ssmClient;
    private final UpdateAssociationTranslator updateAssociationTranslator;
    private final AssociationDescriptionTranslator associationDescriptionTranslator;
    private final ExceptionTranslator exceptionTranslator;
    private final InProgressEventCreator inProgressEventCreator;

    /**
     * Constructor to use by dependencies. Processes initial UpdateHandler requests.
     */
    InitialUpdateHandler(final SsmClient ssmClient) {
        this.ssmClient = ssmClient;
        this.updateAssociationTranslator = new UpdateAssociationTranslator();
        this.associationDescriptionTranslator = new AssociationDescriptionTranslator();
        this.exceptionTranslator = new ExceptionTranslator();
        this.inProgressEventCreator = new InProgressEventCreator();
    }

    /**
     * Used for unit tests.
     *
     * @param ssmClient                        SsmClient implementation to use for API calls.
     * @param updateAssociationTranslator      Translates ResourceModel objects into UpdateAssociation requests.
     * @param associationDescriptionTranslator Translates AssociationDescription into ResourceModel objects.
     * @param exceptionTranslator              Translates service model exceptions.
     * @param inProgressEventCreator           Creates InProgress ProgressEvent objects for progress chaining.
     */
    InitialUpdateHandler(
        final SsmClient ssmClient,
        final UpdateAssociationTranslator updateAssociationTranslator,
        final AssociationDescriptionTranslator associationDescriptionTranslator,
        final ExceptionTranslator exceptionTranslator,
        final InProgressEventCreator inProgressEventCreator) {

        this.ssmClient = ssmClient;
        this.updateAssociationTranslator = updateAssociationTranslator;
        this.associationDescriptionTranslator = associationDescriptionTranslator;
        this.exceptionTranslator = exceptionTranslator;
        this.inProgressEventCreator = inProgressEventCreator;
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel requestModel = request.getDesiredResourceState();

        final String associationId = requestModel.getAssociationId();

        if (StringUtils.isNullOrEmpty(associationId)) {
            return ProgressEvent.failed(request.getPreviousResourceState(),
                callbackContext,
                HandlerErrorCode.InvalidRequest,
                "AssociationId must be present to update the existing association.");
        }

        final UpdateAssociationRequest updateAssociationRequest =
            updateAssociationTranslator.resourceModelToRequest(requestModel);

        final AssociationDescription resultAssociationDescription;

        try {
            resultAssociationDescription =
                proxy.injectCredentialsAndInvokeV2(updateAssociationRequest, ssmClient::updateAssociation)
                    .associationDescription();

        } catch (Exception e) {
            final BaseHandlerException cfnException = exceptionTranslator
                .translateFromServiceException(e, updateAssociationRequest, requestModel);

            logger.log(cfnException.getCause().getMessage());

            throw cfnException;
        }

        final ResourceModel updatedModel =
            associationDescriptionTranslator.associationDescriptionToResourceModel(resultAssociationDescription);

        final Integer waitForSuccessTimeoutSeconds = requestModel.getWaitForSuccessTimeoutSeconds();

        if (waitForSuccessTimeoutSeconds == null) {
            // return success without waiting for association to complete
            return ProgressEvent.defaultSuccessHandler(updatedModel);
        } else {
            // indicates an Update request that needs to wait for association to complete
            return inProgressEventCreator.nextInProgressEvent(waitForSuccessTimeoutSeconds, updatedModel);
        }
    }
}
