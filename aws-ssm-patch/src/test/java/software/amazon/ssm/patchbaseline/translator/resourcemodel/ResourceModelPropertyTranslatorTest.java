package software.amazon.ssm.patchbaseline.translator.resourcemodel;


import org.checkerframework.common.value.qual.StaticallyExecutable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.model.*;
import software.amazon.ssm.patchbaseline.Resource;
import software.amazon.ssm.patchbaseline.ResourceModel;
import software.amazon.ssm.patchbaseline.TestBase;
import software.amazon.ssm.patchbaseline.TestConstants;
import software.amazon.ssm.patchbaseline.utils.SimpleTypeValidator;
import static org.assertj.core.api.Assertions.assertThat;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ResourceModelPropertyTranslatorTest extends TestBase {

    private SimpleTypeValidator simpleTypeValidator;

    private ResourceModelPropertyTranslator resourceModelPropertyTranslator;

    private GetPatchBaselineResponse getPatchBaselineResponse;

    @BeforeEach
    void setUp() {
        // not mocking out SimpleTypeValidator because of the simplicity of its logic
        simpleTypeValidator = new SimpleTypeValidator();

        resourceModelPropertyTranslator = new ResourceModelPropertyTranslator();
    }

    @Test
    void translateToResourceModelSourcesTest() {
        final List<PatchSource> sources = requestsources();
        List<software.amazon.ssm.patchbaseline.PatchSource> expectedSources = sources();
        Optional<List<software.amazon.ssm.patchbaseline.PatchSource>> resourceModelSources =
                resourceModelPropertyTranslator.translateToResourceModelSources(sources);
        assertThat(resourceModelSources).isEqualTo(Optional.of(expectedSources));
    }

    @Test
    void translateToRequestSourcesTest() {
        final List<PatchSource> expectedSources = requestsources();
        List<software.amazon.ssm.patchbaseline.PatchSource> sources = sources();
        Optional<List<PatchSource>> requestSources =
                resourceModelPropertyTranslator.translateToRequestSources(sources);
        assertThat(requestSources).isEqualTo(Optional.of(expectedSources));
    }

    @Test
    void translateToResourceModelGlobalFiltersTest() {
        final PatchFilterGroup globalFilters = requestglobalFilters();
        final software.amazon.ssm.patchbaseline.PatchFilterGroup expectedGlobalFilters = globalFilters();
        final Optional<software.amazon.ssm.patchbaseline.PatchFilterGroup> resourceModelGlobalFilters =
                resourceModelPropertyTranslator.translateToResourceModelGlobalFilters(globalFilters);
        assertThat(resourceModelGlobalFilters).isEqualTo(Optional.of(expectedGlobalFilters));
    }

    @Test
    void translateToRequestGlobalFiltersTest() {
        final PatchFilterGroup expectedGlobalFilters = requestglobalFilters();
        final software.amazon.ssm.patchbaseline.PatchFilterGroup globalFilters = globalFilters();
        final Optional<PatchFilterGroup> requestGlobalFilters =
                resourceModelPropertyTranslator.translateToRequestGlobalFilters(globalFilters);
        assertThat(requestGlobalFilters).isEqualTo(Optional.of(expectedGlobalFilters));
    }

    @Test
    void translateToResourceModelApprovalRulesTest() {
        final PatchRuleGroup approvalRules = requestapprovalRules();
        final software.amazon.ssm.patchbaseline.RuleGroup expectedApprovalRules = approvalRules();
        final Optional<software.amazon.ssm.patchbaseline.RuleGroup> resourceModelApprovalRules =
                resourceModelPropertyTranslator.translateToResourceModelApprovalRules(approvalRules);
        assertThat(resourceModelApprovalRules).isEqualTo(Optional.of(expectedApprovalRules));
    }

    @Test
    void translateToRequestApprovalRulesTest() {
        final PatchRuleGroup expectedApprovalRules = requestapprovalRules();
        final software.amazon.ssm.patchbaseline.RuleGroup approvalRules = approvalRules();
        final Optional<PatchRuleGroup> requestApprovalRules =
                resourceModelPropertyTranslator.translateToRequestApprovalRules(approvalRules);
        assertThat(requestApprovalRules).isEqualTo(Optional.of(expectedApprovalRules));
    }

}


