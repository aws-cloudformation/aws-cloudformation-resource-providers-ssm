package software.amazon.ssm.maintenancewindow;

import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentMatchers;
import software.amazon.awssdk.services.ssm.model.GetMaintenanceWindowRequest;
import software.amazon.awssdk.services.ssm.model.GetMaintenanceWindowResponse;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.ssm.maintenancewindow.translator.ExceptionTranslator;
import software.amazon.ssm.maintenancewindow.translator.resourcemodel.GetMaintenanceWindowTranslator;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.anyString;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest {

    private static final String WINDOW_ID = "mw-1234567890";
    private static final String NAME = "TestMaintenanceWindow";

    private ReadHandler handler;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @Mock
    private GetMaintenanceWindowTranslator getMaintenanceWindowTranslator;

    @Mock
    private ExceptionTranslator exceptionTranslator;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        getMaintenanceWindowTranslator = mock(GetMaintenanceWindowTranslator.class);
        exceptionTranslator = mock(ExceptionTranslator.class);
        logger = mock(Logger.class);
        handler = new ReadHandler(getMaintenanceWindowTranslator, exceptionTranslator);
    }

    @Test
    void handleRequestWithWindowId() {
        final ResourceModel model = ResourceModel.builder()
                .windowId(WINDOW_ID)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final GetMaintenanceWindowRequest expectedGetMaintenanceWindowRequest =
                GetMaintenanceWindowRequest.builder()
                        .windowId(model.getWindowId())
                        .build();

        final GetMaintenanceWindowResponse result =
                GetMaintenanceWindowResponse.builder()
                        .windowId(model.getWindowId())
                        .name(NAME)
                        .build();

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(expectedGetMaintenanceWindowRequest),
                        ArgumentMatchers.<Function<GetMaintenanceWindowRequest, GetMaintenanceWindowResponse>>any()))
                .thenReturn(result);

        final ResourceModel expectedModel = request.getDesiredResourceState();
        expectedModel.setName(NAME);

        when(getMaintenanceWindowTranslator.getMaintenanceWindowResponseToResourceModel(result))
                .thenReturn(expectedModel);

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
                ProgressEvent.defaultSuccessHandler(expectedModel);

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    void handleRequestWithNoWindowId() {
        final ResourceModel model = ResourceModel.builder()
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
                ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .status(OperationStatus.FAILED)
                        .errorCode(HandlerErrorCode.InvalidRequest)
                        .message("WindowId must be present to read the existing maintenance window.")
                        .build();

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(proxy);
        verifyZeroInteractions(getMaintenanceWindowTranslator);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    void handleRequestThrowsTranslatedServiceException() {
        final ResourceModel model = ResourceModel.builder()
                .windowId(WINDOW_ID)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final GetMaintenanceWindowRequest expectedGetMaintenanceWindowRequest =
                GetMaintenanceWindowRequest.builder()
                        .windowId(model.getWindowId())
                        .build();

        final InternalServerErrorException serviceException = InternalServerErrorException.builder().build();

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(expectedGetMaintenanceWindowRequest),
                        ArgumentMatchers.<Function<GetMaintenanceWindowRequest, GetMaintenanceWindowResponse>>any()))
                .thenThrow(serviceException);

        when(
                exceptionTranslator.translateFromServiceException(
                        serviceException,
                        expectedGetMaintenanceWindowRequest))
                .thenReturn(new CfnServiceInternalErrorException("GetMaintenanceWindow", serviceException));

        Assertions.assertThrows(CfnServiceInternalErrorException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });

        verify(exceptionTranslator)
                .translateFromServiceException(
                        serviceException,
                        expectedGetMaintenanceWindowRequest);
        verify(logger).log(anyString());
    }
}
