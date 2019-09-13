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
import com.amazonaws.services.simplesystemsmanagement.model.AssociationDoesNotExistException;
import com.amazonaws.services.simplesystemsmanagement.model.DeleteAssociationRequest;
import com.amazonaws.services.simplesystemsmanagement.model.InternalServerErrorException;
import com.amazonaws.services.simplesystemsmanagement.model.InvalidDocumentException;
import com.amazonaws.services.simplesystemsmanagement.model.InvalidInstanceIdException;
import com.amazonaws.services.simplesystemsmanagement.model.TooManyUpdatesException;
import com.amazonaws.util.StringUtils;

public class DeleteHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final ProgressEvent<ResourceModel, CallbackContext> pe =
            ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .build();

        pe.setStatus(OperationStatus.FAILED);

        DeleteAssociationRequest deleteAssociationRequest = new DeleteAssociationRequest();

        if (!StringUtils.isNullOrEmpty(model.getAssociationId())) {

            deleteAssociationRequest.setAssociationId(model.getAssociationId());

        } else if (!StringUtils.isNullOrEmpty(model.getInstanceId())
            && !StringUtils.isNullOrEmpty(model.getName())) {

            deleteAssociationRequest
                .withInstanceId(model.getInstanceId())
                .setName(model.getName());
        } else {
            // fails delete request validation
            pe.setErrorCode(HandlerErrorCode.InvalidRequest);
            pe.setMessage("AssociationId, or InstanceId and Document Name must be specified to delete an association.");
            return pe;
        }

        final AWSSimpleSystemsManagement client =
            AWSSimpleSystemsManagementClientBuilder.standard()
                .withClientConfiguration(new ClientConfigurationFactory().getConfig())
                .build();

        try {
            proxy.injectCredentialsAndInvoke(deleteAssociationRequest, client::deleteAssociation);
            pe.setStatus(OperationStatus.SUCCESS);
        } catch (AssociationDoesNotExistException e) {
            pe.setStatus(OperationStatus.SUCCESS);
        } catch (TooManyUpdatesException e) {
            pe.setErrorCode(HandlerErrorCode.Throttling);
            pe.setMessage(e.getMessage());
        } catch (InvalidDocumentException
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

        if (pe.isSuccess()) {
            // nullify the model if delete succeeded
            pe.setResourceModel(null);
        }

        return pe;
    }
}
