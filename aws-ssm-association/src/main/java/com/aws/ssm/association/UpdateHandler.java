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
import com.amazonaws.services.simplesystemsmanagement.model.AssociationDescription;
import com.amazonaws.services.simplesystemsmanagement.model.AssociationDoesNotExistException;
import com.amazonaws.services.simplesystemsmanagement.model.AssociationVersionLimitExceededException;
import com.amazonaws.services.simplesystemsmanagement.model.InternalServerErrorException;
import com.amazonaws.services.simplesystemsmanagement.model.InvalidAssociationVersionException;
import com.amazonaws.services.simplesystemsmanagement.model.InvalidDocumentException;
import com.amazonaws.services.simplesystemsmanagement.model.InvalidDocumentVersionException;
import com.amazonaws.services.simplesystemsmanagement.model.InvalidOutputLocationException;
import com.amazonaws.services.simplesystemsmanagement.model.InvalidParametersException;
import com.amazonaws.services.simplesystemsmanagement.model.InvalidScheduleException;
import com.amazonaws.services.simplesystemsmanagement.model.InvalidTargetException;
import com.amazonaws.services.simplesystemsmanagement.model.InvalidUpdateException;
import com.amazonaws.services.simplesystemsmanagement.model.Target;
import com.amazonaws.services.simplesystemsmanagement.model.TooManyUpdatesException;
import com.amazonaws.services.simplesystemsmanagement.model.UpdateAssociationRequest;
import com.amazonaws.util.StringUtils;
import com.aws.ssm.association.util.ResourceModelServiceModelConverter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import java.util.List;
import java.util.stream.Collectors;

public class UpdateHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        logger.log(String.format("Processing UpdateHandler request %s", request));

        final ResourceModel requestModel = request.getDesiredResourceState();

        final ProgressEvent<ResourceModel, CallbackContext> pe = new ProgressEvent<>();
        pe.setResourceModel(request.getPreviousResourceState());
        pe.setStatus(OperationStatus.FAILED);

        final String associationId = requestModel.getAssociationId();

        if (StringUtils.isNullOrEmpty(associationId)) {
            pe.setErrorCode(HandlerErrorCode.InvalidRequest);
            pe.setMessage("AssociationId must be present to update the existing association.");
            return pe;
        }

        final UpdateAssociationRequest updateAssociationRequest =
            resourceModelToUpdateAssociationRequest(requestModel);

        final AWSSimpleSystemsManagement client =
            AWSSimpleSystemsManagementClientBuilder.standard()
                .withClientConfiguration(new ClientConfigurationFactory().getConfig())
                .build();

        try {
            final AssociationDescription association =
                proxy.injectCredentialsAndInvoke(updateAssociationRequest, client::updateAssociation)
                    .getAssociationDescription();

            final ResourceModel updatedModel =
                ResourceModelServiceModelConverter.associationDescriptionToResourceModel(association);

            pe.setResourceModel(updatedModel);
            pe.setStatus(OperationStatus.SUCCESS);

        } catch (AssociationDoesNotExistException e) {
            pe.setErrorCode(HandlerErrorCode.NotFound);
            pe.setMessage(e.getMessage());
        } catch (InvalidScheduleException
            | InvalidParametersException
            | InvalidOutputLocationException
            | InvalidDocumentVersionException
            | InvalidDocumentException
            | InvalidTargetException
            | InvalidAssociationVersionException e) {
            pe.setErrorCode(HandlerErrorCode.InvalidRequest);
            pe.setMessage(e.getMessage());
        } catch (InvalidUpdateException e) {
            pe.setErrorCode(HandlerErrorCode.NotUpdatable);
            pe.setMessage(e.getMessage());
        } catch (TooManyUpdatesException e) {
            pe.setErrorCode(HandlerErrorCode.Throttling);
            pe.setMessage(e.getMessage());
        } catch (AssociationVersionLimitExceededException e) {
            pe.setErrorCode(HandlerErrorCode.ServiceLimitExceeded);
            pe.setMessage(e.getMessage());
        } catch (InternalServerErrorException e) {
            pe.setErrorCode(HandlerErrorCode.ServiceInternalError);
            pe.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.log(e.getMessage());

            pe.setErrorCode(HandlerErrorCode.GeneralServiceException);
            pe.setMessage("Unknown failure occurred.");
        }

        return pe;
    }

    private UpdateAssociationRequest resourceModelToUpdateAssociationRequest(final ResourceModel model) {

        final UpdateAssociationRequest updateAssociationRequest =
            new UpdateAssociationRequest()
                .withAssociationId(model.getAssociationId());

        if (!StringUtils.isNullOrEmpty(model.getName())) {
            updateAssociationRequest.setName(model.getName());
        }

        if (!StringUtils.isNullOrEmpty(model.getAssociationName())) {
            updateAssociationRequest.setAssociationName(model.getAssociationName());
        }

        if (!StringUtils.isNullOrEmpty(model.getDocumentVersion())) {
            updateAssociationRequest.setDocumentVersion(model.getDocumentVersion());
        }

        if (MapUtils.isNotEmpty(model.getParameters())) {
            updateAssociationRequest.setParameters(model.getParameters());
        }

        if (!StringUtils.isNullOrEmpty(model.getScheduleExpression())) {
            updateAssociationRequest.setScheduleExpression(model.getScheduleExpression());
        }

        if (CollectionUtils.isNotEmpty(model.getTargets())) {
            final List<Target> convertedTargets =
                model.getTargets().stream()
                    .map(t -> new Target().withKey(t.getKey()).withValues(t.getValues()))
                    .collect(Collectors.toList());

            updateAssociationRequest.setTargets(convertedTargets);
        }

        final InstanceAssociationOutputLocation resourceModelOutputLocation =
            model.getOutputLocation();

        if (resourceModelOutputLocation != null
            && resourceModelOutputLocation.getS3Location() != null) {

            final S3OutputLocation s3Location = resourceModelOutputLocation.getS3Location();

            final com.amazonaws.services.simplesystemsmanagement.model.S3OutputLocation serviceModelS3Location =
                new com.amazonaws.services.simplesystemsmanagement.model.S3OutputLocation()
                    .withOutputS3BucketName(s3Location.getOutputS3BucketName())
                    .withOutputS3Region(s3Location.getOutputS3Region())
                    .withOutputS3KeyPrefix(s3Location.getOutputS3KeyPrefix());

            final com.amazonaws.services.simplesystemsmanagement.model.InstanceAssociationOutputLocation serviceModelOutputLocation =
                new com.amazonaws.services.simplesystemsmanagement.model.InstanceAssociationOutputLocation()
                    .withS3Location(serviceModelS3Location);

            updateAssociationRequest.setOutputLocation(serviceModelOutputLocation);
        }

        if (!StringUtils.isNullOrEmpty(model.getAutomationTargetParameterName())) {
            updateAssociationRequest.setAutomationTargetParameterName(model.getAutomationTargetParameterName());
        }

        if (!StringUtils.isNullOrEmpty(model.getMaxErrors())) {
            updateAssociationRequest.setMaxErrors(model.getMaxErrors());
        }

        if (!StringUtils.isNullOrEmpty(model.getMaxConcurrency())) {
            updateAssociationRequest.setMaxConcurrency(model.getMaxConcurrency());
        }

        if (!StringUtils.isNullOrEmpty(model.getComplianceSeverity())) {
            updateAssociationRequest.setComplianceSeverity(model.getComplianceSeverity());
        }

        return updateAssociationRequest;
    }
}
