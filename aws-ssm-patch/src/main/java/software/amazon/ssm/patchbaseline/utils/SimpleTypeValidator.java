package software.amazon.ssm.patchbaseline.utils;

import com.amazonaws.util.StringUtils;
import org.apache.commons.collections.MapUtils;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import software.amazon.awssdk.utils.CollectionUtils;
/**
 * Validates data types that are not defined in Resource or Service models.
 */
public class SimpleTypeValidator {
    /**
     * Validates an input String and returns non-empty Optional with the same parameter
     * if the validation is passed; otherwise, Optional.empty() is returned.
     *
     * @param parameter String parameter to validate.
     * @return Optional with the same value as the input parameter after validation;
     * returns Optional.empty() if the parameter is empty/null.
     */
    public static Optional<String> getValidatedString(final String parameter) {
        if (StringUtils.isNullOrEmpty(parameter)) {
            return Optional.empty();
        } else {
            return Optional.of(parameter);
        }
    }

    /**
     * Validates an input String and returns non-empty Optional with the same parameter
     * if the validation is passed; otherwise, Optional.empty() is returned.
     *
     * @param parameter String parameter to validate.
     * @return Optional with the same value as the input parameter after validation;
     * returns Optional.empty() if the parameter is empty/null.
     */
    public static Optional<List<String>> getValidatedList(final List<String> parameter) {
        if (CollectionUtils.isNullOrEmpty(parameter)) {
            return Optional.empty();
        } else {
            return Optional.of(parameter);
        }
    }

    /**
     * Validates an input Map and returns non-empty Optional with the same parameter
     * if the validation is passed; otherwise, Optional.empty() is returned.
     *
     * @param parameter Map parameter to validate.
     * @return Optional with the same value as the input parameter after validation;
     * returns Optional.empty() if the parameter is empty/null.
     */
    public static <T extends Map<?, ?>> Optional<T> getValidatedMap(final T parameter) {
        if (MapUtils.isEmpty(parameter)) {
            return Optional.empty();
        } else {
            return Optional.of(parameter);
        }
    }
}
