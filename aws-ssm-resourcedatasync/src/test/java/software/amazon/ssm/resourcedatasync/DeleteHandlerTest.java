package software.amazon.ssm.resourcedatasync;

import software.amazon.awssdk.services.ssm.model.DeleteResourceDataSyncRequest;
import software.amazon.awssdk.services.ssm.model.LastResourceDataSyncStatus;
import software.amazon.awssdk.services.ssm.model.ListResourceDataSyncRequest;
import software.amazon.awssdk.services.ssm.model.ListResourceDataSyncResponse;
import software.amazon.awssdk.services.ssm.model.ResourceDataSyncNotFoundException;
import software.amazon.awssdk.services.ssm.model.ResourceDataSyncInvalidConfigurationException;

import software.amazon.awssdk.services.ssm.model.ResourceDataSyncItem;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest extends TestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private DeleteHandler handler;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        handler = new DeleteHandler();
    }

    @Test
    public void handleRequest_deletionNotStarted_inProgress() {
        final ResourceModel model = createBasicRDSModel();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final CallbackContext inputContext = CallbackContext.builder()
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, inputContext, logger);

        final CallbackContext outputContext = CallbackContext.builder()
                .deleteResourceDataSyncStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_RESOURCE_DATA_SYNC_DELETE_POLL_RETRIES)
                .build();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackContext()).isEqualToComparingFieldByField(outputContext);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(INITIAL_DELETE_CALLBACK_DELAY_SECONDS);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    /**
     * Test simple delete RDS success Case.
     */
    @Test
    public void handleRequest_success() {
        final ResourceModel model = createBasicRDSModel();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final CallbackContext inputContext = CallbackContext.builder()
                .deleteResourceDataSyncStarted(true)
                .deleteResourceDataSyncStabilized(true)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, inputContext, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(ZERO);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    /**
     * Test create RDS flow from OperationStatus:started to OperationStatus: inProgress
     * with createResourceDataSyncStabilized = true after one retry.
     */
    @Test
    public void handleRequest_deletionStarted_inProgress_notYetStabilized() {
        final ResourceModel model = createBasicRDSModel();

        final ResourceDataSyncItem resourceDataSyncItem = ResourceDataSyncItem.builder()
                .syncName(model.getSyncName())
                .lastStatus(LastResourceDataSyncStatus.SUCCESSFUL)
                .build();

        final ListResourceDataSyncResponse listResourceDataSyncResponse = ListResourceDataSyncResponse.builder()
                .resourceDataSyncItems(resourceDataSyncItem)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(proxy.injectCredentialsAndInvokeV2(any(ListResourceDataSyncRequest.class), any())).thenReturn(listResourceDataSyncResponse);

        final CallbackContext inputContext = CallbackContext.builder()
                .deleteResourceDataSyncStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_RESOURCE_DATA_SYNC_DELETE_POLL_RETRIES)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, inputContext, logger);

        final CallbackContext outputContext = CallbackContext.builder()
                .deleteResourceDataSyncStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_RESOURCE_DATA_SYNC_DELETE_POLL_RETRIES - 1)
                .deleteResourceDataSyncStabilized(false)
                .build();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackContext()).isEqualToComparingFieldByField(outputContext);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(DELETE_CALLBACK_DELAY_SECONDS);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

    }

    /**
     * Test create RDS flow created -> not stabilized -> retry-1 -> RDS createSuccess -> stabilized
     */
    @Test
    public void handleRequest_deletionComplete_success_stabilized() {
        final ResourceModel model = createBasicRDSModel();

        final ListResourceDataSyncResponse listResourceDataSyncResponse = ListResourceDataSyncResponse.builder()
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(proxy.injectCredentialsAndInvokeV2(any(ListResourceDataSyncRequest.class), any())).thenReturn(listResourceDataSyncResponse);

        final CallbackContext inputContext = CallbackContext.builder()
                .deleteResourceDataSyncStarted(true)
                .stabilizationRetriesRemaining(NUMBER_OF_RESOURCE_DATA_SYNC_CREATE_POLL_RETRIES)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, inputContext, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualToComparingFieldByField(model);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

    }

    /**
     * Test Exception ResourceDataSyncNotFoundException
     */
    @Test
    public void handleRequest_deletionNotStarted_deleteFailure_RDSNotFoundException() {
        final ResourceModel model = createBasicRDSModel();

        when(proxy.injectCredentialsAndInvokeV2(any(DeleteResourceDataSyncRequest.class), any())).thenThrow(ResourceDataSyncNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnNotFoundException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
    }

    /**
     * Test Exception ResourceDataSyncInvalidConfigurationException
     */
    @Test
    public void handleRequest_deletionNotStarted_deleteFailure_RDSInvalidConfigurationException() {
        final ResourceModel model = createBasicRDSModel();

        when(proxy.injectCredentialsAndInvokeV2(any(DeleteResourceDataSyncRequest.class), any())).thenThrow(ResourceDataSyncInvalidConfigurationException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnInvalidRequestException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
    }

    /**
     * Test no retry remaining
     */
    @Test
    public void handleRequest_creationStarted_zeroRetries_stabilizationFailed() {
        final ResourceModel model = createBasicRDSModel();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final CallbackContext inputContext = CallbackContext.builder()
                .deleteResourceDataSyncStarted(true)
                .deleteResourceDataSyncStabilized(false)
                .stabilizationRetriesRemaining(ZERO)
                .build();

        assertThrows(CfnNotStabilizedException.class, () -> {
            handler.handleRequest(proxy, request, inputContext, logger);
        });
    }


}