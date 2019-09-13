package com.aws.ssm.association;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.HandlerErrorCode;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import com.amazonaws.services.simplesystemsmanagement.model.AssociationDescription;
import com.amazonaws.services.simplesystemsmanagement.model.AssociationDoesNotExistException;
import com.amazonaws.services.simplesystemsmanagement.model.DescribeAssociationRequest;
import com.amazonaws.services.simplesystemsmanagement.model.DescribeAssociationResult;
import com.amazonaws.services.simplesystemsmanagement.model.InternalServerErrorException;
import com.amazonaws.services.simplesystemsmanagement.model.InvalidAssociationVersionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest {

    private static final String DOCUMENT_NAME = "TestDocument";
    private static final String ASSOCIATION_ID = "test-12345-associationId";

    private ReadHandler handler = new ReadHandler();
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
    public void handleRequestSimpleSuccess() {

        final ResourceModel model = ResourceModel.builder()
            .associationId(ASSOCIATION_ID)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final AssociationDescription associationDescription =
            new AssociationDescription()
                .withAssociationId(ASSOCIATION_ID)
                .withName(DOCUMENT_NAME);

        final DescribeAssociationResult result =
            new DescribeAssociationResult()
                .withAssociationDescription(associationDescription);

        when(
            proxy.injectCredentialsAndInvoke(
                any(DescribeAssociationRequest.class),
                ArgumentMatchers.<Function<DescribeAssociationRequest, DescribeAssociationResult>>any()))
            .thenReturn(result);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        ResourceModel expectedModel = request.getDesiredResourceState();
        expectedModel.setName(DOCUMENT_NAME);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
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
            new AssociationDoesNotExistException("Requested association does not exist");

        when(
            proxy.injectCredentialsAndInvoke(
                any(DescribeAssociationRequest.class),
                ArgumentMatchers.<Function<DescribeAssociationRequest, DescribeAssociationResult>>any()))
            .thenThrow(serviceException);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isEqualTo(serviceException.getMessage());
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotFound);
    }

    @Test
    public void handleRequestWhenAssociationVersionInvalid() {

        final ResourceModel model = ResourceModel.builder()
            .associationId(ASSOCIATION_ID)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final InvalidAssociationVersionException serviceException =
            new InvalidAssociationVersionException("Requested association version is invalid");

        when(
            proxy.injectCredentialsAndInvoke(
                any(DescribeAssociationRequest.class),
                ArgumentMatchers.<Function<DescribeAssociationRequest, DescribeAssociationResult>>any()))
            .thenThrow(serviceException);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
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
            new InternalServerErrorException("Internal server error occurred.");

        when(
            proxy.injectCredentialsAndInvoke(
                any(DescribeAssociationRequest.class),
                ArgumentMatchers.<Function<DescribeAssociationRequest, DescribeAssociationResult>>any()))
            .thenThrow(serviceException);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isEqualTo(serviceException.getMessage());
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.ServiceInternalError);
    }

    @Test
    public void handleRequestWhenUnknownExceptionHappened() {

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
                any(DescribeAssociationRequest.class),
                ArgumentMatchers.<Function<DescribeAssociationRequest, DescribeAssociationResult>>any()))
            .thenThrow(serviceException);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        verify(logger).log(eq(serviceException.getMessage()));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isEqualTo(serviceException.getMessage());
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.GeneralServiceException);
    }
}
