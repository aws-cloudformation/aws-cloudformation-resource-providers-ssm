package software.amazon.ssm.maintenancewindowtask.translator.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.model.DeregisterTaskFromMaintenanceWindowRequest;
import software.amazon.ssm.maintenancewindowtask.ResourceModel;

import static org.assertj.core.api.Assertions.assertThat;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.LAMBDA_TASK_TYPE;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_MAX_CONCURRENCY;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_MAX_ERRORS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_PRIORITY;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_TARGETS;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.TASK_TASK_ARN;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.WINDOW_ID;
import static software.amazon.ssm.maintenancewindowtask.TestConstants.WINDOW_TASK_ID;

@ExtendWith(MockitoExtension.class)
public class DeregisterTaskFromMaintenanceWindowTranslatorTest {

    private DeregisterTaskFromMaintenanceWindowTranslator deregisterTaskFromMaintenanceWindowTranslator;

    @BeforeEach
    void setUp() {
        deregisterTaskFromMaintenanceWindowTranslator = new DeregisterTaskFromMaintenanceWindowTranslator();
    }

    @Test
    void resourceModelToRequestTest() {
        final ResourceModel modelToTranslate = ResourceModel.builder()
                .windowTaskId(WINDOW_TASK_ID)
                .windowId(WINDOW_ID)
                .maxErrors(TASK_MAX_ERRORS)
                .maxConcurrency(TASK_MAX_CONCURRENCY)
                .priority(TASK_PRIORITY)
                .targets(TASK_TARGETS)
                .taskArn(TASK_TASK_ARN)
                .taskType(LAMBDA_TASK_TYPE)
                .build();

        final DeregisterTaskFromMaintenanceWindowRequest deregisterTaskFromMaintenanceWindowRequest =
                deregisterTaskFromMaintenanceWindowTranslator.resourceModelToRequest(modelToTranslate);

        final DeregisterTaskFromMaintenanceWindowRequest expectedRequest =
                DeregisterTaskFromMaintenanceWindowRequest.builder().windowId(WINDOW_ID).windowTaskId(WINDOW_TASK_ID).build();

        assertThat(deregisterTaskFromMaintenanceWindowRequest).isEqualTo(expectedRequest);
    }
}
