package software.amazon.ssm.maintenancewindow.translator.request;

import software.amazon.awssdk.services.ssm.model.UpdateMaintenanceWindowRequest;
import software.amazon.ssm.maintenancewindow.ResourceModel;
import software.amazon.ssm.maintenancewindow.util.SimpleTypeValidator;

public class UpdateMaintenanceWindowTranslator {

    private final SimpleTypeValidator simpleTypeValidator;

    /**
     * Constructor that initializes all required fields.
     */
    public UpdateMaintenanceWindowTranslator() {
        this.simpleTypeValidator = new SimpleTypeValidator();
    }

    /**
     * Used for unit tests.
     *
     * @param simpleTypeValidator Validator for simple data types.
     */
    public UpdateMaintenanceWindowTranslator(final SimpleTypeValidator simpleTypeValidator) {
        this.simpleTypeValidator = simpleTypeValidator;
    }

    /**
     * Generate UpdateMaintenanceWindowRequest from the CreateResource request.
     */
    public UpdateMaintenanceWindowRequest resourceModelToRequest(final ResourceModel model) {
        final UpdateMaintenanceWindowRequest.Builder updateMaintenanceWindowRequestBuilder =
                UpdateMaintenanceWindowRequest.builder()
                        .replace(true)
                        .windowId(model.getWindowId());

        updateMaintenanceWindowRequestBuilder.allowUnassociatedTargets(model.getAllowUnassociatedTargets());

        simpleTypeValidator.getValidatedString(model.getStartDate())
                .ifPresent(updateMaintenanceWindowRequestBuilder::startDate);

        simpleTypeValidator.getValidatedString(model.getDescription())
                .ifPresent(updateMaintenanceWindowRequestBuilder::description);

        simpleTypeValidator.getValidatedInteger(model.getCutoff())
                .ifPresent(updateMaintenanceWindowRequestBuilder::cutoff);

        simpleTypeValidator.getValidatedString(model.getSchedule())
                .ifPresent(updateMaintenanceWindowRequestBuilder::schedule);

        simpleTypeValidator.getValidatedInteger(model.getDuration())
                .ifPresent(updateMaintenanceWindowRequestBuilder::duration);

        simpleTypeValidator.getValidatedString(model.getEndDate())
                .ifPresent(updateMaintenanceWindowRequestBuilder::endDate);

        simpleTypeValidator.getValidatedString(model.getName())
                .ifPresent(updateMaintenanceWindowRequestBuilder::name);

        simpleTypeValidator.getValidatedString(model.getScheduleTimezone())
                .ifPresent(updateMaintenanceWindowRequestBuilder::scheduleTimezone);

        simpleTypeValidator.getValidatedInteger(model.getScheduleOffset())
                .ifPresent(updateMaintenanceWindowRequestBuilder::scheduleOffset);

        return updateMaintenanceWindowRequestBuilder.build();
    }
}
