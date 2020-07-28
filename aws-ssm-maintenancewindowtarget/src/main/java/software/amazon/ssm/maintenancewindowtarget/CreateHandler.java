package software.amazon.ssm.maintenancewindowtarget;

import software.amazon.awssdk.services.ssm.model.RegisterTargetWithMaintenanceWindowRequest;
import software.amazon.awssdk.services.ssm.model.RegisterTargetWithMaintenanceWindowResponse;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.ssm.maintenancewindowtarget.translator.request.RegisterTargetWithMaintenanceWindowTranslator;
import software.amazon.ssm.maintenancewindowtarget.translator.ExceptionTranslator;
import software.amazon.ssm.maintenancewindowtarget.util.SsmClientBuilder;
import software.amazon.ssm.maintenancewindowtarget.util.ResourceHandlerRequestToStringConverter;
import software.amazon.ssm.maintenancewindowtarget.util.ResourceModelToStringConverter;

public class CreateHandler extends BaseHandler<CallbackContext> {

    private static final SsmClient SSM_CLIENT = SsmClientBuilder.getClient();

    private final RegisterTargetWithMaintenanceWindowTranslator registerTargetWithMaintenanceWindowTranslator;
    private final ExceptionTranslator exceptionTranslator;
    private final ResourceHandlerRequestToStringConverter requestToStringConverter;

    CreateHandler() {
        this.registerTargetWithMaintenanceWindowTranslator = new RegisterTargetWithMaintenanceWindowTranslator();
        this.exceptionTranslator = new ExceptionTranslator();
        this.requestToStringConverter = new ResourceHandlerRequestToStringConverter(new ResourceModelToStringConverter());
    }

    /**
     * Used for unit tests.
     *
     * @param registerTargetWithMaintenanceWindowTranslator Translates ResourceModel objects into RegisterTargetWithMaintenanceWindow requests.
     * @param exceptionTranslator Used for translating service model exceptions..
     * @param requestToStringConverter ResourceHandlerRequestToStringConverter used to convert requests to Strings.
     */
    CreateHandler(final RegisterTargetWithMaintenanceWindowTranslator registerTargetWithMaintenanceWindowTranslator,
                  final ExceptionTranslator exceptionTranslator,
                  final ResourceHandlerRequestToStringConverter requestToStringConverter) {
        this.registerTargetWithMaintenanceWindowTranslator = registerTargetWithMaintenanceWindowTranslator;
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

        final ResourceModel model = request.getDesiredResourceState();

        final ProgressEvent<ResourceModel, CallbackContext> progressEvent = new ProgressEvent<>();
        progressEvent.setResourceModel(request.getPreviousResourceState());
        progressEvent.setStatus(OperationStatus.FAILED);

        final RegisterTargetWithMaintenanceWindowRequest registerTargetWithMaintenanceWindowRequest =
            registerTargetWithMaintenanceWindowTranslator.resourceModelToRequest(model);

        final RegisterTargetWithMaintenanceWindowResponse response;

        try {
            response = proxy.injectCredentialsAndInvokeV2(registerTargetWithMaintenanceWindowRequest,
                SSM_CLIENT::registerTargetWithMaintenanceWindow);

            model.setWindowTargetId(response.windowTargetId());

            progressEvent.setStatus(OperationStatus.SUCCESS);
            progressEvent.setResourceModel(model);
        } catch (Exception e) {
            final BaseHandlerException cfnException = exceptionTranslator
                .translateFromServiceException(e, registerTargetWithMaintenanceWindowRequest, request.getDesiredResourceState());

            logger.log(cfnException.getCause().getMessage());

            throw cfnException;
        }

        return progressEvent;
    }
}
