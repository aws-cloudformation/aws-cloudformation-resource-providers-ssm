package software.amazon.ssm.maintenancewindowtarget.util;

import org.junit.jupiter.api.Test;

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
}
