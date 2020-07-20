package software.amazon.ssm.maintenancewindowtarget;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DeregisterTargetFromMaintenanceWindowRequest;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.ssm.maintenancewindowtarget.translator.ExceptionTranslator;
import software.amazon.ssm.maintenancewindowtarget.util.SsmClientBuilder;
import software.amazon.ssm.maintenancewindowtarget.util.ResourceHandlerRequestToStringConverter;
import software.amazon.ssm.maintenancewindowtarget.util.ResourceModelToStringConverter;

import java.util.Optional;

public class DeleteHandler extends BaseHandler<CallbackContext> {

    private static final SsmClient SSM_CLIENT = SsmClientBuilder.getClient();

    private final ExceptionTranslator exceptionTranslator;
    private final ResourceHandlerRequestToStringConverter requestToStringConverter;

    DeleteHandler() {
        this.exceptionTranslator = new ExceptionTranslator();
        this.requestToStringConverter = new ResourceHandlerRequestToStringConverter(new ResourceModelToStringConverter());
    }

    /**
     * Used for unit tests.
     *
     * @param exceptionTranslator Used for translating service model exceptions.
     * @param requestToStringConverter ResourceHandlerRequestToStringConverter used to convert requests to Strings.
     */
    DeleteHandler(final ExceptionTranslator exceptionTranslator,
                  final ResourceHandlerRequestToStringConverter requestToStringConverter) {
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

        final Optional<DeregisterTargetFromMaintenanceWindowRequest.Builder> optionalRequestBuilder = initializeRequestBuilder(model);

        if (!optionalRequestBuilder.isPresent()) {
            progressEvent.setErrorCode(HandlerErrorCode.InvalidRequest);
            progressEvent.setMessage(
                "Both WindowId and WindowTargetId must be specified to delete a maintenance window target.");
            return progressEvent;
        }

        final DeregisterTargetFromMaintenanceWindowRequest deregisterTargetFromMaintenanceWindowRequest = optionalRequestBuilder.get().build();

        try {
            proxy.injectCredentialsAndInvokeV2(deregisterTargetFromMaintenanceWindowRequest, SSM_CLIENT::deregisterTargetFromMaintenanceWindow);
            progressEvent.setStatus(OperationStatus.SUCCESS);
        } catch (Exception e) {
            final BaseHandlerException cfnException = exceptionTranslator
                .translateFromServiceException(e, deregisterTargetFromMaintenanceWindowRequest, request.getDesiredResourceState());

            logger.log(cfnException.getCause().getMessage());

            throw cfnException;
        }

        if (progressEvent.isSuccess()) {
            // nullify the model if delete succeeded
            progressEvent.setResourceModel(null);
        }
        return progressEvent;
    }

    public Optional<DeregisterTargetFromMaintenanceWindowRequest.Builder> initializeRequestBuilder(final ResourceModel model) {
        if (!StringUtils.isNullOrEmpty(model.getWindowId()) && !StringUtils.isNullOrEmpty(model.getWindowId())) {

            return Optional.of(
                DeregisterTargetFromMaintenanceWindowRequest.builder()
                    .windowId(model.getWindowId())
                    .windowTargetId(model.getWindowTargetId()));
        }
        return Optional.empty();
    }
}
