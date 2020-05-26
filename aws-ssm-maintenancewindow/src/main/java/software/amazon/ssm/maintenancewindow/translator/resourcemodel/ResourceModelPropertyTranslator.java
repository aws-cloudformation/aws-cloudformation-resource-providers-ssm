package software.amazon.ssm.maintenancewindow.translator.resourcemodel;

import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.awssdk.services.ssm.model.PatchFilter;
import software.amazon.awssdk.services.ssm.model.PatchFilterGroup;
import software.amazon.awssdk.services.ssm.model.PatchRule;
import software.amazon.awssdk.services.ssm.model.PatchRuleGroup;
import software.amazon.awssdk.services.ssm.model.PatchSource;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.ArrayList;
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

    /**
     * Translate Request Tags to Resource Model Tags
     */
    public  static Optional<List<software.amazon.ssm.maintenancewindow.Tag>> translateToResourceModelTags(final List<Tag> requestTags) {

        if (!CollectionUtils.isNullOrEmpty(requestTags)) {
            List<software.amazon.ssm.maintenancewindow.Tag> resourceModelTags = requestTags.stream()
                    .collect(Collectors.mapping(entry ->
                                    software.amazon.ssm.maintenancewindow.Tag.builder()
                                            .key(entry.key())
                                            .value(entry.value())
                                            .build(),
                            Collectors.toList()));
            return Optional.of(resourceModelTags);
        }
        return Optional.empty();
    }
}
