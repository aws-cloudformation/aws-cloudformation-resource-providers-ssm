package software.amazon.ssm.maintenancewindowtarget.translator.request;

import com.google.common.collect.ImmutableList;
import software.amazon.awssdk.services.ssm.model.DescribeMaintenanceWindowTargetsRequest;
import software.amazon.awssdk.services.ssm.model.DescribeMaintenanceWindowTargetsResponse;
import software.amazon.awssdk.services.ssm.model.MaintenanceWindowFilter;
import software.amazon.awssdk.services.ssm.model.MaintenanceWindowTarget;
import software.amazon.ssm.maintenancewindowtarget.ResourceModel;
import software.amazon.ssm.maintenancewindowtarget.translator.property.TargetsListTranslator;
import software.amazon.ssm.maintenancewindowtarget.util.SimpleTypeValidator;

import java.util.Arrays;
import java.util.Optional;

public class GetMaintenanceWindowTargetTranslator {
    private final SimpleTypeValidator simpleTypeValidator;
    private final TargetsListTranslator targetsListTranslator;

    /**
     * Constructor that initializes all required fields.
     */
    public GetMaintenanceWindowTargetTranslator() {
        this.simpleTypeValidator = new SimpleTypeValidator();
        this.targetsListTranslator = new TargetsListTranslator();
    }

    /**
     * Used for unit tests.
     *
     * @param simpleTypeValidator Validator for simple data types.
     */
    public GetMaintenanceWindowTargetTranslator(final SimpleTypeValidator simpleTypeValidator,
                                                final TargetsListTranslator targetsListTranslator) {
        this.simpleTypeValidator = simpleTypeValidator;
        this.targetsListTranslator = targetsListTranslator;
    }

    /**
     * Generate DescribeMaintenanceWindowTargetsRequest from the CreateResource request.
     */
    public DescribeMaintenanceWindowTargetsRequest resourceModelToRequest(final ResourceModel model) {

        // Create a DescribeMaintenanceWindowTarget request with windowId and windowTargetId specifically

        final DescribeMaintenanceWindowTargetsRequest.Builder describeMaintenanceWindowTargetsRequestBuilder =
                DescribeMaintenanceWindowTargetsRequest.builder()
                        .windowId(model.getWindowId());

        // Add windowTargetId into the filter
        final MaintenanceWindowFilter windowTargetIdFilter = MaintenanceWindowFilter.builder()
            .key("WindowTargetId")
            .values(ImmutableList.of(model.getWindowTargetId()))
            .build();

        Optional.of(Arrays.asList(windowTargetIdFilter))
                .ifPresent(describeMaintenanceWindowTargetsRequestBuilder::filters);

        return describeMaintenanceWindowTargetsRequestBuilder.build();
    }

    /**
     * Generate ResourceModel from DescribeMaintenanceWindowTargetsResponse.
     */
    public ResourceModel responseToResourceModel(final DescribeMaintenanceWindowTargetsResponse response) {
        final ResourceModel model = new ResourceModel();

        MaintenanceWindowTarget target = response.targets().get(0);

        simpleTypeValidator.getValidatedString(target.description())
                .ifPresent(model::setDescription);

        simpleTypeValidator.getValidatedString(target.name())
                .ifPresent(model::setName);

        simpleTypeValidator.getValidatedString(target.ownerInformation())
                .ifPresent(model::setOwnerInformation);

        simpleTypeValidator.getValidatedString(target.resourceTypeAsString())
                .ifPresent(model::setResourceType);

        targetsListTranslator.serviceModelPropertyToResourceModel(target.targets())
                .ifPresent(model::setTargets);

        simpleTypeValidator.getValidatedString(target.windowId())
                .ifPresent(model::setWindowId);

        simpleTypeValidator.getValidatedString(target.windowTargetId())
                .ifPresent(model::setWindowTargetId);

        return model;
    }
}
