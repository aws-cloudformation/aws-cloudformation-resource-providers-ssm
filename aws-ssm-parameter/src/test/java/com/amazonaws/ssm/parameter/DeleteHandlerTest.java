package com.amazonaws.ssm.parameter;

import com.amazonaws.AmazonServiceException;
import org.junit.jupiter.api.AfterEach;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DeleteParameterRequest;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<SsmClient> proxySsmClient;

    @Mock
    SsmClient ssmClient;

    private DeleteHandler handler;

    private ResourceModel RESOURCE_MODEL;

    @BeforeEach
    public void setup() {
        handler = new DeleteHandler();
        ssmClient = mock(SsmClient.class);
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxySsmClient = MOCK_PROXY(proxy, ssmClient);

        RESOURCE_MODEL = ResourceModel.builder()
                .description(DESCRIPTION)
                .name(NAME)
                .value(VALUE)
                .type(TYPE_STRING)
                .tags(TAG_SET)
                .build();
    }

    @AfterEach
    public void post_execute() {
        verify(ssmClient, atLeastOnce()).serviceName();
        verifyNoMoreInteractions(proxySsmClient.client());
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .clientRequestToken("token")
                .desiredResourceTags(TAG_SET)
                .systemTags(SYSTEM_TAGS_SET)
                .desiredResourceState(RESOURCE_MODEL)
                .logicalResourceIdentifier("logicalId").build();
        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxySsmClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxySsmClient.client()).deleteParameter(any(DeleteParameterRequest.class));
    }

    @Test
    public void handleRequest_Failure_ParameterNotFound() {
        when(proxySsmClient.client().deleteParameter(any(DeleteParameterRequest.class)))
                .thenThrow(ParameterNotFoundException.builder().build());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .clientRequestToken("token")
                .desiredResourceTags(TAG_SET)
                .systemTags(SYSTEM_TAGS_SET)
                .desiredResourceState(RESOURCE_MODEL)
                .logicalResourceIdentifier("logicalId").build();

        try {
            handler.handleRequest(proxy, request, new CallbackContext(), proxySsmClient, logger);
        } catch (CfnNotFoundException ex) {
            assertThat(ex).isInstanceOf(CfnNotFoundException.class);
        }

        verify(proxySsmClient.client()).deleteParameter(any(DeleteParameterRequest.class));
    }

    @Test
    public void handleRequest_ThrottlingException() {
        AmazonServiceException amazonServiceException = new AmazonServiceException("Client error");
        amazonServiceException.setStatusCode(429);
        amazonServiceException.setErrorCode("ThrottlingException");

        when(proxySsmClient.client().deleteParameter(any(DeleteParameterRequest.class)))
                .thenThrow(amazonServiceException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .clientRequestToken("token")
                .desiredResourceTags(TAG_SET)
                .systemTags(SYSTEM_TAGS_SET)
                .desiredResourceState(RESOURCE_MODEL)
                .logicalResourceIdentifier("logical_id").build();

        try {
            handler.handleRequest(proxy, request, new CallbackContext(), proxySsmClient, logger);
        } catch (CfnThrottlingException ex) {
            assertThat(ex).isInstanceOf(CfnThrottlingException.class);
        }

        verify(proxySsmClient.client()).deleteParameter(any(DeleteParameterRequest.class));
    }

    @Test
    public void handleRequest_NonThrottlingAmazonServiceException() {
        AmazonServiceException amazonServiceException = new AmazonServiceException("Client error");
        amazonServiceException.setStatusCode(500);

        when(proxySsmClient.client().deleteParameter(any(DeleteParameterRequest.class)))
                .thenThrow(amazonServiceException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .clientRequestToken("token")
                .desiredResourceTags(TAG_SET)
                .systemTags(SYSTEM_TAGS_SET)
                .desiredResourceState(RESOURCE_MODEL)
                .logicalResourceIdentifier("logical_id").build();

        try {
            handler.handleRequest(proxy, request, new CallbackContext(), proxySsmClient, logger);
        } catch (CfnGeneralServiceException ex) {
            assertThat(ex).isInstanceOf(CfnGeneralServiceException.class);
        }

        verify(proxySsmClient.client()).deleteParameter(any(DeleteParameterRequest.class));
    }

    @Test
    public void handleRequest_AmazonServiceExceptionInternalServerError() {
        when(proxySsmClient.client().deleteParameter(any(DeleteParameterRequest.class)))
                .thenThrow(InternalServerErrorException.builder().build());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .clientRequestToken("token")
                .desiredResourceTags(TAG_SET)
                .systemTags(SYSTEM_TAGS_SET)
                .desiredResourceState(RESOURCE_MODEL)
                .logicalResourceIdentifier("logical_id").build();

        try {
            handler.handleRequest(proxy, request, new CallbackContext(), proxySsmClient, logger);
        } catch (CfnServiceInternalErrorException ex) {
            assertThat(ex).isInstanceOf(CfnServiceInternalErrorException.class);
        }

        verify(proxySsmClient.client()).deleteParameter(any(DeleteParameterRequest.class));
    }

    @Test
    public void handleRequest_AmazonServiceException400NonThrottlingException() {
        AmazonServiceException amazonServiceException = new AmazonServiceException("Client error");
        amazonServiceException.setStatusCode(400);
        amazonServiceException.setErrorCode("Invalid Input");

        when(proxySsmClient.client().deleteParameter(any(DeleteParameterRequest.class)))
                .thenThrow(amazonServiceException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .clientRequestToken("token")
                .desiredResourceTags(TAG_SET)
                .systemTags(SYSTEM_TAGS_SET)
                .desiredResourceState(RESOURCE_MODEL)
                .logicalResourceIdentifier("logical_id").build();

        try {
            handler.handleRequest(proxy, request, new CallbackContext(), proxySsmClient, logger);
        } catch (CfnGeneralServiceException ex) {
            assertThat(ex).isInstanceOf(CfnGeneralServiceException.class);
        }

        verify(proxySsmClient.client()).deleteParameter(any(DeleteParameterRequest.class));
    }
}
