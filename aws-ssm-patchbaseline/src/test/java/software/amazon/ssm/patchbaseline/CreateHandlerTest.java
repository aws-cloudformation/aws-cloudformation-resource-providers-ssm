package software.amazon.ssm.patchbaseline;

import software.amazon.awssdk.services.ssm.model.CreatePatchBaselineRequest;
import software.amazon.awssdk.services.ssm.model.CreatePatchBaselineResponse;
import software.amazon.awssdk.services.ssm.model.RegisterPatchBaselineForPatchGroupRequest;
import software.amazon.awssdk.services.ssm.model.RegisterPatchBaselineForPatchGroupResponse;
import software.amazon.awssdk.services.ssm.model.RegisterDefaultPatchBaselineRequest;
import software.amazon.awssdk.services.ssm.model.RegisterDefaultPatchBaselineResponse;
import software.amazon.awssdk.services.ssm.model.PatchFilter;
import software.amazon.awssdk.services.ssm.model.PatchFilterGroup;
import software.amazon.awssdk.services.ssm.model.PatchSource;
import software.amazon.awssdk.services.ssm.model.PatchAction;
import software.amazon.awssdk.services.ssm.model.PatchRuleGroup;
import software.amazon.awssdk.services.ssm.model.PatchRule;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.awssdk.services.ssm.model.AlreadyExistsException;
import software.amazon.awssdk.services.ssm.model.ResourceLimitExceededException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.ssm.patchbaseline.utils.SsmCfnClientSideException;
import static software.amazon.ssm.patchbaseline.TestConstants.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.atLeastOnce;
import org.mockito.ArgumentMatchers;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.function.Function;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * test all 5 responses from the create handler.
 */
@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends TestBase {

    private CreatePatchBaselineRequest createPatchBaselineRequest;
    private CreatePatchBaselineRequest createPatchBaselineRequestMissingName;
    private CreatePatchBaselineRequest.Builder createPatchBaselineRequestBuilder;
    private CreatePatchBaselineResponse createPatchBaselineResponse;
    private RegisterPatchBaselineForPatchGroupResponse registerResponse;
    private RegisterDefaultPatchBaselineRequest registerDefaultPatchBaselineRequest;
    private RegisterDefaultPatchBaselineResponse registerDefaultPatchBaselineResponse;
    private List<Tag> tagsList;

    @InjectMocks
    private CreateHandler createHandler;
    @Mock
    private TagHelper cfnTagHelper;
    @Mock
    private AmazonWebServicesClientProxy proxy;
    @Mock
    private Resource resource;

    @BeforeEach
    public void setup() {
        cfnTagHelper = mock(TagHelper.class);
        createHandler = new CreateHandler(cfnTagHelper);

        createPatchBaselineRequestBuilder = setUpCreatePatchBaselineRequestBuilder();

        createPatchBaselineResponse =  CreatePatchBaselineResponse.builder()
                .baselineId(BASELINE_ID)
                .build();
        registerResponse = RegisterPatchBaselineForPatchGroupResponse.builder()
                .baselineId(BASELINE_ID)
                .build();
        registerDefaultPatchBaselineRequest = RegisterDefaultPatchBaselineRequest.builder()
                .baselineId(BASELINE_ID)
                .build();
        registerDefaultPatchBaselineResponse = RegisterDefaultPatchBaselineResponse.builder()
                .baselineId(BASELINE_ID)
                .build();
    }

    @Test
    public void testSuccess() {
        // set up request
        ResourceHandlerRequest<ResourceModel>  request = buildDefaultInputRequest();

        mockValidateAndMergeTagsForCreate_Success(request);
        mockInvokeCreatePatchBaseline_Success();

        //mock registerPatchBaselineForPatchGroup
        for (String group : PATCH_GROUPS) {
            when(proxy.injectCredentialsAndInvokeV2(
                    eq(buildRegisterGroupRequest(createPatchBaselineResponse.baselineId(), group)),
                    ArgumentMatchers.<Function<RegisterPatchBaselineForPatchGroupRequest, RegisterPatchBaselineForPatchGroupResponse>>any())).thenReturn(registerResponse);
        }

        when(proxy.injectCredentialsAndInvokeV2(
                eq(registerDefaultPatchBaselineRequest),
                ArgumentMatchers.<Function<RegisterDefaultPatchBaselineRequest, RegisterDefaultPatchBaselineResponse>>any())).thenReturn(registerDefaultPatchBaselineResponse);

        //Invoke the handler
        final ProgressEvent<ResourceModel, CallbackContext> response
                = createHandler.handleRequest(proxy, request, null, logger);

        // need to check that the createPatchBaseline was invoked with the correct request made from the model
        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(createPatchBaselineRequest),
                        ArgumentMatchers.<Function<CreatePatchBaselineRequest, CreatePatchBaselineResponse>>any());

        for (String group : PATCH_GROUPS) {
            verify(proxy)
                    .injectCredentialsAndInvokeV2(
                            eq(buildRegisterGroupRequest(createPatchBaselineResponse.baselineId(), group)),
                            ArgumentMatchers.<Function<RegisterPatchBaselineForPatchGroupRequest, RegisterPatchBaselineForPatchGroupResponse>>any());
        }

        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
                ProgressEvent.defaultSuccessHandler(request.getDesiredResourceState());

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(resource);
    }


    @Test
    public void testMissingFieldInRequest() {

        ResourceHandlerRequest<ResourceModel>  request = buildDefaultInputRequest();

        mockValidateAndMergeTagsForCreate_Success(request);

        createPatchBaselineRequestMissingName = createPatchBaselineRequestBuilder.build();
        when(proxy.injectCredentialsAndInvokeV2(
                eq(createPatchBaselineRequestMissingName),
                ArgumentMatchers.<Function<CreatePatchBaselineRequest, CreatePatchBaselineResponse>>any())).thenThrow(exception400);

        //This test is a little different in the sense that we want the handler to send a request with a missing name
        //  to verify the handlers error-catching behavior.
        ArgumentCaptor<CreatePatchBaselineRequest> captor = ArgumentCaptor.forClass(CreatePatchBaselineRequest.class);

        request.getDesiredResourceState().setName(null);

        final ProgressEvent<ResourceModel, CallbackContext> response
                = createHandler.handleRequest(proxy, request, null, logger);

        verify(proxy, atLeastOnce())
                .injectCredentialsAndInvokeV2(
                        captor.capture(),
                        ArgumentMatchers.<Function<CreatePatchBaselineRequest, CreatePatchBaselineResponse>>any());

        final List<CreatePatchBaselineRequest> capturedValues = typeCheckedValues(captor.getAllValues(), CreatePatchBaselineRequest.class);
        assertThat(capturedValues.size()).isEqualTo(1);

        final CreatePatchBaselineRequest actualCreatePatchBaselineRequest = capturedValues.get(0);

        verify(proxy, never()).injectCredentialsAndInvokeV2(
                any(RegisterPatchBaselineForPatchGroupRequest.class), any());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assert(response.getMessage().contains(exception400.getMessage()));
        assertThat(actualCreatePatchBaselineRequest).isEqualTo(createPatchBaselineRequestMissingName);
    }

    @Test
    public void testResourceLimitsExceeded() {

        ResourceHandlerRequest<ResourceModel>  request = buildDefaultInputRequest();

        mockValidateAndMergeTagsForCreate_Success(request);

        createPatchBaselineRequest = createPatchBaselineRequestBuilder.name(BASELINE_NAME).build();
        when(proxy.injectCredentialsAndInvokeV2(
                eq(createPatchBaselineRequest),
                ArgumentMatchers.<Function<CreatePatchBaselineRequest, CreatePatchBaselineResponse>>any())).thenThrow(ResourceLimitExceededException.builder().message("limit exceeded").build());

        //We want to verify that the create handler sends the appropriate response when the user has too many baselines
        final ProgressEvent<ResourceModel, CallbackContext> response
                = createHandler.handleRequest(proxy, request, null, logger);

        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(createPatchBaselineRequest),
                        ArgumentMatchers.<Function<CreatePatchBaselineRequest, CreatePatchBaselineResponse>>any());

        verify(proxy, never()).injectCredentialsAndInvokeV2(
                any(RegisterPatchBaselineForPatchGroupRequest.class), any());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assert(response.getMessage().contains("limit exceeded"));
    }

    @Test
    public void testGroupAlreadyRegistered() {
        ResourceHandlerRequest<ResourceModel>  request = buildDefaultInputRequest();

        mockValidateAndMergeTagsForCreate_Success(request);

        createPatchBaselineRequest = createPatchBaselineRequestBuilder.name(BASELINE_NAME).build();

        when(proxy.injectCredentialsAndInvokeV2(
                eq(createPatchBaselineRequest),
                ArgumentMatchers.<Function<CreatePatchBaselineRequest, CreatePatchBaselineResponse>>any())).thenThrow(AlreadyExistsException.builder().message("already registered!").build());

        //We want to verify the handlers response to when there is already a baseline registered to a specific group.
        final ProgressEvent<ResourceModel, CallbackContext> response
                = createHandler.handleRequest(proxy, request, null, logger);

        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(createPatchBaselineRequest),
                        ArgumentMatchers.<Function<CreatePatchBaselineRequest, CreatePatchBaselineResponse>>any());

        verify(proxy, never()).injectCredentialsAndInvokeV2(
                any(RegisterPatchBaselineForPatchGroupRequest.class), any());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assert (response.getMessage().contains("already registered!"));
    }

    @Test
    public void testTooManyPatchGroups() {
        ResourceHandlerRequest<ResourceModel>  request = buildDefaultInputRequest();

        mockValidateAndMergeTagsForCreate_Success(request);
        mockInvokeCreatePatchBaseline_Success();

        when(proxy.injectCredentialsAndInvokeV2(
                any(RegisterPatchBaselineForPatchGroupRequest.class), any())).thenThrow(ResourceLimitExceededException.builder().message("Too many patch groups!").build());;

        final ProgressEvent<ResourceModel, CallbackContext> response
                = createHandler.handleRequest(proxy, request, null, logger);

        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(createPatchBaselineRequest),
                        ArgumentMatchers.<Function<CreatePatchBaselineRequest, CreatePatchBaselineResponse>>any());

        for (String group : PATCH_GROUPS) {
            verify(proxy).injectCredentialsAndInvokeV2(
                    eq(buildRegisterGroupRequest(createPatchBaselineResponse.baselineId(), group)),
                    ArgumentMatchers.<Function<RegisterPatchBaselineForPatchGroupRequest, RegisterPatchBaselineForPatchGroupResponse>>any());
            break; //Simulate an exception while adding the patch group
        }

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assert (response.getMessage().contains("Too many patch groups!"));
    }

    @Test
    public void testServerError() {

        ResourceHandlerRequest<ResourceModel>  request = buildDefaultInputRequest();

        mockValidateAndMergeTagsForCreate_Success(request);

        createPatchBaselineRequest = createPatchBaselineRequestBuilder.name(BASELINE_NAME).build();
        when(proxy.injectCredentialsAndInvokeV2(
                eq(createPatchBaselineRequest),
                ArgumentMatchers.<Function<CreatePatchBaselineRequest, CreatePatchBaselineResponse>>any())).thenThrow(exception500);


        // verify the handlers response when SSM returns a 5xx error.
        final ProgressEvent<ResourceModel, CallbackContext> response
                = createHandler.handleRequest(proxy, request, null, logger);

        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(createPatchBaselineRequest),
                        ArgumentMatchers.<Function<CreatePatchBaselineRequest, CreatePatchBaselineResponse>>any());

        verify(proxy, never()).injectCredentialsAndInvokeV2(
                any(RegisterPatchBaselineForPatchGroupRequest.class), any());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assert (response.getMessage().contains("Server error"));
    }

    @Test
    public void testSsmCfnClientSideException() {
        when(cfnTagHelper.validateAndMergeTagsForCreate(any(), any())).thenThrow(new SsmCfnClientSideException("Bad data"));

        ResourceHandlerRequest<ResourceModel>  request = buildDefaultInputRequest();
        final ProgressEvent<ResourceModel, CallbackContext> response
                = createHandler.handleRequest(proxy, request, null, logger);

        verifyZeroInteractions(proxy);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assert (response.getMessage().contains("Bad data"));
    }


    private void mockInvokeCreatePatchBaseline_Success() {
        //mock createPatchBaseline
        createPatchBaselineRequest = createPatchBaselineRequestBuilder.name(BASELINE_NAME).build();
        when(proxy.injectCredentialsAndInvokeV2(eq(createPatchBaselineRequest),
                ArgumentMatchers.<Function<CreatePatchBaselineRequest, CreatePatchBaselineResponse>>any()))
                .thenReturn(createPatchBaselineResponse);
    }

    private void mockValidateAndMergeTagsForCreate_Success(ResourceHandlerRequest<ResourceModel> request) {
        //mock validateAndMergeTagsForCreate
        when(cfnTagHelper.validateAndMergeTagsForCreate(request, request.getDesiredResourceState().getTags()))
                .thenReturn(tagsList);
    }


    private static <T> List<T> typeCheckedValues(List<T> values, Class<T> clazz) {
        final List<T> typeCheckedValues = new ArrayList<>();
        for (final T value : values) {
            if (clazz.isInstance(value)) {
                typeCheckedValues.add(value);
            }
        }
        return typeCheckedValues;
    }

    private CreatePatchBaselineRequest.Builder setUpCreatePatchBaselineRequestBuilder() {
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
                .approveUntilDate(APPROVE_UNTIL_DATE)
                .complianceLevel(getComplianceString(ComplianceLevel.HIGH))
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
        List<PatchSource> sourcesList = new ArrayList<>(Arrays.asList(ps1, ps2));
        Tag tag1 = Tag.builder().key(CFN_KEY).value(CFN_VALUE).build();
        Tag tag2 = Tag.builder().key(TAG_KEY).value(TAG_VALUE).build();
        Tag tag3 = Tag.builder().key(SYSTEM_TAG_KEY).value(BASELINE_NAME).build();
        tagsList = new ArrayList<>(Arrays.asList(tag1, tag2, tag3));

        return CreatePatchBaselineRequest.builder()
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
                        .tags(tagsList);
    }

}
