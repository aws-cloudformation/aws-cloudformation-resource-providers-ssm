package software.amazon.ssm.maintenancewindow;

import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentMatchers;
import software.amazon.awssdk.services.ssm.model.AddTagsToResourceResponse;
import software.amazon.awssdk.services.ssm.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.ssm.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.ssm.model.RemoveTagsFromResourceResponse;
import software.amazon.awssdk.services.ssm.model.TooManyUpdatesException;
import software.amazon.awssdk.services.ssm.model.UpdateMaintenanceWindowRequest;
import software.amazon.awssdk.services.ssm.model.UpdateMaintenanceWindowResponse;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.ssm.maintenancewindow.translator.ExceptionTranslator;
import software.amazon.ssm.maintenancewindow.translator.resourcemodel.UpdateMaintenanceWindowToResourceModelTranslator;
import software.amazon.ssm.maintenancewindow.translator.request.UpdateMaintenanceWindowTranslator;

import java.util.Collections;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static software.amazon.ssm.maintenancewindow.TestConstants.CONSOLIDATED_RESOURCE_MODEL_AND_STACK_TAGS;
import static software.amazon.ssm.maintenancewindow.TestConstants.RESOURCE_MODEL_TAG_WITHOUT_RESOURCE_TAG;
import static software.amazon.ssm.maintenancewindow.TestConstants.RESOURCE_TAGS;
import static software.amazon.ssm.maintenancewindow.TestConstants.RESOURCE_TYPE;
import static software.amazon.ssm.maintenancewindow.TestConstants.SERVICE_MODEL_TAG_WITHOUT_RESOURCE_TAGS;
import static software.amazon.ssm.maintenancewindow.TestConstants.SERVICE_MODEL_TAG_WITH_RESOURCE_TAGS;
import static software.amazon.ssm.maintenancewindow.TestConstants.SYSTEM_TAGS;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest {

    private static final String WINDOW_ID = "mw-1234567890";
    private static final String CURRENT_WINDOW_NAME = "TestMaintenanceWindow";
    private static final String NEW_WINDOW_NAME = "NewUpdatedMaintenanceWindow";

    private UpdateHandler handler;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @Mock
    UpdateMaintenanceWindowTranslator updateMaintenanceWindowTranslator;

    @Mock
    UpdateMaintenanceWindowToResourceModelTranslator updateMaintenanceWindowToResourceModelTranslator;

    @Mock
    private ExceptionTranslator exceptionTranslator;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        updateMaintenanceWindowTranslator = mock(UpdateMaintenanceWindowTranslator.class);
        updateMaintenanceWindowToResourceModelTranslator = mock(UpdateMaintenanceWindowToResourceModelTranslator.class);
        exceptionTranslator = mock(ExceptionTranslator.class);
        handler = new UpdateHandler(updateMaintenanceWindowTranslator, updateMaintenanceWindowToResourceModelTranslator, exceptionTranslator);
    }

    @Test
    public void handleRequestWithWindowIdPresent() {
        final ResourceModel.ResourceModelBuilder resourceModelBuilder =
                ResourceModel.builder()
                        .windowId(WINDOW_ID)
                        .name(CURRENT_WINDOW_NAME);
        final ResourceModel previousModel = resourceModelBuilder.build();
        final ResourceModel desiredModel = resourceModelBuilder
                .name(NEW_WINDOW_NAME)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredModel)
                .previousResourceState(previousModel)
                .build();

        final UpdateMaintenanceWindowRequest expectedUpdateMaintenanceWindowRequest =
                UpdateMaintenanceWindowRequest.builder()
                        .windowId(desiredModel.getWindowId())
                        .name(desiredModel.getName())
                        .build();

        final UpdateMaintenanceWindowResponse result =
                UpdateMaintenanceWindowResponse.builder()
                        .name(desiredModel.getName())
                        .windowId(desiredModel.getWindowId())
                        .build();

        final ListTagsForResourceRequest expectedListTagsForResourceRequest =
                ListTagsForResourceRequest.builder()
                        .resourceId(WINDOW_ID)
                        .resourceType(RESOURCE_TYPE)
                        .build();

        final ListTagsForResourceResponse listTagsForResourceResponse = ListTagsForResourceResponse.builder().build();
        final RemoveTagsFromResourceResponse removeTagsFromResourceResponse = RemoveTagsFromResourceResponse.builder().build();
        final AddTagsToResourceResponse addTagsToResourceResponse = AddTagsToResourceResponse.builder().build();

        when(updateMaintenanceWindowTranslator.resourceModelToRequest(desiredModel))
                .thenReturn(expectedUpdateMaintenanceWindowRequest);

        when(updateMaintenanceWindowToResourceModelTranslator.updateMaintenanceWindowResponseToResourceModel(result))
                .thenReturn(desiredModel);

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(expectedUpdateMaintenanceWindowRequest),
                        ArgumentMatchers.<Function<UpdateMaintenanceWindowRequest, UpdateMaintenanceWindowResponse>>any()))
                .thenReturn(result);

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(expectedListTagsForResourceRequest),
                        ArgumentMatchers.<Function<ListTagsForResourceRequest, ListTagsForResourceResponse>>any()))
                .thenReturn(listTagsForResourceResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
                ProgressEvent.defaultSuccessHandler(desiredModel);

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    public void handleRequestWithWindowIdAndRemoveResourceTags() {
        final ResourceModel.ResourceModelBuilder resourceModelBuilder =
                ResourceModel.builder()
                        .windowId(WINDOW_ID)
                        .name(CURRENT_WINDOW_NAME);
        final ResourceModel previousModel = resourceModelBuilder.tags(CONSOLIDATED_RESOURCE_MODEL_AND_STACK_TAGS).build();
        final ResourceModel desiredModel = resourceModelBuilder
                .tags(RESOURCE_MODEL_TAG_WITHOUT_RESOURCE_TAG)
                .name(NEW_WINDOW_NAME)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredModel)
                .previousResourceState(previousModel)
                .systemTags(SYSTEM_TAGS)
                .desiredResourceTags(Collections.emptyMap())
                .build();

        final UpdateMaintenanceWindowRequest expectedUpdateMaintenanceWindowRequest =
                UpdateMaintenanceWindowRequest.builder()
                        .windowId(desiredModel.getWindowId())
                        .name(desiredModel.getName())
                        .build();

        final UpdateMaintenanceWindowResponse result =
                UpdateMaintenanceWindowResponse.builder()
                        .name(desiredModel.getName())
                        .windowId(desiredModel.getWindowId())
                        .build();

        final ListTagsForResourceRequest expectedListTagsForResourceRequest =
                ListTagsForResourceRequest.builder()
                        .resourceId(WINDOW_ID)
                        .resourceType(RESOURCE_TYPE)
                        .build();

        final ListTagsForResourceResponse listTagsForResourceResponse = ListTagsForResourceResponse.builder()
                .tagList(SERVICE_MODEL_TAG_WITH_RESOURCE_TAGS).build();

        when(updateMaintenanceWindowTranslator.resourceModelToRequest(desiredModel))
                .thenReturn(expectedUpdateMaintenanceWindowRequest);

        when(updateMaintenanceWindowToResourceModelTranslator.updateMaintenanceWindowResponseToResourceModel(result))
                .thenReturn(desiredModel);

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(expectedUpdateMaintenanceWindowRequest),
                        ArgumentMatchers.<Function<UpdateMaintenanceWindowRequest, UpdateMaintenanceWindowResponse>>any()))
                .thenReturn(result);

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(expectedListTagsForResourceRequest),
                        ArgumentMatchers.<Function<ListTagsForResourceRequest, ListTagsForResourceResponse>>any()))
                .thenReturn(listTagsForResourceResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
                ProgressEvent.defaultSuccessHandler(desiredModel);

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    public void handleRequestWithWindowIdAndAddResourceTags() {
        final ResourceModel.ResourceModelBuilder resourceModelBuilder =
                ResourceModel.builder()
                        .windowId(WINDOW_ID)
                        .name(CURRENT_WINDOW_NAME);
        final ResourceModel previousModel = resourceModelBuilder.tags(RESOURCE_MODEL_TAG_WITHOUT_RESOURCE_TAG).build();
        final ResourceModel desiredModel = resourceModelBuilder
                .tags(CONSOLIDATED_RESOURCE_MODEL_AND_STACK_TAGS)
                .name(NEW_WINDOW_NAME)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredModel)
                .previousResourceState(previousModel)
                .systemTags(SYSTEM_TAGS)
                .desiredResourceTags(RESOURCE_TAGS)
                .build();

        final UpdateMaintenanceWindowRequest expectedUpdateMaintenanceWindowRequest =
                UpdateMaintenanceWindowRequest.builder()
                        .windowId(desiredModel.getWindowId())
                        .name(desiredModel.getName())
                        .build();

        final UpdateMaintenanceWindowResponse result =
                UpdateMaintenanceWindowResponse.builder()
                        .name(desiredModel.getName())
                        .windowId(desiredModel.getWindowId())
                        .build();

        final ListTagsForResourceRequest expectedListTagsForResourceRequest =
                ListTagsForResourceRequest.builder()
                        .resourceId(WINDOW_ID)
                        .resourceType(RESOURCE_TYPE)
                        .build();

        final ListTagsForResourceResponse listTagsForResourceResponse = ListTagsForResourceResponse.builder()
                .tagList(SERVICE_MODEL_TAG_WITHOUT_RESOURCE_TAGS).build();

        when(updateMaintenanceWindowTranslator.resourceModelToRequest(desiredModel))
                .thenReturn(expectedUpdateMaintenanceWindowRequest);

        when(updateMaintenanceWindowToResourceModelTranslator.updateMaintenanceWindowResponseToResourceModel(result))
                .thenReturn(desiredModel);

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(expectedUpdateMaintenanceWindowRequest),
                        ArgumentMatchers.<Function<UpdateMaintenanceWindowRequest, UpdateMaintenanceWindowResponse>>any()))
                .thenReturn(result);

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(expectedListTagsForResourceRequest),
                        ArgumentMatchers.<Function<ListTagsForResourceRequest, ListTagsForResourceResponse>>any()))
                .thenReturn(listTagsForResourceResponse);

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
                .name(NEW_WINDOW_NAME)
                .build();

        final ResourceModel previousModel = ResourceModel.builder()
                .name(CURRENT_WINDOW_NAME)
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
                        .message("WindowId must be present to update the existing maintenance window.")
                        .build();

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(proxy);
        verifyZeroInteractions(updateMaintenanceWindowToResourceModelTranslator);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    void handleRequestThrowsTranslatedServiceException() {
        final ResourceModel.ResourceModelBuilder resourceModelBuilder =
                ResourceModel.builder()
                        .windowId(WINDOW_ID)
                        .name(CURRENT_WINDOW_NAME);

        final ResourceModel previousModel = resourceModelBuilder.build();
        final ResourceModel desiredModel = resourceModelBuilder
                .name(NEW_WINDOW_NAME)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredModel)
                .previousResourceState(previousModel)
                .build();

        final UpdateMaintenanceWindowRequest expectedUpdateMaintenanceWindowRequest =
                UpdateMaintenanceWindowRequest.builder()
                        .windowId(desiredModel.getWindowId())
                        .name(desiredModel.getName())
                        .build();

        when(updateMaintenanceWindowTranslator.resourceModelToRequest(desiredModel))
                .thenReturn(expectedUpdateMaintenanceWindowRequest);

        final TooManyUpdatesException serviceException = TooManyUpdatesException.builder().build();

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(expectedUpdateMaintenanceWindowRequest),
                        ArgumentMatchers.<Function<UpdateMaintenanceWindowRequest, UpdateMaintenanceWindowResponse>>any()))
                .thenThrow(serviceException);

        when(
                exceptionTranslator.translateFromServiceException(
                        serviceException,
                        expectedUpdateMaintenanceWindowRequest))
                .thenReturn(new CfnThrottlingException("UpdateMaintenanceWindow", serviceException));

        Assertions.assertThrows(CfnThrottlingException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });

        verify(exceptionTranslator)
                .translateFromServiceException(
                        serviceException,
                        expectedUpdateMaintenanceWindowRequest);
    }
}
