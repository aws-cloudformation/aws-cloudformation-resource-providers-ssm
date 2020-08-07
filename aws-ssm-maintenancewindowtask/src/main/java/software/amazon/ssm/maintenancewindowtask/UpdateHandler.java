package software.amazon.ssm.maintenancewindowtask;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.ssm.model.UpdateMaintenanceWindowTaskRequest;
import software.amazon.awssdk.services.ssm.model.UpdateMaintenanceWindowTaskResponse;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.ssm.maintenancewindowtask.translator.ExceptionTranslator;
import software.amazon.ssm.maintenancewindowtask.translator.request.UpdateMaintenanceWindowTaskTranslator;
import software.amazon.ssm.maintenancewindowtask.util.ClientBuilder;
import software.amazon.ssm.maintenancewindowtask.util.ResourceHandlerRequestToStringConverter;
import software.amazon.ssm.maintenancewindowtask.util.ResourceModelToStringConverter;

public class UpdateHandler extends BaseHandler<CallbackContext> {

    private static final SsmClient SSM_CLIENT = ClientBuilder.getClient();

    private final UpdateMaintenanceWindowTaskTranslator updateMaintenanceWindowTaskTranslator;
    private final ExceptionTranslator exceptionTranslator;
    private final ResourceHandlerRequestToStringConverter requestToStringConverter;

    UpdateHandler(){
        this.updateMaintenanceWindowTaskTranslator = new UpdateMaintenanceWindowTaskTranslator();
        this.exceptionTranslator = new ExceptionTranslator();
        this.requestToStringConverter = new ResourceHandlerRequestToStringConverter(new ResourceModelToStringConverter());
    }

    /**
     * Used for unit tests.
     *
     * @param updateMaintenanceWindowTaskTranslator Translator between UpdateMaintenanceWindowTask and ResourceModel objects.
     * @param exceptionTranslator Translates service model exceptions.
     */
    UpdateHandler(final UpdateMaintenanceWindowTaskTranslator updateMaintenanceWindowTaskTranslator,
                  final ExceptionTranslator exceptionTranslator,
                  final ResourceHandlerRequestToStringConverter requestToStringConverter) {
        this.updateMaintenanceWindowTaskTranslator = updateMaintenanceWindowTaskTranslator;
        this.exceptionTranslator = exceptionTranslator;
        this.requestToStringConverter = requestToStringConverter;
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        logger.log(String.format("Processing ReadHandler request: %s", requestToStringConverter.convert(request)));

        final ResourceModel model = request.getDesiredResourceState();

        final ProgressEvent<ResourceModel, CallbackContext> progressEvent = new ProgressEvent<>();
        progressEvent.setResourceModel(request.getPreviousResourceState());
        progressEvent.setStatus(OperationStatus.FAILED);

        if (StringUtils.isNullOrEmpty(model.getWindowId())||StringUtils.isNullOrEmpty(model.getWindowTaskId())){
            progressEvent.setErrorCode(HandlerErrorCode.InvalidRequest);
            progressEvent.setMessage("WindowId and WindowTaskId must be specified to update a maintenance window task.");
            return progressEvent;
        }

        final UpdateMaintenanceWindowTaskRequest updateMaintenanceWindowTaskRequest =
                updateMaintenanceWindowTaskTranslator.resourceModelToRequest(model);

        try {
            final UpdateMaintenanceWindowTaskResponse response =
                    proxy.injectCredentialsAndInvokeV2(updateMaintenanceWindowTaskRequest, SSM_CLIENT::updateMaintenanceWindowTask);

            final ResourceModel updatedModel = updateMaintenanceWindowTaskTranslator.responseToResourceModel(response);

            progressEvent.setResourceModel(updatedModel);
            progressEvent.setStatus(OperationStatus.SUCCESS);

        } catch (final Exception e) {
            final BaseHandlerException cfnException = exceptionTranslator
                    .translateFromServiceException(e, updateMaintenanceWindowTaskRequest, request.getDesiredResourceState());

            logger.log(cfnException.getCause().getMessage());

            throw cfnException;
        }

        return progressEvent;
    }
}
