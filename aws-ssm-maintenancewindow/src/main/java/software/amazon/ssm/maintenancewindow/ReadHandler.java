package software.amazon.ssm.maintenancewindow;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetMaintenanceWindowRequest;
import software.amazon.awssdk.services.ssm.model.GetMaintenanceWindowResponse;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.ssm.maintenancewindow.translator.ExceptionTranslator;
import software.amazon.ssm.maintenancewindow.translator.resourcemodel.GetMaintenanceWindowTranslator;
import software.amazon.ssm.maintenancewindow.util.ClientBuilder;

public class ReadHandler extends BaseHandler<CallbackContext> {

    private static final SsmClient SSM_CLIENT = ClientBuilder.getClient();

    private final GetMaintenanceWindowTranslator getMaintenanceWindowTranslator;
    private final ExceptionTranslator exceptionTranslator;

    ReadHandler() {
        this.getMaintenanceWindowTranslator = new GetMaintenanceWindowTranslator();
        this.exceptionTranslator = new ExceptionTranslator();
    }

    /**
     * Used for unit tests.
     *
     * @param getMaintenanceWindowTranslator Translates GetMaintenanceWindow into ResourceModel objects.
     * @param exceptionTranslator Translates service model exceptions.
     */
    ReadHandler(final GetMaintenanceWindowTranslator getMaintenanceWindowTranslator, final ExceptionTranslator exceptionTranslator) {
        this.getMaintenanceWindowTranslator = getMaintenanceWindowTranslator;
        this.exceptionTranslator = exceptionTranslator;
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        logger.log(String.format("Processing ReadHandler request %s", request));

        final ResourceModel model = request.getDesiredResourceState();

        final ProgressEvent<ResourceModel, CallbackContext> progressEvent = new ProgressEvent<>();
        progressEvent.setStatus(OperationStatus.FAILED);

        final String windowId = model.getWindowId();

        if (StringUtils.isNullOrEmpty(windowId)) {
            progressEvent.setErrorCode(HandlerErrorCode.InvalidRequest);
            progressEvent.setMessage("WindowId must be present to read the existing maintenance window.");
            return progressEvent;
        }

        final GetMaintenanceWindowRequest getMaintenanceWindowRequest =
                GetMaintenanceWindowRequest.builder()
                        .windowId(windowId)
                        .build();

        try {

            final GetMaintenanceWindowResponse response =
                    proxy.injectCredentialsAndInvokeV2(getMaintenanceWindowRequest, SSM_CLIENT::getMaintenanceWindow);

            final ResourceModel resourcemodel =
                    getMaintenanceWindowTranslator.getMaintenanceWindowResponseToResourceModel(response);

            progressEvent.setResourceModel(resourcemodel);
            progressEvent.setStatus(OperationStatus.SUCCESS);

        } catch (final Exception e) {
            final BaseHandlerException cfnException = exceptionTranslator
                    .translateFromServiceException(e, getMaintenanceWindowRequest);

            logger.log(cfnException.getCause().getMessage());

            throw cfnException;
        }

        return progressEvent;
    }
}
