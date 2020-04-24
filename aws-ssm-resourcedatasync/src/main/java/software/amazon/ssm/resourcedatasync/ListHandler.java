package software.amazon.ssm.resourcedatasync;

import software.amazon.awssdk.services.ssm.model.ListResourceDataSyncResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.List;
import java.util.stream.Collectors;

import static software.amazon.ssm.resourcedatasync.HandlerHelper.getListResourceDataSyncResponse;


public class ListHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();

        ListResourceDataSyncResponse response = getListResourceDataSyncResponse(model, proxy);

        final List<ResourceModel> obtainedResourceModels = response.resourceDataSyncItems()
                .stream()
                .map(resourceDataSyncItem -> Translator.createResourceModelFromResourceDataSyncItem(resourceDataSyncItem))
                .collect(Collectors.toList());

        while (response.nextToken() != null) {

            response = getListResourceDataSyncResponse(model, proxy, request.getNextToken());
            obtainedResourceModels.addAll(response.resourceDataSyncItems()
                    .stream()
                    .map(resourceDataSyncItem -> Translator.createResourceModelFromResourceDataSyncItem(resourceDataSyncItem))
                    .collect(Collectors.toList()));
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(obtainedResourceModels)
                .status(OperationStatus.SUCCESS)
                .build();
    }
}