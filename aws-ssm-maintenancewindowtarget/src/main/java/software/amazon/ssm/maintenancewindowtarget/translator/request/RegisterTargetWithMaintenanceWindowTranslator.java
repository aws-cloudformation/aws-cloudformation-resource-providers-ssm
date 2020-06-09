package software.amazon.ssm.maintenancewindowtarget.translator.request;

import software.amazon.awssdk.services.ssm.model.RegisterTargetWithMaintenanceWindowRequest;
import software.amazon.ssm.maintenancewindowtarget.ResourceModel;
import software.amazon.ssm.maintenancewindowtarget.translator.property.TargetsListTranslator;
import software.amazon.ssm.maintenancewindowtarget.util.SimpleTypeValidator;

public class RegisterTargetWithMaintenanceWindowTranslator {

    private final SimpleTypeValidator simpleTypeValidator;
    private final TargetsListTranslator targetsListTranslator;

    /**
     * Constructor that initializes all required fields.
     */
    public RegisterTargetWithMaintenanceWindowTranslator() {
        this.simpleTypeValidator = new SimpleTypeValidator();
        this.targetsListTranslator = new TargetsListTranslator();
    }

    /**
     * Used for unit tests.
     *
     * @param simpleTypeValidator Validator for simple data types.
     */
    public RegisterTargetWithMaintenanceWindowTranslator(final SimpleTypeValidator simpleTypeValidator,
                                                         final TargetsListTranslator targetsListTranslator) {
        this.simpleTypeValidator = simpleTypeValidator;
        this.targetsListTranslator = targetsListTranslator;
    }

    /**
     * Generate RegisterTargetWithMaintenanceWindowRequest from the CreateResource request.
     */
    public RegisterTargetWithMaintenanceWindowRequest resourceModelToRequest(final ResourceModel model) {

        final RegisterTargetWithMaintenanceWindowRequest.Builder registerTargetWithMaintenanceWindowRequestBuilder =
            RegisterTargetWithMaintenanceWindowRequest.builder()
                .resourceType(model.getResourceType())
                .windowId(model.getWindowId());

        targetsListTranslator.resourceModelPropertyToServiceModel((model.getTargets()))
            .ifPresent(registerTargetWithMaintenanceWindowRequestBuilder::targets);

        simpleTypeValidator.getValidatedString(model.getDescription())
            .ifPresent(registerTargetWithMaintenanceWindowRequestBuilder::description);

        simpleTypeValidator.getValidatedString(model.getName())
            .ifPresent(registerTargetWithMaintenanceWindowRequestBuilder::name);

        simpleTypeValidator.getValidatedString(model.getOwnerInformation())
            .ifPresent(registerTargetWithMaintenanceWindowRequestBuilder::ownerInformation);

        return registerTargetWithMaintenanceWindowRequestBuilder.build();
    }
}
