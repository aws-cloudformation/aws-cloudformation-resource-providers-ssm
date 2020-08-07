package software.amazon.ssm.maintenancewindow.translator.resourcemodel;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.ssm.model.Tag;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static software.amazon.ssm.maintenancewindow.TestConstants.CONSOLIDATED_RESOURCE_MODEL_AND_STACK_TAGS;
import static software.amazon.ssm.maintenancewindow.TestConstants.SERVICE_MODEL_TAG_WITH_RESOURCE_TAGS;

public class ResourceModelPropertyTranslatorTest {

    @Test
    void translateToRequestTagsWithNullTags() {
        Optional<List<Tag>> translatedTags = ResourceModelPropertyTranslator.translateToRequestTags(null);

        assertThat(translatedTags).isEqualTo(Optional.empty());
    }

    @Test
    void translateToRequestTagsWithTags() {
        Optional<List<Tag>> translatedTags = ResourceModelPropertyTranslator.translateToRequestTags(CONSOLIDATED_RESOURCE_MODEL_AND_STACK_TAGS);

        assertThat(translatedTags).isEqualTo(Optional.of(SERVICE_MODEL_TAG_WITH_RESOURCE_TAGS));
    }
}
