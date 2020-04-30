package software.amazon.ssm.patchbaseline;

import software.amazon.awssdk.services.ssm.model.UpdatePatchBaselineRequest;
import software.amazon.awssdk.services.ssm.model.UpdatePatchBaselineResponse;
import software.amazon.awssdk.services.ssm.model.GetPatchBaselineRequest;
import software.amazon.awssdk.services.ssm.model.GetPatchBaselineResponse;
import software.amazon.awssdk.services.ssm.model.DeregisterPatchBaselineForPatchGroupRequest;
import software.amazon.awssdk.services.ssm.model.DeregisterPatchBaselineForPatchGroupResponse;
import software.amazon.awssdk.services.ssm.model.RegisterPatchBaselineForPatchGroupRequest;
import software.amazon.awssdk.services.ssm.model.RegisterPatchBaselineForPatchGroupResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.PatchRule;
import software.amazon.awssdk.services.ssm.model.PatchRuleGroup;
import software.amazon.awssdk.services.ssm.model.PatchFilter;
import software.amazon.awssdk.services.ssm.model.PatchAction;
import software.amazon.awssdk.services.ssm.model.PatchFilterGroup;
import software.amazon.awssdk.services.ssm.model.PatchSource;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import static software.amazon.ssm.patchbaseline.TestConstants.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Function;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends TestBase{

    @InjectMocks
    private UpdateHandler updateHandler;
    @Mock
    private TagHelper mockTagHelper;
    @Mock
    private AmazonWebServicesClientProxy proxy;

    private UpdatePatchBaselineRequest mockUpdatePatchBaselineRequest;
    private GetPatchBaselineRequest mockGetPatchBaselineRequest;
    private UpdatePatchBaselineResponse mockUpdatePatchBaselineResponse;
    private GetPatchBaselineResponse mockGetPatchBaselineResponse;
    private RegisterPatchBaselineForPatchGroupResponse mockRegisterGroupResponse;
    private DeregisterPatchBaselineForPatchGroupResponse mockDeregisterGroupResponse;

    public void setupSuccessMocks() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        //set up mock for UpdatePatchBaseline
        mockUpdatePatchBaselineRequest = setUpExpectedUpdatePatchBaselineRequest();
        mockUpdatePatchBaselineResponse = UpdatePatchBaselineResponse.builder().baselineId(BASELINE_ID).build();
        when(proxy.injectCredentialsAndInvokeV2 (
            eq(mockUpdatePatchBaselineRequest), ArgumentMatchers.<Function<UpdatePatchBaselineRequest,UpdatePatchBaselineResponse>>any()))
        .thenReturn(mockUpdatePatchBaselineResponse);

        //set up mock for GetPatchBaseline
        mockGetPatchBaselineRequest = GetPatchBaselineRequest.builder().baselineId(BASELINE_ID).build();
        mockGetPatchBaselineResponse = GetPatchBaselineResponse.builder().baselineId(BASELINE_ID).patchGroups(PATCH_GROUPS).build();
        when(proxy.injectCredentialsAndInvokeV2(
            eq(mockGetPatchBaselineRequest), ArgumentMatchers.<Function<GetPatchBaselineRequest, GetPatchBaselineResponse>>any()))
        .thenReturn(mockGetPatchBaselineResponse);

        //set up mock for RegisterGroup
        when(proxy.injectCredentialsAndInvokeV2(
            any(RegisterPatchBaselineForPatchGroupRequest.class), ArgumentMatchers.<Function<RegisterPatchBaselineForPatchGroupRequest, RegisterPatchBaselineForPatchGroupResponse>>any()))
        .thenReturn(mockRegisterGroupResponse);

        //set up mock for DeregisterGroup
        when(proxy.injectCredentialsAndInvokeV2(
            any(DeregisterPatchBaselineForPatchGroupRequest.class), ArgumentMatchers.<Function<DeregisterPatchBaselineForPatchGroupRequest, DeregisterPatchBaselineForPatchGroupResponse>>any()))
        .thenReturn(mockDeregisterGroupResponse);

        //set up mock for TagHelper.updateTagsForResource()
        doNothing().when(mockTagHelper).updateTagsForResource(
                ArgumentMatchers.<ResourceHandlerRequest<ResourceModel>>any(), any(String.class), any(SsmClient.class), any(AmazonWebServicesClientProxy.class)
        );

    }

    @Test
    public void testUpdateHandler_success(){
        setupSuccessMocks();

        //Invoke updateHandler
        ResourceHandlerRequest<ResourceModel>  request = buildUpdateDefaultInputRequest();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = updateHandler.handleRequest(proxy, request, null, logger);

        verify(proxy).injectCredentialsAndInvokeV2(
                eq(mockUpdatePatchBaselineRequest),
                ArgumentMatchers.<Function<UpdatePatchBaselineRequest, UpdatePatchBaselineResponse>>any());

        verify(proxy).injectCredentialsAndInvokeV2(
                eq(mockGetPatchBaselineRequest),
                ArgumentMatchers.<Function<GetPatchBaselineRequest, GetPatchBaselineResponse>>any());


        List<String> expectedOriginalGroups = new ArrayList<>(Arrays.asList("icecream"));
        List<String> expectedNewGroups = new ArrayList<>(Arrays.asList("foo", "baz"));

        for (String group : expectedOriginalGroups) {
            verify(proxy)
                    .injectCredentialsAndInvokeV2(
                            eq(buildDeregisterGroupRequest(mockGetPatchBaselineResponse.baselineId(), group)),
                            ArgumentMatchers.<Function<DeregisterPatchBaselineForPatchGroupRequest, DeregisterPatchBaselineForPatchGroupResponse>>any());
        }

        for (String group : expectedNewGroups) {
            if (!TestConstants.PATCH_GROUPS.contains(group)) {
                verify(proxy)
                        .injectCredentialsAndInvokeV2(
                                eq(buildRegisterGroupRequest(mockGetPatchBaselineResponse.baselineId(), group)),
                                ArgumentMatchers.<Function<RegisterPatchBaselineForPatchGroupRequest, RegisterPatchBaselineForPatchGroupResponse>>any());
            }
        }

        verify(mockTagHelper).updateTagsForResource(
                ArgumentMatchers.<ResourceHandlerRequest<ResourceModel>>any(), any(String.class), any(SsmClient.class), any(AmazonWebServicesClientProxy.class));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }


    @Test
    public void testUpdateHandler_failure(){
        ResourceHandlerRequest<ResourceModel>  request = buildUpdateDefaultInputRequest();

        mockUpdatePatchBaselineRequest = setUpExpectedUpdatePatchBaselineRequest();
        when(proxy.injectCredentialsAndInvokeV2 (
                eq(mockUpdatePatchBaselineRequest), ArgumentMatchers.<Function<UpdatePatchBaselineRequest,UpdatePatchBaselineResponse>>any()))
                .thenThrow(AwsServiceException.class);

        final ProgressEvent<ResourceModel, CallbackContext> response
                = updateHandler.handleRequest(proxy, request, null, logger);


        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getResourceModel()).isEqualTo(request.getPreviousResourceState());
        assertThat(response.getResourceModel()).isNotEqualTo(request.getDesiredResourceState());
    }

    private UpdatePatchBaselineRequest setUpExpectedUpdatePatchBaselineRequest() {
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
                .approveUntilDate(UPDATED_APPROVE_UNTIL_DATE)
                .complianceLevel(getComplianceString(ComplianceLevel.HIGH))
                .enableNonSecurity(true)
                .build();
        List<PatchRule> patchRuleList = new ArrayList<>(Arrays.asList(patchRule));
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
        List<PatchSource> sourcesList = new ArrayList<PatchSource>(Arrays.asList(ps1, ps2));

        //Build update request. It takes a lot of space because of all the nesting.
        return UpdatePatchBaselineRequest.builder()
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
    }

}
