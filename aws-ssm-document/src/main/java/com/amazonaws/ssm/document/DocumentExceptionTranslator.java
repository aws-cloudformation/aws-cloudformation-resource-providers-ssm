package com.amazonaws.ssm.document;

import com.amazonaws.ssm.document.tags.TagUtil;

import software.amazon.awssdk.services.ssm.model.AutomationDefinitionNotFoundException;
import software.amazon.awssdk.services.ssm.model.AutomationDefinitionVersionNotFoundException;
import software.amazon.awssdk.services.ssm.model.DocumentAlreadyExistsException;
import software.amazon.awssdk.services.ssm.model.DocumentLimitExceededException;
import software.amazon.awssdk.services.ssm.model.DocumentVersionLimitExceededException;
import software.amazon.awssdk.services.ssm.model.DuplicateDocumentContentException;
import software.amazon.awssdk.services.ssm.model.DuplicateDocumentVersionNameException;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.InvalidDocumentContentException;
import software.amazon.awssdk.services.ssm.model.InvalidDocumentException;
import software.amazon.awssdk.services.ssm.model.InvalidDocumentSchemaVersionException;
import software.amazon.awssdk.services.ssm.model.InvalidDocumentVersionException;
import software.amazon.awssdk.services.ssm.model.InvalidNextTokenException;
import software.amazon.awssdk.services.ssm.model.InvalidResourceIdException;
import software.amazon.awssdk.services.ssm.model.MaxDocumentSizeExceededException;
import software.amazon.awssdk.services.ssm.model.SsmException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNetworkFailureException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.exceptions.CfnUnauthorizedTaggingOperationException;
import software.amazon.cloudformation.proxy.Logger;

import lombok.NonNull;
import java.io.IOException;

class DocumentExceptionTranslator {

    private static final int GENERIC_USER_ERROR_STATUS_CODE = 400;
    /**
     * Exception metric filter pattern used to publish exception metrics to cloudwatch.
     * Warn: Modifying this pattern will break cloudwatch metric filters.
     */
    private static final String EXCEPTION_METRIC_FILTER_PATTERN = "[EXCEPTION] Operation: %s, ExceptionType: %s";

    private static DocumentExceptionTranslator INSTANCE;

    static DocumentExceptionTranslator getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DocumentExceptionTranslator();
        }

        return INSTANCE;
    }

    RuntimeException getCfnException(@NonNull final SsmException e, String documentName, @NonNull String operationName,
                                     @NonNull final Logger logger) {

        logger.log(String.format(EXCEPTION_METRIC_FILTER_PATTERN, operationName, e.getClass()));

        if (e instanceof DocumentLimitExceededException || e instanceof DocumentVersionLimitExceededException) {

            return new CfnServiceLimitExceededException(ResourceModel.TYPE_NAME, e.getMessage());

        } else if (e instanceof DocumentAlreadyExistsException) {

            return new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, documentName);

        } else if (e instanceof MaxDocumentSizeExceededException || e instanceof InvalidDocumentContentException
                || e instanceof InvalidDocumentVersionException || e instanceof InvalidDocumentSchemaVersionException
                || e instanceof AutomationDefinitionNotFoundException || e instanceof AutomationDefinitionVersionNotFoundException
                || e instanceof DuplicateDocumentContentException || e instanceof DuplicateDocumentVersionNameException
                || e instanceof InvalidNextTokenException) {

            return new CfnInvalidRequestException(e.getMessage(), e);

        } else if (e instanceof InvalidDocumentException || e instanceof InvalidResourceIdException) {

            return new CfnNotFoundException(ResourceModel.TYPE_NAME, documentName);

        } else if (e.isThrottlingException()) {
            return new CfnThrottlingException(operationName, e);
        } else if (e instanceof InternalServerErrorException) {
            return new CfnServiceInternalErrorException(operationName, e);
        } else if (e.getCause() instanceof IOException) {
            return new CfnNetworkFailureException(operationName, e);
        } else if (e.statusCode() == GENERIC_USER_ERROR_STATUS_CODE) {
            return new CfnInvalidRequestException(e.getMessage(), e);
        }

        return new CfnGeneralServiceException(e);
    }
}
