package com.amazonaws.ssm.association.translator.property;

import com.amazonaws.ssm.association.InstanceAssociationOutputLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.MODEL_OUTPUT_LOCATION;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.SERVICE_OUTPUT_LOCATION;
import static org.assertj.core.api.Assertions.assertThat;

class InstanceAssociationOutputLocationTranslatorTest {

    private InstanceAssociationOutputLocationTranslator instanceAssociationOutputLocationTranslator;

    @BeforeEach
    void setUp() {
        instanceAssociationOutputLocationTranslator = new InstanceAssociationOutputLocationTranslator();
    }

    @Test
    void serviceModelPropertyToResourceModel() {
        Optional<InstanceAssociationOutputLocation> resourceModelOutputLocation =
            instanceAssociationOutputLocationTranslator.serviceModelPropertyToResourceModel(SERVICE_OUTPUT_LOCATION);

        assertThat(resourceModelOutputLocation).isEqualTo(Optional.of(MODEL_OUTPUT_LOCATION));
    }

    @Test
    void serviceModelPropertyToResourceModelReturnsEmptyWithNullInput() {
        Optional<InstanceAssociationOutputLocation> resourceModelOutputLocation =
            instanceAssociationOutputLocationTranslator.serviceModelPropertyToResourceModel(null);

        assertThat(resourceModelOutputLocation).isEqualTo(Optional.empty());
    }

    @Test
    void serviceModelPropertyToResourceModelReturnsEmptyWithNullS3Location() {
        final software.amazon.awssdk.services.ssm.model.S3OutputLocation nullS3Location = null;
        final software.amazon.awssdk.services.ssm.model.InstanceAssociationOutputLocation outputLocationWithNullS3Location =
            software.amazon.awssdk.services.ssm.model.InstanceAssociationOutputLocation.builder().s3Location(nullS3Location).build();

        Optional<InstanceAssociationOutputLocation> resourceModelOutputLocation =
            instanceAssociationOutputLocationTranslator.serviceModelPropertyToResourceModel(outputLocationWithNullS3Location);

        assertThat(resourceModelOutputLocation).isEqualTo(Optional.empty());
    }

    @Test
    void resourceModelPropertyToServiceModel() {
        Optional<software.amazon.awssdk.services.ssm.model.InstanceAssociationOutputLocation> serviceModelOutputLocation =
            instanceAssociationOutputLocationTranslator.resourceModelPropertyToServiceModel(MODEL_OUTPUT_LOCATION);

        assertThat(serviceModelOutputLocation).isEqualTo(Optional.of(SERVICE_OUTPUT_LOCATION));
    }

    @Test
    void resourceModelPropertyToServiceModelReturnsEmptyWithNullInput() {
        Optional<software.amazon.awssdk.services.ssm.model.InstanceAssociationOutputLocation> serviceModelOutputLocation =
            instanceAssociationOutputLocationTranslator.resourceModelPropertyToServiceModel(null);

        assertThat(serviceModelOutputLocation).isEqualTo(Optional.empty());
    }

    @Test
    void resourceModelPropertyToServiceModelReturnsEmptyWithNullS3Location() {
        final InstanceAssociationOutputLocation outputLocationWithNullS3Location = new InstanceAssociationOutputLocation(null);

        Optional<software.amazon.awssdk.services.ssm.model.InstanceAssociationOutputLocation> serviceModelOutputLocation =
            instanceAssociationOutputLocationTranslator.resourceModelPropertyToServiceModel(outputLocationWithNullS3Location);

        assertThat(serviceModelOutputLocation).isEqualTo(Optional.empty());
    }
}
