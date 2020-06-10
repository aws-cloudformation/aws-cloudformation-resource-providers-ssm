package software.amazon.ssm.maintenancewindowtarget.translator.request;


import software.amazon.awssdk.services.ssm.model.UpdateMaintenanceWindowTargetRequest;
import software.amazon.awssdk.services.ssm.model.UpdateMaintenanceWindowTargetResponse;
import software.amazon.ssm.maintenancewindowtarget.ResourceModel;
import software.amazon.ssm.maintenancewindowtarget.util.SimpleTypeValidator;
import software.amazon.ssm.maintenancewindowtarget.translator.property.TargetsListTranslator;

public class UpdateMaintenanceWindowTargetTranslator {
    private final SimpleTypeValidator simpleTypeValidator;
    private final TargetsListTranslator targetsListTranslator;

    /**
     * Constructor that initializes all required fields.
     */
    public UpdateMaintenanceWindowTargetTranslator() {
        this.simpleTypeValidator = new SimpleTypeValidator();
        this.targetsListTranslator = new TargetsListTranslator();
    }

    /**
     * Used for unit tests.
     *
     * @param simpleTypeValidator Validator for simple data types.
     */
    public UpdateMaintenanceWindowTargetTranslator(final SimpleTypeValidator simpleTypeValidator,
                                                   final TargetsListTranslator targetsListTranslator) {
        this.simpleTypeValidator = simpleTypeValidator;
        this.targetsListTranslator = targetsListTranslator;
    }

    /**
     * Generate UpdateMaintenanceWindowRequest from the CreateResource request.
     */
    public UpdateMaintenanceWindowTargetRequest resourceModelToRequest(final ResourceModel model) {
        final UpdateMaintenanceWindowTargetRequest.Builder updateMaintenanceWindowTargetRequestBuilder =
            UpdateMaintenanceWindowTargetRequest.builder()
                .windowId(model.getWindowId())
                .windowTargetId(model.getWindowTargetId());

        targetsListTranslator.resourceModelPropertyToServiceModel(model.getTargets())
            .ifPresent(updateMaintenanceWindowTargetRequestBuilder::targets);

        simpleTypeValidator.getValidatedString(model.getDescription())
            .ifPresent(updateMaintenanceWindowTargetRequestBuilder::description);

        simpleTypeValidator.getValidatedString(model.getName())
            .ifPresent(updateMaintenanceWindowTargetRequestBuilder::name);

        simpleTypeValidator.getValidatedString(model.getOwnerInformation())
            .ifPresent(updateMaintenanceWindowTargetRequestBuilder::ownerInformation);

        simpleTypeValidator.getValidatedBoolean(model.getReplace())
            .ifPresent(updateMaintenanceWindowTargetRequestBuilder::replace);

        simpleTypeValidator.getValidatedString(model.getWindowId())
            .ifPresent(updateMaintenanceWindowTargetRequestBuilder::windowId);

        simpleTypeValidator.getValidatedString(model.getWindowTargetId())
            .ifPresent(updateMaintenanceWindowTargetRequestBuilder::windowTargetId);

        return updateMaintenanceWindowTargetRequestBuilder.build();
    }

    /**
     * Generate ResourceModel from UpdateMaintenanceWindowTargetResponse.
     */
    public ResourceModel responseToResourceModel(final UpdateMaintenanceWindowTargetResponse response) {
        final ResourceModel model = new ResourceModel();

        model.setWindowId(response.windowId());
        model.setWindowTargetId(response.windowTargetId());

        simpleTypeValidator.getValidatedString(response.description())
            .ifPresent(model::setDescription);

        simpleTypeValidator.getValidatedString(response.name())
            .ifPresent(model::setName);

        simpleTypeValidator.getValidatedString(response.ownerInformation())
            .ifPresent(model::setOwnerInformation);

        targetsListTranslator.serviceModelPropertyToResourceModel(response.targets())
            .ifPresent(model::setTargets);

        simpleTypeValidator.getValidatedString(response.windowId())
            .ifPresent(model::setWindowId);

        simpleTypeValidator.getValidatedString(response.windowTargetId())
            .ifPresent(model::setWindowTargetId);

        return model;
    }
}
