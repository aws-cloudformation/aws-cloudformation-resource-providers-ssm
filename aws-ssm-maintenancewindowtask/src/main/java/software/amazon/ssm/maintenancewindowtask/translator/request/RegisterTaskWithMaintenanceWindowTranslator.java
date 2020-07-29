package software.amazon.ssm.maintenancewindowtask.translator.request;

import software.amazon.awssdk.services.ssm.model.RegisterTaskWithMaintenanceWindowRequest;
import software.amazon.ssm.maintenancewindowtask.ResourceModel;
import software.amazon.ssm.maintenancewindowtask.util.ResourceRequestTranslator;
import software.amazon.ssm.maintenancewindowtask.util.SimpleTypeValidator;


public class RegisterTaskWithMaintenanceWindowTranslator {

    private final SimpleTypeValidator simpleTypeValidator;

    /**
     * Constructor that initializes all required fields.
     */
    public RegisterTaskWithMaintenanceWindowTranslator() {
        this.simpleTypeValidator = new SimpleTypeValidator();
    }

    /**
     * Used for unit tests.
     *
     * @param simpleTypeValidator       Validator for simple data types.
     */
    public RegisterTaskWithMaintenanceWindowTranslator(final SimpleTypeValidator simpleTypeValidator) {
        this.simpleTypeValidator = simpleTypeValidator;
    }

    /**
     * Generate RegisterTaskWithMaintenanceWindowRequest from the CreateResource request.
     */
    public RegisterTaskWithMaintenanceWindowRequest resourceModelToRequest(final ResourceModel model) {

        final RegisterTaskWithMaintenanceWindowRequest.Builder registerTaskWithMaintenanceWindowRequestRequestBuilder =
                RegisterTaskWithMaintenanceWindowRequest.builder()
                        .windowId(model.getWindowId())
                        .taskArn(model.getTaskArn())
                        .serviceRoleArn(model.getServiceRoleArn())
                        .taskType(model.getTaskType());

        ResourceRequestTranslator.translateToRequestTargets(model.getTargets())
                .ifPresent(registerTaskWithMaintenanceWindowRequestRequestBuilder::targets);

        simpleTypeValidator.getValidatedString(model.getDescription())
                .ifPresent(registerTaskWithMaintenanceWindowRequestRequestBuilder::description);

        simpleTypeValidator.getValidatedString(model.getMaxConcurrency())
                .ifPresent(registerTaskWithMaintenanceWindowRequestRequestBuilder::maxConcurrency);

        simpleTypeValidator.getValidatedString(model.getMaxErrors())
                .ifPresent(registerTaskWithMaintenanceWindowRequestRequestBuilder::maxErrors);

        simpleTypeValidator.getValidatedString(model.getName())
                .ifPresent(registerTaskWithMaintenanceWindowRequestRequestBuilder::name);

        simpleTypeValidator.getValidatedInteger(model.getPriority())
                .ifPresent(registerTaskWithMaintenanceWindowRequestRequestBuilder::priority);

        ResourceRequestTranslator.translateToRequestLoggingInfo(model.getLoggingInfo())
                .ifPresent(registerTaskWithMaintenanceWindowRequestRequestBuilder::loggingInfo);

        ResourceRequestTranslator.translateToRequestTaskInvocationParameters(model.getTaskInvocationParameters())
                .ifPresent(registerTaskWithMaintenanceWindowRequestRequestBuilder::taskInvocationParameters);

        ResourceRequestTranslator.translateToRequestTaskParameters(model.getTaskParameters())
                .ifPresent(registerTaskWithMaintenanceWindowRequestRequestBuilder::taskParameters);

        return registerTaskWithMaintenanceWindowRequestRequestBuilder.build();
    }
}
