package com.amazonaws.ssm.document;

import jdk.nashorn.internal.runtime.regexp.joni.constants.OPCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.ssm.model.AutomationDefinitionNotFoundException;
import software.amazon.awssdk.services.ssm.model.DocumentAlreadyExistsException;
import software.amazon.awssdk.services.ssm.model.DocumentLimitExceededException;
import software.amazon.awssdk.services.ssm.model.DocumentVersionLimitExceededException;
import software.amazon.awssdk.services.ssm.model.InvalidDocumentContentException;
import software.amazon.awssdk.services.ssm.model.InvalidDocumentException;
import software.amazon.awssdk.services.ssm.model.InvalidDocumentVersionException;
import software.amazon.awssdk.services.ssm.model.MaxDocumentSizeExceededException;
import software.amazon.awssdk.services.ssm.model.SsmException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;

@ExtendWith(MockitoExtension.class)
public class DocumentExceptionTranslatorTest {

    private static final String SAMPLE_DOCUMENT_NAME = "sampleDocument";
    private static final String SAMPLE_OPERATION_NAME = "sampleOperation";
    private static final String ACCESS_DENIED_ERROR_CODE = "AccessDenied";

    private final DocumentExceptionTranslator unitUnderTest = new DocumentExceptionTranslator();

    @Test
    public void testGetCfnException_verifyExceptionsReturned() {
        Assertions.assertTrue(unitUnderTest.getCfnException(InvalidDocumentException.builder().build(), SAMPLE_DOCUMENT_NAME, SAMPLE_OPERATION_NAME) instanceof CfnNotFoundException);

        Assertions.assertTrue(unitUnderTest.getCfnException(DocumentLimitExceededException.builder().build(), SAMPLE_DOCUMENT_NAME, SAMPLE_OPERATION_NAME) instanceof CfnServiceLimitExceededException);

        Assertions.assertTrue(unitUnderTest.getCfnException(DocumentVersionLimitExceededException.builder().build(), SAMPLE_DOCUMENT_NAME, SAMPLE_OPERATION_NAME) instanceof CfnServiceLimitExceededException);

        Assertions.assertTrue(unitUnderTest.getCfnException(DocumentAlreadyExistsException.builder().build(), SAMPLE_DOCUMENT_NAME, SAMPLE_OPERATION_NAME) instanceof ResourceAlreadyExistsException);

        Assertions.assertTrue(unitUnderTest.getCfnException(MaxDocumentSizeExceededException.builder().build(), SAMPLE_DOCUMENT_NAME, SAMPLE_OPERATION_NAME) instanceof CfnInvalidRequestException);

        Assertions.assertTrue(unitUnderTest.getCfnException(InvalidDocumentContentException.builder().build(), SAMPLE_DOCUMENT_NAME, SAMPLE_OPERATION_NAME) instanceof CfnInvalidRequestException);

        Assertions.assertTrue(unitUnderTest.getCfnException(InvalidDocumentVersionException.builder().build(), SAMPLE_DOCUMENT_NAME, SAMPLE_OPERATION_NAME) instanceof CfnInvalidRequestException);

        Assertions.assertTrue(unitUnderTest.getCfnException(AutomationDefinitionNotFoundException.builder().build(), SAMPLE_DOCUMENT_NAME, SAMPLE_OPERATION_NAME) instanceof CfnInvalidRequestException);

        final Exception e = SsmException.builder().awsErrorDetails(AwsErrorDetails.builder().errorCode("InternalFailure").build()).build();
        Assertions.assertTrue(unitUnderTest.getCfnException(e, SAMPLE_DOCUMENT_NAME, SAMPLE_OPERATION_NAME) instanceof CfnGeneralServiceException);
    }

    @Test
    public void testGetCfnException_AccessDeniedException_verifyExceptionReturned() {
        final Exception e = AwsServiceException.builder().awsErrorDetails(AwsErrorDetails.builder().errorCode(ACCESS_DENIED_ERROR_CODE).build()).build();

        Assertions.assertTrue(unitUnderTest.getCfnException(e, SAMPLE_DOCUMENT_NAME, SAMPLE_OPERATION_NAME) instanceof CfnAccessDeniedException);
    }
}
