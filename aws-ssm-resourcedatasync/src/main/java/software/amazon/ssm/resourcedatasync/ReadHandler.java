package software.amazon.ssm.resourcedatasync;

import software.amazon.awssdk.services.ssm.model.ResourceDataSyncItem;
import software.amazon.awssdk.services.ssm.model.SsmException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.List;

import static software.amazon.ssm.resourcedatasync.HandlerHelper.describeResourceDataSyncItem;
import static software.amazon.ssm.resourcedatasync.ResourceModel.TYPE_NAME;


public class ReadHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        List<ResourceDataSyncItem> response;
        final ResourceModel model = request.getDesiredResourceState();

        try {
            response = describeResourceDataSyncItem(model, proxy);
        } catch (SsmException e) {
            throw new CfnGeneralServiceException(e);
        }

        if (response.isEmpty()) {
            throw new CfnNotFoundException(TYPE_NAME, model.getSyncName());
        }

        ResourceModel responseModel = Translator.createResourceModelFromResourceDataSyncItem(response.get(0));

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(responseModel)
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
