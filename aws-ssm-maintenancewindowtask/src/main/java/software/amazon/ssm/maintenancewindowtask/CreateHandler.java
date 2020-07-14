package software.amazon.ssm.maintenancewindowtask;

import software.amazon.awssdk.services.ssm.model.RegisterTaskWithMaintenanceWindowRequest;
import software.amazon.awssdk.services.ssm.model.RegisterTaskWithMaintenanceWindowResponse;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.ssm.maintenancewindowtask.translator.ExceptionTranslator;
import software.amazon.ssm.maintenancewindowtask.translator.request.RegisterTaskWithMaintenanceWindowTranslator;
import software.amazon.ssm.maintenancewindowtask.util.ClientBuilder;
import software.amazon.ssm.maintenancewindowtask.util.ResourceHandlerRequestToStringConverter;
import software.amazon.ssm.maintenancewindowtask.util.ResourceModelToStringConverter;


public class CreateHandler extends BaseHandler<CallbackContext> {

    private static final SsmClient SSM_CLIENT = ClientBuilder.getClient();
    private final RegisterTaskWithMaintenanceWindowTranslator registerTaskWithMaintenanceWindowTranslator;
    private final ExceptionTranslator exceptionTranslator;
    private final ResourceHandlerRequestToStringConverter requestToStringConverter;

    CreateHandler() {
        this.registerTaskWithMaintenanceWindowTranslator = new RegisterTaskWithMaintenanceWindowTranslator();
        this.exceptionTranslator = new ExceptionTranslator();
        requestToStringConverter = new ResourceHandlerRequestToStringConverter(new ResourceModelToStringConverter());
    }

    /**
     * Used for unit tests.
     *
     * @param registerTaskWithMaintenanceWindowTranslator Translates ResourceModel objects into RegisterTaskWithMaintenanceWindow requests.
     * @param exceptionTranslator Used for translating service model exceptions.
     * @param requestToStringConverter ResourceHandlerRequestToStringConverter used to convert requests to Strings.
     */
    CreateHandler(final RegisterTaskWithMaintenanceWindowTranslator registerTaskWithMaintenanceWindowTranslator,
                  final ExceptionTranslator exceptionTranslator,
                  final ResourceHandlerRequestToStringConverter requestToStringConverter) {
        this.registerTaskWithMaintenanceWindowTranslator = registerTaskWithMaintenanceWindowTranslator;
        this.exceptionTranslator = exceptionTranslator;
        this.requestToStringConverter = requestToStringConverter;
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        logger.log(String.format("Processing CreateHandler request: %s", requestToStringConverter.convert(request)));

        final ResourceModel desiredModel = request.getDesiredResourceState();

        final ResourceModel model = request.getDesiredResourceState();

        final ProgressEvent<ResourceModel, CallbackContext> progressEvent = new ProgressEvent<>();
        progressEvent.setResourceModel(request.getPreviousResourceState());
        progressEvent.setStatus(OperationStatus.FAILED);

        final RegisterTaskWithMaintenanceWindowRequest registerTaskWithMaintenanceWindowTaskRequest =
                registerTaskWithMaintenanceWindowTranslator.resourceModelToRequest(model);

        final RegisterTaskWithMaintenanceWindowResponse response;

        try {
            response = proxy.injectCredentialsAndInvokeV2(registerTaskWithMaintenanceWindowTaskRequest, SSM_CLIENT::registerTaskWithMaintenanceWindow);

            model.setWindowTaskId(response.windowTaskId());

            progressEvent.setStatus(OperationStatus.SUCCESS);
            progressEvent.setResourceModel(model);
            return progressEvent;

        } catch (Exception e) {
            final BaseHandlerException cfnException = exceptionTranslator
                    .translateFromServiceException(e, registerTaskWithMaintenanceWindowTaskRequest, desiredModel);

            logger.log(cfnException.getCause().getMessage());

            throw cfnException;
        }
    }
}
