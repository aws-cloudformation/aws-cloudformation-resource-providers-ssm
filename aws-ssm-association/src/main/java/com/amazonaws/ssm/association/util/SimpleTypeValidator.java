package com.amazonaws.ssm.association.util;

import com.amazonaws.util.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public Optional<String> getValidatedString(final String parameter) {
        if (StringUtils.isNullOrEmpty(parameter)) {
            return Optional.empty();
        } else {
            return Optional.of(parameter);
        }
    }

    /**
     * Validates an input Integer and returns non-empty Optional with the same parameter
     * if the validation is passed; otherwise, Optional.empty() is returned.
     *
     * @param parameter Integer parameter to validate.
     * @return Optional with the same value as the input parameter after validation;
     * returns Optional.empty() if the parameter is empty/null.
     */
    public Optional<Integer> getValidatedInteger(final Integer parameter) {
        if (parameter == null) {
            return Optional.empty();
        } else {
            return Optional.of(parameter);
        }
    }

    /**
     * Validates an input List of string and returns non-empty Optional with the same parameter
     * if the validation is passed; otherwise, Optional.empty() is returned.
     *
     * @param parameter List of string parameter to validate.
     * @return Optional with the same value as the input parameter after validation;
     * returns Optional.empty() if the parameter is empty/null.
     */
    public Optional<List<String>> getValidatedStringList(final List<String> parameter) {
        if (CollectionUtils.isNotEmpty(parameter)) {
            return Optional.of(parameter);
        } else {
            return Optional.empty();
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
    public <T extends Map<?, ?>> Optional<T> getValidatedMap(final T parameter) {
        if (MapUtils.isEmpty(parameter)) {
            return Optional.empty();
        } else {
            return Optional.of(parameter);
        }
    }
}
