package software.amazon.ssm.maintenancewindowtarget.translator.property;

import org.apache.commons.collections.CollectionUtils;
import software.amazon.ssm.maintenancewindowtarget.MaintenanceWindowFilter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FiltersListTranslator
        implements PropertyTranslator<List<software.amazon.awssdk.services.ssm.model.MaintenanceWindowFilter>, List<MaintenanceWindowFilter>>{
    @Override
    public Optional<List<MaintenanceWindowFilter>> serviceModelPropertyToResourceModel(
            final List<software.amazon.awssdk.services.ssm.model.MaintenanceWindowFilter> serviceModelFilters) {

            if (CollectionUtils.isNotEmpty(serviceModelFilters)) {
                return Optional.of(
                        serviceModelFilters.stream()
                                .map(serviceModelFilter -> {
                                    MaintenanceWindowFilter resourceModelFilter = new MaintenanceWindowFilter();
                                    resourceModelFilter.setKey(serviceModelFilter.key());
                                    resourceModelFilter.setValues(serviceModelFilter.values());
                                    return resourceModelFilter;
                                })
                                .collect(Collectors.toList()));
            }

            return Optional.empty();
    }

    @Override
    public Optional<List<software.amazon.awssdk.services.ssm.model.MaintenanceWindowFilter>> resourceModelPropertyToServiceModel(
            final List<MaintenanceWindowFilter> resourceModelFilters) {

            if (CollectionUtils.isNotEmpty(resourceModelFilters)) {
                return Optional.of(
                        resourceModelFilters.stream()
                                .map(t -> software.amazon.awssdk.services.ssm.model.MaintenanceWindowFilter.builder()
                                        .key(t.getKey())
                                        .values(t.getValues())
                                        .build())
                                .collect(Collectors.toList()));
            }

            return Optional.empty();
    }
}
