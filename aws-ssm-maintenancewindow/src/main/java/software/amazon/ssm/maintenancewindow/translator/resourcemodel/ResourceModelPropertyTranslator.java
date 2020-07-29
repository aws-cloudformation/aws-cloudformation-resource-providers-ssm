package software.amazon.ssm.maintenancewindow.translator.resourcemodel;

import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ResourceModelPropertyTranslator {

    /**
     * Translate Resource Model Tags to Request Tags
     */
    public static Optional<List<Tag>> translateToRequestTags(final List<software.amazon.ssm.maintenancewindow.Tag> resourceModelTags) {

        if (!CollectionUtils.isNullOrEmpty(resourceModelTags)) {
            List<Tag> requestTags = resourceModelTags.stream()
                    .collect(Collectors.mapping(entry ->
                                    Tag.builder()
                                            .key(entry.getKey())
                                            .value(entry.getValue())
                                            .build(),
                            Collectors.toList()));
            return Optional.of(requestTags);
        }
        return Optional.empty();
    }
}
