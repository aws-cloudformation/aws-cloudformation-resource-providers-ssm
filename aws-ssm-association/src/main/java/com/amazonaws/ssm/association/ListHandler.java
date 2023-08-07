package com.amazonaws.ssm.association;

import com.amazonaws.ssm.association.translator.ExceptionTranslator;
import com.amazonaws.ssm.association.util.ResourceHandlerRequestToStringConverter;
import com.amazonaws.ssm.association.util.ResourceModelToStringConverter;
import com.amazonaws.ssm.association.util.SsmClientBuilder;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.ListAssociationsRequest;
import software.amazon.awssdk.services.ssm.model.ListAssociationsResponse;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class ListHandler extends BaseHandler<CallbackContext> {

    private static final SsmClient SSM_CLIENT = SsmClientBuilder.getClient();

    private final ExceptionTranslator exceptionTranslator;
    private final ResourceHandlerRequestToStringConverter requestToStringConverter;

    private static final int MaxResults = 50;

    /**
     * Constructor to use by dependencies. Processes Read requests.
     */
    ListHandler() {
        this.exceptionTranslator = new ExceptionTranslator();
        this.requestToStringConverter = new ResourceHandlerRequestToStringConverter(new ResourceModelToStringConverter());
    }

    /**
     * Used for unit tests.
     *
     * @param exceptionTranslator Translates service model exceptions.
     * @param requestToStringConverter ResourceHandlerRequestToStringConverter used to convert requests to Strings.
     */
    ListHandler(final ExceptionTranslator exceptionTranslator,
                final ResourceHandlerRequestToStringConverter requestToStringConverter) {
        this.exceptionTranslator = exceptionTranslator;
        this.requestToStringConverter = requestToStringConverter;
    }

    private ListAssociationsRequest generateListAssociationsRequest(
        @Nullable final String nextToken,
        @Nullable final ResourceModel model) {

        return ListAssociationsRequest.builder()
            .maxResults(MaxResults)
            .nextToken(nextToken)
            .build();
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        logger.log(String.format("Processing ListHandler request: %s", requestToStringConverter.convert(request)));

        final ResourceModel requestModel = request.getDesiredResourceState();

        final ProgressEvent<ResourceModel, CallbackContext> progressEvent = new ProgressEvent<>();
        progressEvent.setStatus(OperationStatus.FAILED);

        final ListAssociationsRequest listAssociationRequest =
            generateListAssociationsRequest(request.getNextToken(), requestModel);

        try {
            final ListAssociationsResponse listAssociationsResponse =
                proxy.injectCredentialsAndInvokeV2(listAssociationRequest, SSM_CLIENT::listAssociations);

            final List<ResourceModel> existingModels = listAssociationsResponse
                .associations()
                .stream().map(association -> ResourceModel.builder().associationId(association.associationId()).build()).collect(Collectors.toList());

            progressEvent.setResourceModels(existingModels);
            progressEvent.setStatus(OperationStatus.SUCCESS);
            progressEvent.setNextToken(listAssociationsResponse.nextToken());

        } catch (Exception e) {
            final BaseHandlerException cfnException = exceptionTranslator
                .translateFromServiceException(e, listAssociationRequest, requestModel);

            logger.log(cfnException.getCause().getMessage());

            throw cfnException;
        }

        return progressEvent;
    }
}
