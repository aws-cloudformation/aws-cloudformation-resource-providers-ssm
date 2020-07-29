package software.amazon.ssm.maintenancewindowtarget;

import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentMatchers;
import software.amazon.awssdk.services.ssm.model.DoesNotExistException;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.UpdateMaintenanceWindowTargetRequest;
import software.amazon.awssdk.services.ssm.model.UpdateMaintenanceWindowTargetResponse;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
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
import software.amazon.ssm.maintenancewindowtarget.util.ResourceHandlerRequestToStringConverter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.UPDATED_NAME;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.NAME;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.WINDOW_ID;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.WINDOW_TARGET_ID;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.LOGGED_RESOURCE_HANDLER_REQUEST;

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

    @Mock
    private ResourceHandlerRequestToStringConverter requestToStringConverter;

    @BeforeEach
    public void setup() {
        when(requestToStringConverter.convert(any())).thenReturn(LOGGED_RESOURCE_HANDLER_REQUEST);

        handler = new UpdateHandler(updateMaintenanceWindowTargetTranslator,
                exceptionTranslator,
                requestToStringConverter);
    }

    @Test
    public void handleUpdateRequestWithRequiredParametersPresent(){
        final ResourceModel.ResourceModelBuilder resourceModelBuilder =
            ResourceModel.builder()
                .name(NAME)
                .windowId(WINDOW_ID)
                .windowTargetId(WINDOW_TARGET_ID);

        final ResourceModel previousModel = resourceModelBuilder.build();

        final ResourceModel desiredModel = resourceModelBuilder
            .name(UPDATED_NAME)
            .windowId(WINDOW_ID)
            .windowTargetId(WINDOW_TARGET_ID)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(desiredModel)
            .previousResourceState(previousModel)
            .build();

        final UpdateMaintenanceWindowTargetRequest expectedUpdateMaintenanceWindowTargetRequest =
            UpdateMaintenanceWindowTargetRequest.builder()
                .name(desiredModel.getName())
                .windowId(desiredModel.getWindowId())
                .windowTargetId(desiredModel.getWindowTargetId())
                .build();

        final UpdateMaintenanceWindowTargetResponse result =
            UpdateMaintenanceWindowTargetResponse.builder()
                .name(desiredModel.getName())
                .windowId(desiredModel.getWindowId())
                .windowTargetId(desiredModel.getWindowTargetId())
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
    void handleUpdateRequestWithoutWindowId() {
        final ResourceModel desiredModel = ResourceModel.builder()
            .name(UPDATED_NAME)
            .windowTargetId(WINDOW_TARGET_ID)
            .build();

        final ResourceModel previousModel = ResourceModel.builder()
            .name(NAME)
            .windowTargetId(WINDOW_TARGET_ID)
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
    void handleUpdateRequestWithoutWindowTargetId() {
        final ResourceModel desiredModel = ResourceModel.builder()
            .name(UPDATED_NAME)
            .windowId(WINDOW_ID)
            .build();

        final ResourceModel previousModel = ResourceModel.builder()
            .name(NAME)
            .windowId(WINDOW_ID)
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
    void handleUpdateRequestWithNoRequiredParametersPresent() {
        final ResourceModel desiredModel = ResourceModel.builder()
            .name(UPDATED_NAME)
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
    void handleUpdateRequestThrowsTranslatedServiceException() {
        final ResourceModel.ResourceModelBuilder resourceModelBuilder =
            ResourceModel.builder()
                .name(NAME)
                .windowId(WINDOW_ID)
                .windowTargetId(WINDOW_TARGET_ID);

        final ResourceModel previousModel = resourceModelBuilder.build();
        final ResourceModel desiredModel = resourceModelBuilder
            .name(UPDATED_NAME)
            .windowId(WINDOW_ID)
            .windowTargetId(WINDOW_TARGET_ID)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(desiredModel)
            .previousResourceState(previousModel)
            .build();

        final UpdateMaintenanceWindowTargetRequest expectedUpdateMaintenanceWindowTargetRequest =
            UpdateMaintenanceWindowTargetRequest.builder()
                .name(desiredModel.getName())
                .windowId(desiredModel.getWindowId())
                .windowTargetId(desiredModel.getWindowTargetId())
                .build();

        when(updateMaintenanceWindowTargetTranslator.resourceModelToRequest(desiredModel))
            .thenReturn(expectedUpdateMaintenanceWindowTargetRequest);

        final InternalServerErrorException serviceException = InternalServerErrorException.builder().build();

        when(
            proxy.injectCredentialsAndInvokeV2(
                eq(expectedUpdateMaintenanceWindowTargetRequest),
                ArgumentMatchers.<Function<UpdateMaintenanceWindowTargetRequest, UpdateMaintenanceWindowTargetResponse>>any()))
            .thenThrow(serviceException);

        when(
            exceptionTranslator.translateFromServiceException(
                serviceException,
                expectedUpdateMaintenanceWindowTargetRequest,
                request.getDesiredResourceState()))
            .thenReturn(new CfnThrottlingException(expectedUpdateMaintenanceWindowTargetRequest.toString(), serviceException));

        Assertions.assertThrows(CfnThrottlingException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });

        verify(exceptionTranslator)
            .translateFromServiceException(
                serviceException,
                expectedUpdateMaintenanceWindowTargetRequest,
                request.getDesiredResourceState());
    }

    @Test
    public void handleUpdateRequestThrowsDoesNotExistsException() {
        final ResourceModel.ResourceModelBuilder resourceModelBuilder =
                ResourceModel.builder()
                        .name(NAME)
                        .windowId(WINDOW_ID)
                        .windowTargetId(WINDOW_TARGET_ID);

        final ResourceModel previousModel = resourceModelBuilder.build();
        final ResourceModel desiredModel = resourceModelBuilder
                .name(NAME)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredModel)
                .previousResourceState(previousModel)
                .build();

        final UpdateMaintenanceWindowTargetRequest expectedUpdateMaintenanceWindowTargetRequest =
                UpdateMaintenanceWindowTargetRequest.builder()
                        .name(desiredModel.getName())
                        .windowId(desiredModel.getWindowId())
                        .windowTargetId(desiredModel.getWindowTargetId())
                        .build();

        when(updateMaintenanceWindowTargetTranslator.resourceModelToRequest(desiredModel))
                .thenReturn(expectedUpdateMaintenanceWindowTargetRequest);

        final DoesNotExistException doesNotExistsException = DoesNotExistException.builder().build();

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(expectedUpdateMaintenanceWindowTargetRequest),
                        ArgumentMatchers.<Function<UpdateMaintenanceWindowTargetRequest, UpdateMaintenanceWindowTargetResponse>>any()))
                .thenThrow(doesNotExistsException);


        when(exceptionTranslator.translateFromServiceException(doesNotExistsException, expectedUpdateMaintenanceWindowTargetRequest, request.getDesiredResourceState()))
                .thenReturn(new CfnNotFoundException(doesNotExistsException));

        Assertions.assertThrows(CfnNotFoundException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
        verify(exceptionTranslator)
                .translateFromServiceException(doesNotExistsException, expectedUpdateMaintenanceWindowTargetRequest,request.getDesiredResourceState());
    }
}
