package software.amazon.ssm.maintenancewindow.util;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;


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
