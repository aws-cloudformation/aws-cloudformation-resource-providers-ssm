package software.amazon.ssm.maintenancewindow;

import software.amazon.awssdk.services.ssm.model.CreateMaintenanceWindowRequest;
import software.amazon.awssdk.services.ssm.model.CreateMaintenanceWindowResponse;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.ssm.maintenancewindow.translator.request.CreateMaintenanceWindowTranslator;
import software.amazon.ssm.maintenancewindow.translator.ExceptionTranslator;
import software.amazon.ssm.maintenancewindow.util.ClientBuilder;

public class CreateHandler extends BaseHandler<CallbackContext> {

    private static final SsmClient SSM_CLIENT = ClientBuilder.getClient();
    private final CreateMaintenanceWindowTranslator createMaintenanceWindowTranslator;
    private final ExceptionTranslator exceptionTranslator;

    CreateHandler() {
        this.createMaintenanceWindowTranslator = new CreateMaintenanceWindowTranslator();
        this.exceptionTranslator = new ExceptionTranslator();
    }

    /**
     * Used for unit tests.
     *
     * @param createMaintenanceWindowTranslator Translates ResourceModel objects into CreateMaintenanceWindow requests.
     * @param exceptionTranslator Used for translating service model exceptions..
     */
    CreateHandler(final CreateMaintenanceWindowTranslator createMaintenanceWindowTranslator, final ExceptionTranslator exceptionTranslator) {
        this.createMaintenanceWindowTranslator = createMaintenanceWindowTranslator;
        this.exceptionTranslator = exceptionTranslator;
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        logger.log(String.format("Processing CreateHandler request %s", request));

        final ResourceModel model = request.getDesiredResourceState();

        final ProgressEvent<ResourceModel, CallbackContext> progressEvent = new ProgressEvent<>();
        progressEvent.setResourceModel(request.getPreviousResourceState());
        progressEvent.setStatus(OperationStatus.FAILED);

        final CreateMaintenanceWindowRequest createMaintenanceWindowRequest =
                createMaintenanceWindowTranslator.resourceModelToRequest(model);

        final CreateMaintenanceWindowResponse response;

        try {
            response = proxy.injectCredentialsAndInvokeV2(createMaintenanceWindowRequest, SSM_CLIENT::createMaintenanceWindow);

            model.setWindowId(response.windowId());

            progressEvent.setStatus(OperationStatus.SUCCESS);
            progressEvent.setResourceModel(model);

        } catch (final Exception e) {
            final BaseHandlerException cfnException = exceptionTranslator
                    .translateFromServiceException(e, createMaintenanceWindowRequest);

            logger.log(cfnException.getCause().getMessage());

            throw cfnException;
        }

        return progressEvent;
    }
}
