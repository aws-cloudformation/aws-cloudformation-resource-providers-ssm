package software.amazon.ssm.maintenancewindowtask.util;

import software.amazon.ssm.maintenancewindowtask.ResourceModel;

/**
 * Used for safe printing of {@link ResourceModel} objects.
 */
public class ResourceModelToStringConverter {

    /**
     * Converts ResourceModel objects into Strings that are safe to store. No confidential data will be returned.
     *
     * @param resourceModel Model object that needs to be converted into String.
     * @return String representation of the model object that is safe to store.
     */
    public String convert(final ResourceModel resourceModel) {
        if (resourceModel == null) {
            return "null";
        }

        return String.format("ResourceModel(" +
                        "windowTaskId=%s," +
                        "windowId=%s," +
                        "taskType=%s," +
                        "taskArn=%s," +
                        "priority=%s," +
                        "loggingInfo=%s," +
                        "targets=%s," +
                        "maxConcurrency=%s," +
                        "maxErrors=%s," +
                        "serviceRoleArn=%s," +
                        ")",
                resourceModel.getWindowTaskId(),
                resourceModel.getWindowId(),
                resourceModel.getTaskType(),
                resourceModel.getTaskArn(),
                resourceModel.getPriority(),
                resourceModel.getLoggingInfo(),
                resourceModel.getTargets(),
                resourceModel.getMaxConcurrency(),
                resourceModel.getMaxErrors(),
                resourceModel.getServiceRoleArn()
        );
    }
}
