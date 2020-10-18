package com.amazonaws.ssm.document;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import javax.annotation.Nullable;
import software.amazon.cloudformation.proxy.Logger;

@NoArgsConstructor
public class SafeLogger {

    private static final String DOCUMENT_SAFE_LOG_FORMAT = "DocumentInfo: documentName=%s, documentType=%s, documentFormat=%s";

    private static SafeLogger INSTANCE;

    public static SafeLogger getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SafeLogger();
        }

        return INSTANCE;
    }

    public void safeLogDocumentInformation(@NonNull final ResourceModel resourceModel,
                                            @Nullable final CallbackContext callbackContext,
                                            @NonNull final String customerAccountId,
                                            @NonNull final Logger logger) {
        logger.log("customerAccountId: " + customerAccountId);

        final String documentDetails = String.format(DOCUMENT_SAFE_LOG_FORMAT, resourceModel.getName(),
            resourceModel.getDocumentType(), resourceModel.getDocumentFormat());

        logger.log(documentDetails);

        logger.log("callbackContext: " + callbackContext);
    }

}
