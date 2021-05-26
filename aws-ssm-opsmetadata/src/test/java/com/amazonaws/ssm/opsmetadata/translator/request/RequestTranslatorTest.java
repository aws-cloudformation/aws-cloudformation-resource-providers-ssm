package com.amazonaws.ssm.opsmetadata.translator.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.amazonaws.ssm.opsmetadata.ResourceModel;
import com.amazonaws.ssm.opsmetadata.translator.property.MetadataTranslator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.model.AddTagsToResourceRequest;
import software.amazon.awssdk.services.ssm.model.CreateOpsMetadataRequest;
import software.amazon.awssdk.services.ssm.model.DeleteOpsMetadataRequest;
import software.amazon.awssdk.services.ssm.model.GetOpsMetadataRequest;
import software.amazon.awssdk.services.ssm.model.MetadataValue;
import software.amazon.awssdk.services.ssm.model.RemoveTagsFromResourceRequest;
import software.amazon.awssdk.services.ssm.model.ResourceTypeForTagging;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.awssdk.services.ssm.model.UpdateOpsMetadataRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
public class RequestTranslatorTest {
    private static final String OPSMETADATA_ARN = "arn:aws:ssm:us-east-1:123456789012:opsmetadata/aws/ssm/XYZ_RG/appmanager";
    private static final String OPSMETADATA_ID = "/aws/ssm/XYZ_RG/appmanager";
    private static final String RESOURCE_ID = "arn:aws:resource-groups:us-east-1:123456789012:group/XYZ_RG";
    @Mock
    MetadataTranslator metadataTranslator;

    private RequestTranslator requestTranslator;
    private Map<String, MetadataValue> serviceModelMetadata;
    private Map<String, com.amazonaws.ssm.opsmetadata.MetadataValue> resourceModelMetadata;
    private Map<String, String> TAG_SET;
    private List<Tag> tagList;

    @BeforeEach
    void setUp() {
        this.requestTranslator = new RequestTranslator(metadataTranslator);
        serviceModelMetadata = new HashMap<String, MetadataValue>() {{
            put("some-key-1", MetadataValue.builder().value("some-value-1").build());
            put("some-key-2", MetadataValue.builder().value("some-value-2").build());
        }};
        resourceModelMetadata = new HashMap<String, com.amazonaws.ssm.opsmetadata.MetadataValue>() {{
            put("some-key-1", com.amazonaws.ssm.opsmetadata.MetadataValue.builder().value("some-value-1").build());
            put("some-key-2", com.amazonaws.ssm.opsmetadata.MetadataValue.builder().value("some-value-2").build());
        }};
        TAG_SET = new HashMap<String, String>() {
            {
                put("key1", "value1");
                put("key2", "value2");
            }
        };
        tagList = new ArrayList<>();
        tagList.add(Tag.builder().key("key1").value("value1").build());
        tagList.add(Tag.builder().key("key2").value("value2").build());
    }

    @Test
    void testCreateOpsMetadataRequestWithAllInputFields() {
        final ResourceModel resourceModel = ResourceModel.builder()
                .resourceId(RESOURCE_ID)
                .metadata(resourceModelMetadata)
                .build();
        when(metadataTranslator.resourceModelPropertyToServiceModel(eq(resourceModelMetadata)))
                .thenReturn(Optional.of(serviceModelMetadata));
        CreateOpsMetadataRequest request = requestTranslator.createOpsMetadataRequest(resourceModel, TAG_SET);
        CreateOpsMetadataRequest expectedRequest = CreateOpsMetadataRequest.builder()
                .resourceId(RESOURCE_ID)
                .metadata(serviceModelMetadata)
                .tags(tagList)
                .build();
        assertThat(request).isEqualTo(expectedRequest);
    }

    @Test
    void testCreateOpsMetadataRequestWithNoMetadata() {
        final ResourceModel resourceModel = ResourceModel.builder()
                .resourceId(RESOURCE_ID)
                .build();
        when(metadataTranslator.resourceModelPropertyToServiceModel(eq(null)))
                .thenReturn(Optional.empty());
        CreateOpsMetadataRequest request = requestTranslator.createOpsMetadataRequest(resourceModel, TAG_SET);
        CreateOpsMetadataRequest expectedRequest = CreateOpsMetadataRequest.builder()
                .resourceId(RESOURCE_ID)
                .tags(tagList)
                .build();
        assertThat(request).isEqualTo(expectedRequest);
    }

    @Test
    void testCreateOpsMetadataRequestWithNoTags() {
        final ResourceModel resourceModel = ResourceModel.builder()
                .resourceId(RESOURCE_ID)
                .build();
        when(metadataTranslator.resourceModelPropertyToServiceModel(eq(null)))
                .thenReturn(Optional.empty());
        CreateOpsMetadataRequest request = requestTranslator.createOpsMetadataRequest(resourceModel, new HashMap<>());
        CreateOpsMetadataRequest expectedRequest = CreateOpsMetadataRequest.builder()
                .resourceId(RESOURCE_ID)
                .build();
        assertThat(request).isEqualTo(expectedRequest);
    }

    @Test
    void testUpdateOpsMetadataRequestWithAllInputFields() {
        final ResourceModel resourceModel = ResourceModel.builder()
                .opsMetadataArn(OPSMETADATA_ARN)
                .metadata(resourceModelMetadata)
                .build();
        when(metadataTranslator.resourceModelPropertyToServiceModel(eq(resourceModelMetadata)))
                .thenReturn(Optional.of(serviceModelMetadata));
        UpdateOpsMetadataRequest request = requestTranslator.updateOpsMetadataRequest(resourceModel);
        UpdateOpsMetadataRequest expectedRequest = UpdateOpsMetadataRequest.builder()
                .opsMetadataArn(OPSMETADATA_ARN)
                .metadataToUpdate(serviceModelMetadata)
                .build();
        assertThat(request).isEqualTo(expectedRequest);
    }

    @Test
    void testGetOpsMetadataRequestWithAllInputFields() {
        final ResourceModel resourceModel = ResourceModel.builder()
                .opsMetadataArn(OPSMETADATA_ARN)
                .build();
        GetOpsMetadataRequest request = requestTranslator.getOpsMetadataRequest(resourceModel);
        GetOpsMetadataRequest expectedRequest = GetOpsMetadataRequest.builder()
                .opsMetadataArn(OPSMETADATA_ARN)
                .build();
        assertThat(request).isEqualTo(expectedRequest);
    }

    @Test
    void testDeleteOpsMetadataRequestWithAllInputFields() {
        final ResourceModel resourceModel = ResourceModel.builder()
                .opsMetadataArn(OPSMETADATA_ARN)
                .build();
        DeleteOpsMetadataRequest request = requestTranslator.deleteOpsMetadataRequest(resourceModel);
        DeleteOpsMetadataRequest expectedRequest = DeleteOpsMetadataRequest.builder()
                .opsMetadataArn(OPSMETADATA_ARN)
                .build();
        assertThat(request).isEqualTo(expectedRequest);
    }

    @Test
    void testAddTagsToResourceRequestWithAllInputFields() {
        final ResourceModel resourceModel = ResourceModel.builder()
                .opsMetadataArn(OPSMETADATA_ARN)
                .build();
        AddTagsToResourceRequest request = requestTranslator.addTagsToResourceRequest(resourceModel, tagList);
        AddTagsToResourceRequest expectedRequest = AddTagsToResourceRequest.builder()
                .resourceId(OPSMETADATA_ID)
                .resourceType(ResourceTypeForTagging.OPS_METADATA)
                .tags(tagList)
                .build();
        assertThat(request).isEqualTo(expectedRequest);
    }

    @Test
    void testRemoveTagsFromRequestWithAllInputFields() {
        final ResourceModel resourceModel = ResourceModel.builder()
                .opsMetadataArn(OPSMETADATA_ARN)
                .build();
        RemoveTagsFromResourceRequest request = requestTranslator.removeTagsFromResourceRequest(resourceModel, tagList);
        RemoveTagsFromResourceRequest expectedRequest = RemoveTagsFromResourceRequest.builder()
                .resourceId(OPSMETADATA_ID)
                .resourceType(ResourceTypeForTagging.OPS_METADATA)
                .tagKeys(tagList.stream().map(tag -> tag.key()).collect(Collectors.toList()))
                .build();
        assertThat(request).isEqualTo(expectedRequest);
    }
}
