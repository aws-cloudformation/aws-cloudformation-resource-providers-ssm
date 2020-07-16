package software.amazon.ssm.maintenancewindowtarget.util;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleTypeValidatorTest {

    private final SimpleTypeValidator simpleTypeValidator = new SimpleTypeValidator();

    @Test
    void getValidatedIntegerNotNull() {
        final int inputInteger= 3;
        final Optional<Integer> validatedInteger = simpleTypeValidator.getValidatedInteger(inputInteger);

        assertThat(validatedInteger).isEqualTo(Optional.of(inputInteger));
    }

    @Test
    void getValidatedIntegerEmpty() {
        final Optional<Integer> validatedInteger = simpleTypeValidator.getValidatedInteger(null);

        assertThat(validatedInteger).isEqualTo(Optional.empty());
    }

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
    void getValidatedBooleanNotNull() {
        final boolean inputBoolean = true;
        final Optional<Boolean> validatedBoolean = simpleTypeValidator.getValidatedBoolean(inputBoolean);

        assertThat(validatedBoolean).isEqualTo(Optional.of(inputBoolean));
    }

    @Test
    void getValidatedBooleanEmpty() {
        final Optional<Boolean> validatedBoolean = simpleTypeValidator.getValidatedBoolean(null);

        assertThat(validatedBoolean).isEqualTo(Optional.empty());
    }
}
