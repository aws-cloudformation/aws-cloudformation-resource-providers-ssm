package software.amazon.ssm.maintenancewindowtask.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.ssm.maintenancewindowtask.ResourceModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.LAMBDA_TASK_TYPE;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.LOGGING_INFO;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.RESOURCE_TASK_TASK_PARAMETERS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_MAX_CONCURRENCY;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_MAX_ERRORS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_PRIORITY;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_SERVICE_ROLE_ARN;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_TARGETS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.LAMBDA_TASK_ARN;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.WINDOW_ID;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.WINDOW_TASK_ID;

public class ResourceModelToStringConverterTest {
    private static final ResourceModel RESOURCE_MODEL = ResourceModel.builder()
            .windowTaskId(WINDOW_TASK_ID)
            .windowId(WINDOW_ID)
            .taskType(LAMBDA_TASK_TYPE)
            .taskArn(LAMBDA_TASK_ARN)
            .serviceRoleArn(TASK_SERVICE_ROLE_ARN)
            .targets(TASK_TARGETS)
            .taskParameters(RESOURCE_TASK_TASK_PARAMETERS)
            .priority(TASK_PRIORITY)
            .maxConcurrency(TASK_MAX_CONCURRENCY)
            .maxErrors(TASK_MAX_ERRORS)
            .loggingInfo(LOGGING_INFO)
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

        assertTrue(result.contains(String.format("windowTaskId=%s", WINDOW_TASK_ID)));
        assertTrue(result.contains(String.format("windowId=%s", WINDOW_ID)));
        assertTrue(result.contains(String.format("taskType=%s", LAMBDA_TASK_TYPE)));
        assertTrue(result.contains(String.format("taskArn=%s", LAMBDA_TASK_ARN)));
        assertTrue(result.contains(String.format("priority=%s", TASK_PRIORITY)));
        assertTrue(result.contains(String.format("loggingInfo=%s", LOGGING_INFO)));
        assertTrue(result.contains(String.format("targets=%s", TASK_TARGETS)));
        assertTrue(result.contains(String.format("maxConcurrency=%s", TASK_MAX_CONCURRENCY)));
        assertTrue(result.contains(String.format("maxErrors=%s", TASK_MAX_ERRORS)));
        assertTrue(result.contains(String.format("serviceRoleArn=%s", TASK_SERVICE_ROLE_ARN)));
    }

    @Test
    void convertModelDoesNotContainParameters() {
        final String result = converter.convert(RESOURCE_MODEL);
        final String lowerCasedResult = result.toLowerCase();

        assertFalse(lowerCasedResult.contains("parameters"));
    }
}
