package com.amazonaws.ssm.association.translator.property;

import com.amazonaws.ssm.association.ParameterValuesList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.MODEL_PARAMETERS;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.SERVICE_PARAMETERS;
import static org.assertj.core.api.Assertions.assertThat;

class ParametersTranslatorTest {

    private ParametersTranslator parametersTranslator;

    @BeforeEach
    void setUp() {
        parametersTranslator = new ParametersTranslator();
    }

    @Test
    void serviceModelPropertyToResourceModel() {
        final Optional<Map<String, ParameterValuesList>> resourceModelTargets =
            parametersTranslator.serviceModelPropertyToResourceModel(SERVICE_PARAMETERS);

        assertThat(resourceModelTargets).isEqualTo(Optional.of(MODEL_PARAMETERS));
    }

    @Test
    void serviceModelPropertyToResourceModelReturnsEmptyWithNullInput() {
        final Optional<Map<String, ParameterValuesList>> resourceModelTargets =
            parametersTranslator.serviceModelPropertyToResourceModel(null);

        assertThat(resourceModelTargets).isEqualTo(Optional.empty());
    }

    @Test
    void serviceModelPropertyToResourceModelReturnsEmptyWithEmptyInputMap() {
        final Optional<Map<String, ParameterValuesList>> resourceModelTargets =
            parametersTranslator.serviceModelPropertyToResourceModel(Collections.emptyMap());

        assertThat(resourceModelTargets).isEqualTo(Optional.empty());
    }

    @Test
    void resourceModelPropertyToServiceModel() {
        final Optional<Map<String, List<String>>> serviceModelTargets =
            parametersTranslator.resourceModelPropertyToServiceModel(MODEL_PARAMETERS);

        assertThat(serviceModelTargets).isEqualTo(Optional.of(SERVICE_PARAMETERS));
    }

    @Test
    void resourceModelPropertyToServiceModelReturnsEmptyWithNullInput() {
        final Optional<Map<String, List<String>>> serviceModelTargets =
            parametersTranslator.resourceModelPropertyToServiceModel(null);

        assertThat(serviceModelTargets).isEqualTo(Optional.empty());
    }

    @Test
    void resourceModelPropertyToServiceModelReturnsEmptyWithEmptyInputList() {
        final Optional<Map<String, List<String>>> serviceModelTargets =
            parametersTranslator.resourceModelPropertyToServiceModel(Collections.emptyMap());

        assertThat(serviceModelTargets).isEqualTo(Optional.empty());
    }
}