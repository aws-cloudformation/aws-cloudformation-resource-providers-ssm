package com.aws.ssm.association;

import com.aws.cfn.proxy.AmazonWebServicesClientProxy;
import com.aws.cfn.proxy.Logger;
import com.aws.cfn.proxy.ProgressEvent;
import com.aws.cfn.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.ssm.model.CreateAssociationRequest;
import software.amazon.awssdk.services.ssm.model.CreateAssociationResponse;

public class CreateHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final ProgressEvent<ResourceModel, CallbackContext> pe = new ProgressEvent<>();
        try {
            final CreateAssociationRequest createAssociationRequest = CreateAssociationRequest
                    .builder()
                    .associationName(model.getAssociationName())
                    .documentVersion(model.getDocumentVersion())
                    .instanceId(model.getInstanceId())
                    .name(model.getName())
                    .outputLocation(Utils.translateInstanceAssociationOutputLocation(model.getOutputLocation()))
                    .parameters(Utils.getMapFromParameters(model.getParameters()))
                    .scheduleExpression(model.getScheduleExpression())
                    .targets(Utils.translateTargetCollection(model.getTargets()))
                    .build();
            final CreateAssociationResponse response = proxy.injectCredentialsAndInvokeV2(createAssociationRequest, ClientBuilder.getSsmClient()::createAssociation);
            model.setAssociationId(response.associationDescription().associationId());
            logger.log(String.format("Successfully delete AWS::SSM::Association of {%s} with Request Id %s and Client Token %s", model, response.responseMetadata().requestId(), request.getClientRequestToken()));
            return Utils.defaultSuccessHandler(pe, model);
        } catch (Exception e) {
            logger.log(String.format("Failed to create AWS::SSM::Association of {%s}, caused by Exception {%s} with Client Token %s", model, e.toString(), request.getClientRequestToken()));
            return Utils.defaultFailureHandler(pe, e, null);
        }
    }
}
