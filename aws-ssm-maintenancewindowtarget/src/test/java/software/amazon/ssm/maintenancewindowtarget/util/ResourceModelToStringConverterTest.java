package software.amazon.ssm.maintenancewindowtarget.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.ssm.maintenancewindowtarget.ResourceModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.DESCRIPTION;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.MODEL_TARGETS;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.NAME;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.OWNER_INFORMATION;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.RESOURCE_TYPE;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.WINDOW_ID;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.WINDOW_TARGET_ID;

public class ResourceModelToStringConverterTest {
    private static final ResourceModel RESOURCE_MODEL = ResourceModel.builder()
            .description(DESCRIPTION)
            .name(NAME)
            .ownerInformation(OWNER_INFORMATION)
            .resourceType(RESOURCE_TYPE)
            .targets(MODEL_TARGETS)
            .windowId(WINDOW_ID)
            .windowTargetId(WINDOW_TARGET_ID)
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

        assertTrue(result.contains(String.format("description=%s", DESCRIPTION)));
        assertTrue(result.contains(String.format("name=%s", NAME)));
        assertTrue(result.contains(String.format("ownerInformation=%s", OWNER_INFORMATION)));
        assertTrue(result.contains(String.format("resourceType=%s", RESOURCE_TYPE)));
        assertTrue(result.contains(String.format("windowId=%s", WINDOW_ID)));
        assertTrue(result.contains(String.format("windowTargetId=%s", WINDOW_TARGET_ID)));

    }

    @Test
    void convertModelDoesNotContainParameters() {
        final String result = converter.convert(RESOURCE_MODEL);
        final String lowerCasedResult = result.toLowerCase();

        assertFalse(lowerCasedResult.contains("parameters"));
    }
}