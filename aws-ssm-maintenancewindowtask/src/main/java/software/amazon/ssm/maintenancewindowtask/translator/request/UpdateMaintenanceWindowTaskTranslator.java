package software.amazon.ssm.maintenancewindowtask.translator.request;

import software.amazon.awssdk.services.ssm.model.UpdateMaintenanceWindowTaskRequest;
import software.amazon.awssdk.services.ssm.model.UpdateMaintenanceWindowTaskResponse;
import software.amazon.ssm.maintenancewindowtask.ResourceModel;
import software.amazon.ssm.maintenancewindowtask.util.ResourceRequestTranslator;
import software.amazon.ssm.maintenancewindowtask.util.ResponseResourceTranslator;
import software.amazon.ssm.maintenancewindowtask.util.SimpleTypeValidator;

public class UpdateMaintenanceWindowTaskTranslator {

    private final SimpleTypeValidator simpleTypeValidator;

    /**
     * Constructor that initializes all required fields.
     */
    public UpdateMaintenanceWindowTaskTranslator() {
        this.simpleTypeValidator = new SimpleTypeValidator();
    }

    public UpdateMaintenanceWindowTaskRequest resourceModelToRequest(final ResourceModel model) {
        final UpdateMaintenanceWindowTaskRequest.Builder updateMaintenanceWindowTaskRequestBuilder =
                UpdateMaintenanceWindowTaskRequest.builder()
                        .windowId(model.getWindowId())
                        .windowTaskId(model.getWindowTaskId())
                        .replace(true);

        ResourceRequestTranslator.translateToRequestTargets(model.getTargets())
                .ifPresent(updateMaintenanceWindowTaskRequestBuilder::targets);

        simpleTypeValidator.getValidatedString(model.getTaskArn())
                .ifPresent(updateMaintenanceWindowTaskRequestBuilder::taskArn);

        simpleTypeValidator.getValidatedString(model.getServiceRoleArn())
                .ifPresent(updateMaintenanceWindowTaskRequestBuilder::serviceRoleArn);

        ResourceRequestTranslator.translateToRequestTaskParameters(model.getTaskParameters())
                .ifPresent(updateMaintenanceWindowTaskRequestBuilder::taskParameters);

        ResourceRequestTranslator.translateToRequestTaskInvocationParameters(model.getTaskInvocationParameters())
                .ifPresent(updateMaintenanceWindowTaskRequestBuilder::taskInvocationParameters);

        simpleTypeValidator.getValidatedString(model.getMaxConcurrency())
                .ifPresent(updateMaintenanceWindowTaskRequestBuilder::maxConcurrency);

        simpleTypeValidator.getValidatedString(model.getMaxErrors())
                .ifPresent(updateMaintenanceWindowTaskRequestBuilder::maxErrors);

        simpleTypeValidator.getValidatedInteger(model.getPriority())
                .ifPresent(updateMaintenanceWindowTaskRequestBuilder::priority);

        ResourceRequestTranslator.translateToRequestLoggingInfo(model.getLoggingInfo())
                .ifPresent(updateMaintenanceWindowTaskRequestBuilder::loggingInfo);

        simpleTypeValidator.getValidatedString(model.getName())
                .ifPresent(updateMaintenanceWindowTaskRequestBuilder::name);

        simpleTypeValidator.getValidatedString(model.getDescription())
                .ifPresent(updateMaintenanceWindowTaskRequestBuilder::description);

        return updateMaintenanceWindowTaskRequestBuilder.build();
    }

    public ResourceModel responseToResourceModel(final UpdateMaintenanceWindowTaskResponse response) {
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

        simpleTypeValidator.getValidatedString(response.serviceRoleArn())
                .ifPresent(model::setServiceRoleArn);

        ResponseResourceTranslator.translateToResourceModelTaskParameters(response.taskParameters())
                .ifPresent(model::setTaskParameters);

        ResponseResourceTranslator.translateToResourceModelTaskInvocationParameters(response.taskInvocationParameters())
                .ifPresent(model::setTaskInvocationParameters);

        ResponseResourceTranslator.translateToResourceModelLoggingInfo(response.loggingInfo())
                .ifPresent(model::setLoggingInfo);

        simpleTypeValidator.getValidatedString(response.name())
                .ifPresent(model::setName);

        simpleTypeValidator.getValidatedString(response.description())
                .ifPresent(model::setDescription);

        return model;
    }
}
