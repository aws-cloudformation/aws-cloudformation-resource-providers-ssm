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
import com.amazonaws.services.simplesystemsmanagement.model.DescribeAssociationRequest;
import com.amazonaws.services.simplesystemsmanagement.model.InternalServerErrorException;
import com.amazonaws.services.simplesystemsmanagement.model.InvalidAssociationVersionException;
import com.amazonaws.services.simplesystemsmanagement.model.InvalidDocumentException;
import com.amazonaws.services.simplesystemsmanagement.model.InvalidInstanceIdException;
import com.amazonaws.util.StringUtils;
import com.aws.ssm.association.util.ResourceModelServiceModelConverter;

public class ReadHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        logger.log(String.format("Processing ReadHandler request %s", request));

        final ResourceModel requestModel = request.getDesiredResourceState();

        final ProgressEvent<ResourceModel, CallbackContext> pe = new ProgressEvent<>();
        pe.setStatus(OperationStatus.FAILED);

        final String associationId = requestModel.getAssociationId();

        if (StringUtils.isNullOrEmpty(associationId)) {
            pe.setErrorCode(HandlerErrorCode.InvalidRequest);
            pe.setMessage("AssociationId must be present to read the existing association.");
            return pe;
        }

        final AWSSimpleSystemsManagement client =
            AWSSimpleSystemsManagementClientBuilder.standard()
                .withClientConfiguration(new ClientConfigurationFactory().getConfig())
                .build();

        final DescribeAssociationRequest describeAssociationRequest =
            new DescribeAssociationRequest()
                .withAssociationId(associationId);

        try {
            final AssociationDescription association =
                proxy.injectCredentialsAndInvoke(describeAssociationRequest, client::describeAssociation)
                    .getAssociationDescription();

            final ResourceModel existingModel =
                ResourceModelServiceModelConverter.associationDescriptionToResourceModel(association);

            pe.setResourceModel(existingModel);
            pe.setStatus(OperationStatus.SUCCESS);

        } catch (AssociationDoesNotExistException e) {
            pe.setErrorCode(HandlerErrorCode.NotFound);
            pe.setMessage(e.getMessage());
        } catch (InvalidAssociationVersionException
            | InvalidDocumentException
            | InvalidInstanceIdException e) {
            pe.setErrorCode(HandlerErrorCode.InvalidRequest);
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
}
