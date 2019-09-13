package com.aws.ssm.association;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.HandlerErrorCode;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import com.amazonaws.services.simplesystemsmanagement.model.AssociationDoesNotExistException;
import com.amazonaws.services.simplesystemsmanagement.model.DeleteAssociationRequest;
import com.amazonaws.services.simplesystemsmanagement.model.DeleteAssociationResult;
import com.amazonaws.services.simplesystemsmanagement.model.InternalServerErrorException;
import com.amazonaws.services.simplesystemsmanagement.model.InvalidDocumentException;
import com.amazonaws.services.simplesystemsmanagement.model.TooManyUpdatesException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest {

    private static final String ASSOCIATION_ID = "test-12345-associationId";
    private static final String INSTANCE_ID = "i-1234abcd";
    private static final String DOCUMENT_NAME = "TestDocument";

    final DeleteHandler handler = new DeleteHandler();
    @Mock
    private AmazonWebServicesClientProxy proxy;
    @Mock
    private Logger logger;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
    }

    @Test
    public void handleRequestWithAssociationId() {

        final ResourceModel model = ResourceModel.builder()
            .associationId(ASSOCIATION_ID)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequestWithInstanceIdAndDocumentName() {

        final ResourceModel model = ResourceModel.builder()
            .instanceId(INSTANCE_ID)
            .name(DOCUMENT_NAME)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequestWithNoRequiredParametersPresent() {

        final ResourceModel model = ResourceModel.builder()
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(model);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage())
            .isEqualTo("AssociationId, or InstanceId and Document Name must be specified to delete an association.");
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);
    }

    @Test
    public void handleRequestWhenAssociationDoesNotExist() {

        final ResourceModel model = ResourceModel.builder()
            .associationId(ASSOCIATION_ID)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final AssociationDoesNotExistException serviceException =
            new AssociationDoesNotExistException("Requested association does not exist.");

        when(
            proxy.injectCredentialsAndInvoke(
                any(DeleteAssociationRequest.class),
                ArgumentMatchers.<Function<DeleteAssociationRequest, DeleteAssociationResult>>any()))
            .thenThrow(serviceException);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

    }

    @Test
    public void handleRequestWhenTooManyUpdatesHappened() {

        final ResourceModel model = ResourceModel.builder()
            .associationId(ASSOCIATION_ID)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final TooManyUpdatesException serviceException =
            new TooManyUpdatesException("Too many updates happened at the same time; try again later.");

        when(
            proxy.injectCredentialsAndInvoke(
                any(DeleteAssociationRequest.class),
                ArgumentMatchers.<Function<DeleteAssociationRequest, DeleteAssociationResult>>any()))
            .thenThrow(serviceException);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(model);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isEqualTo(serviceException.getMessage());
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.Throttling);
    }

    @Test
    public void handleRequestWhenDocumentDoesNotExist() {

        final ResourceModel model = ResourceModel.builder()
            .instanceId(INSTANCE_ID)
            .name(DOCUMENT_NAME)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final InvalidDocumentException serviceException =
            new InvalidDocumentException("Document name provided does not exist");

        when(
            proxy.injectCredentialsAndInvoke(
                any(DeleteAssociationRequest.class),
                ArgumentMatchers.<Function<DeleteAssociationRequest, DeleteAssociationResult>>any()))
            .thenThrow(serviceException);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(model);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isEqualTo(serviceException.getMessage());
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);
    }

    @Test
    public void handleRequestWhenInternalServerErrorHappened() {

        final ResourceModel model = ResourceModel.builder()
            .associationId(ASSOCIATION_ID)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final InternalServerErrorException serviceException =
            new InternalServerErrorException("Something unexpected occurred on the server.");

        when(
            proxy.injectCredentialsAndInvoke(
                any(DeleteAssociationRequest.class),
                ArgumentMatchers.<Function<DeleteAssociationRequest, DeleteAssociationResult>>any()))
            .thenThrow(serviceException);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(model);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isEqualTo(serviceException.getMessage());
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.ServiceInternalError);
    }

    @Test
    public void handleRequestWhenUnexpectedExceptionHappened() {

        final ResourceModel model = ResourceModel.builder()
            .associationId(ASSOCIATION_ID)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final IllegalArgumentException serviceException =
            new IllegalArgumentException("Unknown failure occurred.");

        when(
            proxy.injectCredentialsAndInvoke(
                any(DeleteAssociationRequest.class),
                ArgumentMatchers.<Function<DeleteAssociationRequest, DeleteAssociationResult>>any()))
            .thenThrow(serviceException);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(model);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isEqualTo(serviceException.getMessage());
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.GeneralServiceException);
    }
}
