package com.amazonaws.ssm.association;

import com.amazonaws.ssm.association.util.ResourceHandlerRequestToStringConverter;
import com.amazonaws.ssm.association.util.ResourceModelToStringConverter;
import com.amazonaws.ssm.association.util.SsmClientBuilder;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

/**
 * Handles update requests on a given resource.
 */
public class UpdateHandler extends BaseHandler<CallbackContext> {

    private static final SsmClient SSM_CLIENT = SsmClientBuilder.getClient();

    private final BaseHandler<CallbackContext> initialUpdateHandler;
    private final BaseHandler<CallbackContext> inProgressHandler;
    private final ResourceHandlerRequestToStringConverter requestToStringConverter;

    /**
     * Constructor to use by dependencies. Processes Update requests.
     */
    UpdateHandler() {
        this.initialUpdateHandler = new InitialUpdateHandler(SSM_CLIENT);
        this.inProgressHandler = new InProgressHandler(SSM_CLIENT);
        this.requestToStringConverter = new ResourceHandlerRequestToStringConverter(new ResourceModelToStringConverter());
    }

    /**
     * Used for unit tests.
     *
     * @param initialUpdateHandler     Concrete implementation of BaseHandler used to handle first Update requests.
     * @param inProgressHandler        Concrete implementation of BaseHandler used to handle in-progress requests.
     * @param requestToStringConverter ResourceHandlerRequestToStringConverter used to convert requests to Strings.
     */
    UpdateHandler(
        final BaseHandler<CallbackContext> initialUpdateHandler,
        final BaseHandler<CallbackContext> inProgressHandler,
        final ResourceHandlerRequestToStringConverter requestToStringConverter) {

        this.initialUpdateHandler = initialUpdateHandler;
        this.inProgressHandler = inProgressHandler;
        this.requestToStringConverter = requestToStringConverter;
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        logger.log(String.format("Processing UpdateHandler request: %s", requestToStringConverter.convert(request)));

        if (callbackContext == null) {
            return initialUpdateHandler.handleRequest(proxy, request, callbackContext, logger);
        } else {
            return inProgressHandler.handleRequest(proxy, request, callbackContext, logger);
        }
    }
}
