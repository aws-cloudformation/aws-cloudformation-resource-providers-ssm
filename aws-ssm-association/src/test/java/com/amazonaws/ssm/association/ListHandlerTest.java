package com.amazonaws.ssm.association;

import com.amazonaws.ssm.association.translator.ExceptionTranslator;
import com.amazonaws.ssm.association.util.ResourceHandlerRequestToStringConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.model.Association;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.ListAssociationsRequest;
import software.amazon.awssdk.services.ssm.model.ListAssociationsResponse;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Arrays;
import java.util.UUID;

import static com.amazonaws.ssm.association.TestsInputs.LOGGED_RESOURCE_HANDLER_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListHandlerTest {

    private ListHandler handler;
    @Mock
    private AmazonWebServicesClientProxy proxy;
    @Mock
    private Logger logger;
    @Mock
    private ExceptionTranslator exceptionTranslator;
    @Mock
    private ResourceHandlerRequestToStringConverter requestToStringConverter;

    private static final String AssociationId1 = UUID.randomUUID().toString();
    private static final String AssociationId2 = UUID.randomUUID().toString();
    private static final String AssociationId3 = UUID.randomUUID().toString();

    private static final String token1 = "token1";
    private static final String token2 = "token2";

    @BeforeEach
    void setup() {
        handler = new ListHandler(exceptionTranslator, requestToStringConverter);
    }

    @Test
    void defaultConstructorWorks() {
        new ListHandler();
    }

    @Test
    void handleRequest_SimpleSuccess() {
        when(requestToStringConverter.convert(any())).thenReturn(LOGGED_RESOURCE_HANDLER_REQUEST);

        final ListAssociationsResponse listAssociationsResponse = ListAssociationsResponse.builder()
            .associations(Arrays.asList(
                Association.builder().associationId(AssociationId1).build()))
            .nextToken(token2)
            .build();

        when(proxy.injectCredentialsAndInvokeV2(any(ListAssociationsRequest.class), any())).thenReturn(listAssociationsResponse);

        final ResourceModel model1 = ResourceModel.builder()
            .associationId(AssociationId1)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .nextToken(token1)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).containsAll(Arrays.asList(model1));
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getNextToken()).isEqualTo(token2);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    void handleRequest_SuccessWithMultipleAssociations() {
        when(requestToStringConverter.convert(any())).thenReturn(LOGGED_RESOURCE_HANDLER_REQUEST);

        final ListAssociationsResponse listAssociationsResponse = ListAssociationsResponse.builder()
            .associations(Arrays.asList(
                Association.builder().associationId(AssociationId1).build(),
                Association.builder().associationId(AssociationId2).build(),
                Association.builder().associationId(AssociationId3).build()))
            .nextToken(token2)
            .build();

        when(proxy.injectCredentialsAndInvokeV2(any(ListAssociationsRequest.class), any())).thenReturn(listAssociationsResponse);

        final ResourceModel model1 = ResourceModel.builder()
            .associationId(AssociationId1)
            .build();
        final ResourceModel model2 = ResourceModel.builder()
            .associationId(AssociationId2)
            .build();
        final ResourceModel model3 = ResourceModel.builder()
            .associationId(AssociationId3)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .nextToken(token1)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).containsAll(Arrays.asList(model1, model2, model3));
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getNextToken()).isEqualTo(token2);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    public void handleRequest_EmptyToken_throwsException() {
        final ResourceModel model = ResourceModel.builder()
            .name(AssociationId1)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .nextToken("") //empty string as nextToken should mean empty page
            .desiredResourceState(model)
            .build();

        final InternalServerErrorException serviceException = InternalServerErrorException.builder().build();

        final ListAssociationsRequest expectedListAssociationsRequest =
            ListAssociationsRequest.builder()
                .nextToken("")
                .maxResults(50)
                .build();

        when(
            proxy.injectCredentialsAndInvokeV2(
                eq(expectedListAssociationsRequest),
                any()))
            .thenThrow(serviceException);

        when(
            exceptionTranslator.translateFromServiceException(
                serviceException,
                expectedListAssociationsRequest,
                model))
            .thenReturn(new CfnServiceInternalErrorException("ListAssociations", serviceException));

        Assertions.assertThrows(CfnServiceInternalErrorException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });

        verify(exceptionTranslator)
            .translateFromServiceException(
                serviceException,
                expectedListAssociationsRequest,
                model);
        verify(logger).log(anyString());
    }

    @Test
    public void handleRequest_NullToken() {
        final ListAssociationsResponse listAssociationsResponse = ListAssociationsResponse.builder()
                .associations(Arrays.asList(
                    Association.builder().associationId(AssociationId1).build()))
                .nextToken(token1)
                .build();

        when(proxy.injectCredentialsAndInvokeV2(any(ListAssociationsRequest.class), any())).thenReturn(listAssociationsResponse);

        final ResourceModel model = ResourceModel.builder()
                .associationId(AssociationId1)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                //No nextToken in Request should mean first page
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).contains(model);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getNextToken()).isEqualTo(token1);
        verifyZeroInteractions(exceptionTranslator);
    }
}
