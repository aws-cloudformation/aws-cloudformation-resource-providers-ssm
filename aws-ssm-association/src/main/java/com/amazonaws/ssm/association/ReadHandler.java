package com.amazonaws.ssm.association;

import com.amazonaws.ssm.association.translator.AssociationDescriptionTranslator;
import com.amazonaws.ssm.association.translator.ExceptionTranslator;
import com.amazonaws.ssm.association.util.ResourceHandlerRequestToStringConverter;
import com.amazonaws.ssm.association.util.ResourceModelToStringConverter;
import com.amazonaws.ssm.association.util.SsmClientBuilder;
import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.AssociationDescription;
import software.amazon.awssdk.services.ssm.model.DescribeAssociationRequest;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

/**
 * Handles read requests for a given resource.
 */
public class ReadHandler extends BaseHandler<CallbackContext> {

    private static final SsmClient SSM_CLIENT = SsmClientBuilder.getClient();

    private final AssociationDescriptionTranslator associationDescriptionTranslator;
    private final ExceptionTranslator exceptionTranslator;
    private final ResourceHandlerRequestToStringConverter requestToStringConverter;

    /**
     * Constructor to use by dependencies. Processes Read requests.
     */
    ReadHandler() {
        this.associationDescriptionTranslator = new AssociationDescriptionTranslator();
        this.exceptionTranslator = new ExceptionTranslator();
        this.requestToStringConverter = new ResourceHandlerRequestToStringConverter(new ResourceModelToStringConverter());
    }

    /**
     * Used for unit tests.
     *
     * @param associationDescriptionTranslator Translates AssociationDescription into ResourceModel objects.
     * @param exceptionTranslator Translates service model exceptions.
     * @param requestToStringConverter ResourceHandlerRequestToStringConverter used to convert requests to Strings.
     */
    ReadHandler(final AssociationDescriptionTranslator associationDescriptionTranslator,
                final ExceptionTranslator exceptionTranslator,
                final ResourceHandlerRequestToStringConverter requestToStringConverter) {
        this.associationDescriptionTranslator = associationDescriptionTranslator;
        this.exceptionTranslator = exceptionTranslator;
        this.requestToStringConverter = requestToStringConverter;
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        logger.log(String.format("Processing ReadHandler request: %s", requestToStringConverter.convert(request)));

        final ResourceModel requestModel = request.getDesiredResourceState();

        final ProgressEvent<ResourceModel, CallbackContext> progressEvent = new ProgressEvent<>();
        progressEvent.setStatus(OperationStatus.FAILED);

        final String associationId = requestModel.getAssociationId();

        if (StringUtils.isNullOrEmpty(associationId)) {
            progressEvent.setErrorCode(HandlerErrorCode.InvalidRequest);
            progressEvent.setMessage("AssociationId must be present to read the existing association.");
            return progressEvent;
        }

        final DescribeAssociationRequest describeAssociationRequest =
            DescribeAssociationRequest.builder()
                .associationId(associationId)
                .build();

        try {
            final AssociationDescription association =
                proxy.injectCredentialsAndInvokeV2(describeAssociationRequest, SSM_CLIENT::describeAssociation)
                    .associationDescription();

            final ResourceModel existingModel =
                associationDescriptionTranslator.associationDescriptionToResourceModel(association);

            progressEvent.setResourceModel(existingModel);
            progressEvent.setStatus(OperationStatus.SUCCESS);

        } catch (Exception e) {
            final BaseHandlerException cfnException = exceptionTranslator
                .translateFromServiceException(e, describeAssociationRequest, requestModel);

            logger.log(cfnException.getCause().getMessage());

            throw cfnException;
        }

        return progressEvent;
    }
}
