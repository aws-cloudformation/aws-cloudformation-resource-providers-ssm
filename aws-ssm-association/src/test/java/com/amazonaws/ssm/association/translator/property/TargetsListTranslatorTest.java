package com.amazonaws.ssm.association.translator.property;

import com.amazonaws.ssm.association.Target;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.MODEL_TARGETS;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.SERVICE_TARGETS;
import static org.assertj.core.api.Assertions.assertThat;

class TargetsListTranslatorTest {

    private TargetsListTranslator targetsListTranslator;

    @BeforeEach
    void setUp() {
        targetsListTranslator = new TargetsListTranslator();
    }

    @Test
    void serviceModelPropertyToResourceModel() {
        final Optional<List<Target>> resourceModelTargets =
            targetsListTranslator.serviceModelPropertyToResourceModel(SERVICE_TARGETS);

        assertThat(resourceModelTargets).isEqualTo(Optional.of(MODEL_TARGETS));
    }

    @Test
    void serviceModelPropertyToResourceModelReturnsEmptyWithNullInput() {
        final Optional<List<Target>> resourceModelTargets =
            targetsListTranslator.serviceModelPropertyToResourceModel(null);

        assertThat(resourceModelTargets).isEqualTo(Optional.empty());
    }

    @Test
    void serviceModelPropertyToResourceModelReturnsEmptyWithEmptyInputList() {
        final Optional<List<Target>> resourceModelTargets =
            targetsListTranslator.serviceModelPropertyToResourceModel(Collections.emptyList());

        assertThat(resourceModelTargets).isEqualTo(Optional.empty());
    }

    @Test
    void resourceModelPropertyToServiceModel() {
        final Optional<List<software.amazon.awssdk.services.ssm.model.Target>> serviceModelTargets =
            targetsListTranslator.resourceModelPropertyToServiceModel(MODEL_TARGETS);

        assertThat(serviceModelTargets).isEqualTo(Optional.of(SERVICE_TARGETS));
    }

    @Test
    void resourceModelPropertyToServiceModelReturnsEmptyWithNullInput() {
        final Optional<List<software.amazon.awssdk.services.ssm.model.Target>> serviceModelTargets =
            targetsListTranslator.resourceModelPropertyToServiceModel(null);

        assertThat(serviceModelTargets).isEqualTo(Optional.empty());
    }

    @Test
    void resourceModelPropertyToServiceModelReturnsEmptyWithEmptyInputList() {
        final Optional<List<software.amazon.awssdk.services.ssm.model.Target>> serviceModelTargets =
            targetsListTranslator.resourceModelPropertyToServiceModel(Collections.emptyList());

        assertThat(serviceModelTargets).isEqualTo(Optional.empty());
    }
}
