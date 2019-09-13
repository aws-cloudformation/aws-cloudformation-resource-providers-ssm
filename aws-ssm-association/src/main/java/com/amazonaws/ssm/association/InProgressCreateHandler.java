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

import java.util.Optional;

/**
 * Handles noninitial create requests for a given resource.
 */
public class InProgressCreateHandler extends BaseHandler<CallbackContext> {

    private final int callbackDelaySeconds;
    private final SsmClient ssmClient;
    private final AssociationDescriptionTranslator associationDescriptionTranslator;
    private final ExceptionTranslator exceptionTranslator;

    /**
     * Constructor to use by dependencies. Processes noninitial CreateHandler requests.
     *
     * @param callbackDelaySeconds Callback delay period.
     * @param ssmClient SsmClient implementation to use for API calls.
     */
    InProgressCreateHandler(final int callbackDelaySeconds, final SsmClient ssmClient) {
        this.callbackDelaySeconds = callbackDelaySeconds;
        this.ssmClient = ssmClient;
        this.associationDescriptionTranslator = new AssociationDescriptionTranslator();
        this.exceptionTranslator = new ExceptionTranslator();
    }

    /**
     * Used for unit tests.
     *
     * @param callbackDelaySeconds Callback delay period.
     * @param ssmClient SsmClient implementation to use for API calls.
     * @param associationDescriptionTranslator Translates AssociationDescription into ResourceModel objects.
     * @param exceptionTranslator Translates service model exceptions.
     */
    InProgressCreateHandler(final int callbackDelaySeconds,
                            final SsmClient ssmClient,
                            final AssociationDescriptionTranslator associationDescriptionTranslator,
                            final ExceptionTranslator exceptionTranslator) {

        this.callbackDelaySeconds = callbackDelaySeconds;
        this.ssmClient = ssmClient;
        this.associationDescriptionTranslator = associationDescriptionTranslator;
        this.exceptionTranslator = exceptionTranslator;
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
                .translateFromServiceException(e, describeAssociationRequest, Optional.of(associationId));

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

            final Integer remainingTimeoutSeconds = callbackContext.getRemainingTimeoutSeconds();

            if (remainingTimeoutSeconds <= 0) {
                throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME, associationId);
            }

            if (remainingTimeoutSeconds < callbackDelaySeconds) {

                return ProgressEvent.defaultInProgressHandler(
                    CallbackContext.builder()
                        .remainingTimeoutSeconds(0)
                        .associationId(associationId)
                        .build(),
                    remainingTimeoutSeconds,
                    existingModel);
            } else {
                return ProgressEvent.defaultInProgressHandler(
                    CallbackContext.builder()
                        .remainingTimeoutSeconds(remainingTimeoutSeconds - callbackDelaySeconds)
                        .associationId(associationId)
                        .build(),
                    callbackDelaySeconds,
                    existingModel);
            }
        } else {
            throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME, associationId);
        }
    }
}
