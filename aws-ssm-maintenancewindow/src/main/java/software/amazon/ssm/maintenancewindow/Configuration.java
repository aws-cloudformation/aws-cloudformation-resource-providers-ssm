package software.amazon.ssm.maintenancewindow;

import java.util.Map;
import java.util.stream.Collectors;

class Configuration extends BaseConfiguration {

    public Configuration() {
        super("aws-ssm-maintenancewindow.json");
    }

    /**
     * Providers should implement this method if their resource has a 'Tags' property to define resource-level tags
     *
     * @return Map of combined resource tags and stack tags. The combined tags can be obtained from getResourceTags in resource handler request
     */
    public Map<String, String> resourceDefinedTags(final ResourceModel resourceModel) {
        if (resourceModel.getTags() == null) {
            return null;
        } else {
            return resourceModel.getTags().stream().collect(Collectors.toMap(tag -> tag.getKey(), tag -> tag.getValue()));
        }
    }
}
