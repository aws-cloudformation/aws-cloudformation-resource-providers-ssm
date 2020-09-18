package com.amazonaws.ssm.document;

import software.amazon.awssdk.services.ssm.model.AutomationDefinitionNotFoundException;
import software.amazon.awssdk.services.ssm.model.AutomationDefinitionVersionNotFoundException;
import software.amazon.awssdk.services.ssm.model.DocumentAlreadyExistsException;
import software.amazon.awssdk.services.ssm.model.DocumentLimitExceededException;
import software.amazon.awssdk.services.ssm.model.DocumentVersionLimitExceededException;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.InvalidDocumentContentException;
import software.amazon.awssdk.services.ssm.model.InvalidDocumentException;
import software.amazon.awssdk.services.ssm.model.InvalidDocumentSchemaVersionException;
import software.amazon.awssdk.services.ssm.model.InvalidDocumentVersionException;
import software.amazon.awssdk.services.ssm.model.MaxDocumentSizeExceededException;
import software.amazon.awssdk.services.ssm.model.SsmException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;

import lombok.NonNull;

class DocumentExceptionTranslator {

    private static final int GENERIC_USER_ERROR_STATUS_CODE = 400;

    private static DocumentExceptionTranslator INSTANCE;

    static DocumentExceptionTranslator getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DocumentExceptionTranslator();
        }

        return INSTANCE;
    }

    RuntimeException getCfnException(@NonNull final SsmException e, @NonNull String documentName, @NonNull String operationName) {
        if (e instanceof DocumentLimitExceededException || e instanceof DocumentVersionLimitExceededException) {

            return new CfnServiceLimitExceededException(ResourceModel.TYPE_NAME, e.getMessage());

        } else if (e instanceof DocumentAlreadyExistsException) {

            return new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, documentName);

        } else if (e instanceof MaxDocumentSizeExceededException || e instanceof InvalidDocumentContentException
                || e instanceof InvalidDocumentVersionException || e instanceof InvalidDocumentSchemaVersionException
                || e instanceof AutomationDefinitionNotFoundException || e instanceof AutomationDefinitionVersionNotFoundException) {

            return new CfnInvalidRequestException(e.getMessage(), e);

        } else if (e instanceof InvalidDocumentException) {

            return new CfnNotFoundException(ResourceModel.TYPE_NAME, documentName);

        } else if (e.isThrottlingException()) {
            return new CfnThrottlingException(operationName, e);
        } else if (e instanceof InternalServerErrorException) {
            return new CfnServiceInternalErrorException(operationName, e);
        } else if (e.statusCode() == GENERIC_USER_ERROR_STATUS_CODE) {
            return new CfnInvalidRequestException(e.getMessage(), e);
        }

        return new CfnGeneralServiceException(e);
    }
}
