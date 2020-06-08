package software.amazon.ssm.maintenancewindowtarget.translator.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.model.RegisterTargetWithMaintenanceWindowRequest;
import software.amazon.ssm.maintenancewindowtarget.ResourceModel;
import software.amazon.ssm.maintenancewindowtarget.translator.property.TargetsListTranslator;
import software.amazon.ssm.maintenancewindowtarget.util.SimpleTypeValidator;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.DESCRIPTION;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.NAME;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.OWNER_INFORMATION;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.RESOURCE_TYPE;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.WINDOW_ID;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.MODEL_TARGETS;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.SERVICE_TARGETS;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class RegisterTargetWithMaintenanceWindowTranslatorTest {
    @Mock
    private SimpleTypeValidator simpleTypeValidator;
    @Mock
    private TargetsListTranslator targetsListTranslator;

    private RegisterTargetWithMaintenanceWindowTranslator registerTargetWithMaintenanceWindowTranslator;

    @BeforeEach
    void setUp() {
        // not mocking out SimpleTypeValidator because of the simplicity of its logic
        simpleTypeValidator = new SimpleTypeValidator();

        registerTargetWithMaintenanceWindowTranslator =
            new RegisterTargetWithMaintenanceWindowTranslator(simpleTypeValidator,
                targetsListTranslator);
    }

    @Test
    void resourceModelToRequestWithAllModelParametersPresent() {
        when(targetsListTranslator.resourceModelPropertyToServiceModel(MODEL_TARGETS))
            .thenReturn(Optional.of(SERVICE_TARGETS));

        final ResourceModel modelToTranslate =
            ResourceModel.builder()
                .description(DESCRIPTION)
                .name(NAME)
                .ownerInformation(OWNER_INFORMATION)
                .resourceType(RESOURCE_TYPE)
                .targets(MODEL_TARGETS)
                .windowId(WINDOW_ID)
                .build();

        final RegisterTargetWithMaintenanceWindowRequest registerTargetWithMaintenanceWindowRequest =
            registerTargetWithMaintenanceWindowTranslator.resourceModelToRequest(modelToTranslate);

        final RegisterTargetWithMaintenanceWindowRequest expectedRequest =
            RegisterTargetWithMaintenanceWindowRequest.builder()
                .description(DESCRIPTION)
                .name(NAME)
                .ownerInformation(OWNER_INFORMATION)
                .resourceType(RESOURCE_TYPE)
                .targets(SERVICE_TARGETS)
                .windowId(WINDOW_ID)
                .build();

        assertThat(registerTargetWithMaintenanceWindowRequest).isEqualTo(expectedRequest);
    }

    @Test
    void resourceModelToRequestWithSomeModelParametersMissingOrNull() {
        when(targetsListTranslator.resourceModelPropertyToServiceModel(MODEL_TARGETS))
            .thenReturn(Optional.of(SERVICE_TARGETS));

        final ResourceModel modelToTranslate =
            ResourceModel.builder()
                .description(null)
                .name(NAME)
                .resourceType(RESOURCE_TYPE)
                .targets(MODEL_TARGETS)
                .windowId(WINDOW_ID)
                .build();

        final RegisterTargetWithMaintenanceWindowRequest registerTargetWithMaintenanceWindowRequest =
            registerTargetWithMaintenanceWindowTranslator.resourceModelToRequest(modelToTranslate);

        final RegisterTargetWithMaintenanceWindowRequest expectedRequest =
            RegisterTargetWithMaintenanceWindowRequest.builder()
                .name(NAME)
                .resourceType(RESOURCE_TYPE)
                .targets(SERVICE_TARGETS)
                .windowId(WINDOW_ID)
                .build();

        assertThat(registerTargetWithMaintenanceWindowRequest).isEqualTo(expectedRequest);
    }
}
