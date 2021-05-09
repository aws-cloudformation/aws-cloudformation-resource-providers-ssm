package com.amazonaws.ssm.association.translator.property;

import com.amazonaws.ssm.association.Target;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Property translator for the list of targets property.
 */
public class TargetsListTranslator
    implements PropertyTranslator<List<software.amazon.awssdk.services.ssm.model.Target>, List<Target>> {

    /**
     * Gets a list of ResourceModel Targets from service model Targets.
     *
     * @param serviceModelTargets List of service model Targets to convert to ResourceModel Targets.
     * @return Optional with a list of ResourceModel Targets converted from the service model;
     * returns Optional.empty() when the service model list of targets is empty/null.
     */
    @Override
    public Optional<List<Target>> serviceModelPropertyToResourceModel(
        final List<software.amazon.awssdk.services.ssm.model.Target> serviceModelTargets) {

        if (CollectionUtils.isNotEmpty(serviceModelTargets)) {
            return Optional.of(
                serviceModelTargets.stream()
                    .map(serviceModelTarget -> {
                        Target resourceModelTarget = new Target();
                        resourceModelTarget.setKey(serviceModelTarget.key());
                        resourceModelTarget.setValues(serviceModelTarget.values());
                        return resourceModelTarget;
                    })
                    .collect(Collectors.toList()));
        }

        return Optional.empty();
    }

    /**
     * Gets a list of service model Targets from ResourceModel Targets.
     *
     * @param resourceModelTargets List of ResourceModel Targets to convert to service model Targets.
     * @return Optional with a list of service model Targets converted from the ResourceModel;
     * returns Optional.empty() when the ResourceModel list of targets is empty/null.
     */
    @Override
    public Optional<List<software.amazon.awssdk.services.ssm.model.Target>> resourceModelPropertyToServiceModel(
        final List<Target> resourceModelTargets) {

        if (CollectionUtils.isNotEmpty(resourceModelTargets)) {
            return Optional.of(
                resourceModelTargets.stream()
                    .map(t -> software.amazon.awssdk.services.ssm.model.Target.builder()
                        .key(t.getKey())
                        .values(t.getValues())
                        .build())
                    .collect(Collectors.toList()));
        }

        // Test Comment
        return Optional.empty();
    }
}
