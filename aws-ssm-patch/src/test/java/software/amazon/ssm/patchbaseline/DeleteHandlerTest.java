package software.amazon.ssm.patchbaseline;

import software.amazon.awssdk.services.ssm.model.*;
import software.amazon.awssdk.services.ssm.model.DeregisterPatchBaselineForPatchGroupRequest;
import software.amazon.awssdk.services.ssm.model.ResourceInUseException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentMatchers;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyZeroInteractions;
import static software.amazon.ssm.patchbaseline.TestConstants.*;
import java.util.function.Function;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest extends TestBase{

    private DeleteHandler deleteHandler;
    private DeletePatchBaselineRequest deletePatchBaselineRequest;
    private GetPatchBaselineRequest getPatchBaselineRequest;
    private GetPatchBaselineResponse getPatchBaselineResponse;
    private DeletePatchBaselineResponse deletePatchBaselineResponse;
    private DeregisterPatchBaselineForPatchGroupResponse deregisterResponse;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Resource resource;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        resource = mock(Resource.class);

        deleteHandler = new DeleteHandler();

        deletePatchBaselineRequest =  DeletePatchBaselineRequest.builder().baselineId(BASELINE_ID).build();
        deletePatchBaselineResponse =  DeletePatchBaselineResponse.builder().baselineId(BASELINE_ID).build();
        getPatchBaselineResponse = GetPatchBaselineResponse.builder().baselineId(BASELINE_ID).patchGroups(PATCH_GROUPS).build();
        deregisterResponse = DeregisterPatchBaselineForPatchGroupResponse.builder().baselineId(BASELINE_ID).build();
    }

    @Test
    public void testSuccess() {
        getPatchBaselineRequest = GetPatchBaselineRequest.builder().baselineId(BASELINE_ID).build();
        when(proxy.injectCredentialsAndInvokeV2(eq(getPatchBaselineRequest),
                ArgumentMatchers.<Function<GetPatchBaselineRequest, GetPatchBaselineResponse>>any()))
                .thenReturn(getPatchBaselineResponse);
        for (String group : getPatchBaselineResponse.patchGroups()) {
            when(proxy.injectCredentialsAndInvokeV2(
                    eq(buildDeregisterGroupRequest(getPatchBaselineResponse.baselineId(), group)),
                    ArgumentMatchers.<Function<DeregisterPatchBaselineForPatchGroupRequest, DeregisterPatchBaselineForPatchGroupResponse>>any())).thenReturn(deregisterResponse);
        }

        //Each delete request sends to SSM (1) get baseline request(n) deregister group requests (1) delete baseline request
        ResourceModel model = ResourceModel.builder().id(BASELINE_ID).build();
        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                            .desiredResourceState(model)
                                                            .clientRequestToken(CLIENT_REQUEST_TOKEN)
                                                            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = deleteHandler.handleRequest(proxy, request, null, logger);
        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
                ProgressEvent.defaultSuccessHandler(null);

        // need to check that the get/deletePatchBaseline was invoked with the correct request made from the model
        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(getPatchBaselineRequest),
                        ArgumentMatchers.<Function<GetPatchBaselineRequest, GetPatchBaselineResponse>>any());

        for (String group : getPatchBaselineResponse.patchGroups()) {
            verify(proxy)
                    .injectCredentialsAndInvokeV2(
                            eq(buildDeregisterGroupRequest(getPatchBaselineResponse.baselineId(), group)),
                            ArgumentMatchers.<Function<DeregisterPatchBaselineForPatchGroupRequest, DeregisterPatchBaselineForPatchGroupResponse>>any());
        }

        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(deletePatchBaselineRequest),
                        ArgumentMatchers.<Function<DeletePatchBaselineRequest, DeletePatchBaselineResponse>>any());

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(resource);
    }

    @Test
    public void testInvalidBaselineId() {
        getPatchBaselineRequest = GetPatchBaselineRequest.builder().baselineId(BAD_BASELINE_ID).build();
        when(proxy.injectCredentialsAndInvokeV2(
                eq(getPatchBaselineRequest),
                ArgumentMatchers.<Function<GetPatchBaselineRequest, GetPatchBaselineResponse>>any())).thenThrow(exception400);

        //Verify handler response when given an invalid baseline id
        ResourceModel model = ResourceModel.builder().id(BAD_BASELINE_ID).build();
        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .clientRequestToken(CLIENT_REQUEST_TOKEN)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response
                = deleteHandler.handleRequest(proxy, request, null, logger);

        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(getPatchBaselineRequest),
                        ArgumentMatchers.<Function<GetPatchBaselineRequest, GetPatchBaselineResponse>>any());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assert(response.getMessage().contains(exception400.getMessage()));
    }

    @Test
    public void testDeleteDefaultBasline() {
        getPatchBaselineRequest = GetPatchBaselineRequest.builder().baselineId(BASELINE_ID).build();
        when(proxy.injectCredentialsAndInvokeV2(eq(getPatchBaselineRequest),
                ArgumentMatchers.<Function<GetPatchBaselineRequest, GetPatchBaselineResponse>>any()))
                .thenReturn(getPatchBaselineResponse);
        when(proxy.injectCredentialsAndInvokeV2(eq(deletePatchBaselineRequest),
                ArgumentMatchers.<Function<DeletePatchBaselineRequest, DeletePatchBaselineResponse>>any()))
                .thenThrow(ResourceInUseException.builder().message("Attempted to delete default patch baseline!").build());
        for (String group : getPatchBaselineResponse.patchGroups()) {
            when(proxy.injectCredentialsAndInvokeV2(
                    eq(buildDeregisterGroupRequest(getPatchBaselineResponse.baselineId(), group)),
                    ArgumentMatchers.<Function<DeregisterPatchBaselineForPatchGroupRequest, DeregisterPatchBaselineForPatchGroupResponse>>any())).thenReturn(deregisterResponse);
        }

        //This test case handles the situation when a user manually sets the default baseline (outside of CFN)
        //  and then tries to delete the template. We want to throw an exception.
        ResourceModel model = ResourceModel.builder().id(BASELINE_ID).patchGroups(PATCH_GROUPS).build();
        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .clientRequestToken(CLIENT_REQUEST_TOKEN)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = deleteHandler.handleRequest(proxy, request, null, logger);

        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(getPatchBaselineRequest),
                        ArgumentMatchers.<Function<GetPatchBaselineRequest, GetPatchBaselineResponse>>any());

        for (String group : getPatchBaselineResponse.patchGroups()) {
            verify(proxy).injectCredentialsAndInvokeV2(
                    eq(buildDeregisterGroupRequest(getPatchBaselineResponse.baselineId(), group)),
                    ArgumentMatchers.<Function<DeregisterPatchBaselineForPatchGroupRequest, DeregisterPatchBaselineForPatchGroupResponse>>any());
        }

        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(deletePatchBaselineRequest),
                        ArgumentMatchers.<Function<DeletePatchBaselineRequest, DeletePatchBaselineResponse>>any());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
    }

    @Test
    public void testDeleteNonexistentBaseline() {
        getPatchBaselineRequest = GetPatchBaselineRequest.builder().baselineId(BASELINE_ID).build();
        when(proxy.injectCredentialsAndInvokeV2(eq(getPatchBaselineRequest),
                ArgumentMatchers.<Function<GetPatchBaselineRequest, GetPatchBaselineResponse>>any()))
                .thenThrow(DoesNotExistException.builder().message("Oops, not exist").build());

        when(proxy.injectCredentialsAndInvokeV2(eq(deletePatchBaselineRequest),
                ArgumentMatchers.<Function<DeletePatchBaselineRequest, DeletePatchBaselineResponse>>any()))
                .thenReturn(deletePatchBaselineResponse);

        // This tests the case where a user deleted a baseline outside of CloudFormation. We count this as successful,
        // otherwise stack deletion won't succeed unless the user specifies to ignore the resource.
        ResourceModel model = ResourceModel.builder().id(BASELINE_ID).build();
        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .clientRequestToken(CLIENT_REQUEST_TOKEN)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = deleteHandler.handleRequest(proxy, request, null, logger);

        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(getPatchBaselineRequest),
                        ArgumentMatchers.<Function<GetPatchBaselineRequest, GetPatchBaselineResponse>>any());

        verify(proxy, never()).injectCredentialsAndInvokeV2(
                    any(DeregisterPatchBaselineForPatchGroupRequest.class), any());

        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(deletePatchBaselineRequest),
                        ArgumentMatchers.<Function<DeletePatchBaselineRequest, DeletePatchBaselineResponse>>any());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
    }

    @Test
    public void testServerError() {
        getPatchBaselineRequest = GetPatchBaselineRequest.builder().baselineId(BASELINE_ID).build();
        when(proxy.injectCredentialsAndInvokeV2(
                eq(getPatchBaselineRequest),
                ArgumentMatchers.<Function<GetPatchBaselineRequest, GetPatchBaselineResponse>>any())).thenThrow(exception500);

        //Verify handler response when we get 5xx error from SSM
        ResourceModel model = ResourceModel.builder().id(BASELINE_ID).build();
        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .clientRequestToken(CLIENT_REQUEST_TOKEN)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response
                = deleteHandler.handleRequest(proxy, request, null, logger);

        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(getPatchBaselineRequest),
                        ArgumentMatchers.<Function<GetPatchBaselineRequest, GetPatchBaselineResponse>>any());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assert(response.getMessage().contains(exception500.getMessage()));
    }
}
