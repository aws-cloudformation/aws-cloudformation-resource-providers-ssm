package software.amazon.ssm.maintenancewindow;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.UpdateMaintenanceWindowRequest;
import software.amazon.awssdk.services.ssm.model.UpdateMaintenanceWindowResponse;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.ssm.maintenancewindow.translator.ExceptionTranslator;
import software.amazon.ssm.maintenancewindow.translator.resourcemodel.UpdateMaintenanceWindowToResourceModelTranslator;
import software.amazon.ssm.maintenancewindow.translator.request.UpdateMaintenanceWindowTranslator;
import software.amazon.ssm.maintenancewindow.util.ClientBuilder;

public class UpdateHandler extends BaseHandler<CallbackContext> {

    private static final SsmClient SSM_CLIENT = ClientBuilder.getClient();

    private final UpdateMaintenanceWindowTranslator updateMaintenanceWindowTranslator;
    private final UpdateMaintenanceWindowToResourceModelTranslator updateMaintenanceWindowToResourceModelTranslator;
    private final ExceptionTranslator exceptionTranslator;

    UpdateHandler() {
        this.updateMaintenanceWindowTranslator = new UpdateMaintenanceWindowTranslator();
        this.updateMaintenanceWindowToResourceModelTranslator = new UpdateMaintenanceWindowToResourceModelTranslator();
        this.exceptionTranslator = new ExceptionTranslator();
    }

    /**
     * Used for unit tests.
     *
     * @param updateMaintenanceWindowTranslator Generate UpdateMaintenanceWindowRequest from the ResourceModel.
     * @param updateMaintenanceWindowTranslator Translates UpdateMaintenanceWindowResponse into ResourceModel objects.
     * @param exceptionTranslator Translates service model exceptions.
     */
    UpdateHandler(final UpdateMaintenanceWindowTranslator updateMaintenanceWindowTranslator,
                  final UpdateMaintenanceWindowToResourceModelTranslator updateMaintenanceWindowToResourceModelTranslator,
                  final ExceptionTranslator exceptionTranslator) {
        this.updateMaintenanceWindowTranslator = updateMaintenanceWindowTranslator;
        this.updateMaintenanceWindowToResourceModelTranslator = updateMaintenanceWindowToResourceModelTranslator;
        this.exceptionTranslator = exceptionTranslator;
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        logger.log(String.format("Processing UpdateHandler request %s", request));

        final ResourceModel model = request.getDesiredResourceState();

        final ProgressEvent<ResourceModel, CallbackContext> progressEvent = new ProgressEvent<>();
        progressEvent.setResourceModel(request.getPreviousResourceState());
        progressEvent.setStatus(OperationStatus.FAILED);

        final String windowId = model.getWindowId();

        if (StringUtils.isNullOrEmpty(windowId)) {
            progressEvent.setErrorCode(HandlerErrorCode.InvalidRequest);
            progressEvent.setMessage("WindowId must be present to update the existing maintenance window.");
            return progressEvent;
        }

        final UpdateMaintenanceWindowRequest updateMaintenanceWindowRequest =
                updateMaintenanceWindowTranslator.resourceModelToRequest(model);

        try {
            final UpdateMaintenanceWindowResponse response =
                    proxy.injectCredentialsAndInvokeV2(updateMaintenanceWindowRequest, SSM_CLIENT::updateMaintenanceWindow);

            final ResourceModel updatedModel =
                    updateMaintenanceWindowToResourceModelTranslator.updateMaintenanceWindowResponseToResourceModel(response);

            progressEvent.setResourceModel(updatedModel);
            progressEvent.setStatus(OperationStatus.SUCCESS);

        } catch (final Exception e) {
            final BaseHandlerException cfnException = exceptionTranslator
                    .translateFromServiceException(e, updateMaintenanceWindowRequest);

            logger.log(cfnException.getCause().getMessage());

            throw cfnException;
        }

        return progressEvent;
    }
}
