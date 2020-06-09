package software.amazon.ssm.maintenancewindowtarget.translator.property;

import java.util.Optional;

/**
 * Translates between service model properties and resource model properties.
 *
 * @param <ServiceT> Service model property type to translate.
 * @param <ResourceT> Resource model property type to translate.
 */
public interface PropertyTranslator<ServiceT, ResourceT> {
    /**
     * Translates from a service model property to the equivalent resource model property.
     *
     * @param serviceModelProperty Service model property to translate from.
     * @return Resource model property translated from the service model property.
     */
    Optional<ResourceT> serviceModelPropertyToResourceModel(final ServiceT serviceModelProperty);

    /**
     * Translates from a resource model property to the equivalent service model property.
     *
     * @param resourceModelProperty Resource model property to translate from.
     * @return Service model property translated from the resource model property.
     */
    Optional<ServiceT> resourceModelPropertyToServiceModel(final ResourceT resourceModelProperty);
}
