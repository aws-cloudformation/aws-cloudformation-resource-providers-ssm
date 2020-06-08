package software.amazon.ssm.maintenancewindowtarget;

import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentMatchers;
import software.amazon.awssdk.services.ssm.model.TooManyUpdatesException;
import software.amazon.awssdk.services.ssm.model.UpdateMaintenanceWindowTargetRequest;
import software.amazon.awssdk.services.ssm.model.UpdateMaintenanceWindowTargetResponse;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.ssm.maintenancewindowtarget.translator.ExceptionTranslator;
import software.amazon.ssm.maintenancewindowtarget.translator.request.UpdateMaintenanceWindowTargetTranslator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.NEW_NAME;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.NAME;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.WINDOW_ID;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.WINDOW_TARGET_ID;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

    private UpdateHandler handler;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @Mock
    UpdateMaintenanceWindowTargetTranslator updateMaintenanceWindowTargetTranslator;

    @Mock
    private ExceptionTranslator exceptionTranslator;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        updateMaintenanceWindowTargetTranslator = mock(UpdateMaintenanceWindowTargetTranslator.class);
        exceptionTranslator = mock(ExceptionTranslator.class);
        handler = new UpdateHandler(updateMaintenanceWindowTargetTranslator, exceptionTranslator);
    }

    @Test
    public void handleRequestWithWindowIdPresent(){
        final ResourceModel.ResourceModelBuilder resourceModelBuilder =
            ResourceModel.builder()
                .name(NAME)
                .windowId(WINDOW_ID)
                .windowTargetId(WINDOW_TARGET_ID);
        final ResourceModel previousModel = resourceModelBuilder.build();
        final ResourceModel desiredModel = resourceModelBuilder
            .name(NEW_NAME)
            .windowId(WINDOW_ID)
            .windowTargetId(WINDOW_TARGET_ID)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(desiredModel)
            .previousResourceState(previousModel)
            .build();

        final UpdateMaintenanceWindowTargetRequest expectedUpdateMaintenanceWindowTargetRequest =
            UpdateMaintenanceWindowTargetRequest.builder()
                .windowId(desiredModel.getWindowId())
                .name(desiredModel.getName())
                .build();

        final UpdateMaintenanceWindowTargetResponse result =
            UpdateMaintenanceWindowTargetResponse.builder()
                .name(desiredModel.getName())
                .windowId(desiredModel.getWindowId())
                .build();

        when(updateMaintenanceWindowTargetTranslator.resourceModelToRequest(desiredModel))
            .thenReturn(expectedUpdateMaintenanceWindowTargetRequest);

        when(updateMaintenanceWindowTargetTranslator.responseToResourceModel(result))
            .thenReturn(desiredModel);

        when(
            proxy.injectCredentialsAndInvokeV2(
                eq(expectedUpdateMaintenanceWindowTargetRequest),
                ArgumentMatchers.<Function<UpdateMaintenanceWindowTargetRequest, UpdateMaintenanceWindowTargetResponse>>any()))
            .thenReturn(result);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
            ProgressEvent.defaultSuccessHandler(desiredModel);

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    void handleRequestWithNoWindowId() {
        final ResourceModel desiredModel = ResourceModel.builder()
            .name(NEW_NAME)
            .build();

        final ResourceModel previousModel = ResourceModel.builder()
            .name(NAME)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(desiredModel)
            .previousResourceState(previousModel)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
            ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(previousModel)
                .status(OperationStatus.FAILED)
                .errorCode(HandlerErrorCode.InvalidRequest)
                .message("Both WindowId and WindowTargetId must be present to update the existing maintenance window target.")
                .build();

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(proxy);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    void handleRequestThrowsTranslatedServiceException() {
        final ResourceModel.ResourceModelBuilder resourceModelBuilder =
            ResourceModel.builder()
                .name(NAME)
                .windowId(WINDOW_ID)
                .windowTargetId(WINDOW_TARGET_ID);

        final ResourceModel previousModel = resourceModelBuilder.build();
        final ResourceModel desiredModel = resourceModelBuilder
            .name(NEW_NAME)
            .windowId(WINDOW_ID)
            .windowTargetId(WINDOW_TARGET_ID)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(desiredModel)
            .previousResourceState(previousModel)
            .build();

        final UpdateMaintenanceWindowTargetRequest expectedUpdateMaintenanceWindowTargetRequest =
            UpdateMaintenanceWindowTargetRequest.builder()
                .windowId(desiredModel.getWindowId())
                .name(desiredModel.getName())
                .build();

        when(updateMaintenanceWindowTargetTranslator.resourceModelToRequest(desiredModel))
            .thenReturn(expectedUpdateMaintenanceWindowTargetRequest);

        final TooManyUpdatesException serviceException = TooManyUpdatesException.builder().build();

        when(
            proxy.injectCredentialsAndInvokeV2(
                eq(expectedUpdateMaintenanceWindowTargetRequest),
                ArgumentMatchers.<Function<UpdateMaintenanceWindowTargetRequest, UpdateMaintenanceWindowTargetResponse>>any()))
            .thenThrow(serviceException);

        when(
            exceptionTranslator.translateFromServiceException(
                serviceException,
                expectedUpdateMaintenanceWindowTargetRequest))
            .thenReturn(new CfnThrottlingException("UpdateMaintenanceWindow", serviceException));

        Assertions.assertThrows(CfnThrottlingException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });

        verify(exceptionTranslator)
            .translateFromServiceException(
                serviceException,
                expectedUpdateMaintenanceWindowTargetRequest);
    }
}
