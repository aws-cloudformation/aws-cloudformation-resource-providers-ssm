package software.amazon.ssm.resourcedatasync;


import software.amazon.awssdk.services.ssm.model.ResourceDataSyncConflictException;
import software.amazon.awssdk.services.ssm.model.ResourceDataSyncInvalidConfigurationException;
import software.amazon.awssdk.services.ssm.model.ResourceDataSyncNotFoundException;
import software.amazon.awssdk.services.ssm.model.UpdateResourceDataSyncRequest;
import software.amazon.awssdk.services.ssm.model.SsmException;

import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;

import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static software.amazon.ssm.resourcedatasync.ResourceModel.TYPE_NAME;


public class UpdateHandler extends BaseHandler<CallbackContext> {

    private final String SYNC_TYPE_SYNC_FROM_SOURCE = "SyncFromSource";

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final CallbackContext context = callbackContext == null ? CallbackContext.builder().build() : callbackContext;

        final UpdateResourceDataSyncRequest updateResourceDataSyncRequest = Translator.updateResourceDataSyncRequest(model);

        if (!SYNC_TYPE_SYNC_FROM_SOURCE.equals(updateResourceDataSyncRequest.syncType())) {
            // only SyncFromSource supports update operation
            context.setUpdateResourceDataSyncStarted(true);

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .status(OperationStatus.SUCCESS)
                    .build();
        }

        updateResourceDataSync(updateResourceDataSyncRequest, proxy);

        context.setUpdateResourceDataSyncStarted(true);

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }

    private void updateResourceDataSync(UpdateResourceDataSyncRequest request, AmazonWebServicesClientProxy proxy) {

        try {
            proxy.injectCredentialsAndInvokeV2(request,
                    ClientBuilder.getClient()::updateResourceDataSync);
        } catch (final ResourceDataSyncConflictException e) {
            throw new CfnResourceConflictException(TYPE_NAME, request.syncName(), e.getMessage());
        } catch (final ResourceDataSyncInvalidConfigurationException e) {
            throw new CfnInvalidRequestException(TYPE_NAME, e);
        } catch (final ResourceDataSyncNotFoundException e) {
            throw new CfnNotFoundException(e);
        } catch (final SsmException e) {
            throw new CfnGeneralServiceException(TYPE_NAME + e.getMessage());
        }
    }

}
