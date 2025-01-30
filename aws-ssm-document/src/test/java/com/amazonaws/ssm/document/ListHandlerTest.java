package com.amazonaws.ssm.document;

import com.google.common.collect.ImmutableList;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DocumentIdentifier;
import software.amazon.awssdk.services.ssm.model.ListDocumentsRequest;
import software.amazon.awssdk.services.ssm.model.ListDocumentsResponse;
import software.amazon.awssdk.services.ssm.model.SsmException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest {
    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @Mock
    private DocumentModelTranslator documentModelTranslator;

    @Mock
    private DocumentResponseModelTranslator documentResponseModelTranslator;

    @Mock
    private DocumentExceptionTranslator exceptionTranslator;

    @Mock
    private SsmException ssmException;

    @Mock
    private SafeLogger safeLogger;

    @Mock
    private SsmClient ssmClient;

    private ListHandler handler;

    private static final String Document_Identifier = "doc:aws::ssm:test";
    private static final String Document_Identifier2 = "doc:aws::ssm:test2";
    private static final String Document_Identifier3 = "doc:aws::ssm:test3";
    private static final String OPERATION_NAME = "AWS::SSM::ListDocuments";

    @BeforeEach
    public void setup() {
        documentModelTranslator = DocumentModelTranslator.getInstance();
        handler = new ListHandler(documentModelTranslator, documentResponseModelTranslator, ssmClient,
            exceptionTranslator, safeLogger);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ListDocumentsResponse listDocumentsResponse = ListDocumentsResponse.builder()
                .documentIdentifiers(Arrays.asList(
                    DocumentIdentifier.builder().name(Document_Identifier).build(),
                    DocumentIdentifier.builder().name(Document_Identifier2).build()))
                .nextToken("token2")
                .build();

        when(proxy.injectCredentialsAndInvokeV2(any(ListDocumentsRequest.class), any())).thenReturn(listDocumentsResponse);

        final ResourceModel model1 = ResourceModel.builder()
                .name(Document_Identifier)
                .build();
        final ResourceModel model2 = ResourceModel.builder()
                .name(Document_Identifier2)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .nextToken("token")
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).containsAll(Arrays.asList(model1, model2));
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getNextToken()).isEqualTo("token2");
    }

    @Test
    public void handleRequest_EmptyToken_throwsException() {
        final ResourceModel model3 = ResourceModel.builder()
                .name(Document_Identifier3)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .nextToken("") //empty string as nextToken should mean empty page
                .build();

        when(proxy.injectCredentialsAndInvokeV2(any(ListDocumentsRequest.class), any())).thenThrow(ssmException);
        when(exceptionTranslator.getCfnException(ssmException, null, OPERATION_NAME, logger)).thenThrow(CfnInvalidRequestException.class);

        Assertions.assertThrows(CfnInvalidRequestException.class, () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_NullToken() {
        final ListDocumentsResponse listDocumentsResponse = ListDocumentsResponse.builder()
                .documentIdentifiers(Arrays.asList(
                    DocumentIdentifier.builder().name(Document_Identifier3).build()))
                .nextToken("token_next")
                .build();

        when(proxy.injectCredentialsAndInvokeV2(any(ListDocumentsRequest.class), any())).thenReturn(listDocumentsResponse);

        final ResourceModel model3 = ResourceModel.builder()
                .name(Document_Identifier3)
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
        assertThat(response.getResourceModels()).contains(model3);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getNextToken()).isEqualTo("token_next");
    }
}
