package software.amazon.ssm.maintenancewindowtarget;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import software.amazon.awssdk.services.ssm.model.RegisterTargetWithMaintenanceWindowRequest;
import software.amazon.awssdk.services.ssm.model.RegisterTargetWithMaintenanceWindowResponse;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.ProgressEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.ssm.maintenancewindowtarget.translator.request.RegisterTargetWithMaintenanceWindowTranslator;
import software.amazon.ssm.maintenancewindowtarget.translator.ExceptionTranslator;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        exceptionTranslator = mock(ExceptionTranslator.class);
        registerTargetWithMaintenanceWindowTranslator = mock(RegisterTargetWithMaintenanceWindowTranslator.class);
        handler = new CreateHandler(registerTargetWithMaintenanceWindowTranslator, exceptionTranslator);
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
        expectedModel.setWindowTargetId(WINDOW_TARGET_ID);

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

        when(exceptionTranslator.translateFromServiceException(serviceException, registerTargetWithMaintenanceWindowRequest))
            .thenReturn(new CfnServiceInternalErrorException("RegisterTargetWithMaintenanceWindow", serviceException));

        Assertions.assertThrows(CfnServiceInternalErrorException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });

        verify(exceptionTranslator)
            .translateFromServiceException(serviceException, registerTargetWithMaintenanceWindowRequest);

    }
}
