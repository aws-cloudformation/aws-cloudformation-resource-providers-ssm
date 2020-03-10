package com.amazonaws.ssm.parameter;

import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.resource.IdentifierUtils;

public class CreateHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();

        // Set model primary ID if absent
        if(model.getId() == null) {
            model.setId(IdentifierUtils.generateResourceIdentifier(
                request.getLogicalResourceIdentifier(),
                request.getClientRequestToken()
            ));
        }

        // TODO : put your code here

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(model)
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
