package software.amazon.ssm.patchbaseline;


import org.junit.jupiter.api.BeforeAll;
import software.amazon.ssm.patchbaseline.Resource;
import software.amazon.awssdk.services.ssm.model.*;
import software.amazon.awssdk.services.ssm.model.PatchFilter;
import software.amazon.awssdk.services.ssm.model.PatchFilterGroup;
import software.amazon.awssdk.services.ssm.model.PatchSource;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.awssdk.services.ssm.model.ResourceLimitExceededException;
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
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.eq;
import static software.amazon.ssm.patchbaseline.TestConstants.*;


import org.mockito.ArgumentMatchers;
import org.mockito.ArgumentCaptor;

import java.util.*;
import java.util.function.Function;

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
                .rejectedPatchesAction(software.amazon.awssdk.services.ssm.model.PatchAction.BLOCK)
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

         ProgressEvent<ResourceModel, CallbackContext> response
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

    @Test
    public void testEmptyTagsSuccess() {

        List<software.amazon.ssm.patchbaseline.Tag> tags = new ArrayList<>();
        List<software.amazon.ssm.patchbaseline.PatchSource> sources = sources();
        software.amazon.ssm.patchbaseline.PatchFilterGroup globalFilters = globalFilters();
        software.amazon.ssm.patchbaseline.RuleGroup approvalRules = approvalRules();

        Map<String, String> desiredResourceTagsMap = new HashMap<>();

        Map<String, String> systemTagsMap = new HashMap<>();

        ResourceModel model = buildDefaultInputModel(tags, sources, globalFilters, approvalRules,
                BASELINE_ID, BASELINE_NAME, OPERATING_SYSTEM, BASELINE_DESCRIPTION,
                REJECTED_PATCHES, getPatchActionString(TestConstants.PatchAction.BLOCK),
                ACCEPTED_PATCHES, getComplianceString(ComplianceLevel.CRITICAL), PATCH_GROUPS);
        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceTags(desiredResourceTagsMap)
                .desiredResourceState(model)
                .systemTags(systemTagsMap)
                .clientRequestToken(TestConstants.CLIENT_REQUEST_TOKEN)
                .build();

        System.out.print(String.format("Create Request getClientRequestToken %s %n", request.getClientRequestToken()));

        ProgressEvent<ResourceModel, CallbackContext> response
                = createHandler.handleRequest(proxy, request, null, logger);

        System.out.print(String.format("Create Handler Response Status %s %n", response.getStatus()));

        response = createHandler.handleRequest(proxy, request, null, logger);
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        verifyZeroInteractions(resource);
    }

    @Test
    public void testMissingFieldInRequest() {
        when(proxy.injectCredentialsAndInvokeV2(any(CreatePatchBaselineRequest.class),
                ArgumentMatchers.<Function<CreatePatchBaselineRequest, CreatePatchBaselineResponse>>any()))
                .thenThrow(exception400);

        //This test is a little different in the sense that we want the handler to send a request with a missing name
        //  to verify the handlers error-catching behavior.
        ArgumentCaptor<CreatePatchBaselineRequest> captor = ArgumentCaptor.forClass(CreatePatchBaselineRequest.class);
        List<software.amazon.ssm.patchbaseline.Tag> tags = new ArrayList<>();
        List<software.amazon.ssm.patchbaseline.PatchSource> sources = sources();
        software.amazon.ssm.patchbaseline.PatchFilterGroup globalFilters = globalFilters();
        software.amazon.ssm.patchbaseline.RuleGroup approvalRules = approvalRules();

        Map<String, String> desiredResourceTagsMap = new HashMap<>();

        Map<String, String> systemTagsMap = new HashMap<>();

        ResourceModel model = buildDefaultInputModel(tags, sources, globalFilters, approvalRules,
                BASELINE_ID, BASELINE_NAME, OPERATING_SYSTEM, BASELINE_DESCRIPTION,
                REJECTED_PATCHES, getPatchActionString(TestConstants.PatchAction.BLOCK),
                ACCEPTED_PATCHES, getComplianceString(ComplianceLevel.CRITICAL), PATCH_GROUPS);
        ResourceHandlerRequest<ResourceModel>  request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceTags(desiredResourceTagsMap)
                .desiredResourceState(model)
                .systemTags(systemTagsMap)
                .clientRequestToken(TestConstants.CLIENT_REQUEST_TOKEN)
                .build();
        ProgressEvent<ResourceModel, CallbackContext> response = createHandler.handleRequest(proxy, request, null, logger);

        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        captor.capture(),
                        ArgumentMatchers.<Function<CreatePatchBaselineRequest, CreatePatchBaselineResponse>>any());

        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(1).isEqualTo( captor.getValue().rejectedPatches().size());

        verifyZeroInteractions(resource);
    }
    @Test
    public void testResourceLimitsExceeded() {
        when(proxy.injectCredentialsAndInvokeV2(any(CreatePatchBaselineRequest.class),
                ArgumentMatchers.<Function<CreatePatchBaselineRequest, CreatePatchBaselineResponse>>any()))
                .thenThrow( ResourceLimitExceededException.builder().message("Limits Exceeded").build());
        ArgumentCaptor<CreatePatchBaselineRequest> captor = ArgumentCaptor.forClass(CreatePatchBaselineRequest.class);

        //We want to verify that the create handler sends the appropriate response when the user has too many baselines
        ResourceHandlerRequest<ResourceModel> request = buildDefaultInputRequest();
        ProgressEvent<ResourceModel, CallbackContext> response = createHandler.handleRequest(proxy,request, null, logger);

        verify(proxy)
                //.injectCredentialsAndInvokeV2(any(CreatePatchBaselineRequest.class),
                  //      ArgumentMatchers.<Function<CreatePatchBaselineRequest, CreatePatchBaselineResponse>>any());
            .injectCredentialsAndInvokeV2(captor.capture(),
                    ArgumentMatchers.<Function<CreatePatchBaselineRequest, CreatePatchBaselineResponse>>any());

            verify(proxy, never())
                    .injectCredentialsAndInvokeV2(
                            eq(buildRegisterGroupRequest(createPatchBaselineResponse.baselineId(),"any")),
                            ArgumentMatchers.<Function<RegisterPatchBaselineForPatchGroupRequest, RegisterPatchBaselineForPatchGroupResponse>>any());


        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat (response.getMessage().contains("Limits Exceeded"));
        verifyZeroInteractions(resource);
    }
    @Test
    public void testGroupAlreadyRegistered() {
        when(proxy.injectCredentialsAndInvokeV2(any(CreatePatchBaselineRequest.class),
                ArgumentMatchers.<Function<CreatePatchBaselineRequest, CreatePatchBaselineResponse>>any()))
                .thenThrow( AlreadyExistsException.builder().message("already registered!").build());

        //We want to verify the handlers response to when there is already a baseline registered to a specific group.
        ResourceHandlerRequest<ResourceModel>  request = buildDefaultInputRequest();
        ProgressEvent<ResourceModel, CallbackContext>  response = createHandler.handleRequest(proxy,request, null, logger);

        verify(proxy)
                .injectCredentialsAndInvokeV2(any(CreatePatchBaselineRequest.class),
                        ArgumentMatchers.<Function<CreatePatchBaselineRequest, CreatePatchBaselineResponse>>any());


        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);

        assertThat (response.getMessage().contains("already registered!"));
    }
    @Test
    public void testServerError() {
        when(proxy.injectCredentialsAndInvokeV2(any(CreatePatchBaselineRequest.class),
                ArgumentMatchers.<Function<CreatePatchBaselineRequest, CreatePatchBaselineResponse>>any()))
                .thenThrow(exception500);

        //Finally, verify the handlers response when SSM returns a 5xx error.
        ResourceHandlerRequest<ResourceModel>  request = buildDefaultInputRequest();

        System.out.print(String.format("Create Request getClientRequestToken %s %n", request.getClientRequestToken()));

        ProgressEvent<ResourceModel, CallbackContext> response
                = createHandler.handleRequest(proxy, request, null, logger);

        System.out.print(String.format("Create Handler Response Status %s %n", response.getStatus()));

        // need to check that the createPatchBaseline was invoked with the correct request made from the model
        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(createPatchBaselineRequest),
                        ArgumentMatchers.<Function<CreatePatchBaselineRequest, CreatePatchBaselineResponse>>any());
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat (response.getMessage().contains("Server error"));
    }
   
}

}
