package software.amazon.ssm.resourcedatasync;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.model.ResourceDataSyncItem;
import software.amazon.awssdk.services.ssm.model.ResourceDataSyncS3Destination;
import software.amazon.awssdk.services.ssm.model.ResourceDataSyncSource;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


@ExtendWith(MockitoExtension.class)
public class TranslatorTest extends TestBase {

    @BeforeEach
    public void setup() {

    }

    @Test
    void createRDSRequestFromResourceMode_success() {
        final ResourceModel model = createSyncToDestinationRDSModel();
        final ResourceDataSyncItem resourceDataSyncItem = Translator.createResourceDataSyncItemFromResourceModel(model);

        assertThat(resourceDataSyncItem.syncName()).isNotNull();
    }

    @Test
    void isThereTopLevelS3DestinationConfiguration_success() {
        final ResourceModel model = createSyncToDestinationRDSModelTopLevelConfig();
        final boolean isTopLevelConfig = Translator.isThereTopLevelS3DestinationConfiguration(model);

        assertThat(isTopLevelConfig).isTrue();
    }

    @Test
    void createTopLevelS3Destination_success() {
        final ResourceModel model = createSyncToDestinationRDSModel();
        final ResourceDataSyncS3Destination resourceDataSyncS3Destination = Translator.createTopLevelS3Destination(model);

        assertThat(resourceDataSyncS3Destination.bucketName()).isEqualTo(null);
        assertThat(resourceDataSyncS3Destination.syncFormat()).isEqualTo(null);
        assertThat(resourceDataSyncS3Destination.region()).isEqualTo(null);

    }

    @Test
    void createResourceDataSyncSource_success() {
        final SyncSource model = createSyncSourceModel();
        final ResourceDataSyncSource resourceDataSyncSource = Translator.createResourceDataSyncSource(model);

        assertThat(resourceDataSyncSource.sourceRegions()).isEqualTo(SOURCE_REGIONS);
    }

    @Test
    void createResourceDataSyncSource_null() {
        final ResourceDataSyncSource resourceDataSyncSource = Translator.createResourceDataSyncSource(null);

        assertThat(resourceDataSyncSource).isNull();
    }

    @Test
    void createResourceModelFromResourceDataSyncItem_success() {
        final ResourceDataSyncItem rdsItem = createSyncToDestinationRDSItem();
        final ResourceModel model = Translator.createResourceModelFromResourceDataSyncItem(rdsItem);

        assertThat(model.getSyncName()).isNotNull();
        assertThat(model.getBucketName()).isNotNull();
        assertThat(model.getBucketPrefix()).isNotNull();
        assertThat(model.getBucketRegion()).isNotNull();
        assertThat(model.getSyncType()).isNotNull();
        assertThat(model.getSyncFormat()).isNotNull();
    }
}
