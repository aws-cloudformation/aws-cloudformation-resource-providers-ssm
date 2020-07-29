package software.amazon.ssm.maintenancewindow.translator.resourcemodel;

import software.amazon.awssdk.services.ssm.model.GetMaintenanceWindowResponse;
import software.amazon.ssm.maintenancewindow.ResourceModel;
import software.amazon.ssm.maintenancewindow.util.SimpleTypeValidator;

public class GetMaintenanceWindowTranslator {
    private final SimpleTypeValidator simpleTypeValidator;

    /**
     * Constructor that initializes all required fields.
     */
    public GetMaintenanceWindowTranslator() {
        this.simpleTypeValidator = new SimpleTypeValidator();
    }

    /**
     * Used for unit tests.
     *
     * @param simpleTypeValidator Validator for simple data types.
     */
    public GetMaintenanceWindowTranslator(final SimpleTypeValidator simpleTypeValidator) {
        this.simpleTypeValidator = simpleTypeValidator;
    }

    public ResourceModel getMaintenanceWindowResponseToResourceModel(final GetMaintenanceWindowResponse response) {
        final ResourceModel model = new ResourceModel();

        model.setWindowId(response.windowId());
        model.setAllowUnassociatedTargets(response.allowUnassociatedTargets());

        simpleTypeValidator.getValidatedString(response.name())
                .ifPresent(model::setName);

        simpleTypeValidator.getValidatedString(response.description())
                .ifPresent(model::setDescription);

        simpleTypeValidator.getValidatedString(response.startDate())
                .ifPresent(model::setStartDate);

        simpleTypeValidator.getValidatedString(response.endDate())
                .ifPresent(model::setEndDate);

        simpleTypeValidator.getValidatedString(response.schedule())
                .ifPresent(model::setSchedule);

        simpleTypeValidator.getValidatedString(response.scheduleTimezone())
                .ifPresent(model::setScheduleTimezone);

        simpleTypeValidator.getValidatedInteger(response.duration())
                .ifPresent(model::setDuration);

        simpleTypeValidator.getValidatedInteger(response.cutoff())
                .ifPresent(model::setCutoff);

        simpleTypeValidator.getValidatedInteger(response.scheduleOffset())
                .ifPresent(model::setScheduleOffset);

        return model;
    }
}
