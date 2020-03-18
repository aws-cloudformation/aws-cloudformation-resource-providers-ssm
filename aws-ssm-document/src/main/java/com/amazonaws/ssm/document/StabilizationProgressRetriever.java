package com.amazonaws.ssm.document;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetDocumentRequest;
import software.amazon.awssdk.services.ssm.model.GetDocumentResponse;
import software.amazon.awssdk.services.ssm.model.SsmException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;

import static com.amazonaws.ssm.document.ResourceModel.TYPE_NAME;

/**
 * Updates the progression status of Create or Update Resource Operations.
 */
@RequiredArgsConstructor
class StabilizationProgressRetriever {

    private static final String RESOURCE_MODEL_ACTIVE_STATE = "Active";
    private static final String RESOURCE_MODEL_UPDATING_STATE = "Updating";

    private static StabilizationProgressRetriever INSTANCE;

    @NonNull
    private final DocumentModelTranslator documentModelTranslator;

    @NonNull
    private final DocumentResponseModelTranslator documentResponseModelTranslator;

    static StabilizationProgressRetriever getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new StabilizationProgressRetriever(DocumentModelTranslator.getInstance(),
                    DocumentResponseModelTranslator.getInstance());
        }

        return INSTANCE;
    }

    /**
     * Get the latest Event Progress State by making a GetDocument call to SsmClient.
     *
     * @throws SsmException when the SsmClient throws SsmException.
     * @throws CfnNotStabilizedException when the number of retries are exhausted.
     */
    GetProgressResponse getEventProgress(@NonNull final ResourceModel model,
                                         @NonNull final CallbackContext context,
                                         @NonNull final SsmClient ssmClient,
                                         @NonNull final AmazonWebServicesClientProxy proxy,
                                         @NonNull final Logger logger) {
        if (context.getStabilizationRetriesRemaining() == 0) {
            logger.log(String.format(
                    "Maximum stabilization retries reached for %s [%s]. Resource not stabilized",
                    TYPE_NAME,
                    model.getName()));
            throw new CfnNotStabilizedException(TYPE_NAME, model.getName());
        }

        final GetDocumentRequest describeDocumentRequest = documentModelTranslator.generateGetDocumentRequest(model);
        context.decrementStabilizationRetriesRemaining();

        final GetDocumentResponse response =
                proxy.injectCredentialsAndInvokeV2(describeDocumentRequest, ssmClient::getDocument);

        final ResourceInformation resourceInformation = documentResponseModelTranslator.generateResourceModel(response);

        return GetProgressResponse.builder()
                .resourceInformation(resourceInformation)
                .callbackContext(context)
                .build();
    }
}
