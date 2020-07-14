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

/**
 * Handles initial create requests for a given resource.
 */
public class InitialCreateHandler extends BaseHandler<CallbackContext> {

    private final SsmClient ssmClient;
    private final CreateAssociationTranslator createAssociationTranslator;
    private final AssociationDescriptionTranslator associationDescriptionTranslator;
    private final ExceptionTranslator exceptionTranslator;
    private final InProgressEventCreator inProgressEventCreator;

    /**
     * Constructor to use by dependencies. Processes initial CreateHandler requests.
     *
     * @param ssmClient SsmClient implementation to use for API calls.
     */
    InitialCreateHandler(final SsmClient ssmClient) {
        this.ssmClient = ssmClient;
        this.createAssociationTranslator = new CreateAssociationTranslator();
        this.associationDescriptionTranslator = new AssociationDescriptionTranslator();
        this.exceptionTranslator = new ExceptionTranslator();
        this.inProgressEventCreator = new InProgressEventCreator();
    }

    /**
     * Used for unit tests.
     *
     * @param ssmClient SsmClient implementation to use for API calls.
     * @param createAssociationTranslator Translates ResourceModel objects into CreateAssociation requests.
     * @param associationDescriptionTranslator Translates AssociationDescription into ResourceModel objects.
     * @param exceptionTranslator Translates service model exceptions.
     * @param inProgressEventCreator Creates InProgress ProgressEvent objects for progress chaining.
     */
    InitialCreateHandler(final SsmClient ssmClient,
                         final CreateAssociationTranslator createAssociationTranslator,
                         final AssociationDescriptionTranslator associationDescriptionTranslator,
                         final ExceptionTranslator exceptionTranslator,
                         final InProgressEventCreator inProgressEventCreator) {

        this.ssmClient = ssmClient;
        this.createAssociationTranslator = createAssociationTranslator;
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
                .translateFromServiceException(e, createAssociationRequest, desiredModel);

            logger.log(cfnException.getCause().getMessage());

            throw cfnException;
        }

        final ResourceModel resultModel =
            associationDescriptionTranslator.associationDescriptionToResourceModel(resultAssociationDescription);

        final Integer waitForSuccessTimeoutSeconds = desiredModel.getWaitForSuccessTimeoutSeconds();

        if (waitForSuccessTimeoutSeconds == null) {
            // return success without waiting for association to complete
            return ProgressEvent.defaultSuccessHandler(resultModel);
        } else {
            // indicates a Create request that needs to wait for association to complete
            return inProgressEventCreator.nextInProgressEvent(waitForSuccessTimeoutSeconds, resultModel);
        }
    }
}
