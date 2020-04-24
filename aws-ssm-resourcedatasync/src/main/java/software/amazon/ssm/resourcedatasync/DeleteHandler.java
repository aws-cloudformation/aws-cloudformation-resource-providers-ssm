package software.amazon.ssm.resourcedatasync;

import software.amazon.awssdk.services.ssm.model.DeleteResourceDataSyncResponse;
import software.amazon.awssdk.services.ssm.model.LastResourceDataSyncStatus;
import software.amazon.awssdk.services.ssm.model.ResourceDataSyncInvalidConfigurationException;
import software.amazon.awssdk.services.ssm.model.ResourceDataSyncItem;
import software.amazon.awssdk.services.ssm.model.ResourceDataSyncNotFoundException;
import software.amazon.awssdk.services.ssm.model.SsmException;

import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.List;

import static software.amazon.ssm.resourcedatasync.ResourceModel.TYPE_NAME;

public class DeleteHandler extends BaseHandler<CallbackContext> {
    /**
     * Time period after which the Handler should be called again to check the status of the request.
     */
    private static final int CALLBACK_DELAY_SECONDS = 30;

    private static final int INITIAL_CALLBACK_DELAY_SECONDS = 5;

    private static final int NUMBER_OF_RESOURCE_DATA_SYNC_DELETE_POLL_RETRIES = 2 * 60 / CALLBACK_DELAY_SECONDS;

    private static final int ZERO = 0;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        final CallbackContext context = callbackContext == null ? CallbackContext.builder().build() : callbackContext;
        final ResourceModel model = request.getDesiredResourceState();

        logger.log(String.format("Processing DeleteHandler request %s", request));

        if (!context.isDeleteResourceDataSyncStarted()) {
            deleteResourceDataSync(model, proxy);

            logger.log(String.format("%s [%s] delete request created successfully", TYPE_NAME, model.getSyncName()));

            context.setDeleteResourceDataSyncStarted(true);
            context.setStabilizationRetriesRemaining(NUMBER_OF_RESOURCE_DATA_SYNC_DELETE_POLL_RETRIES);

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .callbackContext(context)
                    .status(OperationStatus.IN_PROGRESS)
                    .callbackDelaySeconds(INITIAL_CALLBACK_DELAY_SECONDS)
                    .resourceModel(model)
                    .build();
        }

        // Stabilization is the process of waiting for a resource to be in a particular (typically "success") state.
        if (!context.isDeleteResourceDataSyncStabilized()) {
            context.setDeleteResourceDataSyncStabilized(isRDSDeleteStabilized(model, proxy, context, logger));

            if (!context.isDeleteResourceDataSyncStabilized()) {
                return ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .callbackContext(context)
                        .status(OperationStatus.IN_PROGRESS)
                        .callbackDelaySeconds(CALLBACK_DELAY_SECONDS)
                        .resourceModel(model)
                        .build();
            }
        }

        return ProgressEvent.defaultSuccessHandler(model);
    }

    /**
     * A wrapper function that is responsible to call deleteResourceDataSync API and handler corresponded exception
     *
     * @param model       ResourceModel
     * @param proxyClient proxyClient will be calling ssm::createResourceDataSync by injecting credentials
     * @return DeleteResourceDataSyncResponse
     */
    private DeleteResourceDataSyncResponse deleteResourceDataSync(ResourceModel model, AmazonWebServicesClientProxy proxy) {
        try {
            return proxy.injectCredentialsAndInvokeV2(Translator.deleteResourceDataSyncRequest(model), ClientBuilder.getClient()::deleteResourceDataSync);
        } catch (final ResourceDataSyncInvalidConfigurationException e) {
            throw new CfnInvalidRequestException(TYPE_NAME, e);
        } catch (final ResourceDataSyncNotFoundException e) {
            throw new CfnNotFoundException(e);
        } catch (final SsmException e) {
            throw new CfnGeneralServiceException(TYPE_NAME + e.getMessage());
        }
    }

    /**
     * A function that checks whether the async DeleteResourceDataSync is stabilized.
     * Stabilization is the process of waiting for a resource to be in a particular (typically "success") state.
     * In particular, we check if resource data sync is deleted successfully by calling listResourceDataSync API.
     *
     * @param model
     * @param proxy
     * @param context
     * @param logger
     * @return
     */
    private boolean isRDSDeleteStabilized(final ResourceModel model, final AmazonWebServicesClientProxy proxy, final CallbackContext context, final Logger logger) {

        final String resourceDataSyncName = model.getSyncName();

        if (context.getStabilizationRetriesRemaining() == ZERO) {
            logger.log(String.format("Maximum stabilization retries reached for %s [%s]. Resource not stabilized", TYPE_NAME, resourceDataSyncName));
            throw new CfnNotStabilizedException(TYPE_NAME, resourceDataSyncName);
        }
        context.decrementStabilizationRetriesRemaining();

        final List<ResourceDataSyncItem> resourceDataSyncItems = HandlerHelper.describeResourceDataSyncItem(model, proxy);

        if (resourceDataSyncItems.isEmpty()) {
            return true;
        }

        final LastResourceDataSyncStatus currentStatus = resourceDataSyncItems.get(0).lastStatus();
        logger.log(String.format("%s [%s] is in %s stage", TYPE_NAME, resourceDataSyncName, currentStatus));

        return false;

    }

}
