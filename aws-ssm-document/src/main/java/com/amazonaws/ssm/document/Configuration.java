package com.amazonaws.ssm.document;

import java.util.Map;
import java.util.stream.Collectors;
import org.json.JSONObject;
import org.json.JSONTokener;

class Configuration extends BaseConfiguration {

    public Configuration() {
        super("aws-ssm-document.json");
    }

    /**
     * Providers should implement this method if their resource has a 'Tags' property to define resource-level tags
     */
    public Map<String, String> resourceDefinedTags(final ResourceModel resourceModel) {
        if (resourceModel.getTags() == null) {
            return null;
        } else {
            return resourceModel.getTags().stream()
                .collect(Collectors.toMap(Tag::getKey, Tag::getValue));
        }
    }
}
