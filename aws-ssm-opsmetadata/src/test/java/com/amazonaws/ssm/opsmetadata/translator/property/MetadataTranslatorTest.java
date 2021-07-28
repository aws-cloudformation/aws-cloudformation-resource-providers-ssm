package com.amazonaws.ssm.opsmetadata.translator.property;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import software.amazon.awssdk.services.ssm.model.MetadataValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MetadataTranslatorTest {

    private MetadataTranslator metadataTranslator;
    private Map<String, MetadataValue> serviceModelMetadata;
    private Map<String, com.amazonaws.ssm.opsmetadata.MetadataValue> resourceModelMetadata;

    @BeforeEach
    void setUp() {
        metadataTranslator = new MetadataTranslator();

        serviceModelMetadata = new HashMap<String, MetadataValue>() {{
            put("some-key-1", MetadataValue.builder().value("some-value-1").build());
            put("some-key-2", MetadataValue.builder().value("some-value-2").build());
        }};
        resourceModelMetadata = new HashMap<String, com.amazonaws.ssm.opsmetadata.MetadataValue>() {{
            put("some-key-1", com.amazonaws.ssm.opsmetadata.MetadataValue.builder().value("some-value-1").build());
            put("some-key-2", com.amazonaws.ssm.opsmetadata.MetadataValue.builder().value("some-value-2").build());
        }};
    }

    @Test
    void serviceModelPropertyToResourceModel() {
        Optional<Map<String, com.amazonaws.ssm.opsmetadata.MetadataValue>> resourceModel =
                metadataTranslator.serviceModelPropertyToResourceModel(serviceModelMetadata);
        assertThat(resourceModel).isEqualTo(Optional.of(resourceModelMetadata));
    }

    @Test
    void serviceModelPropertyToResourceModelReturnsEmptyWithNullInput() {
        Optional<Map<String, com.amazonaws.ssm.opsmetadata.MetadataValue>> resourceModel =
                metadataTranslator.serviceModelPropertyToResourceModel(null);
        assertThat(resourceModel).isEqualTo(Optional.empty());
    }

    @Test
    void serviceModelPropertyToResourceModelReturnsEmptyWithEmptyMap() {
        Optional<Map<String, com.amazonaws.ssm.opsmetadata.MetadataValue>> resourceModel =
                metadataTranslator.serviceModelPropertyToResourceModel(new HashMap<>());
        assertThat(resourceModel).isEqualTo(Optional.empty());
    }

    @Test
    void resourceModelPropertyToServiceModel() {
        Optional<Map<String, MetadataValue>> serviceModel =
                metadataTranslator.resourceModelPropertyToServiceModel(resourceModelMetadata);
        assertThat(serviceModel).isEqualTo(Optional.of(serviceModelMetadata));
    }

    @Test
    void resourceModelPropertyToServiceModelReturnsEmptyWithNullInput() {
        Optional<Map<String, MetadataValue>> serviceModel =
                metadataTranslator.resourceModelPropertyToServiceModel(null);
        assertThat(serviceModel).isEqualTo(Optional.empty());
    }

    @Test
    void resourceModelPropertyToServiceModelReturnsEmptyWithEmptyMap() {
        Optional<Map<String, MetadataValue>> serviceModel =
                metadataTranslator.resourceModelPropertyToServiceModel(new HashMap<>());
        assertThat(serviceModel).isEqualTo(Optional.empty());
    }
}
