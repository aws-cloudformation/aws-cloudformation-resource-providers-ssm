package software.amazon.ssm.maintenancewindowtarget.translator.property;

import software.amazon.ssm.maintenancewindowtarget.MaintenanceWindowFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.MODEL_FILTERS;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.SERVICE_FILTERS;
import static org.assertj.core.api.Assertions.assertThat;

class FiltersListTranslatorTest {

    private FiltersListTranslator filtersListTranslator;

    @BeforeEach
    void setUp() {
        filtersListTranslator = new FiltersListTranslator();
    }

    @Test
    void serviceModelPropertyToResourceModel() {
        final Optional<List<MaintenanceWindowFilter>> resourceModelFilters =
                filtersListTranslator.serviceModelPropertyToResourceModel(SERVICE_FILTERS);

        assertThat(resourceModelFilters).isEqualTo(Optional.of(MODEL_FILTERS));
    }

    @Test
    void serviceModelPropertyToResourceModelReturnsEmptyWithNullInput() {
        final Optional<List<MaintenanceWindowFilter>> resourceModelFilters =
                filtersListTranslator.serviceModelPropertyToResourceModel(null);

        assertThat(resourceModelFilters).isEqualTo(Optional.empty());
    }

    @Test
    void serviceModelPropertyToResourceModelReturnsEmptyWithEmptyInputList() {
        final Optional<List<MaintenanceWindowFilter>> resourceModelFilters =
                filtersListTranslator.serviceModelPropertyToResourceModel(Collections.emptyList());

        assertThat(resourceModelFilters).isEqualTo(Optional.empty());
    }

    @Test
    void resourceModelPropertyToServiceModel() {
        final Optional<List<software.amazon.awssdk.services.ssm.model.MaintenanceWindowFilter>> serviceModelFilters =
                filtersListTranslator.resourceModelPropertyToServiceModel(MODEL_FILTERS);

        assertThat(serviceModelFilters).isEqualTo(Optional.of(SERVICE_FILTERS));
    }

    @Test
    void resourceModelPropertyToServiceModelReturnsEmptyWithNullInput() {
        final Optional<List<software.amazon.awssdk.services.ssm.model.MaintenanceWindowFilter>> serviceModelFilters =
                filtersListTranslator.resourceModelPropertyToServiceModel(null);

        assertThat(serviceModelFilters).isEqualTo(Optional.empty());
    }

    @Test
    void resourceModelPropertyToServiceModelReturnsEmptyWithEmptyInputList() {
        final Optional<List<software.amazon.awssdk.services.ssm.model.MaintenanceWindowFilter>> serviceModelFilters =
                filtersListTranslator.resourceModelPropertyToServiceModel(Collections.emptyList());

        assertThat(serviceModelFilters).isEqualTo(Optional.empty());
    }
}
