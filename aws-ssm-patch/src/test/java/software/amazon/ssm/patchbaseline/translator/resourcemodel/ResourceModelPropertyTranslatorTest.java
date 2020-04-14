package software.amazon.ssm.patchbaseline.translator.resourcemodel;

import software.amazon.awssdk.services.ssm.model.PatchSource;
import software.amazon.awssdk.services.ssm.model.PatchFilterGroup;
import software.amazon.awssdk.services.ssm.model.PatchRuleGroup;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.ssm.patchbaseline.TestBase;
import static software.amazon.ssm.patchbaseline.TestConstants.TAG_KEY;
import static software.amazon.ssm.patchbaseline.TestConstants.TAG_VALUE;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ResourceModelPropertyTranslatorTest extends TestBase {

    @Test
    void testTranslateToRequestTags() {
        final List<Tag> expectedTags = requestTags(TAG_KEY, TAG_VALUE);
        List<software.amazon.ssm.patchbaseline.Tag> tags = tags(TAG_KEY, TAG_VALUE);
        Optional<List<Tag>> requestTags = ResourceModelPropertyTranslator.translateToRequestTags(tags);
        assertThat(requestTags).isEqualTo(Optional.of(expectedTags));
    }

    @Test
    void testTranslateToRequestTags_emptyList() {
        List<software.amazon.ssm.patchbaseline.Tag> tags = new ArrayList<>();
        Optional<List<Tag>> requestTags = ResourceModelPropertyTranslator.translateToRequestTags(tags);
        assertThat(requestTags).isEqualTo(Optional.empty());
    }

    @Test
    void testTranslateToRequestTags_null() {
        Optional<List<Tag>> requestTags = ResourceModelPropertyTranslator.translateToRequestTags(null);
        assertThat(requestTags).isEqualTo(Optional.empty());
    }

    @Test
    void testTranslateToResourceModelTags() {
        final List<Tag> tags = requestTags(TAG_KEY, TAG_VALUE);
        List<software.amazon.ssm.patchbaseline.Tag> expectedTags = tags(TAG_KEY, TAG_VALUE);
        Optional<List<software.amazon.ssm.patchbaseline.Tag>> resourceModelTags =
                ResourceModelPropertyTranslator.translateToResourceModelTags(tags);
        assertThat(resourceModelTags).isEqualTo(Optional.of(expectedTags));
    }

    @Test
    void testTranslateToResourceModelTags_emptyList() {
        final List<Tag> tags = new ArrayList<>();
        Optional<List<software.amazon.ssm.patchbaseline.Tag>> resourceModelTags =
                ResourceModelPropertyTranslator.translateToResourceModelTags(tags);
        assertThat(resourceModelTags).isEqualTo(Optional.empty());
    }

    @Test
    void testTranslateToResourceModelTags_null() {
        Optional<List<software.amazon.ssm.patchbaseline.Tag>> resourceModelTags =
                ResourceModelPropertyTranslator.translateToResourceModelTags(null);
        assertThat(resourceModelTags).isEqualTo(Optional.empty());
    }


    @Test
    void testTranslateToResourceModelSources() {
        final List<PatchSource> sources = requestSources();
        List<software.amazon.ssm.patchbaseline.PatchSource> expectedSources = sources();
        Optional<List<software.amazon.ssm.patchbaseline.PatchSource>> resourceModelSources =
                ResourceModelPropertyTranslator.translateToResourceModelSources(sources);
        assertThat(resourceModelSources).isEqualTo(Optional.of(expectedSources));
    }

    @Test
    void testTranslateToResourceModelSources_emptyList() {
        final List<PatchSource> sources = new ArrayList<>();
        Optional<List<software.amazon.ssm.patchbaseline.PatchSource>> resourceModelSources =
                ResourceModelPropertyTranslator.translateToResourceModelSources(sources);
        assertThat(resourceModelSources).isEqualTo(Optional.empty());
    }

    @Test
    void testTranslateToResourceModelSources_null() {
        Optional<List<software.amazon.ssm.patchbaseline.PatchSource>> resourceModelSources =
                ResourceModelPropertyTranslator.translateToResourceModelSources(null);
        assertThat(resourceModelSources).isEqualTo(Optional.empty());
    }

    @Test
    void testTranslateToRequestSources() {
        final List<PatchSource> expectedSources = requestSources();
        List<software.amazon.ssm.patchbaseline.PatchSource> sources = sources();
        Optional<List<PatchSource>> requestSources =
                ResourceModelPropertyTranslator.translateToRequestSources(sources);
        assertThat(requestSources).isEqualTo(Optional.of(expectedSources));
    }

    @Test
    void testTranslateToRequestSources_emptyList() {
        List<software.amazon.ssm.patchbaseline.PatchSource> sources = new ArrayList<>();
        Optional<List<PatchSource>> requestSources =
                ResourceModelPropertyTranslator.translateToRequestSources(sources);
        assertThat(requestSources).isEqualTo(Optional.empty());
    }

    @Test
    void testTranslateToRequestSources_null() {
        Optional<List<PatchSource>> requestSources =
                ResourceModelPropertyTranslator.translateToRequestSources(null);
        assertThat(requestSources).isEqualTo(Optional.empty());
    }

    @Test
    void testTranslateToResourceModelGlobalFilters() {
        final PatchFilterGroup globalFilters = requestGlobalFilters();
        final software.amazon.ssm.patchbaseline.PatchFilterGroup expectedGlobalFilters = globalFilters();
        final Optional<software.amazon.ssm.patchbaseline.PatchFilterGroup> resourceModelGlobalFilters =
                ResourceModelPropertyTranslator.translateToResourceModelGlobalFilters(globalFilters);
        assertThat(resourceModelGlobalFilters).isEqualTo(Optional.of(expectedGlobalFilters));
    }

    @Test
    void testTranslateToResourceModelGlobalFilters_empty() {
        final PatchFilterGroup globalFilters = PatchFilterGroup.builder().build();
        final Optional<software.amazon.ssm.patchbaseline.PatchFilterGroup> resourceModelGlobalFilters =
                ResourceModelPropertyTranslator.translateToResourceModelGlobalFilters(globalFilters);
        assertThat(resourceModelGlobalFilters).isEqualTo(Optional.empty());
    }

    @Test
    void testTranslateToResourceModelGlobalFilters_null() {
        final Optional<software.amazon.ssm.patchbaseline.PatchFilterGroup> resourceModelGlobalFilters =
                ResourceModelPropertyTranslator.translateToResourceModelGlobalFilters(null);
        assertThat(resourceModelGlobalFilters).isEqualTo(Optional.empty());
    }

    @Test
    void testTranslateToRequestGlobalFilters() {
        final PatchFilterGroup expectedGlobalFilters = requestGlobalFilters();
        final software.amazon.ssm.patchbaseline.PatchFilterGroup globalFilters = globalFilters();
        final Optional<PatchFilterGroup> requestGlobalFilters =
                ResourceModelPropertyTranslator.translateToRequestGlobalFilters(globalFilters);
        assertThat(requestGlobalFilters).isEqualTo(Optional.of(expectedGlobalFilters));
    }

    @Test
    void testTranslateToRequestGlobalFilters_empty() {
        final software.amazon.ssm.patchbaseline.PatchFilterGroup globalFilters =
                software.amazon.ssm.patchbaseline.PatchFilterGroup.builder().build();
        final Optional<PatchFilterGroup> requestGlobalFilters =
                ResourceModelPropertyTranslator.translateToRequestGlobalFilters(globalFilters);
        assertThat(requestGlobalFilters).isEqualTo(Optional.empty());
    }

    @Test
    void testTranslateToRequestGlobalFilters_null() {
        final Optional<PatchFilterGroup> requestGlobalFilters =
                ResourceModelPropertyTranslator.translateToRequestGlobalFilters(null);
        assertThat(requestGlobalFilters).isEqualTo(Optional.empty());
    }

    @Test
    void testTranslateToResourceModelApprovalRules() {
        final PatchRuleGroup approvalRules = requestApprovalRules();
        final software.amazon.ssm.patchbaseline.RuleGroup expectedApprovalRules = approvalRules();
        final Optional<software.amazon.ssm.patchbaseline.RuleGroup> resourceModelApprovalRules =
                ResourceModelPropertyTranslator.translateToResourceModelApprovalRules(approvalRules);
        assertThat(resourceModelApprovalRules).isEqualTo(Optional.of(expectedApprovalRules));
    }

    @Test
    void testTranslateToResourceModelApprovalRules_empty() {
        final PatchRuleGroup approvalRules = PatchRuleGroup.builder().build();
        final Optional<software.amazon.ssm.patchbaseline.RuleGroup> resourceModelApprovalRules =
                ResourceModelPropertyTranslator.translateToResourceModelApprovalRules(approvalRules);
        assertThat(resourceModelApprovalRules).isEqualTo(Optional.empty());
    }

    @Test
    void testTranslateToResourceModelApprovalRules_null() {
        final Optional<software.amazon.ssm.patchbaseline.RuleGroup> resourceModelApprovalRules =
                ResourceModelPropertyTranslator.translateToResourceModelApprovalRules(null);
        assertThat(resourceModelApprovalRules).isEqualTo(Optional.empty());
    }

    @Test
    void testTranslateToRequestApprovalRules() {
        final PatchRuleGroup expectedApprovalRules = requestApprovalRules();
        final software.amazon.ssm.patchbaseline.RuleGroup approvalRules = approvalRules();
        final Optional<PatchRuleGroup> requestApprovalRules =
                ResourceModelPropertyTranslator.translateToRequestApprovalRules(approvalRules);
        assertThat(requestApprovalRules).isEqualTo(Optional.of(expectedApprovalRules));
    }

    @Test
    void testTranslateToRequestApprovalRules_empty() {
        final software.amazon.ssm.patchbaseline.RuleGroup approvalRules =
                software.amazon.ssm.patchbaseline.RuleGroup.builder().build();
        final Optional<PatchRuleGroup> requestApprovalRules =
                ResourceModelPropertyTranslator.translateToRequestApprovalRules(approvalRules);
        assertThat(requestApprovalRules).isEqualTo(Optional.empty());
    }

    @Test
    void testTranslateToRequestApprovalRules_null() {
        final Optional<PatchRuleGroup> requestApprovalRules =
                ResourceModelPropertyTranslator.translateToRequestApprovalRules(null);
        assertThat(requestApprovalRules).isEqualTo(Optional.empty());
    }

}
