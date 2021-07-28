package software.amazon.ssm.resourcedatasync;

import software.amazon.awssdk.services.ssm.model.ResourceDataSyncConflictException;
import software.amazon.awssdk.services.ssm.model.ResourceDataSyncInvalidConfigurationException;
import software.amazon.awssdk.services.ssm.model.ResourceDataSyncNotFoundException;
import software.amazon.awssdk.services.ssm.model.UpdateResourceDataSyncRequest;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends TestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private UpdateHandler handler;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        handler = new UpdateHandler();
    }

    @Test
    public void handleRequest_success() {
        final ResourceModel model = createBasicRDSModel();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final CallbackContext inputContext = CallbackContext.builder()
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, inputContext, logger);

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
    public void handleRequest_syncToDestination_UpdateIgnored() {
        final ResourceModel model = createSyncToDestinationRDSModel();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final CallbackContext inputContext = CallbackContext.builder()
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, inputContext, logger);

        verify(proxy, times(0)).injectCredentialsAndInvokeV2(any(UpdateResourceDataSyncRequest.class), any());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }


    /**
     * Test Exception ResourceDataSyncInvalidConfigurationException
     */
    @Test
    public void handleRequest_updateNotStarted_deleteFailure_RDSInvalidConfigurationException() {
        final ResourceModel model = createSyncFromSourceRDSModel(createSyncSourceModel());

        when(proxy.injectCredentialsAndInvokeV2(any(UpdateResourceDataSyncRequest.class), any())).thenThrow(ResourceDataSyncInvalidConfigurationException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnInvalidRequestException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
    }

    /**
     * Test Exception ResourceDataSyncNotFoundException
     */
    @Test
    public void handleRequest_updateNotStarted_deleteFailure_ResourceDataSyncNotFoundException() {
        final ResourceModel model = createSyncFromSourceRDSModel(createSyncSourceModel());

        when(proxy.injectCredentialsAndInvokeV2(any(UpdateResourceDataSyncRequest.class), any())).thenThrow(ResourceDataSyncNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnNotFoundException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
    }

    /**
     * Test Exception ResourceDataSyncConflictException
     */
    @Test
    public void handleRequest_updateNotStarted_deleteFailure_ResourceDataSyncConflictException() {
        final ResourceModel model = createSyncFromSourceRDSModel(createSyncSourceModel());

        when(proxy.injectCredentialsAndInvokeV2(any(UpdateResourceDataSyncRequest.class), any())).thenThrow(ResourceDataSyncConflictException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnResourceConflictException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
    }
}
