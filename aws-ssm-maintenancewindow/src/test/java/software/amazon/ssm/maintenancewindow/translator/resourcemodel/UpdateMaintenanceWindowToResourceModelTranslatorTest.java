package software.amazon.ssm.maintenancewindow.translator.resourcemodel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import software.amazon.awssdk.services.ssm.model.UpdateMaintenanceWindowResponse;
import software.amazon.ssm.maintenancewindow.ResourceModel;
import software.amazon.ssm.maintenancewindow.util.SimpleTypeValidator;

import static software.amazon.ssm.maintenancewindow.TestConstants.ALLOWED_UNASSOCIATED_TARGETS;
import static software.amazon.ssm.maintenancewindow.TestConstants.WINDOW_ID;
import static org.assertj.core.api.Assertions.assertThat;

public class UpdateMaintenanceWindowToResourceModelTranslatorTest {

    @Mock
    private SimpleTypeValidator simpleTypeValidator;

    private UpdateMaintenanceWindowToResourceModelTranslator updateMaintenanceWindowToResourceModelTranslator;

    @BeforeEach
    void setUp() {
        simpleTypeValidator = new SimpleTypeValidator();

        updateMaintenanceWindowToResourceModelTranslator = new UpdateMaintenanceWindowToResourceModelTranslator(simpleTypeValidator);
    }

    @Test
    void responseToResourceModelTest() {
        final UpdateMaintenanceWindowResponse responseToTranslate =
                UpdateMaintenanceWindowResponse.builder()
                        .windowId(WINDOW_ID)
                        .allowUnassociatedTargets(ALLOWED_UNASSOCIATED_TARGETS)
                        .build();
        final ResourceModel resourceModel =
                updateMaintenanceWindowToResourceModelTranslator.updateMaintenanceWindowResponseToResourceModel(responseToTranslate);

        final ResourceModel expectedModel = ResourceModel.builder()
                .windowId(WINDOW_ID)
                .allowUnassociatedTargets(ALLOWED_UNASSOCIATED_TARGETS)
                .build();

        assertThat(resourceModel).isEqualTo(expectedModel);
    }

}
