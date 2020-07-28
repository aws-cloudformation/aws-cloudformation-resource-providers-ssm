package software.amazon.ssm.maintenancewindowtarget;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.UpdateMaintenanceWindowTargetRequest;
import software.amazon.awssdk.services.ssm.model.UpdateMaintenanceWindowTargetResponse;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.ssm.maintenancewindowtarget.translator.ExceptionTranslator;
import software.amazon.ssm.maintenancewindowtarget.translator.request.UpdateMaintenanceWindowTargetTranslator;
import software.amazon.ssm.maintenancewindowtarget.util.SsmClientBuilder;
import software.amazon.ssm.maintenancewindowtarget.util.ResourceHandlerRequestToStringConverter;
import software.amazon.ssm.maintenancewindowtarget.util.ResourceModelToStringConverter;

public class UpdateHandler extends BaseHandler<CallbackContext> {

    private static final SsmClient SSM_CLIENT = SsmClientBuilder.getClient();

    private final UpdateMaintenanceWindowTargetTranslator updateMaintenanceWindowTargetTranslator;
    private final ExceptionTranslator exceptionTranslator;
    private final ResourceHandlerRequestToStringConverter requestToStringConverter;


    UpdateHandler() {
        this.updateMaintenanceWindowTargetTranslator = new UpdateMaintenanceWindowTargetTranslator();
        this.exceptionTranslator = new ExceptionTranslator();
        this.requestToStringConverter = new ResourceHandlerRequestToStringConverter(new ResourceModelToStringConverter());
    }

    /**
     * Used for unit tests.
     *
     * @param updateMaintenanceWindowTargetTranslator Translates UpdateMaintenanceWindowTargetResponse into ResourceModel objects.
     * @param exceptionTranslator Translates service model exceptions.
     * @param requestToStringConverter ResourceHandlerRequestToStringConverter used to convert requests to Strings.
     */
    UpdateHandler(final UpdateMaintenanceWindowTargetTranslator updateMaintenanceWindowTargetTranslator,
                  final ExceptionTranslator exceptionTranslator,
                  final ResourceHandlerRequestToStringConverter requestToStringConverter) {
        this.updateMaintenanceWindowTargetTranslator = updateMaintenanceWindowTargetTranslator;
        this.exceptionTranslator = exceptionTranslator;
        this.requestToStringConverter = requestToStringConverter;
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        logger.log(String.format("Processing UpdateHandler request: %s", requestToStringConverter.convert(request)));

        final ResourceModel model = request.getDesiredResourceState();

        final ProgressEvent<ResourceModel, CallbackContext> progressEvent = new ProgressEvent<>();
        progressEvent.setResourceModel(request.getPreviousResourceState());
        progressEvent.setStatus(OperationStatus.FAILED);

        final String windowId = model.getWindowId();
        final String windowTargetId = model.getWindowTargetId();

        if (StringUtils.isNullOrEmpty(windowId) || StringUtils.isNullOrEmpty(windowTargetId)) {
            progressEvent.setErrorCode(HandlerErrorCode.InvalidRequest);
            progressEvent.setMessage("" +
                "Both WindowId and WindowTargetId must be present to update the existing maintenance window target.");
            return progressEvent;
        }

        final UpdateMaintenanceWindowTargetRequest updateMaintenanceWindowTargetRequest =
            updateMaintenanceWindowTargetTranslator.resourceModelToRequest(model);

        try {
            final UpdateMaintenanceWindowTargetResponse response =
                proxy.injectCredentialsAndInvokeV2(updateMaintenanceWindowTargetRequest, SSM_CLIENT::updateMaintenanceWindowTarget);

            final ResourceModel updatedModel =
                updateMaintenanceWindowTargetTranslator.responseToResourceModel(response);

            progressEvent.setResourceModel(updatedModel);
            progressEvent.setStatus(OperationStatus.SUCCESS);

        } catch (Exception e) {
            final BaseHandlerException cfnException = exceptionTranslator
                .translateFromServiceException(e, updateMaintenanceWindowTargetRequest, request.getDesiredResourceState());

            logger.log(cfnException.getCause().getMessage());

            throw cfnException;
        }
        return progressEvent;
    }
}
