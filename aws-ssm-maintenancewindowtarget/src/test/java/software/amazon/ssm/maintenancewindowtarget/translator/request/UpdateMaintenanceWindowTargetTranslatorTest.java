package software.amazon.ssm.maintenancewindowtarget.translator.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.model.UpdateMaintenanceWindowTargetRequest;
import software.amazon.awssdk.services.ssm.model.UpdateMaintenanceWindowTargetResponse;
import software.amazon.ssm.maintenancewindowtarget.ResourceModel;
import software.amazon.ssm.maintenancewindowtarget.util.SimpleTypeValidator;

import static org.assertj.core.api.Assertions.assertThat;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.DESCRIPTION;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.MODEL_TARGETS;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.NAME;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.OWNER_INFORMATION;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.SERVICE_TARGETS;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.WINDOW_ID;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.WINDOW_TARGET_ID;

@ExtendWith(MockitoExtension.class)
public class UpdateMaintenanceWindowTargetTranslatorTest {
    @Mock
    private SimpleTypeValidator simpleTypeValidator;

    private UpdateMaintenanceWindowTargetTranslator updateMaintenanceWindowTargetTranslator;

    @BeforeEach
    void setUp() {
        // not mocking out SimpleTypeValidator because of the simplicity of its logic
        simpleTypeValidator = new SimpleTypeValidator();
        updateMaintenanceWindowTargetTranslator = new UpdateMaintenanceWindowTargetTranslator();
    }

    @Test
    void resourceModelToRequestWithRequiredFieldsTest() {
        final ResourceModel modelToTranslate = ResourceModel.builder()
                .windowId(WINDOW_ID)
                .windowTargetId(WINDOW_TARGET_ID)
                .build();

        final UpdateMaintenanceWindowTargetRequest updateMaintenanceWindowTargetRequest =
                updateMaintenanceWindowTargetTranslator.resourceModelToRequest(modelToTranslate);

        final UpdateMaintenanceWindowTargetRequest expectedRequest = UpdateMaintenanceWindowTargetRequest
                .builder()
                .windowId(WINDOW_ID)
                .windowTargetId(WINDOW_TARGET_ID)
                .replace(true)
                .build();

        assertThat(updateMaintenanceWindowTargetRequest).isEqualTo(expectedRequest);
    }

    @Test
    void resourceModelToRequestWithAllFieldsTest() {
        final ResourceModel modelToTranslate = ResourceModel.builder()
                .description(DESCRIPTION)
                .name(NAME)
                .ownerInformation(OWNER_INFORMATION)
                .replace(true)
                .targets(MODEL_TARGETS)
                .windowTargetId(WINDOW_TARGET_ID)
                .windowId(WINDOW_ID)
                .build();

        final UpdateMaintenanceWindowTargetRequest updateMaintenanceWindowTargetRequest =
                updateMaintenanceWindowTargetTranslator.resourceModelToRequest(modelToTranslate);

        final UpdateMaintenanceWindowTargetRequest expectedRequest = UpdateMaintenanceWindowTargetRequest
                .builder()
                .description(DESCRIPTION)
                .name(NAME)
                .ownerInformation(OWNER_INFORMATION)
                .replace(true)
                .targets(SERVICE_TARGETS)
                .windowTargetId(WINDOW_TARGET_ID)
                .windowId(WINDOW_ID)
                .build();

        assertThat(updateMaintenanceWindowTargetRequest).isEqualTo(expectedRequest);
    }

    @Test
    void responseToResourceModelTest() {
        final UpdateMaintenanceWindowTargetResponse responseToTranslate = UpdateMaintenanceWindowTargetResponse.builder()
                .description(DESCRIPTION)
                .name(NAME)
                .ownerInformation(OWNER_INFORMATION)
                .targets(SERVICE_TARGETS)
                .windowTargetId(WINDOW_TARGET_ID)
                .windowId(WINDOW_ID)
                .build();

        final ResourceModel resourceModel =
                updateMaintenanceWindowTargetTranslator.responseToResourceModel(responseToTranslate);

        final ResourceModel expectedModel = ResourceModel.builder()
                .description(DESCRIPTION)
                .name(NAME)
                .ownerInformation(OWNER_INFORMATION)
                .targets(MODEL_TARGETS)
                .windowTargetId(WINDOW_TARGET_ID)
                .windowId(WINDOW_ID)
                .build();

        assertThat(resourceModel).isEqualTo(expectedModel);
    }

    @Test
    void responseToResourceModelWithAllParametersTest() {
        final UpdateMaintenanceWindowTargetResponse responseToTranslate = UpdateMaintenanceWindowTargetResponse.builder()
                .description(DESCRIPTION)
                .name(NAME)
                .ownerInformation(OWNER_INFORMATION)
                .targets(SERVICE_TARGETS)
                .windowTargetId(WINDOW_TARGET_ID)
                .windowId(WINDOW_ID)
                .build();

        final ResourceModel resourceModel =
                updateMaintenanceWindowTargetTranslator.responseToResourceModel(responseToTranslate);

        final ResourceModel expectedModel = ResourceModel.builder()
                .description(DESCRIPTION)
                .name(NAME)
                .ownerInformation(OWNER_INFORMATION)
                .targets(MODEL_TARGETS)
                .windowTargetId(WINDOW_TARGET_ID)
                .windowId(WINDOW_ID)
                .build();

        assertThat(resourceModel).isEqualTo(expectedModel);
    }
}
