package software.amazon.ssm.patchbaseline.translator.resourcemodel;


import org.checkerframework.common.value.qual.StaticallyExecutable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.model.GetPatchBaselineResponse;
import software.amazon.awssdk.services.ssm.model.PatchFilterGroup;
import software.amazon.awssdk.services.ssm.model.PatchRuleGroup;
import software.amazon.awssdk.services.ssm.model.PatchSource;
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
public class ReadResourceModelTranslatorTest extends TestBase {

    private SimpleTypeValidator simpleTypeValidator;

    private ReadResourceModelTranslator readResourceModelTranslator;

    private GetPatchBaselineResponse getPatchBaselineResponse;

    @BeforeEach
    void setUp() {
        // not mocking out SimpleTypeValidator because of the simplicity of its logic
        simpleTypeValidator = new SimpleTypeValidator();

        readResourceModelTranslator = new ReadResourceModelTranslator();
    }

    @Test
    void ReadResourceModelTranslatorWithAllPropertiesPresent() {

        List<PatchSource> sources = requestsources();
        PatchFilterGroup globalFilters = requestglobalFilters();
        PatchRuleGroup approvalRules = requestapprovalRules();
        getPatchBaselineResponse = GetPatchBaselineResponse.builder()
                .baselineId(TestConstants.BASELINE_ID)
                .name(TestConstants.BASELINE_NAME)
                .operatingSystem(TestConstants.OPERATING_SYSTEM)
                .description(TestConstants.BASELINE_DESCRIPTION)
                .rejectedPatches(TestConstants.REJECTED_PATCHES)
                .rejectedPatchesAction("BLOCK")
                .approvedPatches(TestConstants.ACCEPTED_PATCHES)
                .approvalRules(approvalRules)
                .approvedPatchesComplianceLevel(getComplianceString(TestConstants.ComplianceLevel.CRITICAL))
                .approvedPatchesEnableNonSecurity(true)
                .globalFilters(globalFilters)
                .sources(sources)
                .patchGroups(TestConstants.PATCH_GROUPS)
                .build();

        final ResourceModel resultModel =
                readResourceModelTranslator.translateToResourceModel(getPatchBaselineResponse);

        final ResourceModel expectedModel = buildDefaultInputRequest().getDesiredResourceState();
        expectedModel.setTags(null);

        assertThat(resultModel).isEqualTo(expectedModel);
    }
}
