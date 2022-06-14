package software.amazon.ssm.resourcedatasync;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.ssm.model.CreateResourceDataSyncRequest;
import software.amazon.awssdk.services.ssm.model.ListResourceDataSyncRequest;
import software.amazon.awssdk.services.ssm.model.ResourceDataSyncAwsOrganizationsSource;
import software.amazon.awssdk.services.ssm.model.ResourceDataSyncS3Destination;
import software.amazon.awssdk.services.ssm.model.ResourceDataSyncSource;
import software.amazon.awssdk.services.ssm.model.ResourceDataSyncOrganizationalUnit;
import software.amazon.awssdk.services.ssm.model.ResourceDataSyncItem;
import software.amazon.awssdk.services.ssm.model.DeleteResourceDataSyncRequest;
import software.amazon.awssdk.services.ssm.model.UpdateResourceDataSyncRequest;
import software.amazon.awssdk.services.ssm.model.ResourceDataSyncSourceWithState;

import software.amazon.awssdk.services.ssm.model.LastResourceDataSyncStatus;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class Translator {

    private static final int RESOURCE_DATA_SYNC_LIMIT = 5;

    /**
     * Translate ResourceModel to SSM::CreateResourceDataSyncRequest
     *
     * @param model ResourceModel
     * @return CreateResourceDataSyncRequest
     */
    static CreateResourceDataSyncRequest createResourceDataSyncRequest(final ResourceModel model) {
        boolean isThereATopLevelConfig = isThereTopLevelS3DestinationConfiguration(model);
        if (model.getS3Destination() != null && isThereATopLevelConfig) {
            throw new CfnInvalidRequestException("Two configurations for S3Destination are found");
        }

        return CreateResourceDataSyncRequest.builder()
                .syncName(model.getSyncName())
                .syncType(model.getSyncType())
                .syncSource(createResourceDataSyncSource(model.getSyncSource()))
                .s3Destination(isThereATopLevelConfig
                        ? createTopLevelS3Destination(model)
                        : createRDSS3Destination(model.getS3Destination()))
                .build();
    }

    /**
     * Translate ResourceModel to SSM::ResourceDataSyncS3Destination
     *
     * @param model ResourceModel
     * @return boolean
     */
    static boolean isThereTopLevelS3DestinationConfiguration(final ResourceModel model) {
        return model.getBucketName() != null
                || model.getBucketRegion() != null
                || model.getSyncFormat() != null
                || model.getBucketPrefix() != null
                || model.getKMSKeyArn() != null;
    }

    /**
     * Translate ResourceModel to SSM::ResourceDataSyncS3Destination
     *
     * @param model ResourceModel
     * @return ResourceDataSyncS3Destination
     */
    static ResourceDataSyncS3Destination createTopLevelS3Destination(final ResourceModel model) {
        return ResourceDataSyncS3Destination.builder()
                .bucketName(model.getBucketName())
                .awskmsKeyARN(model.getKMSKeyArn())
                .syncFormat(model.getSyncFormat())
                .prefix(model.getBucketPrefix())
                .region(model.getBucketRegion())
                .build();
    }

    /**
     * Translate ResourceModel::syncSource to SSM::ResourceDataSyncSource
     *
     * @param syncSourceModel SyncSource
     * @return ResourceDataSyncSource
     */
    static ResourceDataSyncSource createResourceDataSyncSource(final SyncSource syncSourceModel) {
        if (syncSourceModel == null) {
            return null;
        }

        AwsOrganizationsSource awsOrganizationsSourceModel = syncSourceModel.getAwsOrganizationsSource();

        return ResourceDataSyncSource.builder()
                .awsOrganizationsSource(createResourceDataSyncAwsOrganizationsSource(awsOrganizationsSourceModel))
                .sourceRegions(syncSourceModel.getSourceRegions())
                .includeFutureRegions(syncSourceModel.getIncludeFutureRegions())
                .sourceType(syncSourceModel.getSourceType())
                .build();
    }

    /**
     * Translate ResourceModel::AwsOrganizationsSource to SSM::ResourceDataSyncAwsOrganizationsSource
     *
     * @param awsOrganizationsSourceModel AwsOrganizationsSource
     * @return ResourceDataSyncAwsOrganizationsSource
     */
    static ResourceDataSyncAwsOrganizationsSource createResourceDataSyncAwsOrganizationsSource(
            final AwsOrganizationsSource awsOrganizationsSourceModel) {
        if (awsOrganizationsSourceModel == null) {
            return null;
        }
        List<ResourceDataSyncOrganizationalUnit> RDSOrganizationalUnitList = null;
        if (awsOrganizationsSourceModel.getOrganizationalUnits() != null) {
            RDSOrganizationalUnitList = awsOrganizationsSourceModel
                    .getOrganizationalUnits().stream()
                    .map(
                            id -> ResourceDataSyncOrganizationalUnit.builder().organizationalUnitId(id).build())
                    .collect(Collectors.toList());
        }

        return ResourceDataSyncAwsOrganizationsSource.builder()
                .organizationSourceType(awsOrganizationsSourceModel.getOrganizationSourceType())
                .organizationalUnits(RDSOrganizationalUnitList)
                .build();
    }

    /**
     * Translate ResourceModel::S3Destination to SSM::ResourceDataSyncS3Destination
     *
     * @param s3DestinationModel S3Destination
     * @return ResourceDataSyncS3Destination
     */
    static ResourceDataSyncS3Destination createRDSS3Destination(final S3Destination s3DestinationModel) {
        //TODO: phrase 2 will release s3-data-sharing.
        if (s3DestinationModel == null) {
            return null;
        }

        return ResourceDataSyncS3Destination.builder()
                .bucketName(s3DestinationModel.getBucketName())
                .awskmsKeyARN(s3DestinationModel.getKMSKeyArn())
                .syncFormat(s3DestinationModel.getSyncFormat())
                .prefix(s3DestinationModel.getBucketPrefix())
                .region(s3DestinationModel.getBucketRegion())
                .build();
    }

    /**
     * Translate ResourceModel to SSM::ListResourceDataSyncRequest without nextToken
     *
     * @param model ResourceModel
     * @return ListResourceDataSyncRequest
     */
    static ListResourceDataSyncRequest createListResourceDataSyncRequest(final ResourceModel model) {
        return ListResourceDataSyncRequest.builder()
                .syncType(model.getSyncType())
                .maxResults(RESOURCE_DATA_SYNC_LIMIT)
                .build();
    }

    /**
     * Translate ResourceModel to SSM::ListResourceDataSyncRequest with nextToken
     *
     * @param model     ResourceModel
     * @param nextToken String
     * @return ListResourceDataSyncRequest
     */
    static ListResourceDataSyncRequest createListResourceDataSyncRequest(final ResourceModel model, final String nextToken) {
        return ListResourceDataSyncRequest.builder()
                .syncType(model.getSyncType())
                .maxResults(RESOURCE_DATA_SYNC_LIMIT)
                .nextToken(nextToken)
                .build();
    }

    /**
     * Translate ResourceModel to SSM::DeleteResourceDataSyncRequest
     *
     * @param model ResourceModel
     * @return DeleteResourceDataSyncRequest
     */
    static DeleteResourceDataSyncRequest deleteResourceDataSyncRequest(final ResourceModel model) {
        return DeleteResourceDataSyncRequest.builder()
                .syncName(model.getSyncName())
                .syncType(model.getSyncType())
                .build();
    }

    /**
     * Translate ResourceModel to SSM::UpdateResourceDataSyncRequest
     *
     * @param model ResourceModel
     * @return UpdateResourceDataSyncRequest
     */
    static UpdateResourceDataSyncRequest updateResourceDataSyncRequest(final ResourceModel model) {
        return UpdateResourceDataSyncRequest.builder()
                .syncName(model.getSyncName())
                .syncType(model.getSyncType())
                .syncSource(createResourceDataSyncSource(model.getSyncSource()))
                .build();
    }

    /**
     * Translate ResourceModel to SSM::ResourceDataSyncItem
     *
     * @param model ResourceModel
     * @return ResourceDataSyncItem
     */
    static ResourceDataSyncItem createResourceDataSyncItemFromResourceModel(final ResourceModel model) {
        return ResourceDataSyncItem.builder()
                .syncName(model.getSyncName())
                .syncType(model.getSyncType())
                .lastStatus(LastResourceDataSyncStatus.SUCCESSFUL)
                .s3Destination(createRDSS3Destination(model.getS3Destination()))
                .syncSource(createResourceDataSyncWithState(model))
                .build();


    }

    /**
     * Translate ResourceModel::SyncSource to SSM::ResourceDataSyncSourceWithState
     *
     * @param model ResourceModel
     * @return ResourceDataSyncSourceWithState
     */
    static ResourceDataSyncSourceWithState createResourceDataSyncWithState(final ResourceModel model) {
        SyncSource syncSource = model.getSyncSource();
        if (syncSource == null) {
            return null;
        }
        return ResourceDataSyncSourceWithState.builder()
                .awsOrganizationsSource(createResourceDataSyncAwsOrganizationsSource(syncSource.getAwsOrganizationsSource()))
                .includeFutureRegions(syncSource.getIncludeFutureRegions())
                .sourceRegions(syncSource.getSourceRegions())
                .sourceType(syncSource.getSourceType())
                .build();
    }

    /**
     * Translate SSM::ResourceDataSyncItem to ResourceModel
     *
     * @param resourceDataSyncItem ResourceDataSyncItem
     * @return ResourceModel
     */
    static ResourceModel createResourceModelFromResourceDataSyncItem(final ResourceDataSyncItem resourceDataSyncItem) {

        return new ResourceModel.ResourceModelBuilder()
                .syncName(resourceDataSyncItem.syncName())
                .syncType(resourceDataSyncItem.syncType())
                .syncSource(createSyncSource(resourceDataSyncItem.syncSource()))
                .s3Destination(createS3Destination(resourceDataSyncItem.s3Destination()))
                .bucketName(resourceDataSyncItem.s3Destination() != null ? resourceDataSyncItem.s3Destination().bucketName() : null)
                .bucketRegion(resourceDataSyncItem.s3Destination() != null ? resourceDataSyncItem.s3Destination().region() : null)
                .bucketPrefix(resourceDataSyncItem.s3Destination() != null ? resourceDataSyncItem.s3Destination().prefix() : null)
                .kMSKeyArn(resourceDataSyncItem.s3Destination() != null ? resourceDataSyncItem.s3Destination().awskmsKeyARN() : null)
                .syncFormat(resourceDataSyncItem.s3Destination() != null &&
                        resourceDataSyncItem.s3Destination().syncFormat() != null ?
                        resourceDataSyncItem.s3Destination().syncFormat().toString() : null)
                .build();
    }

    /**
     * Translate SSM::ResourceDataSyncSourceWithState
     * to ResourceModel::SyncSource
     *
     * @param resourceSyncSourceModel ResourceDataSyncSourceWithState
     * @return SyncSource
     */
    static SyncSource createSyncSource(final ResourceDataSyncSourceWithState resourceSyncSourceModel) {
        if (resourceSyncSourceModel == null) {
            return null;
        }

        ResourceDataSyncAwsOrganizationsSource awsOrganizationsSourceModel = resourceSyncSourceModel.awsOrganizationsSource();

        return SyncSource.builder()
                .awsOrganizationsSource(createAwsOrganizationSource(awsOrganizationsSourceModel))
                .sourceRegions(resourceSyncSourceModel.sourceRegions())
                .includeFutureRegions(resourceSyncSourceModel.includeFutureRegions())
                .sourceType(resourceSyncSourceModel.sourceType())
                .build();
    }

    /**
     * Translate SSM::ResourceDataSyncAwsOrganizationsSource
     * to ResourceModel::AwsOrganizationsSource
     *
     * @param awsOrganizationsSourceModel ResourceDataSyncAwsOrganizationsSource
     * @return AwsOrganizationsSource
     */
    static AwsOrganizationsSource createAwsOrganizationSource(final ResourceDataSyncAwsOrganizationsSource awsOrganizationsSourceModel) {
        if (awsOrganizationsSourceModel == null) {
            return null;
        }

        List<String> organizationalUnitList = awsOrganizationsSourceModel
                .organizationalUnits().stream().map(
                        OU -> OU.organizationalUnitId()
                ).collect(Collectors.toList());

        return AwsOrganizationsSource.builder()
                .organizationSourceType(awsOrganizationsSourceModel.organizationSourceType())
                .organizationalUnits(organizationalUnitList)
                .build();

    }

    /**
     * Translate SSM::ResourceDataSyncS3Destination
     * to ResourceModel::S3Destination
     *
     * @param s3DestinationModel ResourceDataSyncS3Destination
     * @return S3Destination
     */
    static S3Destination createS3Destination(final ResourceDataSyncS3Destination s3DestinationModel) {
        //TODO: phrase 2 will release s3-data-sharing.
        if (s3DestinationModel == null) {
            return null;
        }

        return S3Destination.builder()
                .bucketName(s3DestinationModel.bucketName())
                .kMSKeyArn(s3DestinationModel.awskmsKeyARN())
                .syncFormat(s3DestinationModel.syncFormat() != null ? s3DestinationModel.syncFormat().toString() : null)
                .bucketPrefix(s3DestinationModel.prefix())
                .bucketRegion(s3DestinationModel.region())
                .build();
    }

}
