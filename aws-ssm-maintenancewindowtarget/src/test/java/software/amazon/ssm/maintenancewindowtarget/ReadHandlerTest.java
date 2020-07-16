package software.amazon.ssm.maintenancewindowtarget;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import software.amazon.awssdk.services.ssm.model.DescribeMaintenanceWindowTargetsRequest;
import software.amazon.awssdk.services.ssm.model.DescribeMaintenanceWindowTargetsResponse;
import software.amazon.awssdk.services.ssm.model.DoesNotExistException;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.MaintenanceWindowTarget;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.ssm.maintenancewindowtarget.translator.ExceptionTranslator;
import software.amazon.ssm.maintenancewindowtarget.translator.request.GetMaintenanceWindowTargetTranslator;
import software.amazon.ssm.maintenancewindowtarget.util.ResourceHandlerRequestToStringConverter;
import software.amazon.awssdk.services.ssm.model.MaintenanceWindowFilter;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.DESCRIPTION;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.LOGGED_RESOURCE_HANDLER_REQUEST;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.NAME;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.NEXT_TOKEN;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.OWNER_INFORMATION;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.RESOURCE_TYPE;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.SERVICE_TARGETS;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.WINDOW_ID;
import static software.amazon.ssm.maintenancewindowtarget.TestsInputs.WINDOW_TARGET_ID;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest {
    private ReadHandler handler;

    private static final MaintenanceWindowFilter windowTargetIdFilter = MaintenanceWindowFilter.builder()
            .key("WindowTargetId")
            .values(ImmutableList.of(WINDOW_TARGET_ID))
            .build();

    private static final MaintenanceWindowFilter emptyFilter = MaintenanceWindowFilter.builder()
            .build();

    private static final MaintenanceWindowTarget successMaintenanceWindowTarget = MaintenanceWindowTarget.builder()
            .description(DESCRIPTION)
            .name(NAME)
            .ownerInformation(OWNER_INFORMATION)
            .resourceType(RESOURCE_TYPE)
            .targets(SERVICE_TARGETS)
            .windowId(WINDOW_ID)
            .windowTargetId(WINDOW_TARGET_ID)
            .build();

    private static final DescribeMaintenanceWindowTargetsRequest describeMaintenanceWindowTargetsRequest =
            DescribeMaintenanceWindowTargetsRequest.builder()
                    .windowId(WINDOW_ID)
                    .build();

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @Mock
    private GetMaintenanceWindowTargetTranslator getMaintenanceWindowTargetTranslator;

    @Mock
    private ExceptionTranslator exceptionTranslator;

    @Mock
    private ResourceHandlerRequestToStringConverter requestToStringConverter;

    @BeforeEach
    public void setup() {
        when(requestToStringConverter.convert(any())).thenReturn(LOGGED_RESOURCE_HANDLER_REQUEST);

        handler = new ReadHandler(getMaintenanceWindowTargetTranslator,
                exceptionTranslator,
                requestToStringConverter);
    }

    public void handleReadRequestWithRequiredParametersPresent() {
        final ResourceModel model = ResourceModel.builder()
                .windowId(WINDOW_ID)
                .windowTargetId(WINDOW_TARGET_ID)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final DescribeMaintenanceWindowTargetsRequest expectedGetMaintenanceWindowTargetRequest =
                DescribeMaintenanceWindowTargetsRequest.builder()
                        .windowId(model.getWindowId())
                        .filters(windowTargetIdFilter)
                        .build();

        final DescribeMaintenanceWindowTargetsResponse result = DescribeMaintenanceWindowTargetsResponse.builder()
                .nextToken(NEXT_TOKEN)
                .targets(successMaintenanceWindowTarget)
                .build();

        when(getMaintenanceWindowTargetTranslator.resourceModelToRequest(model))
                .thenReturn(describeMaintenanceWindowTargetsRequest);

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(expectedGetMaintenanceWindowTargetRequest),
                        ArgumentMatchers.<Function<DescribeMaintenanceWindowTargetsRequest, DescribeMaintenanceWindowTargetsResponse>>any()))
                .thenReturn(result);

        final ResourceModel expectedModel = request.getDesiredResourceState();
        //expectedModel.setName(NAME);

        when(getMaintenanceWindowTargetTranslator.responseToResourceModel(result))
                .thenReturn(expectedModel);

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
                ProgressEvent.defaultSuccessHandler(expectedModel);

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    public void handleReadRequestWithNoRequiredParametersPresent(){
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
                        .message("Both WindowId and WindowTargetId must be present to get an existing maintenance window target.")
                        .build();

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(proxy);
        verifyZeroInteractions(getMaintenanceWindowTargetTranslator);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    public void handleReadRequestThrowsDoesNotExistsException() {
        final ResourceModel model = ResourceModel.builder()
                .windowId(WINDOW_ID)
                .windowTargetId(WINDOW_TARGET_ID)
                .build();

        final DoesNotExistException doesNotExistsException = DoesNotExistException.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(getMaintenanceWindowTargetTranslator.resourceModelToRequest(model))
                .thenReturn(describeMaintenanceWindowTargetsRequest);

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(describeMaintenanceWindowTargetsRequest),
                        ArgumentMatchers.<Function<DescribeMaintenanceWindowTargetsRequest, DescribeMaintenanceWindowTargetsResponse>>any()))
                .thenThrow(doesNotExistsException);


        when(exceptionTranslator.translateFromServiceException(doesNotExistsException, describeMaintenanceWindowTargetsRequest, request.getDesiredResourceState()))
                .thenReturn(new CfnNotFoundException(doesNotExistsException));

        Assertions.assertThrows(CfnNotFoundException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
        verify(exceptionTranslator)
                .translateFromServiceException(doesNotExistsException, describeMaintenanceWindowTargetsRequest,request.getDesiredResourceState());
    }

    @Test
    void handleReadRequestThrowsTranslatedServiceException() {
        final ResourceModel model = ResourceModel.builder()
                .windowId(WINDOW_ID)
                .windowTargetId(WINDOW_TARGET_ID)
                .build();

        when(getMaintenanceWindowTargetTranslator.resourceModelToRequest(model))
                .thenReturn(describeMaintenanceWindowTargetsRequest);

        final InternalServerErrorException serviceException = InternalServerErrorException.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(describeMaintenanceWindowTargetsRequest),
                        ArgumentMatchers.<Function<DescribeMaintenanceWindowTargetsRequest, DescribeMaintenanceWindowTargetsResponse>>any()))
                .thenThrow(serviceException);

        when(exceptionTranslator.translateFromServiceException(serviceException, describeMaintenanceWindowTargetsRequest, request.getDesiredResourceState()))
                .thenReturn(new CfnServiceInternalErrorException(describeMaintenanceWindowTargetsRequest.toString(), serviceException));

        Assertions.assertThrows(CfnServiceInternalErrorException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });

        verify(exceptionTranslator)
                .translateFromServiceException(serviceException, describeMaintenanceWindowTargetsRequest, request.getDesiredResourceState());
    }
}
