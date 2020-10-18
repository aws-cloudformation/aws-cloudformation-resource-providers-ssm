package com.amazonaws.ssm.document;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import javax.annotation.Nullable;
import java.util.Map;
import software.amazon.cloudformation.proxy.Logger;

@NoArgsConstructor
public class SafeLogger {

    private static final String DOCUMENT_INFO_SAFE_LOG_FORMAT = "{documentName=%s, documentType=%s, documentFormat=%s}";
    private static final String STACK_ID_KEY = "aws:cloudformation:stack-id";
    private static final String REQUEST_SAFE_LOG_FORMAT = "CustomerAccountId: %s, DocumentInfo: %s, CallbackContext: %s, StackId: %s";

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
                                           @Nullable final Map<String, String> systemTags,
                                           @NonNull final Logger logger) {
        final String documentDetails = String.format(DOCUMENT_INFO_SAFE_LOG_FORMAT, resourceModel.getName(),
            resourceModel.getDocumentType(), resourceModel.getDocumentFormat());

        final String stackId = systemTags != null ? systemTags.get(STACK_ID_KEY) : null;

        final String loggingInfo = String.format(REQUEST_SAFE_LOG_FORMAT, customerAccountId, documentDetails, callbackContext, stackId);

        logger.log(loggingInfo);
    }

}
