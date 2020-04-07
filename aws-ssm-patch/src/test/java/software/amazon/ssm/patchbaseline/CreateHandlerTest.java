package software.amazon.ssm.patchbaseline;

import org.junit.jupiter.api.BeforeAll;
import software.amazon.ssm.patchbaseline.Resource;
import software.amazon.awssdk.services.ssm.model.*;
import software.amazon.awssdk.services.ssm.model.PatchFilter;
import software.amazon.awssdk.services.ssm.model.PatchFilterGroup;
import software.amazon.awssdk.services.ssm.model.PatchSource;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.ssm.patchbaseline.TagHelper;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.ArgumentMatchers;

import java.util.Optional;
import java.util.function.Function;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * test all 5 responses from the create handler.
 */
@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends TestBase {
    @Mock
    private AmazonWebServicesClientProxy proxy;

    private CreateHandler createHandler;
    private CreatePatchBaselineRequest createPatchBaselineRequest;
    private CreatePatchBaselineResponse createPatchBaselineResponse;
    @Mock
    private Resource resource;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        resource = mock(Resource.class);

        createHandler = new CreateHandler();


        // This mountain of code is the manual creation of the request in testCreateSuccess.json
        // We pass the one read from the .json and verify it is the same as this one
        //  ensuring the reading process doesn't change any data.

        PatchFilter pf1 = PatchFilter.builder()
                .key("PRODUCT")
                .values(Collections.singletonList("Ubuntu16.04"))
                .build();
        PatchFilter pf2 = PatchFilter.builder()
                .key("SECTION")
                .values(Collections.singletonList("python"))
                .build();
        PatchFilter pf3 = PatchFilter.builder()
                .key("PRIORITY")
                .values(Collections.singletonList("high"))
                .build();
//        List<PatchFilter> patchFiltersList = Collections.emptyList();
//        patchFiltersList.add(pf1);
//        patchFiltersList.add(pf2);
        PatchFilterGroup patchFilterGroup = PatchFilterGroup.builder()
                .patchFilters(Collections.singletonList(pf1))
                .build();
        PatchRule patchRule = PatchRule.builder()
                .patchFilterGroup(patchFilterGroup)
                .approveAfterDays(10)
                .complianceLevel(getComplianceString(TestConstants.ComplianceLevel.HIGH))
                .enableNonSecurity(true)
                .build();
//        List<PatchRule> patchRuleList = Collections.emptyList();
//        patchRuleList.add(patchRule);
        PatchRuleGroup approvalRules = PatchRuleGroup.builder()
                .patchRules(Collections.singletonList(patchRule))
                .build();
        PatchFilterGroup globalFilters = PatchFilterGroup.builder()
                .patchFilters(Collections.singletonList(pf3))
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
        Tag tag1 = Tag.builder().key(TestConstants.CFN_KEY).value(TestConstants.CFN_VALUE).build();
        Tag tag2 = Tag.builder().key(TestConstants.TAG_KEY).value(TestConstants.TAG_VALUE).build();
        Tag tag3 = Tag.builder().key(TestConstants.SYSTEM_TAG_KEY).value(TestConstants.BASELINE_NAME).build();
        List<Tag> tagsList = new ArrayList<>();
        tagsList.add(tag1);
        tagsList.add(tag2);
        tagsList.add(tag3);

        createPatchBaselineRequest = CreatePatchBaselineRequest.builder()
                .name(TestConstants.BASELINE_NAME)
                .description(TestConstants.BASELINE_DESCRIPTION)
                .operatingSystem(TestConstants.OPERATING_SYSTEM)
                .rejectedPatches(TestConstants.REJECTED_PATCHES)
                .rejectedPatchesAction(PatchAction.BLOCK)
                .approvedPatches(TestConstants.ACCEPTED_PATCHES)
                .clientToken(TestConstants.CLIENT_REQUEST_TOKEN)
                .approvalRules(approvalRules)
                .approvedPatchesComplianceLevel(getComplianceString(TestConstants.ComplianceLevel.CRITICAL))
                .approvedPatchesEnableNonSecurity(true)
                .globalFilters(globalFilters)
                .sources(sourcesList)
                .tags(tagsList)
                .build();

        createPatchBaselineResponse =  CreatePatchBaselineResponse.builder()
                .baselineId(TestConstants.BASELINE_ID)
                .build();

        System.out.print(String.format("Initialize at this point with baselineId %s %n", TestConstants.BASELINE_ID));

    }

    @Test
    public void testSuccess() {
       when(proxy.injectCredentialsAndInvokeV2(eq(createPatchBaselineRequest),
                ArgumentMatchers.<Function<CreatePatchBaselineRequest, CreatePatchBaselineResponse>>any()))
                .thenReturn(createPatchBaselineResponse);

        //Invoke the handler
        ResourceHandlerRequest<ResourceModel>  request = buildDefaultInputRequest();

        System.out.print(String.format("Create Request getClientRequestToken %s %n", request.getClientRequestToken()));

        final ProgressEvent<ResourceModel, CallbackContext> response
                = createHandler.handleRequest(proxy, request, null, logger);

        System.out.print(String.format("Create Handler Response Status %s %n", response.getStatus()));

        // need to check that the createPatchBaseline was invoked with the correct request made from the model
        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(createPatchBaselineRequest),
                        ArgumentMatchers.<Function<CreatePatchBaselineRequest, CreatePatchBaselineResponse>>any());

        for (String group : TestConstants.PATCH_GROUPS) {
            verify(proxy)
                    .injectCredentialsAndInvokeV2(
                            eq(buildRegisterGroupRequest(createPatchBaselineResponse.baselineId(), group)),
                            ArgumentMatchers.<Function<RegisterPatchBaselineForPatchGroupRequest, RegisterPatchBaselineForPatchGroupResponse>>any());
        }

        //Finally, assert that the responses are also how we want them.
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verifyZeroInteractions(resource);
    }
}
