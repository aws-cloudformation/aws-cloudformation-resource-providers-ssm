package software.amazon.ssm.maintenancewindowtask.translator.request;

import software.amazon.awssdk.services.ssm.model.GetMaintenanceWindowTaskRequest;
import software.amazon.awssdk.services.ssm.model.GetMaintenanceWindowTaskResponse;
import software.amazon.ssm.maintenancewindowtask.ResourceModel;
import software.amazon.ssm.maintenancewindowtask.util.ResponseResourceTranslator;
import software.amazon.ssm.maintenancewindowtask.util.SimpleTypeValidator;

public class GetMaintenanceWindowTaskTranslator {

    private final SimpleTypeValidator simpleTypeValidator;

    /**
     * Constructor that initializes all required fields.
     */
    public GetMaintenanceWindowTaskTranslator() {
        this.simpleTypeValidator = new SimpleTypeValidator();
    }

    /**
     * Used for unit tests.
     *
     * @param simpleTypeValidator Validator for simple data types.
     */
    public GetMaintenanceWindowTaskTranslator(final SimpleTypeValidator simpleTypeValidator) {
        this.simpleTypeValidator = simpleTypeValidator;
    }

    public GetMaintenanceWindowTaskRequest resourceModelToRequest(final ResourceModel model) {
        final GetMaintenanceWindowTaskRequest getMaintenanceWindowTaskRequest =
                GetMaintenanceWindowTaskRequest.builder()
                        .windowId(model.getWindowId())
                        .windowTaskId(model.getWindowTaskId())
                        .build();

        return getMaintenanceWindowTaskRequest;
    }

    public ResourceModel responseToResourceModel(final GetMaintenanceWindowTaskResponse response) {
        final ResourceModel model = new ResourceModel();

        model.setWindowId(response.windowId());
        model.setWindowTaskId(response.windowTaskId());

        ResponseResourceTranslator.translateToResourceModelTargets(response.targets())
                .ifPresent(model::setTargets);

        simpleTypeValidator.getValidatedString(response.taskArn())
                .ifPresent(model::setTaskArn);

        simpleTypeValidator.getValidatedString(response.maxErrors())
                .ifPresent(model::setMaxErrors);

        simpleTypeValidator.getValidatedString(response.maxConcurrency())
                .ifPresent(model::setMaxConcurrency);

        simpleTypeValidator.getValidatedInteger(response.priority())
                .ifPresent(model::setPriority);

        simpleTypeValidator.getValidatedString(response.taskType().toString())
                .ifPresent(model::setTaskType);

        simpleTypeValidator.getValidatedString(response.description())
                .ifPresent(model::setDescription);

        simpleTypeValidator.getValidatedString(response.name())
                .ifPresent(model::setName);

        ResponseResourceTranslator.translateToResourceModelLoggingInfo(response.loggingInfo())
                .ifPresent(model::setLoggingInfo);

        ResponseResourceTranslator.translateToResourceModelTaskInvocationParameters(response.taskInvocationParameters())
                .ifPresent(model::setTaskInvocationParameters);

        ResponseResourceTranslator.translateToResourceModelTaskParameters(response.taskParameters())
                .ifPresent(model::setTaskParameters);

        simpleTypeValidator.getValidatedString(response.serviceRoleArn())
                .ifPresent(model::setServiceRoleArn);

        return model;
    }
}
