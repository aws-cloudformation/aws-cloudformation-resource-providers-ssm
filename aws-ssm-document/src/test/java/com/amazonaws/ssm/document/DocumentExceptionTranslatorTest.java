package com.amazonaws.ssm.document;

import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.model.AutomationDefinitionNotFoundException;
import software.amazon.awssdk.services.ssm.model.DocumentAlreadyExistsException;
import software.amazon.awssdk.services.ssm.model.DocumentLimitExceededException;
import software.amazon.awssdk.services.ssm.model.DocumentVersionLimitExceededException;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.InvalidDocumentContentException;
import software.amazon.awssdk.services.ssm.model.InvalidDocumentException;
import software.amazon.awssdk.services.ssm.model.InvalidDocumentVersionException;
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

@ExtendWith(MockitoExtension.class)
public class DocumentExceptionTranslatorTest {

    private static final String SAMPLE_DOCUMENT_NAME = "sampleDocument";
    private static final String SAMPLE_OPERATION_NAME = "sampleOperation";

    private static final String ADD_TAGS_ERROR_MESSAGE = "not authorized to perform: ssm:AddTagsToResource";
    private static final String REMOVE_TAGS_ERROR_MESSAGE = "not authorized to perform: ssm:RemoveTagsFromResource";

    private final DocumentExceptionTranslator unitUnderTest = new DocumentExceptionTranslator();

    @Mock
    private SsmException ssmException;

    @Mock
    private IOException ioException;

    @Mock
    private Logger logger;

    @Test
    public void testGetCfnException_verifyExceptionsReturned() {
        Mockito.when(ssmException.statusCode()).thenReturn(500);

        Assertions.assertTrue(unitUnderTest.getCfnException(InvalidDocumentException.builder().build(), SAMPLE_DOCUMENT_NAME, SAMPLE_OPERATION_NAME, logger) instanceof CfnNotFoundException);

        Assertions.assertTrue(unitUnderTest.getCfnException(InvalidResourceIdException.builder().build(), SAMPLE_DOCUMENT_NAME, SAMPLE_OPERATION_NAME, logger) instanceof CfnNotFoundException);

        Assertions.assertTrue(unitUnderTest.getCfnException(DocumentLimitExceededException.builder().build(), SAMPLE_DOCUMENT_NAME, SAMPLE_OPERATION_NAME, logger) instanceof CfnServiceLimitExceededException);

        Assertions.assertTrue(unitUnderTest.getCfnException(DocumentVersionLimitExceededException.builder().build(), SAMPLE_DOCUMENT_NAME, SAMPLE_OPERATION_NAME, logger) instanceof CfnServiceLimitExceededException);

        Assertions.assertTrue(unitUnderTest.getCfnException(DocumentAlreadyExistsException.builder().build(), SAMPLE_DOCUMENT_NAME, SAMPLE_OPERATION_NAME, logger) instanceof CfnAlreadyExistsException);

        Assertions.assertTrue(unitUnderTest.getCfnException(MaxDocumentSizeExceededException.builder().build(), SAMPLE_DOCUMENT_NAME, SAMPLE_OPERATION_NAME, logger) instanceof CfnInvalidRequestException);

        Assertions.assertTrue(unitUnderTest.getCfnException(InvalidDocumentContentException.builder().build(), SAMPLE_DOCUMENT_NAME, SAMPLE_OPERATION_NAME, logger) instanceof CfnInvalidRequestException);

        Assertions.assertTrue(unitUnderTest.getCfnException(InvalidDocumentVersionException.builder().build(), SAMPLE_DOCUMENT_NAME, SAMPLE_OPERATION_NAME, logger) instanceof CfnInvalidRequestException);

        Assertions.assertTrue(unitUnderTest.getCfnException(AutomationDefinitionNotFoundException.builder().build(), SAMPLE_DOCUMENT_NAME, SAMPLE_OPERATION_NAME, logger) instanceof CfnInvalidRequestException);

        Assertions.assertTrue(unitUnderTest.getCfnException(InternalServerErrorException.builder().build(), SAMPLE_DOCUMENT_NAME, SAMPLE_OPERATION_NAME, logger) instanceof CfnServiceInternalErrorException);

        Assertions.assertTrue(unitUnderTest.getCfnException(ssmException, SAMPLE_DOCUMENT_NAME, SAMPLE_OPERATION_NAME, logger) instanceof CfnGeneralServiceException);
    }

    @Test
    public void testGetCfnException_ThrottlingException_verifyExceptionsReturned() {
        Mockito.when(ssmException.isThrottlingException()).thenReturn(true);

        Assertions.assertTrue(unitUnderTest.getCfnException(ssmException, SAMPLE_DOCUMENT_NAME, SAMPLE_OPERATION_NAME, logger) instanceof CfnThrottlingException);
    }

    @Test
    public void testGetCfnException_Non400StatusCode_verifyExceptionsReturned() {
        Mockito.when(ssmException.statusCode()).thenReturn(500);

        Assertions.assertTrue(unitUnderTest.getCfnException(ssmException, SAMPLE_DOCUMENT_NAME, SAMPLE_OPERATION_NAME, logger) instanceof CfnGeneralServiceException);
    }

    @Test
    public void testGetCfnException_IOExceptionCause_verifyExceptionsReturned() {
        Mockito.when(ssmException.getCause()).thenReturn(ioException);

        Assertions.assertTrue(unitUnderTest.getCfnException(ssmException, SAMPLE_DOCUMENT_NAME, SAMPLE_OPERATION_NAME, logger) instanceof CfnNetworkFailureException);
    }

    @Test
    public void testGetCfnException_400StatusCode_verifyExceptionsReturned() {
        Mockito.when(ssmException.statusCode()).thenReturn(400);

        Assertions.assertTrue(unitUnderTest.getCfnException(ssmException, SAMPLE_DOCUMENT_NAME, SAMPLE_OPERATION_NAME, logger) instanceof CfnInvalidRequestException);
    }
}
