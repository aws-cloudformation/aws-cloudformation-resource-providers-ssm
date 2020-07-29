package software.amazon.ssm.maintenancewindow.translator.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.model.UpdateMaintenanceWindowRequest;
import software.amazon.ssm.maintenancewindow.ResourceModel;
import software.amazon.ssm.maintenancewindow.translator.resourcemodel.ResourceModelPropertyTranslator;
import software.amazon.ssm.maintenancewindow.util.SimpleTypeValidator;

import static software.amazon.ssm.maintenancewindow.TestConstants.ALLOWED_UNASSOCIATED_TARGETS;
import static software.amazon.ssm.maintenancewindow.TestConstants.CUTOFF;
import static software.amazon.ssm.maintenancewindow.TestConstants.DURATION;
import static software.amazon.ssm.maintenancewindow.TestConstants.SCHEDULE;
import static software.amazon.ssm.maintenancewindow.TestConstants.WINDOW_ID;
import static software.amazon.ssm.maintenancewindow.TestConstants.WINDOW_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class UpdateMaintenanceWindowTranslatorTest {
    @Mock
    private SimpleTypeValidator simpleTypeValidator;
    @Mock
    private ResourceModelPropertyTranslator resourceModelPropertyTranslator;

    private UpdateMaintenanceWindowTranslator updateMaintenanceWindowTranslator;

    @BeforeEach
    void setUp() {
        simpleTypeValidator = new SimpleTypeValidator();
        resourceModelPropertyTranslator = new ResourceModelPropertyTranslator();
        updateMaintenanceWindowTranslator = new UpdateMaintenanceWindowTranslator(simpleTypeValidator);
    }

    @Test
    void resourceModelToRequestTest() {
        final ResourceModel modelToTranslate = ResourceModel.builder()
                .windowId(WINDOW_ID)
                .name(WINDOW_NAME)
                .allowUnassociatedTargets(ALLOWED_UNASSOCIATED_TARGETS)
                .schedule(SCHEDULE)
                .duration(DURATION)
                .cutoff(CUTOFF)
                .build();

        final UpdateMaintenanceWindowRequest updateMaintenanceWindowRequest =
                updateMaintenanceWindowTranslator.resourceModelToRequest(modelToTranslate);

        final UpdateMaintenanceWindowRequest expectedRequest =
                UpdateMaintenanceWindowRequest.builder()
                        .windowId(WINDOW_ID)
                        .name(WINDOW_NAME)
                        .allowUnassociatedTargets(ALLOWED_UNASSOCIATED_TARGETS)
                        .schedule(SCHEDULE)
                        .duration(DURATION)
                        .cutoff(CUTOFF)
                        .replace(true)
                        .build();
        assertThat(updateMaintenanceWindowRequest).isEqualTo(expectedRequest);

    }
}
