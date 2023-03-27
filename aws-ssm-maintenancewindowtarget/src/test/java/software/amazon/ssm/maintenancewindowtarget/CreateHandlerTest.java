package software.amazon.ssm.maintenancewindowtarget;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import software.amazon.awssdk.services.ssm.model.DoesNotExistException;
import software.amazon.awssdk.services.ssm.model.FeatureNotAvailableException;
import software.amazon.awssdk.services.ssm.model.RegisterTargetWithMaintenanceWindowRequest;
import software.amazon.awssdk.services.ssm.model.RegisterTargetWithMaintenanceWindowResponse;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.ResourceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.ssm.maintenancewindowtarget.util.ResourceHandlerRequestToStringConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.ssm.maintenancewindowtarget.translator.request.RegisterTargetWithMaintenanceWindowTranslator;
import software.amazon.ssm.maintenancewindowtarget.translator.ExceptionTranslator;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.DESCRIPTION;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.NAME;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.OWNER_INFORMATION;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.RESOURCE_TYPE;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.MODEL_TARGETS;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.SERVICE_TARGETS;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.WINDOW_ID;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.WINDOW_TARGET_ID;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.LOGGED_RESOURCE_HANDLER_REQUEST;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest {

    private static final ResourceModel model = ResourceModel.builder()
        .description(DESCRIPTION)
        .name(NAME)
        .ownerInformation(OWNER_INFORMATION)
        .resourceType(RESOURCE_TYPE)
        .targets(MODEL_TARGETS)
        .windowId(WINDOW_ID)
        .build();

    private static final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
        .desiredResourceState(model)
        .build();

    private static final RegisterTargetWithMaintenanceWindowRequest registerTargetWithMaintenanceWindowRequest =
        RegisterTargetWithMaintenanceWindowRequest.builder()
            .description(model.getDescription())
            .name(model.getName())
            .ownerInformation(model.getOwnerInformation())
            .resourceType(model.getResourceType())
            .targets(SERVICE_TARGETS)
            .windowId(model.getWindowId())
            .build();

    private CreateHandler handler;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @Mock
    private ExceptionTranslator exceptionTranslator;

    @Mock
    private RegisterTargetWithMaintenanceWindowTranslator registerTargetWithMaintenanceWindowTranslator;

    @Mock
    private ResourceHandlerRequestToStringConverter requestToStringConverter;

    @BeforeEach
    public void setup() {
        when(requestToStringConverter.convert(any())).thenReturn(LOGGED_RESOURCE_HANDLER_REQUEST);

        handler = new CreateHandler(registerTargetWithMaintenanceWindowTranslator,
                exceptionTranslator,
                requestToStringConverter);
    }

    @Test
    public void handleCreateRequestForSuccess() {
        when(registerTargetWithMaintenanceWindowTranslator.resourceModelToRequest(model))
            .thenReturn(registerTargetWithMaintenanceWindowRequest);

        final RegisterTargetWithMaintenanceWindowResponse result =
            RegisterTargetWithMaintenanceWindowResponse.builder()
                .windowTargetId(WINDOW_TARGET_ID)
                .build();

        when(
            proxy.injectCredentialsAndInvokeV2(
                eq(registerTargetWithMaintenanceWindowRequest),
                ArgumentMatchers.<Function<RegisterTargetWithMaintenanceWindowRequest, RegisterTargetWithMaintenanceWindowResponse>>any()))
            .thenReturn(result);

        final ResourceModel expectedModel = request.getDesiredResourceState();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
            ProgressEvent.defaultSuccessHandler(expectedModel);

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    void handleCreateRequestThrowsTranslatedServiceException() {

        when(registerTargetWithMaintenanceWindowTranslator.resourceModelToRequest(model))
            .thenReturn(registerTargetWithMaintenanceWindowRequest);

        final InternalServerErrorException serviceException = InternalServerErrorException.builder().build();

        when(
            proxy.injectCredentialsAndInvokeV2(
                eq(registerTargetWithMaintenanceWindowRequest),
                ArgumentMatchers.<Function<RegisterTargetWithMaintenanceWindowRequest, RegisterTargetWithMaintenanceWindowResponse>>any()))
            .thenThrow(serviceException);

        when(exceptionTranslator.translateFromServiceException(serviceException, registerTargetWithMaintenanceWindowRequest, request.getDesiredResourceState()))
            .thenReturn(new CfnServiceInternalErrorException(registerTargetWithMaintenanceWindowRequest.toString(), serviceException));

        Assertions.assertThrows(CfnServiceInternalErrorException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });

        verify(exceptionTranslator)
            .translateFromServiceException(serviceException, registerTargetWithMaintenanceWindowRequest, request.getDesiredResourceState());

    }

    @Test
    public void handleCreateRequestThrowsDoesNotExistsException() {
        when(registerTargetWithMaintenanceWindowTranslator.resourceModelToRequest(model))
                .thenReturn(registerTargetWithMaintenanceWindowRequest);

        final DoesNotExistException doesNotExistsException = DoesNotExistException.builder().build();

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(registerTargetWithMaintenanceWindowRequest),
                        ArgumentMatchers.<Function<RegisterTargetWithMaintenanceWindowRequest, RegisterTargetWithMaintenanceWindowResponse>>any()))
                .thenThrow(doesNotExistsException);

        when(exceptionTranslator.translateFromServiceException(doesNotExistsException, registerTargetWithMaintenanceWindowRequest, request.getDesiredResourceState()))
                .thenReturn(new CfnNotFoundException(doesNotExistsException));

        Assertions.assertThrows(CfnNotFoundException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
        verify(exceptionTranslator)
                .translateFromServiceException(doesNotExistsException, registerTargetWithMaintenanceWindowRequest, request.getDesiredResourceState());
    }

    @Test
    public void handleCreateRequestThrowsInvalidRequestException(){
        when(registerTargetWithMaintenanceWindowTranslator.resourceModelToRequest(model))
                .thenReturn(registerTargetWithMaintenanceWindowRequest);

        final FeatureNotAvailableException featureNotAvailableException = FeatureNotAvailableException.builder().build();

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(registerTargetWithMaintenanceWindowRequest),
                        ArgumentMatchers.<Function<RegisterTargetWithMaintenanceWindowRequest, RegisterTargetWithMaintenanceWindowResponse>>any()))
                .thenThrow(featureNotAvailableException);

        when(exceptionTranslator.translateFromServiceException(featureNotAvailableException, registerTargetWithMaintenanceWindowRequest, request.getDesiredResourceState()))
                .thenReturn(new CfnInvalidRequestException(registerTargetWithMaintenanceWindowRequest.toString(),featureNotAvailableException));

        Assertions.assertThrows(CfnInvalidRequestException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
        verify(exceptionTranslator)
                .translateFromServiceException(featureNotAvailableException, registerTargetWithMaintenanceWindowRequest, request.getDesiredResourceState());
    }

    @Test
    public void handleCreateRequestThrowsResourceLimitExceedException(){
        when(registerTargetWithMaintenanceWindowTranslator.resourceModelToRequest(model))
                .thenReturn(registerTargetWithMaintenanceWindowRequest);

        final ResourceLimitExceededException resourceLimitExceededException = ResourceLimitExceededException.builder().build();

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(registerTargetWithMaintenanceWindowRequest),
                        ArgumentMatchers.<Function<RegisterTargetWithMaintenanceWindowRequest, RegisterTargetWithMaintenanceWindowResponse>>any()))
                .thenThrow(resourceLimitExceededException);

        when(exceptionTranslator.translateFromServiceException(resourceLimitExceededException, registerTargetWithMaintenanceWindowRequest, request.getDesiredResourceState()))
                .thenReturn(new CfnServiceLimitExceededException(resourceLimitExceededException));

        Assertions.assertThrows(CfnServiceLimitExceededException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
        verify(exceptionTranslator)
                .translateFromServiceException(resourceLimitExceededException, registerTargetWithMaintenanceWindowRequest, request.getDesiredResourceState());
    }
}
