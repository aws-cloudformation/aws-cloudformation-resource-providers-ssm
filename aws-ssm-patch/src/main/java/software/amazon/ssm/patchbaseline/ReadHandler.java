package software.amazon.ssm.patchbaseline;

import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import static software.amazon.ssm.patchbaseline.ResourceModel.TYPE_NAME;
import software.amazon.ssm.patchbaseline.utils.SsmClientBuilder;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetPatchBaselineRequest;
import software.amazon.awssdk.services.ssm.model.GetPatchBaselineResponse;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.HashMap;
import java.util.Map;


public class ReadHandler extends BaseHandler<CallbackContext> {

    private static final SsmClient ssmClient = SsmClientBuilder.getClient();

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final String baselineId = model.getId();

        logger.log(String.format(
                "INFO Activity %s request with clientRequestToken: %s %n", TYPE_NAME, request.getClientRequestToken()));

        try {
            GetPatchBaselineRequest getPatchBaselineRequest = GetPatchBaselineRequest.builder()
                                                                        .baselineId(baselineId)
                                                                        .build();

            GetPatchBaselineResponse getPatchBaselineResponse =
                    proxy.injectCredentialsAndInvokeV2(getPatchBaselineRequest, ssmClient::getPatchBaseline);

//            //Turn the result into JSON
//            TypeReference<ResourceModel> typeRef = new TypeReference<ResourceModel>() {};
//            ObjectMapper om = MapperFactory.buildJsonMapper();
//            ResourceModel resourcemodel = om.convertValue(getPatchBaselineResponse, typeRef);

            //Send a success response to CloudFormation with the JSON
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .status(OperationStatus.SUCCESS)
                    .build();
        } catch (Exception e) {
            return Resource.handleException(e, model, baselineId, logger);
        }
    }

//    static ResourceModel translateToResourceModel(GetPatchBaselineResponse getPatchBaselineResponse) {
//        final ResourceModel model = new ResourceModel();
//
//        model.setId(getPatchBaselineResponse.baselineId());
//        model.setName(getPatchBaselineResponse.name());
//        model.setOperatingSystem(getPatchBaselineResponse.operatingSystemAsString());
//        model.setDescription(getPatchBaselineResponse.description());
//        model.setApprovalRules(getPatchBaselineResponse.approvalRules());
//        model.setSources(getPatchBaselineResponse.sources());
//        model.setRejectedPatches(getPatchBaselineResponse.rejectedPatches());
//        model.setApprovedPatches(getPatchBaselineResponse.approvedPatches());
//        model.setRejectedPatchesAction(getPatchBaselineResponse.rejectedPatchesActionAsString());
//        model.setPatchGroups(getPatchBaselineResponse.patchGroups());
//        model.setApprovedPatchesComplianceLevel(getPatchBaselineResponse.approvedPatchesComplianceLevelAsString());
//        model.setApprovedPatchesEnableNonSecurity(getPatchBaselineResponse.approvedPatchesEnableNonSecurity());
//        model.setGlobalFilters(getPatchBaselineResponse.globalFilters());
//
//        return model;
//    }

//    /**
//     * Validates an input String and returns non-empty Optional with the same parameter
//     * if the validation is passed; otherwise, Optional.empty() is returned.
//     *
//     * @param parameter String parameter to validate.
//     * @return Optional with the same value as the input parameter after validation;
//     * returns Optional.empty() if the parameter is empty/null.
//     */
//    private static Optional<String> getValidatedString(final String parameter) {
//        if (StringUtils.isNullOrEmpty(parameter)) {
//            return Optional.empty();
//        } else {
//            return Optional.of(parameter);
//        }
//    }
}
