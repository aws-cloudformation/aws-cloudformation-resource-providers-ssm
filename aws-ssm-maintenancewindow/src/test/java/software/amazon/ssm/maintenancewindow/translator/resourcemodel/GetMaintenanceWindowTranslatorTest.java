package software.amazon.ssm.maintenancewindow.translator.resourcemodel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.model.GetMaintenanceWindowResponse;
import software.amazon.ssm.maintenancewindow.ResourceModel;
import software.amazon.ssm.maintenancewindow.util.SimpleTypeValidator;

import static software.amazon.ssm.maintenancewindow.TestConstants.ALLOWED_UNASSOCIATED_TARGETS;
import static software.amazon.ssm.maintenancewindow.TestConstants.WINDOW_ID;
import static org.assertj.core.api.Assertions.assertThat;

public class GetMaintenanceWindowTranslatorTest {

    @Mock
    private SimpleTypeValidator simpleTypeValidator;

    private GetMaintenanceWindowTranslator getMaintenanceWindowTranslator;

    @BeforeEach
    void setUp() {
        simpleTypeValidator = new SimpleTypeValidator();

        getMaintenanceWindowTranslator = new GetMaintenanceWindowTranslator(simpleTypeValidator);
    }

    @Test
    void responseToResourceModelTest() {
        final GetMaintenanceWindowResponse responseToTranslate =
                GetMaintenanceWindowResponse.builder()
                        .windowId(WINDOW_ID)
                        .allowUnassociatedTargets(ALLOWED_UNASSOCIATED_TARGETS)
                        .build();
        final ResourceModel resourceModel =
                getMaintenanceWindowTranslator.getMaintenanceWindowResponseToResourceModel(responseToTranslate);

        final ResourceModel expectedModel = ResourceModel.builder()
                .windowId(WINDOW_ID)
                .allowUnassociatedTargets(ALLOWED_UNASSOCIATED_TARGETS)
                .build();

        assertThat(resourceModel).isEqualTo(expectedModel);
    }

}
