package com.aws.ssm.association;

import com.aws.cfn.proxy.AmazonWebServicesClientProxy;
import com.aws.cfn.proxy.Logger;
import com.aws.cfn.proxy.ProgressEvent;
import com.aws.cfn.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.ssm.model.DeleteAssociationRequest;
import software.amazon.awssdk.services.ssm.model.DeleteAssociationResponse;

public class DeleteHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final ProgressEvent<ResourceModel, CallbackContext> pe = new ProgressEvent<>();
        try {
            final DeleteAssociationRequest deleteAssociationRequest = DeleteAssociationRequest
                    .builder()
                    .associationId(model.getAssociationId())
                    .build();
            final DeleteAssociationResponse response = proxy.injectCredentialsAndInvokeV2(deleteAssociationRequest, ClientBuilder.getSsmClient()::deleteAssociation);
            logger.log(String.format("Successfully delete AWS::SSM::Association of {%s} with Request Id %s and Client Token %s", model, response.responseMetadata().requestId(), request.getClientRequestToken()));
            return Utils.defaultSuccessHandler(pe, model);
        } catch (Exception e) {
            logger.log(String.format("Failed to delete AWS::SSM::Association of {%s}, caused by Exception {%s} with Client Token %s", model, e.toString(), request.getClientRequestToken()));
            return Utils.defaultFailureHandler(pe, e, null);
        }
    }
}
