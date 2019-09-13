package com.aws.ssm.association;

import com.amazonaws.ClientConfigurationFactory;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.HandlerErrorCode;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.AssociationAlreadyExistsException;
import com.amazonaws.services.simplesystemsmanagement.model.AssociationDescription;
import com.amazonaws.services.simplesystemsmanagement.model.AssociationDoesNotExistException;
import com.amazonaws.services.simplesystemsmanagement.model.AssociationLimitExceededException;
import com.amazonaws.services.simplesystemsmanagement.model.AssociationStatusName;
import com.amazonaws.services.simplesystemsmanagement.model.CreateAssociationRequest;
import com.amazonaws.services.simplesystemsmanagement.model.DescribeAssociationRequest;
import com.amazonaws.services.simplesystemsmanagement.model.InternalServerErrorException;
import com.amazonaws.services.simplesystemsmanagement.model.InvalidAssociationVersionException;
import com.amazonaws.services.simplesystemsmanagement.model.InvalidDocumentException;
import com.amazonaws.services.simplesystemsmanagement.model.InvalidDocumentVersionException;
import com.amazonaws.services.simplesystemsmanagement.model.InvalidInstanceIdException;
import com.amazonaws.services.simplesystemsmanagement.model.InvalidOutputLocationException;
import com.amazonaws.services.simplesystemsmanagement.model.InvalidParametersException;
import com.amazonaws.services.simplesystemsmanagement.model.InvalidScheduleException;
import com.amazonaws.services.simplesystemsmanagement.model.InvalidTargetException;
import com.amazonaws.services.simplesystemsmanagement.model.UnsupportedPlatformTypeException;
import com.amazonaws.util.StringUtils;
import com.aws.ssm.association.util.ResourceModelServiceModelConverter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import java.util.List;
import java.util.stream.Collectors;

public class CreateHandler extends BaseHandler<CallbackContext> {

    private static final int CALLBACK_DELAY_SECONDS = 15;
    private static final int NUM_STABILIZATION_RETRIES = 10;
    private static final AWSSimpleSystemsManagement SSM_CLIENT =
        AWSSimpleSystemsManagementClientBuilder.standard()
            .withClientConfiguration(new ClientConfigurationFactory().getConfig())
            .build();

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        if (callbackContext != null) {
            return handleInProgressCreateRequest(proxy, logger, callbackContext);
        } else {
            return handleInitialCreateRequest(proxy, logger, request.getDesiredResourceState());
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> handleInitialCreateRequest(
        final AmazonWebServicesClientProxy proxy,
        final Logger logger,
        final ResourceModel desiredModel) {

        if (StringUtils.isNullOrEmpty(desiredModel.getName())) {
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .status(OperationStatus.FAILED)
                .errorCode(HandlerErrorCode.InvalidRequest)
                .message("Document name must be specified to create an association.")
                .build();
        }

        final CreateAssociationRequest createAssociationRequest = resourceModelToCreateAssociationRequest(desiredModel);

        final AssociationDescription resultAssociationDescription;

        try {
            resultAssociationDescription =
                proxy.injectCredentialsAndInvoke(createAssociationRequest, SSM_CLIENT::createAssociation)
                    .getAssociationDescription();
        } catch (AssociationAlreadyExistsException e) {
            return ProgressEvent.defaultFailureHandler(e, HandlerErrorCode.AlreadyExists);
        } catch (AssociationLimitExceededException e) {
            return ProgressEvent.defaultFailureHandler(e, HandlerErrorCode.ServiceLimitExceeded);
        } catch (InvalidDocumentException
            | InvalidDocumentVersionException
            | InvalidInstanceIdException
            | UnsupportedPlatformTypeException
            | InvalidOutputLocationException
            | InvalidParametersException
            | InvalidTargetException
            | InvalidScheduleException e) {

            return ProgressEvent.defaultFailureHandler(e, HandlerErrorCode.InvalidRequest);
        } catch (InternalServerErrorException e) {
            return ProgressEvent.defaultFailureHandler(e, HandlerErrorCode.ServiceInternalError);
        } catch (Exception e) {
            logger.log(e.getMessage());

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .status(OperationStatus.FAILED)
                .errorCode(HandlerErrorCode.GeneralServiceException)
                .message("Unknown failure occurred.")
                .build();
        }

        final ResourceModel resultModel =
            ResourceModelServiceModelConverter.associationDescriptionToResourceModel(resultAssociationDescription);

        if (desiredModel.getWaitForAssociationSuccess() != null
            && desiredModel.getWaitForAssociationSuccess()) {
            // indicates a Create request that needs to wait for association to complete
            return ProgressEvent.defaultInProgressHandler(
                CallbackContext.builder()
                    .stabilizationRetriesRemaining(NUM_STABILIZATION_RETRIES)
                    .associationDescription(resultAssociationDescription)
                    .build(),
                CALLBACK_DELAY_SECONDS,
                resultModel);
        } else {
            // return success without waiting for association to complete
            return ProgressEvent.defaultSuccessHandler(resultModel);
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> handleInProgressCreateRequest(
        final AmazonWebServicesClientProxy proxy,
        final Logger logger,
        final CallbackContext callbackContext) {

        final AssociationDescription callbackContextAssociation = callbackContext.getAssociationDescription();

        final DescribeAssociationRequest describeAssociationRequest =
            new DescribeAssociationRequest()
                .withAssociationId(callbackContextAssociation.getAssociationId());

        final AssociationDescription requestAssociation;

        try {
            requestAssociation =
                proxy.injectCredentialsAndInvoke(describeAssociationRequest, SSM_CLIENT::describeAssociation)
                    .getAssociationDescription();

        } catch (AssociationDoesNotExistException e) {
            return ProgressEvent.defaultFailureHandler(e, HandlerErrorCode.NotFound);
        } catch (InvalidAssociationVersionException
            | InvalidDocumentException
            | InvalidInstanceIdException e) {

            return ProgressEvent.defaultFailureHandler(e, HandlerErrorCode.InvalidRequest);
        } catch (InternalServerErrorException e) {
            return ProgressEvent.defaultFailureHandler(e, HandlerErrorCode.ServiceInternalError);
        } catch (Exception e) {
            logger.log(e.getMessage());

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .status(OperationStatus.FAILED)
                .errorCode(HandlerErrorCode.GeneralServiceException)
                .message("Unknown failure occurred.")
                .build();
        }

        final ResourceModel existingModel =
            ResourceModelServiceModelConverter.associationDescriptionToResourceModel(requestAssociation);

        if (AssociationStatusName.Success.name()
            .equalsIgnoreCase(requestAssociation.getOverview().getStatus())) {

            return ProgressEvent.defaultSuccessHandler(existingModel);

        } else if (AssociationStatusName.Pending.name()
            .equalsIgnoreCase(requestAssociation.getOverview().getStatus())) {

            final int stabilizationRetriesRemaining = callbackContext.getStabilizationRetriesRemaining() - 1;

            if (stabilizationRetriesRemaining < 1) {
                return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.FAILED)
                    .errorCode(HandlerErrorCode.NotStabilized)
                    .message("Timed out waiting for association to succeed.")
                    .build();
            }

            return ProgressEvent.defaultInProgressHandler(
                CallbackContext.builder()
                    .stabilizationRetriesRemaining(stabilizationRetriesRemaining)
                    .associationDescription(requestAssociation)
                    .build(),
                CALLBACK_DELAY_SECONDS,
                existingModel);

        } else {

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .status(OperationStatus.FAILED)
                .errorCode(HandlerErrorCode.NotStabilized)
                .message(String.format(
                    "Association failed; detailed status: %s",
                    requestAssociation.getOverview().getDetailedStatus()))
                .build();
        }
    }

    private CreateAssociationRequest resourceModelToCreateAssociationRequest(final ResourceModel model) {

        final CreateAssociationRequest createAssociationRequest =
            new CreateAssociationRequest()
                .withName(model.getName());

        if (!StringUtils.isNullOrEmpty(model.getAssociationName())) {
            createAssociationRequest.setAssociationName(model.getAssociationName());
        }

        if (!StringUtils.isNullOrEmpty(model.getDocumentVersion())) {
            createAssociationRequest.setDocumentVersion(model.getDocumentVersion());
        }

        if (!StringUtils.isNullOrEmpty(model.getInstanceId())) {
            createAssociationRequest.setInstanceId(model.getInstanceId());
        }

        if (MapUtils.isNotEmpty(model.getParameters())) {
            createAssociationRequest.setParameters(model.getParameters());
        }

        if (!StringUtils.isNullOrEmpty(model.getScheduleExpression())) {
            createAssociationRequest.setScheduleExpression(model.getScheduleExpression());
        }

        if (CollectionUtils.isNotEmpty(model.getTargets())) {
            final List<com.amazonaws.services.simplesystemsmanagement.model.Target> convertedTargets =
                model.getTargets().stream()
                    .map(t -> new com.amazonaws.services.simplesystemsmanagement.model.Target()
                        .withKey(t.getKey())
                        .withValues(t.getValues()))
                    .collect(Collectors.toList());

            createAssociationRequest.setTargets(convertedTargets);
        }

        final InstanceAssociationOutputLocation resourceModelOutputLocation = model.getOutputLocation();

        if (resourceModelOutputLocation != null
            && resourceModelOutputLocation.getS3Location() != null) {

            final S3OutputLocation s3Location = resourceModelOutputLocation.getS3Location();

            com.amazonaws.services.simplesystemsmanagement.model.S3OutputLocation serviceModelS3Location =
                new com.amazonaws.services.simplesystemsmanagement.model.S3OutputLocation()
                    .withOutputS3BucketName(s3Location.getOutputS3BucketName())
                    .withOutputS3Region(s3Location.getOutputS3Region())
                    .withOutputS3KeyPrefix(s3Location.getOutputS3KeyPrefix());

            com.amazonaws.services.simplesystemsmanagement.model.InstanceAssociationOutputLocation serviceModelOutputLocation =
                new com.amazonaws.services.simplesystemsmanagement.model.InstanceAssociationOutputLocation()
                    .withS3Location(serviceModelS3Location);

            createAssociationRequest.setOutputLocation(serviceModelOutputLocation);
        }

        if (!StringUtils.isNullOrEmpty(model.getAutomationTargetParameterName())) {
            createAssociationRequest.setAutomationTargetParameterName(model.getAutomationTargetParameterName());
        }

        if (!StringUtils.isNullOrEmpty(model.getMaxErrors())) {
            createAssociationRequest.setMaxErrors(model.getMaxErrors());
        }

        if (!StringUtils.isNullOrEmpty(model.getMaxConcurrency())) {
            createAssociationRequest.setMaxConcurrency(model.getMaxConcurrency());
        }

        if (!StringUtils.isNullOrEmpty(model.getComplianceSeverity())) {
            createAssociationRequest.setComplianceSeverity(model.getComplianceSeverity());
        }

        return createAssociationRequest;
    }
}
