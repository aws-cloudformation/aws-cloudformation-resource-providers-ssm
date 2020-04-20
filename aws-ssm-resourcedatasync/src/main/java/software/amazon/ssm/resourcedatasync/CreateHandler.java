package software.amazon.ssm.resourcedatasync;

import software.amazon.awssdk.services.ssm.model.*;
import software.amazon.cloudformation.exceptions.*;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.List;

import static software.amazon.ssm.resourcedatasync.ResourceModel.TYPE_NAME;

public class CreateHandler extends BaseHandler<CallbackContext> {
    /**
     * Time period after which the Handler should be called again to check the status of the request.
     */
    private static final int CALLBACK_DELAY_SECONDS = 10;

    private static final int NUMBER_OF_RESOURCE_DATA_SYNC_CREATE_POLL_RETRIES = 60 / CALLBACK_DELAY_SECONDS;

    private static final int ZERO = 0;

    private final String SYNC_TYPE_SYNC_FROM_SOURCE = "SyncFromSource";


    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        logger.log(String.format("Processing CreateHandler request %s", request));

        final CallbackContext context = callbackContext == null ? CallbackContext.builder().build() : callbackContext;
        final ResourceModel model = request.getDesiredResourceState();

        if (!context.isCreateResourceDataSyncStarted()) {

            final CreateResourceDataSyncRequest createResourceDataSyncRequest = Translator.createResourceDataSyncRequest(model);
            createResourceDataSync(createResourceDataSyncRequest, proxy);

            context.setCreateResourceDataSyncStarted(true);
            context.setStabilizationRetriesRemaining(NUMBER_OF_RESOURCE_DATA_SYNC_CREATE_POLL_RETRIES);

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .callbackContext(context)
                    .status(OperationStatus.IN_PROGRESS)
                    .callbackDelaySeconds(CALLBACK_DELAY_SECONDS)
                    .resourceModel(model)
                    .build();
        }

        if (!context.isCreateResourceDataSyncStabilized()) {

            context.setCreateResourceDataSyncStabilized(isRDSStabilized(model, proxy, context, logger));

            if (!context.isCreateResourceDataSyncStabilized()) {
                return ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .callbackContext(context)
                        .callbackDelaySeconds(CALLBACK_DELAY_SECONDS)
                        .status(OperationStatus.IN_PROGRESS)
                        .resourceModel(request.getDesiredResourceState())
                        .build();
            }
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }

    private CreateResourceDataSyncResponse createResourceDataSync(final CreateResourceDataSyncRequest request,
                                                                  final AmazonWebServicesClientProxy proxyClient) {
        final CreateResourceDataSyncResponse response;

        try {
            response = proxyClient.injectCredentialsAndInvokeV2(request,
                    ClientBuilder.getClient()::createResourceDataSync);
        } catch (final ResourceDataSyncCountExceededException e) {
            throw new CfnServiceLimitExceededException(TYPE_NAME, e.getMessage(), e);
        } catch (final ResourceDataSyncInvalidConfigurationException e) {
            throw new CfnInvalidRequestException(TYPE_NAME, e);
        } catch (final ResourceDataSyncAlreadyExistsException e) {
            throw new CfnAlreadyExistsException(e);
        } catch (final SsmException e) {
            throw new CfnGeneralServiceException(TYPE_NAME + e.getMessage());
        }
        return response;
    }

    private boolean isRDSStabilized(final ResourceModel model, final AmazonWebServicesClientProxy proxy, final CallbackContext context, final Logger logger) {

        final String resourceDataSyncName = model.getSyncName();

        if (context.getStabilizationRetriesRemaining() == ZERO) {
            logger.log(String.format("Maximum stabilization retries reached for %s [%s]. Resource not stabilized", TYPE_NAME, resourceDataSyncName));
            throw new CfnNotStabilizedException(TYPE_NAME, resourceDataSyncName);
        }

        context.decrementStabilizationRetriesRemaining();

        final List<ResourceDataSyncItem> resourceDataSyncItems = HandlerHelper.describeResourceDataSyncItem(model, proxy);
        if (resourceDataSyncItems.size() == 1) {
            final LastResourceDataSyncStatus currentStatus = resourceDataSyncItems.get(0).lastStatus();

            logger.log(String.format("%s [%s] is in %s stage", TYPE_NAME, resourceDataSyncName, currentStatus));

            if (currentStatus == null && resourceDataSyncItems.get(0).syncType().equals(SYNC_TYPE_SYNC_FROM_SOURCE)) {
                return true;
            }
            if (currentStatus.equals(LastResourceDataSyncStatus.SUCCESSFUL)) {
                return true;
            }
            if (currentStatus.equals(LastResourceDataSyncStatus.FAILED)) {
                throw new CfnNotStabilizedException(TYPE_NAME, resourceDataSyncName);
            }
        }
        return false;
    }

}
