package software.amazon.ssm.resourcedatasync;

import software.amazon.awssdk.services.ssm.model.*;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

final class HandlerHelper {

    /**
     * Describe Resource Data Sync Item based on model:syncName. First calling getListResourceDataSyncResponse to fetch
     * list of resource data sync items based on model:syncType, then filter it by model:syncName to get specific
     * ResourceDataSyncItem.
     *
     * @param model ResourceModel
     * @param proxy AmazonWebServicesClientProxy
     * @return List<ResourceDataSyncItem>
     */
    static List<ResourceDataSyncItem> describeResourceDataSyncItem(final ResourceModel model, final AmazonWebServicesClientProxy proxy) {

        ListResourceDataSyncResponse response = getListResourceDataSyncResponse(model, proxy);
        return filterResourceDataSyncResponse(response, proxy, model);

    }

    /**
     * Function for calling ssm::listResourceDataSync API and handle corresponded Exception without nextToken.
     *
     * @param model ResourceModel
     * @param proxy AmazonWebServicesClientProxy
     * @return ListResourceDataSyncResponse
     */
    static ListResourceDataSyncResponse getListResourceDataSyncResponse(final ResourceModel model, final AmazonWebServicesClientProxy proxy) {

        final ListResourceDataSyncRequest request = Translator.createListResourceDataSyncRequest(model);

        try {
            return proxy.injectCredentialsAndInvokeV2(request, ClientBuilder.getClient()::listResourceDataSync);
        } catch (final ResourceDataSyncInvalidConfigurationException e) {
            throw new CfnInvalidRequestException(e);
        } catch (final SsmException e) {
            throw new CfnGeneralServiceException(e.getMessage(), e);
        }
    }

    /**
     * Function for calling ssm::listResourceDataSync API and handle corresponded Exception with nextToken.
     *
     * @param model ResourceModel
     * @param proxy AmazonWebServicesClientProxy
     * @return nextToken that is used for fetching next page.
     */
    static ListResourceDataSyncResponse getListResourceDataSyncResponse(final ResourceModel model, final AmazonWebServicesClientProxy proxy, final String nextToken) {

        final ListResourceDataSyncRequest request = Translator.createListResourceDataSyncRequest(model, nextToken);

        try {
            return proxy.injectCredentialsAndInvokeV2(request, ClientBuilder.getClient()::listResourceDataSync);
        } catch (SsmException e) {
            if (e instanceof InvalidNextTokenException || e instanceof ResourceDataSyncInvalidConfigurationException) {
                throw new CfnInvalidRequestException(e);
            }
            throw new CfnGeneralServiceException(e.getMessage(), e);
        }
    }

    /**
     * Function for filtering ListResourceDataSyncResponse by model.syncName.
     *
     * @param model ResourceModel
     * @param proxy AmazonWebServicesClientProxy
     * @return List<ResourceDataSyncItem> that consist either null or corresponded ResourceDataSyncItem
     */
    static List<ResourceDataSyncItem> filterResourceDataSyncResponse(final ListResourceDataSyncResponse response, final AmazonWebServicesClientProxy proxy, final ResourceModel model) {
        List<ResourceDataSyncItem> itemList = new ArrayList<>();
        Stream<ResourceDataSyncItem> rdsWithSameSyncName = response.resourceDataSyncItems().stream().filter(p -> p.syncName().equals(model.getSyncName()));
        rdsWithSameSyncName.forEach(p -> itemList.add(p));

        while (response.nextToken() != null) {
            ListResourceDataSyncResponse subResponse = getListResourceDataSyncResponse(model, proxy, response.nextToken());
            response.toBuilder().resourceDataSyncItems(subResponse.resourceDataSyncItems()).nextToken(subResponse.nextToken()).build();
        }

        return itemList;
    }

}

