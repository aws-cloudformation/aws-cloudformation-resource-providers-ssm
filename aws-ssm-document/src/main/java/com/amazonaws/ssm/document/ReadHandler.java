package com.amazonaws.ssm.document;

import com.amazonaws.ssm.document.tags.TagReader;
import com.google.common.annotations.VisibleForTesting;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import java.util.Map;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DescribeDocumentRequest;
import software.amazon.awssdk.services.ssm.model.DescribeDocumentResponse;
import software.amazon.awssdk.services.ssm.model.GetDocumentRequest;
import software.amazon.awssdk.services.ssm.model.GetDocumentResponse;
import software.amazon.awssdk.services.ssm.model.SsmException;
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

    private static final String OPERATION_NAME = "AWS::SSM::GetDocument";
    private static final String ACCESS_DENIED_ERROR_CODE = "AccessDeniedException";

    @NonNull
    private final DocumentModelTranslator documentModelTranslator;

    @NonNull
    private final DocumentResponseModelTranslator documentResponseModelTranslator;

    @NonNull
    private final SsmClient ssmClient;

    @NonNull
    private final TagReader tagReader;

    @NonNull
    private final DocumentExceptionTranslator exceptionTranslator;

    @NonNull
    private final SafeLogger safeLogger;

    @VisibleForTesting
    public ReadHandler() {
        this(DocumentModelTranslator.getInstance(), DocumentResponseModelTranslator.getInstance(), ClientBuilder.getClient(),
            TagReader.getInstance(), DocumentExceptionTranslator.getInstance(), SafeLogger.getInstance());
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();

        safeLogger.safeLogDocumentInformation(model, callbackContext, request.getAwsAccountId(), request.getSystemTags(), logger);

        final GetDocumentRequest getDocumentRequest = documentModelTranslator.generateGetDocumentRequest(model);

        final DescribeDocumentRequest describeDocumentRequest = documentModelTranslator.generateDescribeDocumentRequest(model);

        try {
            final GetDocumentResponse getDocumentResponse = proxy.injectCredentialsAndInvokeV2(getDocumentRequest, ssmClient::getDocument);

            final Map<String, String> documentTags = tagReader.getDocumentTags(model.getName(), ssmClient, proxy);
            final ResourceInformation resourceInformation =
                    documentResponseModelTranslator.generateResourceInformation(getDocumentResponse, documentTags);

            try {
                final DescribeDocumentResponse describeDocumentResponse = proxy.injectCredentialsAndInvokeV2(describeDocumentRequest, ssmClient::describeDocument);

                resourceInformation.getResourceModel().setTargetType(describeDocumentResponse.document().targetType());
                logger.log(String.format("Attempting target type: " + describeDocumentResponse.document().targetType()));
                logger.log(String.format("Appended target type: " + resourceInformation.getResourceModel().getTargetType()));
            } catch(SsmException e) {
                if (!ACCESS_DENIED_ERROR_CODE.equalsIgnoreCase(e.awsErrorDetails().errorCode())) {
                    throw e;
                }
                logger.log(String.format("Soft fail describe document in ReadHandler due to insufficient permissions %s",
                        describeDocumentRequest.name()));
            }

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(resourceInformation.getResourceModel())
                    .status(OperationStatus.SUCCESS)
                    .build();
        } catch (final SsmException e) {
            throw exceptionTranslator.getCfnException(e, model.getName(), OPERATION_NAME, logger);
        }
    }
}
