package com.amazonaws.ssm.association;

import com.amazonaws.ssm.association.translator.AssociationDescriptionTranslator;
import com.amazonaws.ssm.association.translator.ExceptionTranslator;
import com.amazonaws.ssm.association.translator.request.CreateAssociationTranslator;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.AssociationDescription;
import software.amazon.awssdk.services.ssm.model.CreateAssociationRequest;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Optional;

/**
 * Handles initial create requests for a given resource.
 */
public class InitialCreateHandler extends BaseHandler<CallbackContext> {

    private final int callbackDelaySeconds;
    private final SsmClient ssmClient;
    private final CreateAssociationTranslator createAssociationTranslator;
    private final AssociationDescriptionTranslator associationDescriptionTranslator;
    private final ExceptionTranslator exceptionTranslator;

    /**
     * Constructor to use by dependencies. Processes initial CreateHandler requests.
     *
     * @param callbackDelaySeconds Callback delay period.
     * @param ssmClient SsmClient implementation to use for API calls.
     */
    InitialCreateHandler(final int callbackDelaySeconds, final SsmClient ssmClient) {
        this.callbackDelaySeconds = callbackDelaySeconds;
        this.ssmClient = ssmClient;
        this.createAssociationTranslator = new CreateAssociationTranslator();
        this.associationDescriptionTranslator = new AssociationDescriptionTranslator();
        this.exceptionTranslator = new ExceptionTranslator();
    }

    /**
     * Used for unit tests.
     *
     * @param callbackDelaySeconds Callback delay period.
     * @param ssmClient SsmClient implementation to use for API calls.
     * @param createAssociationTranslator Translates ResourceModel objects into CreateAssociation requests.
     * @param associationDescriptionTranslator Translates AssociationDescription into ResourceModel objects.
     * @param exceptionTranslator Translates service model exceptions.
     */
    InitialCreateHandler(final int callbackDelaySeconds,
                         final SsmClient ssmClient,
                         final CreateAssociationTranslator createAssociationTranslator,
                         final AssociationDescriptionTranslator associationDescriptionTranslator,
                         final ExceptionTranslator exceptionTranslator) {

        this.callbackDelaySeconds = callbackDelaySeconds;
        this.ssmClient = ssmClient;
        this.createAssociationTranslator = createAssociationTranslator;
        this.associationDescriptionTranslator = associationDescriptionTranslator;
        this.exceptionTranslator = exceptionTranslator;
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel desiredModel = request.getDesiredResourceState();

        final CreateAssociationRequest createAssociationRequest =
            createAssociationTranslator.resourceModelToRequest(desiredModel);

        final AssociationDescription resultAssociationDescription;

        try {
            resultAssociationDescription =
                proxy.injectCredentialsAndInvokeV2(createAssociationRequest, ssmClient::createAssociation)
                    .associationDescription();
        } catch (Exception e) {
            final BaseHandlerException cfnException = exceptionTranslator
                .translateFromServiceException(e, createAssociationRequest, Optional.empty());

            logger.log(cfnException.getCause().getMessage());

            throw cfnException;
        }

        final ResourceModel resultModel =
            associationDescriptionTranslator.associationDescriptionToResourceModel(resultAssociationDescription);

        if (desiredModel.getWaitForSuccessTimeoutSeconds() == null) {
            // return success without waiting for association to complete
            return ProgressEvent.defaultSuccessHandler(resultModel);
        } else {
            // indicates a Create request that needs to wait for association to complete
            final int remainingTimeoutSeconds =
                desiredModel.getWaitForSuccessTimeoutSeconds() - callbackDelaySeconds;

            return ProgressEvent.defaultInProgressHandler(
                CallbackContext.builder()
                    .remainingTimeoutSeconds(remainingTimeoutSeconds)
                    .associationId(resultAssociationDescription.associationId())
                    .build(),
                callbackDelaySeconds,
                resultModel);
        }
    }
}
