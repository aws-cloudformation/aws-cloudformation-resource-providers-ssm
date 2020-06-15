package com.amazonaws.ssm.association;

import com.amazonaws.ssm.association.translator.AssociationDescriptionTranslator;
import com.amazonaws.ssm.association.translator.ExceptionTranslator;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.AssociationDescription;
import software.amazon.awssdk.services.ssm.model.AssociationStatusName;
import software.amazon.awssdk.services.ssm.model.DescribeAssociationRequest;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

/**
 * Handles noninitial create requests for a given resource.
 */
public class InProgressHandler extends BaseHandler<CallbackContext> {

    private final SsmClient ssmClient;
    private final AssociationDescriptionTranslator associationDescriptionTranslator;
    private final ExceptionTranslator exceptionTranslator;
    private final InProgressEventCreator inProgressEventCreator;

    /**
     * Constructor to use by dependencies. Processes noninitial CreateHandler requests.
     *
     * @param ssmClient SsmClient implementation to use for API calls.
     */
    InProgressHandler(final SsmClient ssmClient) {
        this.ssmClient = ssmClient;
        this.associationDescriptionTranslator = new AssociationDescriptionTranslator();
        this.exceptionTranslator = new ExceptionTranslator();
        this.inProgressEventCreator = new InProgressEventCreator();
    }

    /**
     * Used for unit tests.
     *
     * @param ssmClient SsmClient implementation to use for API calls.
     * @param associationDescriptionTranslator Translates AssociationDescription into ResourceModel objects.
     * @param exceptionTranslator Translates service model exceptions.
     * @param inProgressEventCreator Creates InProgress ProgressEvent objects for progress chaining.
     */
    InProgressHandler(final SsmClient ssmClient,
                      final AssociationDescriptionTranslator associationDescriptionTranslator,
                      final ExceptionTranslator exceptionTranslator,
                      final InProgressEventCreator inProgressEventCreator) {

        this.ssmClient = ssmClient;
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

        final String associationId = callbackContext.getAssociationId();
        final DescribeAssociationRequest describeAssociationRequest =
            DescribeAssociationRequest.builder()
                .associationId(associationId)
                .build();

        final AssociationDescription requestAssociation;

        try {
            requestAssociation =
                proxy.injectCredentialsAndInvokeV2(describeAssociationRequest, ssmClient::describeAssociation)
                    .associationDescription();

        } catch (Exception e) {
            final BaseHandlerException cfnException = exceptionTranslator
                .translateFromServiceException(e, describeAssociationRequest, request.getDesiredResourceState());

            logger.log(cfnException.getCause().getMessage());

            throw cfnException;
        }

        final ResourceModel existingModel =
            associationDescriptionTranslator.associationDescriptionToResourceModel(requestAssociation);

        if (AssociationStatusName.SUCCESS.name()
            .equalsIgnoreCase(requestAssociation.overview().status())) {

            return ProgressEvent.defaultSuccessHandler(existingModel);

        } else if (AssociationStatusName.PENDING.name()
            .equalsIgnoreCase(requestAssociation.overview().status())) {

            final int remainingTimeoutSeconds = callbackContext.getRemainingTimeoutSeconds();

            if (remainingTimeoutSeconds <= 0) {
                throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME, associationId);
            }

            return inProgressEventCreator.nextInProgressEvent(remainingTimeoutSeconds, existingModel);
        } else {
            throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME, associationId);
        }
    }
}
