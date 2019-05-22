package com.aws.ssm.association;

import com.aws.cfn.proxy.AmazonWebServicesClientProxy;
import com.aws.cfn.proxy.HandlerErrorCode;
import com.aws.cfn.proxy.Logger;
import com.aws.cfn.proxy.ProgressEvent;
import com.aws.cfn.proxy.OperationStatus;
import com.aws.cfn.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.ssm.model.UpdateAssociationRequest;
import software.amazon.awssdk.services.ssm.model.UpdateAssociationResponse;

public class UpdateHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final ResourceModel previousModel = request.getPreviousResourceState();

        if (ImmutabilityAnalyzer.isImmutableChange(previousModel, model)) {
            logger.log(String.format("Immutable update for AWS::SSM::Association {%s} with ClientToken %s", model.getAssociationId(), request.getClientRequestToken()));
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .errorCode(HandlerErrorCode.NotUpdatable)
                    .status(OperationStatus.FAILED)
                    .build();
        }

        try {
            final UpdateAssociationRequest updateAssociationRequest = UpdateAssociationRequest
                    .builder()
                    .associationId(previousModel.getAssociationId())
                    .associationName(model.getAssociationName())
                    .documentVersion(model.getDocumentVersion())
                    .outputLocation(Utils.translateInstanceAssociationOutputLocation(model.getOutputLocation()))
                    .parameters(Utils.getMapFromParameters(model.getParameters()))
                    .scheduleExpression(model.getScheduleExpression())
                    .build();
            final UpdateAssociationResponse response = proxy.injectCredentialsAndInvokeV2(updateAssociationRequest, ClientBuilder.getSsmClient()::updateAssociation);
            logger.log(String.format("Successfully update AWS::SSM::Association of {%s} with Request Id %s and ClientToken %s", model.getAssociationId(), response.responseMetadata().requestId(), request.getClientRequestToken()));
            return Utils.defaultSuccessHandler(model);
        } catch (Exception e) {
            logger.log(String.format("Failed to update AWS::SSM::Association of {%s}, caused by Exception {%s} with ClientToken %s", model.getAssociationId(), e.toString(), request.getClientRequestToken()));
            return Utils.defaultFailureHandler(e, null);
        }
    }
}
