package software.amazon.ssm.maintenancewindowtask;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.ssm.model.DeregisterTaskFromMaintenanceWindowRequest;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.ssm.maintenancewindowtask.translator.ExceptionTranslator;
import software.amazon.ssm.maintenancewindowtask.translator.request.DeregisterTaskFromMaintenanceWindowTranslator;
import software.amazon.ssm.maintenancewindowtask.util.ClientBuilder;
import software.amazon.ssm.maintenancewindowtask.util.ResourceHandlerRequestToStringConverter;
import software.amazon.ssm.maintenancewindowtask.util.ResourceModelToStringConverter;

public class DeleteHandler extends BaseHandler<CallbackContext> {

    private static final SsmClient SSM_CLIENT = ClientBuilder.getClient();
    private final DeregisterTaskFromMaintenanceWindowTranslator deregisterTaskFromMaintenanceWindowTranslator;
    private final ExceptionTranslator exceptionTranslator;
    private final ResourceHandlerRequestToStringConverter requestToStringConverter;

    DeleteHandler() {
        this.deregisterTaskFromMaintenanceWindowTranslator = new DeregisterTaskFromMaintenanceWindowTranslator();
        this.exceptionTranslator = new ExceptionTranslator();
        requestToStringConverter = new ResourceHandlerRequestToStringConverter(new ResourceModelToStringConverter());
    }

    /**
     * Used for unit tests.
     *
     * @param deregisterTaskFromMaintenanceWindowTranslator Translates ResourceModel objects into DeregisterTaskFromMaintenanceWindow requests.
     * @param exceptionTranslator Used for translating service model exceptions.
     * @param requestToStringConverter ResourceHandlerRequestToStringConverter used to convert requests to Strings.
     */
    DeleteHandler(final DeregisterTaskFromMaintenanceWindowTranslator deregisterTaskFromMaintenanceWindowTranslator,
                  final ExceptionTranslator exceptionTranslator,
                  final ResourceHandlerRequestToStringConverter requestToStringConverter) {
        this.deregisterTaskFromMaintenanceWindowTranslator = deregisterTaskFromMaintenanceWindowTranslator;
        this.exceptionTranslator = exceptionTranslator;
        this.requestToStringConverter = requestToStringConverter;
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        logger.log(String.format("Processing DeleteHandler request: %s", requestToStringConverter.convert(request)));

        final ResourceModel model = request.getDesiredResourceState();

        final ProgressEvent<ResourceModel, CallbackContext> progressEvent =
                ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .resourceModel(model)
                        .build();

        progressEvent.setStatus(OperationStatus.FAILED);

        if(StringUtils.isNullOrEmpty(model.getWindowId())||StringUtils.isNullOrEmpty(model.getWindowTaskId())){
            progressEvent.setErrorCode(HandlerErrorCode.InvalidRequest);
            progressEvent.setMessage("WindowId and WindowTaskId must be specified to deregister a maintenance window task.");
            return progressEvent;
        }

        final DeregisterTaskFromMaintenanceWindowRequest deregisterTaskFromMaintenanceWindowRequest =
                deregisterTaskFromMaintenanceWindowTranslator.resourceModelToRequest(model);

        try {
            proxy.injectCredentialsAndInvokeV2(deregisterTaskFromMaintenanceWindowRequest, SSM_CLIENT::deregisterTaskFromMaintenanceWindow);
            progressEvent.setStatus(OperationStatus.SUCCESS);
        } catch (Exception e) {
            final BaseHandlerException cfnException = exceptionTranslator
                    .translateFromServiceException(e, deregisterTaskFromMaintenanceWindowRequest, request.getDesiredResourceState());

            logger.log(cfnException.getCause().getMessage());

            throw cfnException;
        }

        if (progressEvent.isSuccess()) {
            // nullify the model if delete succeeded
            progressEvent.setResourceModel(null);
        }

        return progressEvent;
    }
}
