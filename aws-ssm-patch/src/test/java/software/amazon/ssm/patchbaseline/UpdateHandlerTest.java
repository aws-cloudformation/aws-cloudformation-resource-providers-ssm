package software.amazon.ssm.patchbaseline;

import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.*;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.awssdk.services.ssm.model.PatchRule;
import software.amazon.awssdk.services.ssm.model.PatchRuleGroup;
import software.amazon.awssdk.services.ssm.model.PatchFilter;
import software.amazon.awssdk.services.ssm.model.PatchAction;
import software.amazon.awssdk.services.ssm.model.PatchFilterGroup;
import software.amazon.awssdk.services.ssm.model.PatchSource;
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

import static software.amazon.ssm.patchbaseline.TestConstants.*;

import java.util.*;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends TestBase{

    private UpdateHandler updateHandler;
    private UpdatePatchBaselineRequest updatePatchBaselineRequest;
    private UpdatePatchBaselineResponse updatePatchBaselineResponse;
    private GetPatchBaselineRequest getPatchBaselineRequest;
    private GetPatchBaselineResponse getPatchBaselineResponse;
    private ListTagsForResourceRequest listTagsForResourceRequest;
    private ListTagsForResourceResponse listTagsForResourceResponse;
    private DeregisterPatchBaselineForPatchGroupResponse deregisterPatchBaselineForPatchGroupResponse;
    private RemoveTagsFromResourceRequest removeTagsRequest;
    private AddTagsToResourceRequest addTagsRequest;
    private RemoveTagsFromResourceResponse removeTagsFromResourceResponse;
    private AddTagsToResourceResponse addTagsToResourceResponse;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @Mock
    private TagHelper tagHelper;

    @Mock
    private SsmClient ssmClient;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        tagHelper = mock(TagHelper.class);
        ssmClient = mock(SsmClient.class);

        updateHandler = new UpdateHandler();
        getPatchBaselineRequest = GetPatchBaselineRequest.builder().baselineId(BASELINE_ID).build();
        getPatchBaselineResponse = GetPatchBaselineResponse.builder().baselineId(BASELINE_ID).patchGroups(PATCH_GROUPS).build();
        listTagsForResourceRequest = ListTagsForResourceRequest.builder().resourceType(PATCH_BASELINE_RESOURCE_NAME).resourceId(BASELINE_ID).build();
        //        deregisterPatchBaselineForPatchGroupResponse = new DeregisterPatchBaselineForPatchGroupResponse();
        PatchFilter pf1 = PatchFilter.builder()
                .key("PRODUCT")
                .values(Collections.singletonList("Ubuntu16.04"))
                .build();
        PatchFilter pf3 = PatchFilter.builder()
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
        List<PatchRule> patchRuleList = new ArrayList<>();
        patchRuleList.add(patchRule);
        PatchRuleGroup approvalRules = PatchRuleGroup.builder()
                .patchRules(patchRuleList)
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

        //Build update request. It takes a lot of space because of all the nesting.
        updatePatchBaselineRequest = UpdatePatchBaselineRequest.builder()
                .name(UPDATED_BASELINE_NAME)
                .description(UPDATED_BASELINE_DESC)
                .baselineId(BASELINE_ID)
                .rejectedPatches(UPDATED_REJECTED_PATCHES)
                .rejectedPatchesAction(PatchAction.ALLOW_AS_DEPENDENCY)
                .approvedPatches(UPDATED_ACCEPTED_PATCHES)
                .approvalRules(approvalRules)
                .approvedPatchesComplianceLevel(getComplianceString(ComplianceLevel.MEDIUM))
                .approvedPatchesEnableNonSecurity(true)
                .sources(sourcesList)
                .globalFilters(globalFilters)
                .replace(true)
                .build();

        updatePatchBaselineResponse = UpdatePatchBaselineResponse.builder().baselineId(BASELINE_ID).build();

    }

    @Test
    public void testSuccess() {

        List<String> expectedRemoveTags = Arrays.asList(CFN_KEY,SYSTEM_TAG_KEY);
        List<Tag> expectedAddTags = Arrays.asList(
                Tag.builder().key(UPDATED_CFN_KEY).value(UPDATED_CFN_VALUE).build(),
                Tag.builder().key(NEW_TAG_KEY).value(NEW_TAG_VALUE).build(),
                Tag.builder().key(SYSTEM_TAG_KEY).value(UPDATED_BASELINE_NAME).build());
        List<Tag> existedTags = Arrays.asList(
                Tag.builder().key(CFN_KEY).value(CFN_VALUE).build(),
                Tag.builder().key(SYSTEM_TAG_KEY).value(BASELINE_NAME).build()
        );
        listTagsForResourceResponse = ListTagsForResourceResponse.builder().tagList(existedTags).build();
        removeTagsRequest = RemoveTagsFromResourceRequest.builder().tagKeys(expectedRemoveTags).build();
        addTagsRequest = AddTagsToResourceRequest.builder().tags(expectedAddTags).build();
        removeTagsFromResourceResponse = RemoveTagsFromResourceResponse.builder().build();
        addTagsToResourceResponse = AddTagsToResourceResponse.builder().build();

        when(proxy.injectCredentialsAndInvokeV2(eq(updatePatchBaselineRequest),
                ArgumentMatchers.<Function<UpdatePatchBaselineRequest, UpdatePatchBaselineResponse>>any()))
                .thenReturn(updatePatchBaselineResponse);

        when(proxy.injectCredentialsAndInvokeV2(eq(getPatchBaselineRequest),
                ArgumentMatchers.<Function<GetPatchBaselineRequest, GetPatchBaselineResponse>>any()))
                .thenReturn(getPatchBaselineResponse);

        when(proxy.injectCredentialsAndInvokeV2(
                eq(listTagsForResourceRequest),
                ArgumentMatchers.<Function<ListTagsForResourceRequest, ListTagsForResourceResponse>>any()))
                .thenReturn(listTagsForResourceResponse);

        //Invoke the handler
        ResourceHandlerRequest<ResourceModel>  request = buildUpdateDefaultInputRequest();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = updateHandler.handleRequest(proxy, request, null, logger);

        System.out.print(String.format("Update Handler Response Status %s %n", response.getStatus()));

        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(updatePatchBaselineRequest),
                        ArgumentMatchers.<Function<UpdatePatchBaselineRequest, UpdatePatchBaselineResponse>>any());

        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(getPatchBaselineRequest),
                        ArgumentMatchers.<Function<GetPatchBaselineRequest, GetPatchBaselineResponse>>any());

        List<String> originalGroups = PATCH_GROUPS;
        List<String> newGroups = UPDATE_PATCH_GROUPS;

        //Compute the intersection of the two lists (the groups that don't need to be changed)
        List<String> intersectingGroups = new ArrayList<>(originalGroups);
        intersectingGroups.retainAll(newGroups);

        //The groups we need to remove are ORIGINAL - INTERSECT
        //The groups we need to add are DESIRED - INTERSECT
        originalGroups.removeAll(intersectingGroups);
        newGroups.removeAll(intersectingGroups);

        for (String group : originalGroups) {
            verify(proxy)
                    .injectCredentialsAndInvokeV2(
                            eq(buildDeregisterGroupRequest(getPatchBaselineResponse.baselineId(), group)),
                            ArgumentMatchers.<Function<DeregisterPatchBaselineForPatchGroupRequest, DeregisterPatchBaselineForPatchGroupResponse>>any());
        }

        for (String group : newGroups) {
            if (!TestConstants.PATCH_GROUPS.contains(group)) {
                verify(proxy)
                        .injectCredentialsAndInvokeV2(
                                eq(buildRegisterGroupRequest(getPatchBaselineResponse.baselineId(), group)),
                                ArgumentMatchers.<Function<RegisterPatchBaselineForPatchGroupRequest, RegisterPatchBaselineForPatchGroupResponse>>any());
            }
        }

        //when(tagHelper.updateTagsForResource(request, PATCH_BASELINE_RESOURCE_NAME, ssmClient, proxy)).thenReturn();
        when(proxy.injectCredentialsAndInvokeV2(
                eq(removeTagsRequest),
                ArgumentMatchers.<Function<RemoveTagsFromResourceRequest, RemoveTagsFromResourceResponse>>any()))
                .thenReturn(removeTagsFromResourceResponse);
        when(proxy.injectCredentialsAndInvokeV2(
                eq(addTagsRequest),
                ArgumentMatchers.<Function<AddTagsToResourceRequest, AddTagsToResourceResponse>>any()))
                .thenReturn(addTagsToResourceResponse);
        //not working
        //when(tagHelper.updateTagsForResource(request, PATCH_BASELINE_RESOURCE_NAME, ssmClient, proxy)).thenReturn();
        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(listTagsForResourceRequest),
                        ArgumentMatchers.<Function<ListTagsForResourceRequest, ListTagsForResourceResponse>>any());

        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(removeTagsRequest),
                        ArgumentMatchers.<Function<RemoveTagsFromResourceRequest, RemoveTagsFromResourceResponse>>any());

        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(addTagsRequest),
                        ArgumentMatchers.<Function<AddTagsToResourceRequest, AddTagsToResourceResponse>>any());

        for (Tag tag : listTagsForResourceResponse.tagList())
            System.out.print(String.format("listTagsForResourceResponse tag key %s, tag val %s %n", tag.key(), tag.value()));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        //assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }


}
