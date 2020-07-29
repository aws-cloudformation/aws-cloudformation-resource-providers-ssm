package software.amazon.ssm.maintenancewindowtask.translator.request;

import software.amazon.awssdk.services.ssm.model.DeregisterTaskFromMaintenanceWindowRequest;
import software.amazon.ssm.maintenancewindowtask.ResourceModel;

public class DeregisterTaskFromMaintenanceWindowTranslator {

    /**
     * Used for unit tests.
     *
     */
    public DeregisterTaskFromMaintenanceWindowTranslator() {
    }

    /**
     * Generate DeregisterTaskFromMaintenanceWindowRequest from the DeleteResource request.
     */
    public DeregisterTaskFromMaintenanceWindowRequest resourceModelToRequest(final ResourceModel model) {
        final DeregisterTaskFromMaintenanceWindowRequest deregisterTaskFromMaintenanceWindowRequestRequest =
                DeregisterTaskFromMaintenanceWindowRequest.builder()
                        .windowId(model.getWindowId())
                        .windowTaskId(model.getWindowTaskId())
                        .build();

        return deregisterTaskFromMaintenanceWindowRequestRequest;
    }
}
