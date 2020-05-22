package software.amazon.ssm.maintenancewindowtask.util;

import com.amazonaws.util.StringUtils;

import java.util.Optional;

/**
 * Validates data types that are not defined in Resource or Service models.
 */
public class SimpleTypeValidator {

    /**
     * Validates an input String.
     */
    public Optional<String> getValidatedString(final String parameter) {
        if (StringUtils.isNullOrEmpty(parameter)) {
            return Optional.empty();
        } else {
            return Optional.of(parameter);
        }
    }

    /**
     * Validates an input Integer
     */
    public Optional<Integer> getValidatedInteger(final Integer parameter) {
        if (parameter == null) {
            return Optional.empty();
        } else {
            return Optional.of(parameter);
        }
    }
}
