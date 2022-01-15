package software.amazon.ssm.patchbaseline;

import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.ssm.model.PatchOrchestratorFilter;
import software.amazon.awssdk.services.ssm.model.DescribePatchBaselinesRequest;
import software.amazon.awssdk.services.ssm.model.DescribePatchBaselinesResponse;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.ssm.patchbaseline.utils.SsmClientBuilder;
import software.amazon.awssdk.services.ssm.model.SsmException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.google.common.annotations.VisibleForTesting;

public class ListHandler extends BaseHandler<CallbackContext> {

    private static final SsmClient ssmClient = SsmClientBuilder.getClient();
    private static final Integer MAX_RESULTS = 50;

    private ReadHandler readHandler;

    public ListHandler() {
        readHandler = new ReadHandler();
    }

    @VisibleForTesting
    void setReadHandler(final ReadHandler readHandler) {
        this.readHandler = readHandler;
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        final List<ResourceModel> models = new ArrayList<>();

        DescribePatchBaselinesResponse describePatchBaselinesResponse = describePatchBaselinesResponse(proxy);

        if (describePatchBaselinesResponse.hasBaselineIdentities()) {
            models.addAll(getResourceModelFromResponse(describePatchBaselinesResponse, request, proxy, logger));
        }

        while (describePatchBaselinesResponse.nextToken() != null) {
            describePatchBaselinesResponse = describePatchBaselinesResponse(describePatchBaselinesResponse.nextToken(), proxy);
            if (describePatchBaselinesResponse.hasBaselineIdentities()) {
                models.addAll(getResourceModelFromResponse(describePatchBaselinesResponse, request, proxy, logger));
            }
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(models)
                .status(OperationStatus.SUCCESS)
                .build();
    }

    /**
     * Function for calling ssm::describePatchBaselines API and handle corresponded Exception without nextToken.
     *
     * @param proxy AmazonWebServicesClientProxy
     * @return DescribePatchBaselinesResponse
     */
    private DescribePatchBaselinesResponse describePatchBaselinesResponse(final AmazonWebServicesClientProxy proxy) {

        DescribePatchBaselinesRequest describePatchBaselinesRequest = DescribePatchBaselinesRequest.builder()
                .maxResults(MAX_RESULTS)
                .build();

        try {
            return proxy.injectCredentialsAndInvokeV2(describePatchBaselinesRequest, ssmClient::describePatchBaselines);
        } catch (Exception e) {
            throw new CfnGeneralServiceException(e.getMessage(), e);
        }
    }

    /**
     * Function for calling ssm::describePatchBaselines API and handle corresponded Exception with nextToken.
     *
     * @parem nextToken nextToken
     * @param proxy AmazonWebServicesClientProxy
     * @return DescribePatchBaselinesResponse
     */
    private DescribePatchBaselinesResponse describePatchBaselinesResponse(final String nextToken,
                                                                          final AmazonWebServicesClientProxy proxy) {

        DescribePatchBaselinesRequest describePatchBaselinesRequest = DescribePatchBaselinesRequest.builder()
                .nextToken(nextToken)
                .maxResults(MAX_RESULTS)
                .build();

        try {
            return proxy.injectCredentialsAndInvokeV2(describePatchBaselinesRequest, ssmClient::describePatchBaselines);
        } catch (Exception e) {
            throw new CfnGeneralServiceException(e.getMessage(), e);
        }
    }

    /**
     * DescribePatchBaselinesResponse returns limited information about resource model.
     * Use ReadHandler to read full template based on given baselineId.
     *
     * @param describePatchBaselinesResponse describePatchBaselinesResponse
     * @param request ResourceHandlerRequest
     * @return List<ResourceModel>
     */
    private List<ResourceModel> getResourceModelFromResponse(final DescribePatchBaselinesResponse describePatchBaselinesResponse,
                                                             final ResourceHandlerRequest<ResourceModel> request,
                                                             final AmazonWebServicesClientProxy proxy,
                                                             final Logger logger) {

        final List<ResourceModel> models = new ArrayList<>();
        final List<String> baselineIdList = describePatchBaselinesResponse
                .baselineIdentities()
                .stream().map(baseline -> baseline.baselineId()).collect(Collectors.toList());

        for (String baselineId : baselineIdList) {
            ResourceModel model = ResourceModel.builder()
                    .id(baselineId)
                    .build();
            ResourceHandlerRequest<ResourceModel> requestFromId = ResourceHandlerRequest.<ResourceModel>builder()
                    .desiredResourceState(model)
                    .clientRequestToken(request.getClientRequestToken())
                    .build();
            ProgressEvent<ResourceModel, CallbackContext> response
                    = readHandler.handleRequest(proxy, requestFromId, null, logger);

            ResourceModel resourceModel = response.getResourceModel();
            models.add(resourceModel);
        }

        return models;
    }

}