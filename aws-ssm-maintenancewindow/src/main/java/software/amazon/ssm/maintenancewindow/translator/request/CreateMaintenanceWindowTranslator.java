package software.amazon.ssm.maintenancewindow.translator.request;

import software.amazon.awssdk.services.ssm.model.CreateMaintenanceWindowRequest;
import software.amazon.ssm.maintenancewindow.ResourceModel;
import software.amazon.ssm.maintenancewindow.util.SimpleTypeValidator;

public class CreateMaintenanceWindowTranslator {

    private final SimpleTypeValidator simpleTypeValidator;

    /**
     * Constructor that initializes all required fields.
     */
    public CreateMaintenanceWindowTranslator() {
        this.simpleTypeValidator = new SimpleTypeValidator();
    }

    /**
     * Used for unit tests.
     *
     * @param simpleTypeValidator Validator for simple data types.
     */
    public CreateMaintenanceWindowTranslator(final SimpleTypeValidator simpleTypeValidator) {
        this.simpleTypeValidator = simpleTypeValidator;
    }

    /**
     * Generate CreateMaintenanceWindowRequest from the CreateResource request.
     */
    public CreateMaintenanceWindowRequest resourceModelToRequest(final ResourceModel model) {

        final CreateMaintenanceWindowRequest.Builder createMaintenanceWindowRequestBuilder =
                CreateMaintenanceWindowRequest.builder()
                        .name(model.getName())
                        .allowUnassociatedTargets(model.getAllowUnassociatedTargets())
                        .cutoff(model.getCutoff())
                        .schedule(model.getSchedule())
                        .duration(model.getDuration());

        simpleTypeValidator.getValidatedString(model.getDescription())
                .ifPresent(createMaintenanceWindowRequestBuilder::description);

        simpleTypeValidator.getValidatedString(model.getScheduleTimezone())
                .ifPresent(createMaintenanceWindowRequestBuilder::scheduleTimezone);

        simpleTypeValidator.getValidatedString(model.getStartDate())
                .ifPresent(createMaintenanceWindowRequestBuilder::startDate);

        simpleTypeValidator.getValidatedString(model.getEndDate())
                .ifPresent(createMaintenanceWindowRequestBuilder::endDate);

        simpleTypeValidator.translateToRequestTags(model.getTags())
                .ifPresent(createMaintenanceWindowRequestBuilder::tags);

        return createMaintenanceWindowRequestBuilder.build();
    }
}
