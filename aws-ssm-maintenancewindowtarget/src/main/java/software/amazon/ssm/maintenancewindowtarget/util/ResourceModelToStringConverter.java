package software.amazon.ssm.maintenancewindowtarget.util;

import software.amazon.ssm.maintenancewindowtarget.ResourceModel;

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
                "clientToken=%s," +
                "filters=%s," +
                "maxResults=%s," +
                "nextToken=%s," +
                "resourceType=%s," +
                "targets=%s," +
                "windowId=%s," +
                "windowTargetId=%s" +
                ")",
            resourceModel.getClientToken(),
            resourceModel.getFilters(),
            resourceModel.getMaxResults(),
            resourceModel.getNextToken(),
            resourceModel.getResourceType(),
            resourceModel.getTargets(),
            resourceModel.getWindowId(),
            resourceModel.getWindowTargetId()
        );
    }
}
