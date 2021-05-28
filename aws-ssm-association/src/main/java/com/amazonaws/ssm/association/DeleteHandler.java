package com.amazonaws.ssm.association;

import com.amazonaws.ssm.association.translator.ExceptionTranslator;
import com.amazonaws.ssm.association.util.ResourceHandlerRequestToStringConverter;
import com.amazonaws.ssm.association.util.ResourceModelToStringConverter;
import com.amazonaws.ssm.association.util.SsmClientBuilder;
import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.AssociationDoesNotExistException;
import software.amazon.awssdk.services.ssm.model.DeleteAssociationRequest;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Optional;

/**
 * Handles delete requests for a given resource.
 */
public class DeleteHandler extends BaseHandler<CallbackContext> {

    private static final SsmClient SSM_CLIENT = SsmClientBuilder.getClient();
    private final ExceptionTranslator exceptionTranslator;
    private final ResourceHandlerRequestToStringConverter requestToStringConverter;

    /**
     * Constructor to use by dependencies. Processes Delete requests.
     */
    DeleteHandler() {
        this.exceptionTranslator = new ExceptionTranslator();
        this.requestToStringConverter = new ResourceHandlerRequestToStringConverter(new ResourceModelToStringConverter());
    }

    /**
     * Used for unit tests.
     *
     * @param exceptionTranslator Used for translating service model exceptions.
     * @param requestToStringConverter ResourceHandlerRequestToStringConverter used to convert requests to Strings.
     */
    DeleteHandler(final ExceptionTranslator exceptionTranslator,
                  final ResourceHandlerRequestToStringConverter requestToStringConverter) {
        this.exceptionTranslator = exceptionTranslator;
        this.requestToStringConverter = requestToStringConverter;
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        logger.log(String.format("Processing DeleteHandler request: %s", requestToStringConverter.convert(request)));

        final ResourceModel model = request.getDesiredResourceState();
        final ProgressEvent<ResourceModel, CallbackContext> progressEvent =
            ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .build();

        progressEvent.setStatus(OperationStatus.FAILED);

        final Optional<DeleteAssociationRequest.Builder> optionalRequestBuilder = initializeRequestBuilder(model);

        if (!optionalRequestBuilder.isPresent()) {
            // Optional.empty() means the request failed delete request validation
            progressEvent.setErrorCode(HandlerErrorCode.InvalidRequest);
            progressEvent.setMessage("AssociationId, or InstanceId and Document Name must be specified to delete an association.");
            return progressEvent;
        }

        final DeleteAssociationRequest deleteAssociationRequest = optionalRequestBuilder.get().build();

        try {
            proxy.injectCredentialsAndInvokeV2(deleteAssociationRequest, SSM_CLIENT::deleteAssociation);
            progressEvent.setStatus(OperationStatus.SUCCESS);
        } catch (final Exception e) {
            final BaseHandlerException cfnException = exceptionTranslator.
                translateFromServiceException(e, deleteAssociationRequest, model);

            logger.log(cfnException.getCause().getMessage());

            throw cfnException;
        }

        if (progressEvent.isSuccess()) {
            // nullify the model if delete succeeded
            progressEvent.setResourceModel(null);
        }

        return progressEvent;
    }

    /**
     * Initializes DeleteAssociationRequest.Builder if the provided ResourceModel passes basic
     * DeleteAssociation validation. Otherwise, returns Optional.empty().
     *
     * @param model ResourceModel object to create DeleteAssociationRequest.Builder from.
     * @return Optional with DeleteAssociationRequest.Builder if the validation passed;
     * otherwise, returns Optional.empty().
     */
    private Optional<DeleteAssociationRequest.Builder> initializeRequestBuilder(final ResourceModel model) {
        if (!StringUtils.isNullOrEmpty(model.getAssociationId())) {

            return Optional.of(
                DeleteAssociationRequest.builder()
                    .associationId(model.getAssociationId()));

        } else if (!StringUtils.isNullOrEmpty(model.getInstanceId())
            && !StringUtils.isNullOrEmpty(model.getName())) {

            return Optional.of(
                DeleteAssociationRequest.builder()
                    .instanceId(model.getInstanceId())
                    .name(model.getName()));
        }

        return Optional.empty();
    }
}
