package com.amazonaws.ssm.association.translator.property;

import com.amazonaws.ssm.association.ParameterValuesList;
import org.apache.commons.collections.MapUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Property translator for the Parameters property.
 */
public class ParametersTranslator
    implements PropertyTranslator<Map<String, List<String>>, Map<String, ParameterValuesList>> {

    /**
     * Gets a map of parameter keys to ParameterValuesList of the ResourceModel.
     *
     * @param serviceModelProperty Map of Parameters to convert to ResourceModel Parameters with ParameterValuesList.
     * @return Optional with a map of String parameter keys and ResourceModel ParameterValuesList;
     * returns Optional.empty() when the service model list of targets is empty/null.
     */
    @Override
    public Optional<Map<String, ParameterValuesList>> serviceModelPropertyToResourceModel(
        final Map<String, List<String>> serviceModelProperty) {

        if (MapUtils.isNotEmpty(serviceModelProperty)) {
            return Optional.of(
                serviceModelProperty.entrySet().stream()
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new ParameterValuesList(entry.getValue()))));
        }

        return Optional.empty();
    }

    /**
     * Gets a map of parameter keys and list of values as expected by the service model.
     *
     * @param resourceModelProperty ResourceModel map of String parameter keys and ParameterValuesList.
     * @return Optional with a map of String parameter keys and list of Strings as parameter values;
     * returns Optional.empty() when the service model list of targets is empty/null.
     */
    @Override
    public Optional<Map<String, List<String>>> resourceModelPropertyToServiceModel(
        final Map<String, ParameterValuesList> resourceModelProperty) {

        if (MapUtils.isNotEmpty(resourceModelProperty)) {
            return Optional.of(resourceModelProperty.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().getParameterValues())));
        }

        return Optional.empty();
    }
}
