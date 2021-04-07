package com.amazonaws.ssm.opsmetadata.translator.property;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import software.amazon.awssdk.services.ssm.model.MetadataValue;

public class MetadataTranslator
        implements PropertyTranslator<Map<String, MetadataValue>, Map<String, com.amazonaws.ssm.opsmetadata.MetadataValue>> {
    @Override
    public Optional<Map<String, com.amazonaws.ssm.opsmetadata.MetadataValue>> serviceModelPropertyToResourceModel(
            final Map<String, MetadataValue> serviceModelMetadata) {
        if (serviceModelMetadata == null || serviceModelMetadata.isEmpty()) {
            return  Optional.empty();
        }
        Map<String, com.amazonaws.ssm.opsmetadata.MetadataValue> resourceModelMetadata = serviceModelMetadata.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(),
                        e -> com.amazonaws.ssm.opsmetadata.MetadataValue.builder()
                                .value(e.getValue().value())
                                .build()
                ));
        return Optional.of(resourceModelMetadata);
    }

    @Override
    public Optional<Map<String, MetadataValue>> resourceModelPropertyToServiceModel(
            final Map<String, com.amazonaws.ssm.opsmetadata.MetadataValue> resourceModelMetadata) {
        if (resourceModelMetadata == null || resourceModelMetadata.isEmpty()) {
            return  Optional.empty();
        }
        Map<String, MetadataValue> serviceModelMetadata = resourceModelMetadata.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(),
                        e -> MetadataValue.builder()
                                .value(e.getValue().getValue())
                                .build()
                ));
        return Optional.of(serviceModelMetadata);

    }
}
