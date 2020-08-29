package software.amazon.ssm.maintenancewindow.translator.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.model.CreateMaintenanceWindowRequest;
import software.amazon.ssm.maintenancewindow.ResourceModel;
import software.amazon.ssm.maintenancewindow.translator.resourcemodel.ResourceModelPropertyTranslator;
import software.amazon.ssm.maintenancewindow.util.SimpleTypeValidator;

import static org.assertj.core.api.Assertions.assertThat;
import static software.amazon.ssm.maintenancewindow.TestConstants.ALLOWED_UNASSOCIATED_TARGETS;
import static software.amazon.ssm.maintenancewindow.TestConstants.CUTOFF;
import static software.amazon.ssm.maintenancewindow.TestConstants.DURATION;
import static software.amazon.ssm.maintenancewindow.TestConstants.RESOURCE_TAGS;
import static software.amazon.ssm.maintenancewindow.TestConstants.SCHEDULE;
import static software.amazon.ssm.maintenancewindow.TestConstants.SERVICE_MODEL_TAG_WITH_RESOURCE_TAGS;
import static software.amazon.ssm.maintenancewindow.TestConstants.SYSTEM_TAGS;
import static software.amazon.ssm.maintenancewindow.TestConstants.WINDOW_DESCRIPTION;
import static software.amazon.ssm.maintenancewindow.TestConstants.WINDOW_NAME;

@ExtendWith(MockitoExtension.class)
public class CreateMaintenanceWindowTranslatorTest {
    @Mock
    private SimpleTypeValidator simpleTypeValidator;
    @Mock
    private ResourceModelPropertyTranslator resourceModelPropertyTranslator;

    private CreateMaintenanceWindowTranslator createMaintenanceWindowTranslator;

    @BeforeEach
    void setUp() {
        simpleTypeValidator = new SimpleTypeValidator();
        resourceModelPropertyTranslator = new ResourceModelPropertyTranslator();
        createMaintenanceWindowTranslator = new CreateMaintenanceWindowTranslator(simpleTypeValidator, resourceModelPropertyTranslator);
    }

    @Test
    void resourceModelToRequestTest() {
        final ResourceModel modelToTranslate = ResourceModel.builder()
                .name(WINDOW_NAME)
                .allowUnassociatedTargets(ALLOWED_UNASSOCIATED_TARGETS)
                .schedule(SCHEDULE)
                .duration(DURATION)
                .cutoff(CUTOFF)
                .description(WINDOW_DESCRIPTION)
                .build();

        final CreateMaintenanceWindowRequest createMaintenanceWindowRequest =
                createMaintenanceWindowTranslator.resourceModelToRequest(modelToTranslate, RESOURCE_TAGS, SYSTEM_TAGS);

        final CreateMaintenanceWindowRequest expectedRequest =
                CreateMaintenanceWindowRequest.builder()
                        .name(WINDOW_NAME)
                        .allowUnassociatedTargets(ALLOWED_UNASSOCIATED_TARGETS)
                        .schedule(SCHEDULE)
                        .duration(DURATION)
                        .cutoff(CUTOFF)
                        .description(WINDOW_DESCRIPTION)
                        .tags(SERVICE_MODEL_TAG_WITH_RESOURCE_TAGS)
                        .build();

        assertThat(createMaintenanceWindowRequest).isEqualTo(expectedRequest);

    }
}
