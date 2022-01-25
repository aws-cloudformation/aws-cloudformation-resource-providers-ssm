package com.amazonaws.ssm.parameter;

import java.util.Map;
import java.util.stream.Collectors;

class Configuration extends BaseConfiguration {

	public Configuration() {
		super("aws-ssm-parameter.json");
	}

	/**
	 * Providers should implement this method if their resource has a 'Tags' property to define resource-level tags
	 *
	 * @return
	 */
	public Map<String, String> resourceDefinedTags(final ResourceModel resourceModel) {
		if (resourceModel.getTags() == null) {
			return null;
		} else {
			return resourceModel.getTags().entrySet().stream()
				.collect(Collectors.toMap(tag -> tag.getKey(), tag -> tag.getValue().toString()));
		}
	}
}
