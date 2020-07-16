package software.amazon.ssm.maintenancewindowtarget.util;

import software.amazon.ssm.maintenancewindowtarget.ResourceModel;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

/**
 * Used for safe printing of {@link ResourceHandlerRequest<ResourceModel>} requests.
 */
public class ResourceHandlerRequestToStringConverter {

    private ResourceModelToStringConverter resourceModelToStringConverter;

    /**
     * Constructor that initializes all required fields.
     *
     * @param resourceModelToStringConverter Safe string converter of {@link ResourceModel} objects.
     */
    public ResourceHandlerRequestToStringConverter(final ResourceModelToStringConverter resourceModelToStringConverter) {
        this.resourceModelToStringConverter = resourceModelToStringConverter;
    }

    /**
     * Converts requests into Strings that are safe to store. No confidential data will be returned.
     *
     * @param request Request that needs to be converted into String.
     * @return String representation of the request that is safe to store.
     */
    public String convert(final ResourceHandlerRequest<ResourceModel> request) {
        if (request == null) {
            return "null";
        }

        return String.format("ResourceHandlerRequest(" +
                "clientRequestToken=%s," +
                "desiredResourceState=%s," +
                "previousResourceState=%s," +
                "desiredResourceTags=%s," +
                "systemTags=%s," +
                "awsAccountId=%s," +
                "awsPartition=%s," +
                "logicalResourceIdentifier=%s," +
                "nextToken=%s," +
                "region=%s" +
                ")",
            request.getClientRequestToken(),
            resourceModelToStringConverter.convert(request.getDesiredResourceState()),
            resourceModelToStringConverter.convert(request.getPreviousResourceState()),
            request.getDesiredResourceTags(),
            request.getSystemTags(),
            request.getAwsAccountId(),
            request.getAwsPartition(),
            request.getLogicalResourceIdentifier(),
            request.getNextToken(),
            request.getRegion());
    }
}