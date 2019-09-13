package com.amazonaws.ssm.association;

import com.amazonaws.ssm.association.util.SsmClientBuilder;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

/**
 * Handles all create requests for a given resource.
 */
public class CreateHandler extends BaseHandler<CallbackContext> {

    private static final int CALLBACK_DELAY_SECONDS = 15;
    private static final SsmClient SSM_CLIENT = SsmClientBuilder.getClient();

    private final BaseHandler<CallbackContext> initialCreateHandler;
    private final BaseHandler<CallbackContext> inProgressCreateHandler;

    /**
     * Empty constructor used by wrapper classes.
     */
    CreateHandler() {
        initialCreateHandler = new InitialCreateHandler(CALLBACK_DELAY_SECONDS, SSM_CLIENT);
        inProgressCreateHandler = new InProgressCreateHandler(CALLBACK_DELAY_SECONDS, SSM_CLIENT);
    }

    /**
     * Used for unit tests.
     *
     * @param initialCreateHandler Concrete implementation of BaseCreateHandler used to handle first Create requests.
     * @param inProgressCreateHandler Concrete implementation of BaseCreateHandler used to handle noninitial Create requests.
     */
    CreateHandler(final BaseHandler<CallbackContext> initialCreateHandler,
                  final BaseHandler<CallbackContext> inProgressCreateHandler) {

        this.initialCreateHandler = initialCreateHandler;
        this.inProgressCreateHandler = inProgressCreateHandler;
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        logger.log(String.format("Processing CreateHandler request %s", request));

        if (callbackContext == null) {
            return initialCreateHandler.handleRequest(proxy, request, callbackContext, logger);
        } else {
            return inProgressCreateHandler.handleRequest(proxy, request, callbackContext, logger);
        }
    }
}
