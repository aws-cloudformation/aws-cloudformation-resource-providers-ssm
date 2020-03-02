package com.amazonaws.ssm.document;

import com.google.common.annotations.VisibleForTesting;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetDocumentRequest;
import software.amazon.awssdk.services.ssm.model.GetDocumentResponse;
import software.amazon.awssdk.services.ssm.model.InvalidDocumentException;
import software.amazon.awssdk.services.ssm.model.InvalidDocumentVersionException;
import software.amazon.awssdk.services.ssm.model.SsmException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

/**
 * Get AWS::SSM::Document resource.
 */
@RequiredArgsConstructor
public class ReadHandler extends BaseHandler<CallbackContext> {

    @NonNull
    private final DocumentModelTranslator documentModelTranslator;

    @NonNull
    private final DocumentResponseModelTranslator documentResponseModelTranslator;

    @NonNull
    private final SsmClient ssmClient;

    @VisibleForTesting
    public ReadHandler() {
        this(new DocumentModelTranslator(), new DocumentResponseModelTranslator(), SsmClient.create());
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();

        final GetDocumentRequest getDocumentRequest = documentModelTranslator.generateGetDocumentRequest(model);

        try {
            final GetDocumentResponse getDocumentResponse = proxy.injectCredentialsAndInvokeV2(getDocumentRequest, ssmClient::getDocument);

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(documentResponseModelTranslator.generateResourceModel(getDocumentResponse))
                    .status(OperationStatus.SUCCESS)
                    .build();
        } catch (final InvalidDocumentException e) {
            throw new CfnNotFoundException(e);
        } catch (final InvalidDocumentVersionException e) {
            throw new CfnInvalidRequestException(e);
        } catch (final SsmException e) {
            throw new CfnGeneralServiceException(e);
        }
    }
}
