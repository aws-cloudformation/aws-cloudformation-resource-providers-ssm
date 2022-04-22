package com.amazonaws.ssm.association.util;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleTypeValidatorTest {

    private final SimpleTypeValidator simpleTypeValidator = new SimpleTypeValidator();

    @Test
    void getValidatedStringNotEmpty() {
        final String inputString = "TestString";
        final Optional<String> validatedString = simpleTypeValidator.getValidatedString(inputString);

        assertThat(validatedString).isEqualTo(Optional.of(inputString));
    }

    @Test
    void getValidatedStringEmpty() {
        final Optional<String> validatedString = simpleTypeValidator.getValidatedString("");

        assertThat(validatedString).isEqualTo(Optional.empty());
    }

    @Test
    void getValidatedStringNull() {
        final Optional<String> validatedString = simpleTypeValidator.getValidatedString(null);

        assertThat(validatedString).isEqualTo(Optional.empty());
    }

    @Test
    void getValidatedIntegerNotEmpty() {
        final Integer inputInteger = 1;
        final Optional<Integer> validatedInteger = simpleTypeValidator.getValidatedInteger(inputInteger);

        assertThat(validatedInteger).isEqualTo(Optional.of(inputInteger));
    }

    @Test
    void getValidatedIntegerNull() {
        final Optional<Integer> validatedInteger = simpleTypeValidator.getValidatedInteger(null);

        assertThat(validatedInteger).isEqualTo(Optional.empty());
    }

    @Test
    void getValidatedMapNotEmpty() {
        final Map<String, Integer> inputMap = Collections.singletonMap("MapKey", 32);
        final Optional<Map<String, Integer>> validatedMap = simpleTypeValidator.getValidatedMap(inputMap);

        assertThat(validatedMap).isEqualTo(Optional.of(inputMap));
    }

    @Test
    void getValidatedMapEmpty() {
        final Optional<Map<String, Integer>> validatedMap = simpleTypeValidator.getValidatedMap(Collections.emptyMap());

        assertThat(validatedMap).isEqualTo(Optional.empty());
    }

    @Test
    void getValidatedMapNull() {
        final Optional<Map<String, Integer>> validatedMap = simpleTypeValidator.getValidatedMap(null);

        assertThat(validatedMap).isEqualTo(Optional.empty());
    }
}
