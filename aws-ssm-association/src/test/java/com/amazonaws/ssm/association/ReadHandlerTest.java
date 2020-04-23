package com.amazonaws.ssm.association;

import com.amazonaws.ssm.association.translator.AssociationDescriptionTranslator;
import com.amazonaws.ssm.association.translator.ExceptionTranslator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.model.AssociationDescription;
import software.amazon.awssdk.services.ssm.model.DescribeAssociationRequest;
import software.amazon.awssdk.services.ssm.model.DescribeAssociationResponse;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReadHandlerTest {

    private static final String DOCUMENT_NAME = "TestDocument";
    private static final String ASSOCIATION_ID = "test-12345-associationId";

    private ReadHandler handler;
    @Mock
    private AmazonWebServicesClientProxy proxy;
    @Mock
    private Logger logger;
    @Mock
    private AssociationDescriptionTranslator associationDescriptionTranslator;
    @Mock
    private ExceptionTranslator exceptionTranslator;

    @BeforeEach
    void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        associationDescriptionTranslator = mock(AssociationDescriptionTranslator.class);
        exceptionTranslator = mock(ExceptionTranslator.class);
        handler = new ReadHandler(associationDescriptionTranslator, exceptionTranslator);
    }

    @Test
    void handleRequestWithAssociationId() {
        final ResourceModel model = ResourceModel.builder()
            .associationId(ASSOCIATION_ID)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final AssociationDescription associationDescription =
            AssociationDescription.builder()
                .associationId(model.getAssociationId())
                .name(DOCUMENT_NAME)
                .build();

        final DescribeAssociationRequest expectedDescribeAssociationRequest =
            DescribeAssociationRequest.builder()
                .associationId(model.getAssociationId())
                .build();

        final DescribeAssociationResponse result =
            DescribeAssociationResponse.builder()
                .associationDescription(associationDescription)
                .build();

        when(
            proxy.injectCredentialsAndInvokeV2(
                eq(expectedDescribeAssociationRequest),
                ArgumentMatchers.<Function<DescribeAssociationRequest, DescribeAssociationResponse>>any()))
            .thenReturn(result);

        final ResourceModel expectedModel = request.getDesiredResourceState();
        expectedModel.setName(DOCUMENT_NAME);

        when(associationDescriptionTranslator.associationDescriptionToResourceModel(associationDescription))
            .thenReturn(expectedModel);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
            ProgressEvent.defaultSuccessHandler(expectedModel);

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    void handleRequestWithNoAssociationId() {
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
                .message("AssociationId must be present to read the existing association.")
                .build();

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(proxy);
        verifyZeroInteractions(associationDescriptionTranslator);
        verifyZeroInteractions(exceptionTranslator);
    }

    @Test
    void handleRequestThrowsTranslatedServiceException() {
        final ResourceModel model = ResourceModel.builder()
            .associationId(ASSOCIATION_ID)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final DescribeAssociationRequest expectedDescribeAssociationRequest =
            DescribeAssociationRequest.builder()
                .associationId(model.getAssociationId())
                .build();

        final InternalServerErrorException serviceException = InternalServerErrorException.builder().build();

        when(
            proxy.injectCredentialsAndInvokeV2(
                eq(expectedDescribeAssociationRequest),
                ArgumentMatchers.<Function<DescribeAssociationRequest, DescribeAssociationResponse>>any()))
            .thenThrow(serviceException);

        when(
            exceptionTranslator.translateFromServiceException(
                serviceException,
                expectedDescribeAssociationRequest,
                model))
            .thenReturn(new CfnServiceInternalErrorException("DescribeAssociation", serviceException));

        Assertions.assertThrows(CfnServiceInternalErrorException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
        verify(exceptionTranslator)
            .translateFromServiceException(
                serviceException,
                expectedDescribeAssociationRequest,
                model);
        verify(logger).log(anyString());
    }
}
