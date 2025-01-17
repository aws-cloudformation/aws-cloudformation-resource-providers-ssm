package com.amazonaws.ssm.document;

import com.google.common.annotations.VisibleForTesting;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.ListDocumentsRequest;
import software.amazon.awssdk.services.ssm.model.ListDocumentsResponse;
import software.amazon.awssdk.services.ssm.model.SsmException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ListHandler extends BaseHandler<CallbackContext> {

    @NonNull
    private final DocumentModelTranslator documentModelTranslator;

    @NonNull
    private final DocumentResponseModelTranslator documentResponseModelTranslator;

    @NonNull
    private final SsmClient ssmClient;

    @NonNull
    private final DocumentExceptionTranslator exceptionTranslator;

    @NonNull
    private final SafeLogger safeLogger;

    @VisibleForTesting
    public ListHandler() {
        this(DocumentModelTranslator.getInstance(), DocumentResponseModelTranslator.getInstance(), ClientBuilder.getClient(),
             DocumentExceptionTranslator.getInstance(), SafeLogger.getInstance());
    }


    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {
        final ResourceModel model = request.getDesiredResourceState();

        safeLogger.safeLogDocumentInformation(model, callbackContext, request.getAwsAccountId(), request.getSystemTags(), logger);

        try {
            final ListDocumentsRequest listDocumentsRequest = DocumentModelTranslator.generateListDocumentsRequest(request.getNextToken(), model);
            final ListDocumentsResponse listDocumentsResponse = proxy.injectCredentialsAndInvokeV2(
                    listDocumentsRequest, ClientBuilder.getClient()::listDocuments);

            final List<ResourceModel> models = listDocumentsResponse
                    .documentIdentifiers()
                    .stream().map(documentIdentifier -> ResourceModel.builder().name(documentIdentifier.name()).build()).collect(Collectors.toList());

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModels(models)
                    .status(OperationStatus.SUCCESS)
                    .nextToken(listDocumentsResponse.nextToken())
                    .build();
        } catch (final SsmException e) {
            throw exceptionTranslator.getCfnException(e, null, "AWS::SSM::ListDocuments", logger);
        }
    }


}