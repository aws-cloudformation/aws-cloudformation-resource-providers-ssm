package software.amazon.ssm.maintenancewindowtarget;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DescribeMaintenanceWindowTargetsRequest;
import software.amazon.awssdk.services.ssm.model.DescribeMaintenanceWindowTargetsResponse;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.ssm.maintenancewindowtarget.translator.ExceptionTranslator;
import software.amazon.ssm.maintenancewindowtarget.translator.request.GetMaintenanceWindowTargetTranslator;
import software.amazon.ssm.maintenancewindowtarget.util.ResourceHandlerRequestToStringConverter;
import software.amazon.ssm.maintenancewindowtarget.util.ResourceModelToStringConverter;
import software.amazon.ssm.maintenancewindowtarget.util.ClientBuilder;

public class ReadHandler extends BaseHandler<CallbackContext> {

    private static final SsmClient SSM_CLIENT = ClientBuilder.getClient();

    private final GetMaintenanceWindowTargetTranslator getMaintenanceWindowTargetTranslator;
    private final ExceptionTranslator exceptionTranslator;
    private final ResourceHandlerRequestToStringConverter requestToStringConverter;

    ReadHandler() {
        this.getMaintenanceWindowTargetTranslator = new GetMaintenanceWindowTargetTranslator();
        this.exceptionTranslator = new ExceptionTranslator();
        this.requestToStringConverter = new ResourceHandlerRequestToStringConverter(new ResourceModelToStringConverter());
    }

    /**
     * Used for unit tests.
     *
     * @param getMaintenanceWindowTargetTranslator Translates ResourceModel objects into RegisterTargetWithMaintenanceWindow requests.
     * @param exceptionTranslator Used for translating service model exceptions..
     * @param requestToStringConverter ResourceHandlerRequestToStringConverter used to convert requests to Strings.
     */
    ReadHandler(final GetMaintenanceWindowTargetTranslator getMaintenanceWindowTargetTranslator,
                  final ExceptionTranslator exceptionTranslator,
                  final ResourceHandlerRequestToStringConverter requestToStringConverter) {
        this.getMaintenanceWindowTargetTranslator = getMaintenanceWindowTargetTranslator;
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

        // Ensure that both windowId and windowTargetId are required
        final String windowId = model.getWindowId();
        final String windowTargetId = model.getWindowTargetId();

        if (StringUtils.isNullOrEmpty(windowId) || StringUtils.isNullOrEmpty(windowTargetId)) {
            progressEvent.setErrorCode(HandlerErrorCode.InvalidRequest);
            progressEvent.setMessage("" +
                    "Both WindowId and WindowTargetId must be present to get an existing maintenance window target.");
            return progressEvent;
        }

        // Create a DescribeMaintenanceWindowTargetsRequest, using windowId and windowTargetId in the filter
        final DescribeMaintenanceWindowTargetsRequest describeMaintenanceWindowTargetsRequest =
                getMaintenanceWindowTargetTranslator.resourceModelToRequest(model);

        final DescribeMaintenanceWindowTargetsResponse response;

        try {
            response = proxy.injectCredentialsAndInvokeV2(describeMaintenanceWindowTargetsRequest,
                    SSM_CLIENT::describeMaintenanceWindowTargets);

            final ResourceModel readModel =
                    getMaintenanceWindowTargetTranslator.responseToResourceModel(response);

            progressEvent.setStatus(OperationStatus.SUCCESS);
            progressEvent.setResourceModel(readModel);
        } catch (Exception e) {
            final BaseHandlerException cfnException = exceptionTranslator
                    .translateFromServiceException(e, describeMaintenanceWindowTargetsRequest, request.getDesiredResourceState());

            logger.log(cfnException.getCause().getMessage());

            throw cfnException;
        }

        return progressEvent;
    }
}
