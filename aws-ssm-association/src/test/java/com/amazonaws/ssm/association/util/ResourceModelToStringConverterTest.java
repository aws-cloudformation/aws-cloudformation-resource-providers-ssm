package com.amazonaws.ssm.association.util;

import com.amazonaws.ssm.association.ResourceModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.amazonaws.ssm.association.TestsInputs.ASSOCIATION_ID;
import static com.amazonaws.ssm.association.TestsInputs.ASSOCIATION_NAME;
import static com.amazonaws.ssm.association.TestsInputs.AUTOMATION_TARGET_PARAMETER_NAME;
import static com.amazonaws.ssm.association.TestsInputs.COMPLIANCE_SEVERITY;
import static com.amazonaws.ssm.association.TestsInputs.DOCUMENT_NAME;
import static com.amazonaws.ssm.association.TestsInputs.DOCUMENT_VERSION;
import static com.amazonaws.ssm.association.TestsInputs.INSTANCE_ID;
import static com.amazonaws.ssm.association.TestsInputs.MAX_CONCURRENCY;
import static com.amazonaws.ssm.association.TestsInputs.MAX_ERRORS;
import static com.amazonaws.ssm.association.TestsInputs.MODEL_OUTPUT_LOCATION;
import static com.amazonaws.ssm.association.TestsInputs.MODEL_TARGETS;
import static com.amazonaws.ssm.association.TestsInputs.PARAMETERS;
import static com.amazonaws.ssm.association.TestsInputs.SCHEDULE_EXPRESSION;
import static com.amazonaws.ssm.association.TestsInputs.SYNC_COMPLIANCE;
import static com.amazonaws.ssm.association.TestsInputs.WAIT_FOR_SUCCESS_TIMEOUT_IN_SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceModelToStringConverterTest {

    private static final ResourceModel RESOURCE_MODEL = ResourceModel.builder()
        .associationId(ASSOCIATION_ID)
        .associationName(ASSOCIATION_NAME)
        .documentVersion(DOCUMENT_VERSION)
        .instanceId(INSTANCE_ID)
        .name(DOCUMENT_NAME)
        .parameters(PARAMETERS)
        .scheduleExpression(SCHEDULE_EXPRESSION)
        .targets(MODEL_TARGETS)
        .outputLocation(MODEL_OUTPUT_LOCATION)
        .automationTargetParameterName(AUTOMATION_TARGET_PARAMETER_NAME)
        .maxErrors(MAX_ERRORS)
        .maxConcurrency(MAX_CONCURRENCY)
        .complianceSeverity(COMPLIANCE_SEVERITY)
        .syncCompliance(SYNC_COMPLIANCE)
        .waitForSuccessTimeoutSeconds(WAIT_FOR_SUCCESS_TIMEOUT_IN_SECONDS)
        .build();

    private ResourceModelToStringConverter converter;

    @BeforeEach
    void setUp() {
        converter = new ResourceModelToStringConverter();
    }

    @Test
    void convertNullModelReturnsNull() {
        final String result = converter.convert(null);

        assertEquals("null", result);
    }

    @Test
    void convertModelReturnsModelParameters() {
        final String result = converter.convert(RESOURCE_MODEL);

        assertTrue(result.contains(String.format("associationId=%s", ASSOCIATION_ID)));
        assertTrue(result.contains(String.format("associationName=%s", ASSOCIATION_NAME)));
        assertTrue(result.contains(String.format("documentVersion=%s", DOCUMENT_VERSION)));
        assertTrue(result.contains(String.format("instanceId=%s", INSTANCE_ID)));
        assertTrue(result.contains(String.format("name=%s", DOCUMENT_NAME)));
        assertTrue(result.contains(String.format("scheduleExpression=%s", SCHEDULE_EXPRESSION)));
        assertTrue(result.contains(String.format("targets=%s", MODEL_TARGETS)));
        assertTrue(result.contains(String.format("outputLocation=%s", MODEL_OUTPUT_LOCATION)));
        assertTrue(result.contains(String.format("automationTargetParameterName=%s", AUTOMATION_TARGET_PARAMETER_NAME)));
        assertTrue(result.contains(String.format("maxErrors=%s", MAX_ERRORS)));
        assertTrue(result.contains(String.format("maxConcurrency=%s", MAX_CONCURRENCY)));
        assertTrue(result.contains(String.format("complianceSeverity=%s", COMPLIANCE_SEVERITY)));
        assertTrue(result.contains(String.format("syncCompliance=%s", SYNC_COMPLIANCE)));
        assertTrue(result.contains(String.format("waitForSuccessTimeoutSeconds=%s", WAIT_FOR_SUCCESS_TIMEOUT_IN_SECONDS)));
    }

    @Test
    void convertModelDoesNotContainParameters() {
        final String result = converter.convert(RESOURCE_MODEL);
        final String lowerCasedResult = result.toLowerCase();

        assertFalse(lowerCasedResult.contains("parameters"));
    }
}
