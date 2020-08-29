package software.amazon.ssm.maintenancewindowtarget.translator.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.model.DescribeMaintenanceWindowTargetsRequest;
import software.amazon.awssdk.services.ssm.model.DescribeMaintenanceWindowTargetsResponse;
import software.amazon.ssm.maintenancewindowtarget.ResourceModel;
import software.amazon.ssm.maintenancewindowtarget.translator.property.TargetsListTranslator;
import software.amazon.ssm.maintenancewindowtarget.util.SimpleTypeValidator;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.DESCRIPTION;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.MODEL_TARGETS;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.NAME;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.NEXT_TOKEN;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.OWNER_INFORMATION;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.RESOURCE_TYPE;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.SERVICE_FILTERS;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.SERVICE_MAINTENANCE_WINDOW_TARGETS;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.SERVICE_TARGETS;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.WINDOW_ID;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.WINDOW_TARGET_ID;

@ExtendWith(MockitoExtension.class)
public class GetMaintenanceWindowTargetTranslatorTest {
    @Mock
    private SimpleTypeValidator simpleTypeValidator;
    @Mock
    private TargetsListTranslator targetsListTranslator;


    private GetMaintenanceWindowTargetTranslator getMaintenanceWindowTargetTranslator;

    @BeforeEach
    void setUp() {
        // not mocking out SimpleTypeValidator because of the simplicity of its logic
        simpleTypeValidator = new SimpleTypeValidator();

        getMaintenanceWindowTargetTranslator =
                new GetMaintenanceWindowTargetTranslator(simpleTypeValidator,
                        targetsListTranslator);
    }

    @Test
    void resourceModelToRequestWithRequiredParametersPresent() {
        final ResourceModel modelToTranslate =
                ResourceModel.builder()
                        .windowId(WINDOW_ID)
                        .windowTargetId(WINDOW_TARGET_ID)
                        .build();

        final DescribeMaintenanceWindowTargetsRequest describeMaintenanceWindowTargetsRequest =
                getMaintenanceWindowTargetTranslator.resourceModelToRequest(modelToTranslate);

        final DescribeMaintenanceWindowTargetsRequest expectedRequest =
                DescribeMaintenanceWindowTargetsRequest.builder()
                        .windowId(WINDOW_ID)
                        .filters(SERVICE_FILTERS)
                        .build();

        assertThat(describeMaintenanceWindowTargetsRequest).isEqualTo(expectedRequest);
    }

    @Test
    void responseToResourceModelWithRequiredParametersPresent() {
        final DescribeMaintenanceWindowTargetsResponse responseToTranslate =
                DescribeMaintenanceWindowTargetsResponse.builder()
                        .nextToken(NEXT_TOKEN)
                        .targets(SERVICE_MAINTENANCE_WINDOW_TARGETS)
                        .build();

        when(targetsListTranslator.serviceModelPropertyToResourceModel(SERVICE_TARGETS))
                .thenReturn(Optional.of(MODEL_TARGETS));

        final ResourceModel resourceModel =
                getMaintenanceWindowTargetTranslator.responseToResourceModel(responseToTranslate);

        final ResourceModel expectedModel = ResourceModel.builder()
                .description(DESCRIPTION)
                .windowId(WINDOW_ID)
                .name(NAME)
                .ownerInformation(OWNER_INFORMATION)
                .resourceType(RESOURCE_TYPE)
                .targets(MODEL_TARGETS)
                .windowId(WINDOW_ID)
                .windowTargetId(WINDOW_TARGET_ID)
                .build();

        assertThat(resourceModel).isEqualTo(expectedModel);
    }
}
