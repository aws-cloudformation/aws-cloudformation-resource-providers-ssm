package com.amazonaws.ssm.association.util;

import com.amazonaws.ssm.association.ResourceModel;
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

import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.ASSOCIATION_ID;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.DOCUMENT_NAME;
import static com.amazonaws.ssm.association.translator.TranslatorTestsInputs.INSTANCE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ExceptionTranslatorTest {

    private ExceptionTranslator exceptionTranslator;
    private ResourceModel model;
    private ResourceModel modelWithoutId;

    @BeforeEach
    void setUp() {
        exceptionTranslator = new ExceptionTranslator();
        model = ResourceModel.builder().associationId(ASSOCIATION_ID).name(DOCUMENT_NAME).build();
        modelWithoutId = ResourceModel.builder().name(DOCUMENT_NAME).instanceId(INSTANCE_ID).build();
    }

    @Test
    void translateFromAssociationAlreadyExistsExceptionWithoutAssociationId() {
        final AssociationAlreadyExistsException serviceException = AssociationAlreadyExistsException.builder().build();
        final CreateAssociationRequest request = CreateAssociationRequest.builder()
            .name(DOCUMENT_NAME)
            .build();
        final Exception cfnException = exceptionTranslator
            .translateFromServiceException(serviceException,
                request,
                modelWithoutId);

        assertTrue(cfnException instanceof CfnAlreadyExistsException);
        final String expectedMessage =
            String.format("Resource of type 'AWS::SSM::Association' with identifier 'Name=%s,InstanceId=%s' already exists.",
                modelWithoutId.getName(), modelWithoutId.getInstanceId());
        assertEquals(expectedMessage, cfnException.getMessage());
    }

    @Test
    void translateFromAssociationAlreadyExistsExceptionWithAssociationId() {
        final AssociationAlreadyExistsException serviceException = AssociationAlreadyExistsException.builder().build();
        final CreateAssociationRequest request = CreateAssociationRequest.builder()
            .name(DOCUMENT_NAME)
            .build();
        final Exception cfnException = exceptionTranslator
            .translateFromServiceException(serviceException,
                request,
                model);

        assertTrue(cfnException instanceof CfnAlreadyExistsException);
        final String expectedMessage =
            String.format("Resource of type 'AWS::SSM::Association' with identifier '%s' already exists.", model.getAssociationId());
        assertEquals(expectedMessage, cfnException.getMessage());
    }

    @Test
    void translateFromAssociationLimitExceededException() {
        final String serviceExceptionMessage = "Limit exceeded!";
        final AssociationLimitExceededException serviceException =
            AssociationLimitExceededException.builder().message(serviceExceptionMessage).build();
        final CreateAssociationRequest request = CreateAssociationRequest.builder()
            .name(DOCUMENT_NAME)
            .build();
        final Exception cfnException = exceptionTranslator
            .translateFromServiceException(serviceException,
                request,
                model);

        assertTrue(cfnException instanceof CfnServiceLimitExceededException);
        final String expectedMessage =
            String.format("Limit exceeded for resource of type 'AWS::SSM::Association'. Reason: %s", serviceExceptionMessage);
        assertEquals(expectedMessage, cfnException.getMessage());
    }

    @Test
    void translateFromAssociationVersionLimitExceededException() {
        final String serviceExceptionMessage = "Limit exceeded!";
        final AssociationVersionLimitExceededException serviceException =
            AssociationVersionLimitExceededException.builder().message(serviceExceptionMessage).build();
        final UpdateAssociationRequest request = UpdateAssociationRequest.builder()
            .associationId(ASSOCIATION_ID)
            .name(DOCUMENT_NAME)
            .build();
        final Exception cfnException = exceptionTranslator
            .translateFromServiceException(serviceException,
                request,
                model);

        assertTrue(cfnException instanceof CfnServiceLimitExceededException);
        final String expectedMessage =
            String.format("Limit exceeded for resource of type 'AWS::SSM::Association'. Reason: %s", serviceExceptionMessage);
        assertEquals(expectedMessage, cfnException.getMessage());
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
                model);

        assertTrue(cfnException instanceof CfnNotFoundException);
        final String expectedMessage =
            String.format("Resource of type 'AWS::SSM::Association' with identifier '%s' was not found.", model.getAssociationId());
        assertEquals(expectedMessage, cfnException.getMessage());
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
                modelWithoutId);

        assertTrue(cfnException instanceof CfnNotFoundException);
        final String expectedMessage =
            String.format("Resource of type 'AWS::SSM::Association' with identifier 'Name=%s,InstanceId=%s' was not found.",
                modelWithoutId.getName(), modelWithoutId.getInstanceId());
        assertEquals(expectedMessage, cfnException.getMessage());
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
                model);

        assertTrue(cfnException instanceof CfnServiceInternalErrorException);
        final String expectedMessage = "Internal error reported from downstream service during operation 'DescribeAssociation'.";
        assertEquals(expectedMessage, cfnException.getMessage());
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
                model);

        assertTrue(cfnException instanceof CfnInvalidRequestException);
        final String expectedMessage = String.format("Invalid request provided: %s", request.toString());
        assertEquals(expectedMessage, cfnException.getMessage());
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
                model);

        assertTrue(cfnException instanceof CfnInvalidRequestException);
        final String expectedMessage = String.format("Invalid request provided: %s", request.toString());
        assertEquals(expectedMessage, cfnException.getMessage());
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
                model);

        assertTrue(cfnException instanceof CfnInvalidRequestException);
        final String expectedMessage = String.format("Invalid request provided: %s", request.toString());
        assertEquals(expectedMessage, cfnException.getMessage());
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
                model);

        assertTrue(cfnException instanceof CfnInvalidRequestException);
        final String expectedMessage = String.format("Invalid request provided: %s", request.toString());
        assertEquals(expectedMessage, cfnException.getMessage());
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
                model);

        assertTrue(cfnException instanceof CfnThrottlingException);
        final String expectedMessage = "Rate exceeded for operation 'UpdateAssociation'.";
        assertEquals(expectedMessage, cfnException.getMessage());
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
                model);

        assertTrue(cfnException instanceof CfnGeneralServiceException);
        final String expectedMessage = "Error occurred during operation 'UpdateAssociation'.";
        assertEquals(expectedMessage, cfnException.getMessage());
    }
}
