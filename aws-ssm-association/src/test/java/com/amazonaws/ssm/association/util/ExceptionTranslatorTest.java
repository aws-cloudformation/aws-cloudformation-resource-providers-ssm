package com.amazonaws.ssm.association.util;

import com.amazonaws.ssm.association.translator.ExceptionTranslator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.model.AssociationAlreadyExistsException;
import software.amazon.awssdk.services.ssm.model.AssociationDoesNotExistException;
import software.amazon.awssdk.services.ssm.model.AssociationLimitExceededException;
import software.amazon.awssdk.services.ssm.model.AssociationVersionLimitExceededException;
import software.amazon.awssdk.services.ssm.model.CreateAssociationRequest;
import software.amazon.awssdk.services.ssm.model.DeleteAssociationRequest;
import software.amazon.awssdk.services.ssm.model.DescribeAssociationRequest;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.InvalidAssociationVersionException;
import software.amazon.awssdk.services.ssm.model.InvalidDocumentException;
import software.amazon.awssdk.services.ssm.model.InvalidDocumentVersionException;
import software.amazon.awssdk.services.ssm.model.TooManyUpdatesException;
import software.amazon.awssdk.services.ssm.model.UnsupportedPlatformTypeException;
import software.amazon.awssdk.services.ssm.model.UpdateAssociationRequest;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;

import java.util.Optional;

import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.ASSOCIATION_ID;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.DOCUMENT_NAME;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.INSTANCE_ID;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ExceptionTranslatorTest {

    private ExceptionTranslator exceptionTranslator;

    @BeforeEach
    void setUp() {
        exceptionTranslator = new ExceptionTranslator();
    }

    @Test
    void translateFromAssociationAlreadyExistsException() {
        final AssociationAlreadyExistsException serviceException = AssociationAlreadyExistsException.builder().build();
        final CreateAssociationRequest request = CreateAssociationRequest.builder()
            .name(DOCUMENT_NAME)
            .build();
        final Exception cfnException = exceptionTranslator
            .translateFromServiceException(serviceException,
                request,
                Optional.empty());

        assertTrue(cfnException instanceof CfnAlreadyExistsException);
    }

    @Test
    void translateFromAssociationLimitExceededException() {
        final AssociationLimitExceededException serviceException = AssociationLimitExceededException.builder().build();
        final CreateAssociationRequest request = CreateAssociationRequest.builder()
            .name(DOCUMENT_NAME)
            .build();
        final Exception cfnException = exceptionTranslator
            .translateFromServiceException(serviceException,
                request,
                Optional.empty());

        assertTrue(cfnException instanceof CfnServiceLimitExceededException);
    }

    @Test
    void translateFromAssociationVersionLimitExceededException() {
        final AssociationVersionLimitExceededException serviceException = AssociationVersionLimitExceededException.builder().build();
        final UpdateAssociationRequest request = UpdateAssociationRequest.builder()
            .associationId(ASSOCIATION_ID)
            .name(DOCUMENT_NAME)
            .build();
        final Exception cfnException = exceptionTranslator
            .translateFromServiceException(serviceException,
                request,
                Optional.of(ASSOCIATION_ID));

        assertTrue(cfnException instanceof CfnServiceLimitExceededException);
    }

    @Test
    void translateFromAssociationDoesNotExistExceptionWithAssociationId() {
        final AssociationDoesNotExistException serviceException = AssociationDoesNotExistException.builder().build();
        final DescribeAssociationRequest request = DescribeAssociationRequest.builder()
            .associationId(ASSOCIATION_ID)
            .build();
        final Exception cfnException = exceptionTranslator
            .translateFromServiceException(serviceException,
                request,
                Optional.of(ASSOCIATION_ID));

        assertTrue(cfnException instanceof CfnNotFoundException);
    }

    @Test
    void translateFromAssociationDoesNotExistExceptionWithoutAssociationId() {
        final AssociationDoesNotExistException serviceException = AssociationDoesNotExistException.builder().build();
        final DeleteAssociationRequest request = DeleteAssociationRequest.builder()
            .name(DOCUMENT_NAME)
            .instanceId(INSTANCE_ID)
            .build();
        final Exception cfnException = exceptionTranslator
            .translateFromServiceException(serviceException,
                request,
                Optional.empty());

        assertTrue(cfnException instanceof CfnNotFoundException);
    }

    @Test
    void translateFromInternalServerErrorException() {
        final InternalServerErrorException serviceException = InternalServerErrorException.builder().build();
        final DescribeAssociationRequest request = DescribeAssociationRequest.builder()
            .associationId(ASSOCIATION_ID)
            .build();
        final Exception cfnException = exceptionTranslator
            .translateFromServiceException(serviceException,
                request,
                Optional.of(ASSOCIATION_ID));

        assertTrue(cfnException instanceof CfnServiceInternalErrorException);
    }

    @Test
    void translateFromInvalidAssociationVersionException() {
        final InvalidAssociationVersionException serviceException = InvalidAssociationVersionException.builder().build();
        final UpdateAssociationRequest request = UpdateAssociationRequest.builder()
            .associationId(ASSOCIATION_ID)
            .associationVersion("5")
            .build();
        final Exception cfnException = exceptionTranslator
            .translateFromServiceException(serviceException,
                request,
                Optional.of(ASSOCIATION_ID));

        assertTrue(cfnException instanceof CfnInvalidRequestException);
    }

    @Test
    void translateFromInvalidDocumentException() {
        final InvalidDocumentException serviceException = InvalidDocumentException.builder().build();
        final UpdateAssociationRequest request = UpdateAssociationRequest.builder()
            .associationId(ASSOCIATION_ID)
            .name(DOCUMENT_NAME)
            .build();
        final Exception cfnException = exceptionTranslator
            .translateFromServiceException(serviceException,
                request,
                Optional.of(ASSOCIATION_ID));

        assertTrue(cfnException instanceof CfnInvalidRequestException);
    }

    @Test
    void translateFromInvalidDocumentVersionException() {
        final InvalidDocumentVersionException serviceException = InvalidDocumentVersionException.builder().build();
        final UpdateAssociationRequest request = UpdateAssociationRequest.builder()
            .associationId(ASSOCIATION_ID)
            .documentVersion("5")
            .build();
        final Exception cfnException = exceptionTranslator
            .translateFromServiceException(serviceException,
                request,
                Optional.of(ASSOCIATION_ID));

        assertTrue(cfnException instanceof CfnInvalidRequestException);
    }

    @Test
    void translateFromUnsupportedPlatformTypeException() {
        final UnsupportedPlatformTypeException serviceException = UnsupportedPlatformTypeException.builder().build();
        final CreateAssociationRequest request = CreateAssociationRequest.builder()
            .name(DOCUMENT_NAME)
            .instanceId(INSTANCE_ID)
            .build();
        final Exception cfnException = exceptionTranslator
            .translateFromServiceException(serviceException,
                request,
                Optional.empty());

        assertTrue(cfnException instanceof CfnInvalidRequestException);
    }

    @Test
    void translateFromTooManyUpdatesException() {
        final TooManyUpdatesException serviceException = TooManyUpdatesException.builder().build();
        final UpdateAssociationRequest request = UpdateAssociationRequest.builder()
            .associationId(ASSOCIATION_ID)
            .documentVersion("5")
            .build();
        final Exception cfnException = exceptionTranslator
            .translateFromServiceException(serviceException,
                request,
                Optional.of(ASSOCIATION_ID));

        assertTrue(cfnException instanceof CfnThrottlingException);
    }

    @Test
    void translateFromUnknownServiceException() {
        final IllegalArgumentException serviceException = new IllegalArgumentException();
        final UpdateAssociationRequest request = UpdateAssociationRequest.builder()
            .associationId(ASSOCIATION_ID)
            .documentVersion("5")
            .build();
        final Exception cfnException = exceptionTranslator
            .translateFromServiceException(serviceException,
                request,
                Optional.of(ASSOCIATION_ID));

        assertTrue(cfnException instanceof CfnGeneralServiceException);
    }
}
