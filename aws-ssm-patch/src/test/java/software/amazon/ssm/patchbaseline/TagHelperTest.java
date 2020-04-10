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
import static software.amazon.ssm.patchbaseline.TestConstants.PATCH_BASELINE_RESOURCE_NAME;

import software.amazon.ssm.patchbaseline.utils.SsmCfnClientSideException;

import java.util.*;
import java.util.function.Function;

@ExtendWith(MockitoExtension.class)
public class TagHelperTest extends TestBase{

    private SimpleTypeValidator simpleTypeValidator;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    private TagHelper cfnTagHelper;

    private static final String TEST_TAG_PROPERTY_NAME = "MyTags";
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

        List<Tag> tagListResourceTag = buildRequestTagList(oldResourceTags);
        List<Tag> tagList = buildRequestTagList(oldStackTags);
        tagList.addAll(tagListResourceTag);

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

        when(proxy.injectCredentialsAndInvokeV2(
                eq(listTagsForResourceRequest),
                ArgumentMatchers.<Function<ListTagsForResourceRequest, ListTagsForResourceResponse>>any()))
                .thenReturn(listTagsForResourceResponse);

        cfnTagHelper.updateTagsForResource(request, TEST_RESOURCE_TYPE, ssmClient, proxy);

        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(listTagsForResourceRequest),
                        ArgumentMatchers.<Function<ListTagsForResourceRequest, ListTagsForResourceResponse>>any());


        ArgumentCaptor<RemoveTagsFromResourceRequest> removeTagsRequest = ArgumentCaptor.forClass(RemoveTagsFromResourceRequest.class);
        ArgumentCaptor<AddTagsToResourceRequest> addTagsRequest = ArgumentCaptor.forClass(AddTagsToResourceRequest.class);

        verify(proxy, atLeastOnce())
                .injectCredentialsAndInvokeV2(
                        removeTagsRequest.capture(),
                        ArgumentMatchers.<Function<RemoveTagsFromResourceRequest, RemoveTagsFromResourceResponse>>any());

        verify(proxy, atLeastOnce())
                .injectCredentialsAndInvokeV2(
                        addTagsRequest.capture(),
                        ArgumentMatchers.<Function<AddTagsToResourceRequest, AddTagsToResourceResponse>>any());

        final List<RemoveTagsFromResourceRequest> capturedRemovedValues = typeCheckedValues(removeTagsRequest.getAllValues(), RemoveTagsFromResourceRequest.class);
        assertThat(capturedRemovedValues.size()).isEqualTo(1);
        final RemoveTagsFromResourceRequest actualRemoveTags = capturedRemovedValues.get(0);

        final List<AddTagsToResourceRequest> capturedAddedValues = typeCheckedValues(addTagsRequest.getAllValues(), AddTagsToResourceRequest.class);
        assertThat(capturedAddedValues.size()).isEqualTo(1);
        final AddTagsToResourceRequest actualAddTags = capturedAddedValues.get(0);

        assertThat(TEST_RESOURCE_ID).isEqualTo(actualRemoveTags.resourceId());
        assertThat(TEST_RESOURCE_TYPE).isEqualTo(actualRemoveTags.resourceTypeAsString());

        assertThat(TEST_RESOURCE_ID).isEqualTo(actualAddTags.resourceId());
        assertThat(TEST_RESOURCE_TYPE).isEqualTo( actualAddTags.resourceTypeAsString());

        // Because internally we use a map, list ordering will be unpredictable, so we'll
        // just confirm that lists contain same elements
        Collections.sort(expectedRemoveTags);
        List<String> actualRemoveTagsKeys = actualRemoveTags.tagKeys();

        for (String key : actualRemoveTagsKeys) {
            System.out.print(String.format("actualRemoveTagsKeys key %s %n", key));
        }
        for (String key : expectedRemoveTags) {
            System.out.print(String.format("expectedRemoveTags key %s %n", key));
        }

        List<String> actualRemoveTagsKeysConvertType = new ArrayList<>(actualRemoveTagsKeys);
        Collections.sort(actualRemoveTagsKeysConvertType);
        assertThat(expectedRemoveTags).isEqualTo(actualRemoveTagsKeysConvertType);

        Collections.sort(expectedAddTags, Comparator.comparing(Tag::key));
        List<Tag> actualAddTagsList = actualAddTags.tags();
        List<Tag> actualAddTagsListConvertType = new ArrayList<>(actualAddTagsList);
        Collections.sort(actualAddTagsListConvertType, Comparator.comparing(Tag::key));  // why is this reporting error?
        assertThat(expectedAddTags).isEqualTo(actualAddTagsListConvertType);
    }

    @Test
    public void testUpdateTagsForResource_SystemTagResource() {
        Map<String, String> newResourceTags = new HashMap<>();
        newResourceTags.put("aws:foo", "bar");

        Map<String, Object> oldResourceProperties = new HashMap<>();
        oldResourceProperties.put(TEST_TAG_PROPERTY_NAME, buildCfnTagList(new HashMap<>()));

        Map<String, Object> newResourceProperties = new HashMap<>();
        newResourceProperties.put(TEST_TAG_PROPERTY_NAME, buildCfnTagList(newResourceTags));


        listTagsForResourceResponse = ListTagsForResourceResponse.builder().tagList(tagList).build();

        ResourceModel oldResourceModel = ResourceModel.builder().tags(buildCfnTagList(oldResourceTags)).id(TEST_RESOURCE_ID).build();

        ResourceModel newResourceModel = ResourceModel.builder().tags(buildCfnTagList(newResourceTags)).id(TEST_RESOURCE_ID).build();

        RequestData requestData = new RequestData();
        requestData.setPreviousStackTags(new HashMap<>());
        requestData.setStackTags(new HashMap<>());
        requestData.setPreviousResourceProperties(oldResourceProperties);
        requestData.setResourceProperties(newResourceProperties);
        requestData.setPhysicalResourceId(TEST_RESOURCE_ID);

        try {
            cfnTagHelper.updateTagsForResource(requestData, TEST_TAG_PROPERTY_NAME, TEST_RESOURCE_TYPE, ssmClient);
            fail("Should have thrown an exception");
        } catch (SsmCfnClientSideException e) {
            assertEquals(NO_SYSTEM_TAGS, e.getMessage());
            verifyZeroInteractions(ssmClient);
        }
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

    private static <T> List<T> typeCheckedValues(List<T> values, Class<T> clazz) {
        final List<T> typeCheckedValues = new ArrayList<>();
        for (final T value : values) {
            if (clazz.isInstance(value)) {
                typeCheckedValues.add(value);
            }
        }
        return typeCheckedValues;
    }

}

