package software.amazon.ssm.patchbaseline;

import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.resource.IdentifierUtils;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.CreatePatchBaselineRequest;
import software.amazon.awssdk.services.ssm.model.CreatePatchBaselineResponse;
import software.amazon.awssdk.services.ssm.model.PatchStatus;



public class CreateHandler extends BaseHandler<CallbackContext> {

    private final SsmClient ssmClient;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final CallbackContext context = callbackContext == null ? CallbackContext.builder().build() : callbackContext;

        final ResourceModel model = request.getDesiredResourceState();

        // Set model primary ID if absent
        if(model.getId() == null) {
            model.setId(IdentifierUtils.generateResourceIdentifier(
                    request.getLogicalResourceIdentifier(),
                    request.getClientRequestToken()
            ));
        }

        final CreatePatchBaselineRequest createPatchBaselineRequest = CreatePatchBaseline(model);
        final CreatePatchBaselineResponse createPatchBaselineResponse;
        try {
            createPatchBaselineResponse = proxy.injectCredentialsAndInvokeV2(createPatchBaselineRequest,
                    ssmClient::createPatchBaseline);
        }



        // TODO : put your code here

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }

    private OperationStatus getOperationStatus(@NonNull final PatchStatus status) {
        switch (status) {
            case ACTIVE:
                return OperationStatus.SUCCESS;
            case CREATING:
                return OperationStatus.IN_PROGRESS;
            default:
                return OperationStatus.FAILED;
        }
    }

    private CreatePatchBaselineRequest CreatePatchBaseline(@NonNull final ResourceModel model) {

        return CreatePatchBaselineRequest.builder()
                .withApprovalRules(model.getApprovalRules())
                .withApprovedPatches(model.getApprovedPatches())
                .withApprovedPatchesComplianceLevel(model.getApprovedPatchesComplianceLevel())
                .withApprovedPatchesEnableNonSecurity(model.isApprovedPatchesEnableNonSecurity())
                .withGlobalFilters(model.getGlobalFilters())
                .withOperatingSystem(model.getOperatingSystem())
                .withRejectedPatches(model.getRejectedPatches())
                .withRejectedPatchesAction(model.getRejectedPatchesAction())
                .withTags(model.getTags())
                .withName(model.getName())
                .withSources(model.getSources())
                .withDescription(model.getDescription())
                .withPatchGroups(model.getPatchGroups())
                .build();
    }


}

