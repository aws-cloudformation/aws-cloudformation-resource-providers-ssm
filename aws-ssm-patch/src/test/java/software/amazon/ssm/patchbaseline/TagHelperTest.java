package software.amazon.ssm.patchbaseline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.*;
import software.amazon.awssdk.services.ssm.model.PatchFilterGroup;
import software.amazon.awssdk.services.ssm.model.PatchSource;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.ssm.patchbaseline.Resource;
import software.amazon.ssm.patchbaseline.ResourceModel;
import software.amazon.ssm.patchbaseline.TestBase;
import software.amazon.ssm.patchbaseline.TestConstants;
import software.amazon.ssm.patchbaseline.utils.SimpleTypeValidator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import software.amazon.ssm.patchbaseline.utils.SsmCfnClientSideException;

import java.util.*;
import java.util.function.Function;

@ExtendWith(MockitoExtension.class)
public class TagHelperTest extends TestBase{

    private SimpleTypeValidator simpleTypeValidator;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    private TagHelper cfnTagHelper;

    private static final String TEST_RESOURCE_TYPE = "AResource";
    private static final String TEST_RESOURCE_ID = "pb-123";
    private ListTagsForResourceRequest listTagsForResourceRequest;
    private ListTagsForResourceResponse listTagsForResourceResponse;

    @BeforeEach
    void setUp() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        ssmClient = mock(SsmClient.class);
        simpleTypeValidator = new SimpleTypeValidator();
        cfnTagHelper = new TagHelper();
        listTagsForResourceRequest = ListTagsForResourceRequest.builder().resourceType(TEST_RESOURCE_TYPE).resourceId(TEST_RESOURCE_ID).build();
    }

    @Test
    public void testUpdateTagsForResource_Nominal() {
        List<String> expectedRemoveTags = Arrays.asList(
                "resourcekey2",
                "stackkey2"
        );

        List<Tag> expectedAddTags = Arrays.asList(
                 Tag.builder().key("resourcekey1").value("newresource1").build(),
                 Tag.builder().key("resourcekey3").value("newresource3").build(),
                 Tag.builder().key("stackkey1").value("newstack1").build(),
                 Tag.builder().key("stackkey3").value("newstack3").build()
        );

        Map<String, String> oldResourceTags = new HashMap<>();
        oldResourceTags.put("resourcekey1", "oldresource1");
        oldResourceTags.put("resourcekey2", "oldresource2");

        Map<String, String> newResourceTags = new HashMap<>();
        newResourceTags.put("resourcekey1", "newresource1");
        newResourceTags.put("resourcekey3", "newresource3");

        Map<String, String> oldStackTags = new HashMap<>();
        oldStackTags.put("stackkey1", "oldstack1");
        oldStackTags.put("stackkey2", "oldstack2");

        Map<String, String> newStackTags = new HashMap<>();
        newStackTags.put("stackkey1", "newstack1");
        newStackTags.put("stackkey3", "newstack3");

        List<Tag> tagList1 = buildRequestTagList(oldResourceTags);
        List<Tag> tagList = buildRequestTagList(oldStackTags);
        tagList.addAll(tagList1);

        listTagsForResourceResponse = ListTagsForResourceResponse.builder().tagList(tagList).build();

        ResourceModel oldResourceModel = ResourceModel.builder().tags(buildCfnTagList(oldResourceTags)).id(TEST_RESOURCE_ID).build();

        ResourceModel newResourceModel = ResourceModel.builder().tags(buildCfnTagList(newResourceTags)).id(TEST_RESOURCE_ID).build();

        ResourceHandlerRequest<ResourceModel> request = new ResourceHandlerRequest<ResourceModel>();
        request.setDesiredResourceState(newResourceModel);
        request.setDesiredResourceTags(newStackTags);
        request.setPreviousResourceState(oldResourceModel);
        request.setLogicalResourceIdentifier(TEST_RESOURCE_ID);

        System.out.print(String.format("build request Id from getDesiredResourceState %s %n", request.getDesiredResourceState().getId()));
        System.out.print(String.format("build request Id from getPreviousResourceState %s %n", request.getPreviousResourceState().getId()));

        cfnTagHelper.updateTagsForResource(request, TEST_RESOURCE_TYPE, ssmClient, proxy);

        when(proxy.injectCredentialsAndInvokeV2(
                eq(listTagsForResourceRequest),
                ArgumentMatchers.<Function<ListTagsForResourceRequest, ListTagsForResourceResponse>>any()))
                .thenReturn(listTagsForResourceResponse);

        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(listTagsForResourceRequest),
                        ArgumentMatchers.<Function<ListTagsForResourceRequest, ListTagsForResourceResponse>>any());


        ArgumentCaptor<RemoveTagsFromResourceRequest> removeTagsRequest = ArgumentCaptor.forClass(RemoveTagsFromResourceRequest.class);
        ArgumentCaptor<AddTagsToResourceRequest> addTagsRequest = ArgumentCaptor.forClass(AddTagsToResourceRequest.class);

        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(removeTagsRequest.capture()),
                        ArgumentMatchers.<Function<RemoveTagsFromResourceRequest, RemoveTagsFromResourceResponse>>any());

        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(addTagsRequest.capture()),
                        ArgumentMatchers.<Function<AddTagsToResourceRequest, AddTagsToResourceResponse>>any());

//        verify(ssmClient).removeTagsFromResource(removeTagsRequest.capture());
//        verify(ssmClient).addTagsToResource(addTagsRequest.capture());

        RemoveTagsFromResourceRequest actualRemoveTags = removeTagsRequest.getValue();
        AddTagsToResourceRequest actualAddTags = addTagsRequest.getValue();

        assertThat(TEST_RESOURCE_ID).isEqualTo(actualRemoveTags.resourceId());
        assertThat(TEST_RESOURCE_TYPE).isEqualTo(actualRemoveTags.resourceTypeAsString());

        assertThat(TEST_RESOURCE_ID).isEqualTo(actualAddTags.resourceId());
        assertThat(TEST_RESOURCE_TYPE).isEqualTo( actualAddTags.resourceTypeAsString());

        // Because internally we use a map, list ordering will be unpredictable, so we'll
        // just confirm that lists contain same elements
        Collections.sort(expectedRemoveTags);
        Collections.sort(actualRemoveTags.tagKeys());
        assertThat(expectedRemoveTags).isEqualTo(actualRemoveTags.tagKeys());

        Collections.sort(expectedAddTags, Comparator.comparing(Tag::key));
        Collections.sort(actualAddTags.tags(), Comparator.comparing(Tag::key));
        assertThat(expectedAddTags).isEqualTo(actualAddTags.tags());
    }

    private List<software.amazon.ssm.patchbaseline.Tag> buildCfnTagList(Map<String, String> tags) {
        List<software.amazon.ssm.patchbaseline.Tag> tagPropertiesList = new ArrayList<>();

        for (Map.Entry<String, String> tag : tags.entrySet()) {
            software.amazon.ssm.patchbaseline.Tag newTag = software.amazon.ssm.patchbaseline.Tag.builder()
                    .key(tag.getKey())
                    .value(tag.getValue())
                    .build();
            tagPropertiesList.add(newTag);
        }
        return tagPropertiesList;
    }

    private List<Tag> buildRequestTagList(Map<String, String> tags) {
        List<Tag> tagPropertiesList = new ArrayList<>();

        for (Map.Entry<String, String> tag : tags.entrySet()) {
            Tag newTag = Tag.builder()
                    .key(tag.getKey())
                    .value(tag.getValue())
                    .build();
            tagPropertiesList.add(newTag);
        }
        return tagPropertiesList;
    }


}

