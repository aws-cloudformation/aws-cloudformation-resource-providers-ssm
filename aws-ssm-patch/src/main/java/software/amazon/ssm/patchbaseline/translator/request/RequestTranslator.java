package software.amazon.ssm.patchbaseline.translator.request;

import software.amazon.ssm.patchbaseline.ResourceModel;
import software.amazon.awssdk.services.ssm.model.SsmRequest;

/**
 * Translates ResourceModel objects into SsmRequests.
 *
 * @param <RequestT> Type of SsmRequest the translator works with.
 */
public interface RequestTranslator<RequestT extends SsmRequest> {
    /**
     * Translates ResourceModel objects into SsmRequests.
     *
     * @param model ResourceModel object used for conversion into a request.
     * @return AWS SDK's SsmRequest object translated from the ResourceModel.
     */
    RequestT resourceModelToRequest(final ResourceModel model);
}
