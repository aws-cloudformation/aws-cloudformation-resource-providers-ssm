package software.amazon.ssm.resourcedatasync;

import software.amazon.awssdk.services.ssm.model.ResourceDataSyncItem;
import software.amazon.awssdk.services.ssm.model.ResourceDataSyncS3Destination;

import java.util.Arrays;
import java.util.List;

public class TestBase {

    static final int CREATE_CALLBACK_DELAY_SECONDS = 10;

    static final int INITIAL_CREATE_CALLBACK_DELAY_SECONDS = 2;

    static final int DELETE_CALLBACK_DELAY_SECONDS = 30;

    static final int INITIAL_DELETE_CALLBACK_DELAY_SECONDS = 2;

    static final int NUMBER_OF_RESOURCE_DATA_SYNC_CREATE_POLL_RETRIES = 60 / CREATE_CALLBACK_DELAY_SECONDS;

    static final int NUMBER_OF_RESOURCE_DATA_SYNC_DELETE_POLL_RETRIES = 2 * 60 / DELETE_CALLBACK_DELAY_SECONDS;

    static final int ZERO = 0;

    static final String RESOURCE_DATA_SYNC_NAME = "TestResouceDataSync";

    static final String SOURCE_TYPE_SINGLE_ACCOUNT_MULTI_REGION = "SingleAccountMultiRegions";
    static final String SOURCE_TYPE_AWS_ORG = "AwsOrganizations";

    static final String REGION = "us-east-1";

    static final String SYNC_TYPE_SYNC_FROM_SOURCE = "SyncFromSource";
    static final String SYNC_TYPE_SYNC_TO_DESTINATION = "SyncToDestination";

    static final String NO_AWS_ORGANIZATION = "";
    static final String ORGANIZATION_UNITS = "OrganizationalUnits";
    static final String ENTIRE_ORGANIZATION = "EntireOrganization";

    static final String ORGANIZATIONAL_UNIT_ID = "123";
    static final List<String> ORGANIZATION_UNITS_LIST = Arrays.asList(ORGANIZATIONAL_UNIT_ID);

    public static final String BUCKET_NAME = "TestBucketName";
    public static final String BUCKET_REGION = "TestBucketRegion";
    public static final String BUCKET_PREFIX = "TestBucketPrefix";

    public static final String KMS_KEY_ARN = "TestArn";
    public static final String SYNC_FORMAT = "JsonSerDe";

    public static final List<String> SOURCE_REGIONS = Arrays.asList(REGION);
    public static final boolean INCLUDE_FUTURE_REGIONS = false;

    public static final String S3_DESTINATION_AS_SUB_REFERENCE_CONFIG = "S3DestinationSubReferenceConfig";
    public static final String S3_DESTINATION_TOP_LEVEL_CONFIG = "S3DestinationTopLevelConfig";
    public static final List<String> S3_DESTINATION_REQUEST_TYPE_KEYS = Arrays.asList(S3_DESTINATION_AS_SUB_REFERENCE_CONFIG,
            S3_DESTINATION_TOP_LEVEL_CONFIG);

    ResourceModel createBasicRDSModel() {
        return ResourceModel.builder()
                .syncName(RESOURCE_DATA_SYNC_NAME)
                .build();
    }

    ResourceModel createSyncToDestinationRDSModel() {
        return ResourceModel.builder()
                .syncName(RESOURCE_DATA_SYNC_NAME)
                .syncType(SYNC_TYPE_SYNC_TO_DESTINATION)
                .s3Destination(createS3DestinationModel())
                .build();
    }

    ResourceModel createSyncToDestinationRDSModelTopLevelConfig() {
        return ResourceModel.builder()
                .syncName(RESOURCE_DATA_SYNC_NAME)
                .bucketName(BUCKET_NAME)
                .bucketRegion(BUCKET_REGION)
                .build();
    }

    ResourceModel createSyncFromSourceRDSModel(SyncSource syncSource) {
        return ResourceModel.builder()
                .syncName(RESOURCE_DATA_SYNC_NAME)
                .syncType(SYNC_TYPE_SYNC_FROM_SOURCE)
                .syncSource(syncSource)
                .build();
    }

    SyncSource createSyncSourceModel() {
        return SyncSource.builder()
                .sourceType(SOURCE_TYPE_AWS_ORG)
                .sourceRegions(SOURCE_REGIONS)
                .build();
    }

    S3Destination createS3DestinationModel() {
        return S3Destination.builder()
                .bucketRegion(REGION)
                .bucketName(BUCKET_NAME)
                .syncFormat(SYNC_FORMAT)
                .build();
    }

    SyncSource createUpdatedSyncSourceModel() {
        return SyncSource.builder()
                .sourceType(SOURCE_TYPE_SINGLE_ACCOUNT_MULTI_REGION)
                .sourceRegions(SOURCE_REGIONS)
                .includeFutureRegions(true)
                .build();
    }

    ResourceDataSyncItem createSyncToDestinationRDSItem() {
        return ResourceDataSyncItem.builder()
                .syncName(RESOURCE_DATA_SYNC_NAME)
                .syncType(SYNC_TYPE_SYNC_TO_DESTINATION)
                .s3Destination(
                        ResourceDataSyncS3Destination.builder()
                                .region(BUCKET_REGION)
                                .prefix(BUCKET_PREFIX)
                                .bucketName(BUCKET_NAME)
                                .syncFormat(SYNC_FORMAT)
                                .build()
                ).build();
    }
}
