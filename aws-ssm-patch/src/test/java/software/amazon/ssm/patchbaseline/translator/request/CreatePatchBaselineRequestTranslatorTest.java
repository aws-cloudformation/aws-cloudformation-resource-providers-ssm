package software.amazon.ssm.patchbaseline.translator.request;

import software.amazon.awssdk.services.ssm.model.CreatePatchBaselineRequest;
import software.amazon.awssdk.services.ssm.model.PatchFilterGroup;
import software.amazon.awssdk.services.ssm.model.PatchSource;
import software.amazon.awssdk.services.ssm.model.PatchRuleGroup;
import software.amazon.awssdk.services.ssm.model.PatchFilter;
import software.amazon.awssdk.services.ssm.model.PatchAction;
import software.amazon.awssdk.services.ssm.model.PatchRule;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.ssm.patchbaseline.ResourceModel;
import software.amazon.ssm.patchbaseline.TestBase;
import software.amazon.ssm.patchbaseline.TestConstants;
import software.amazon.ssm.patchbaseline.utils.SimpleTypeValidator;
import static software.amazon.ssm.patchbaseline.TestConstants.*;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

@ExtendWith(MockitoExtension.class)
public class CreatePatchBaselineRequestTranslatorTest extends TestBase{

    private SimpleTypeValidator simpleTypeValidator;
    private CreatePatchBaselineRequest createPatchBaselineRequest;

    @BeforeEach
    void setUp() {
        // not mocking out SimpleTypeValidator because of the simplicity of its logic
        simpleTypeValidator = new SimpleTypeValidator();
    }

    @Test
    void testCreatePatchBaselineRequestTranslator_Nominal() {

        ResourceHandlerRequest<ResourceModel>  request = buildDefaultInputRequest();
        ResourceModel model = request.getDesiredResourceState();

        createPatchBaselineRequest =
                CreatePatchBaselineRequestTranslator.createPatchBaseline(model, TestConstants.CLIENT_REQUEST_TOKEN);

        PatchFilter pf1 = PatchFilter.builder()
                .key("PRODUCT")
                .values(Collections.singletonList("Ubuntu16.04"))
                .build();
        PatchFilter pf2 = PatchFilter.builder()
                .key("PRIORITY")
                .values(Collections.singletonList("high"))
                .build();
        PatchFilterGroup patchFilterGroup = PatchFilterGroup.builder()
                .patchFilters(Collections.singletonList(pf1))
                .build();
        PatchRule patchRule = PatchRule.builder()
                .patchFilterGroup(patchFilterGroup)
                .approveAfterDays(10)
                .complianceLevel(getComplianceString(TestConstants.ComplianceLevel.HIGH))
                .enableNonSecurity(true)
                .build();
        PatchRuleGroup approvalRules = PatchRuleGroup.builder()
                .patchRules(Collections.singletonList(patchRule))
                .build();
        PatchFilterGroup globalFilters = PatchFilterGroup.builder()
                .patchFilters(Collections.singletonList(pf2))
                .build();
        PatchSource ps1 = PatchSource.builder()
                .name("main")
                .products(Collections.singletonList("*"))
                .configuration("deb http://example.com distro component")
                .build();
        PatchSource ps2 = PatchSource.builder()
                .name("universe")
                .products(Collections.singletonList("Ubuntu14.04"))
                .configuration("deb http://example.com distro universe")
                .build();
        List<PatchSource> sourcesList = new ArrayList<>();
        sourcesList.add(ps1);
        sourcesList.add(ps2);
        Tag tag = Tag.builder().key(TAG_KEY).value(TAG_VALUE).build();
        List<Tag> tagsList = new ArrayList<>();
        tagsList.add(tag);

        CreatePatchBaselineRequest createPatchBaselineRequestExpected = CreatePatchBaselineRequest.builder()
                        .name(BASELINE_NAME)
                        .description(BASELINE_DESCRIPTION)
                        .operatingSystem(OPERATING_SYSTEM)
                        .rejectedPatches(REJECTED_PATCHES)
                        .rejectedPatchesAction(PatchAction.BLOCK)
                        .approvedPatches(ACCEPTED_PATCHES)
                        .clientToken(CLIENT_REQUEST_TOKEN)
                        .approvalRules(approvalRules)
                        .approvedPatchesComplianceLevel(getComplianceString(ComplianceLevel.CRITICAL))
                        .approvedPatchesEnableNonSecurity(true)
                        .globalFilters(globalFilters)
                        .sources(sourcesList)
                        .tags(tagsList)
                        .build();

        assertThat(createPatchBaselineRequest).isEqualTo(createPatchBaselineRequestExpected);
    }

    @Test
    void testCreatePatchBaselineRequestTranslator_Null() {
        ResourceModel model = ResourceModel.builder().name(BASELINE_NAME).build();

        createPatchBaselineRequest =
                CreatePatchBaselineRequestTranslator.createPatchBaseline(model, null);

        CreatePatchBaselineRequest createPatchBaselineRequestExpected =
                CreatePatchBaselineRequest.builder().name(BASELINE_NAME).build();

        assertThat(createPatchBaselineRequest).isEqualTo(createPatchBaselineRequestExpected);
    }

    @Test
    void testCreatePatchBaselineRequestTranslator_Empty() {
        ResourceModel model = ResourceModel.builder()
                .name(BASELINE_NAME)
                .rejectedPatches(new ArrayList<>())
                .approvedPatches(new ArrayList<>())
                .approvalRules(software.amazon.ssm.patchbaseline.RuleGroup.builder().build())
                .globalFilters(software.amazon.ssm.patchbaseline.PatchFilterGroup.builder().build())
                .sources(new ArrayList<>())
                .tags(new ArrayList<>())
                .build();

        createPatchBaselineRequest =
                CreatePatchBaselineRequestTranslator.createPatchBaseline(model, null);

        CreatePatchBaselineRequest createPatchBaselineRequestExpected =
                CreatePatchBaselineRequest.builder().name(BASELINE_NAME).build();

        assertThat(createPatchBaselineRequest).isEqualTo(createPatchBaselineRequestExpected);
    }

    @Test
    void testCreatePatchBaselineRequestTranslator_AllNull() {
        ResourceModel model = ResourceModel.builder().build();

        createPatchBaselineRequest =
                CreatePatchBaselineRequestTranslator.createPatchBaseline(model, null);

        CreatePatchBaselineRequest createPatchBaselineRequestExpected =
                CreatePatchBaselineRequest.builder().build();

        assertThat(createPatchBaselineRequest).isEqualTo(createPatchBaselineRequestExpected);
    }

}
