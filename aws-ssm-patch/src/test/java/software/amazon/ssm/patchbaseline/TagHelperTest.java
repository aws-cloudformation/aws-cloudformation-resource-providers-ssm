package software.amazon.ssm.patchbaseline;

import software.amazon.awssdk.services.ssm.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.ssm.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.ssm.model.RemoveTagsFromResourceRequest;
import software.amazon.awssdk.services.ssm.model.RemoveTagsFromResourceResponse;
import software.amazon.awssdk.services.ssm.model.AddTagsToResourceRequest;
import software.amazon.awssdk.services.ssm.model.AddTagsToResourceResponse;
import software.amazon.awssdk.services.ssm.model.Tag;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import static software.amazon.ssm.patchbaseline.utils.ErrorMessage.NO_DUPLICATE_TAGS;
import static software.amazon.ssm.patchbaseline.utils.ErrorMessage.NO_SYSTEM_TAGS;
import static software.amazon.ssm.patchbaseline.utils.ErrorMessage.TAG_KEY_NULL;
import static software.amazon.ssm.patchbaseline.utils.ErrorMessage.TAG_NULL;
import software.amazon.ssm.patchbaseline.utils.SsmCfnClientSideException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.Collections;
import java.util.Comparator;

@ExtendWith(MockitoExtension.class)
public class TagHelperTest extends TestBase{

    private static final String TEST_RESOURCE_TYPE = "AResource";
    private static final String TEST_RESOURCE_ID = "pb-123";
    private ListTagsForResourceRequest listTagsForResourceRequest;
    private ListTagsForResourceResponse listTagsForResourceResponse;

    @InjectMocks
    private TagHelper cfnTagHelper;
    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Test
    public void testConvertRequestTagsToMap_Nominal() {
        Map<String, String> expected = new HashMap<>();
        expected.put("foo", "bar");
        expected.put("smash", "pow");

        List<Tag> input = Arrays.asList(
                 Tag.builder().key("foo").value("bar").build(),
                 Tag.builder().key("smash").value("pow").build()
        );
        assertThat(cfnTagHelper.convertRequestTagsToMap(input)).isEqualTo(expected);
    }

    @Test
    public void testConvertRequestTagsToMap_Null() {
        assertThat(cfnTagHelper.convertRequestTagsToMap(null)).isEqualTo(new HashMap<>());
    }

    @Test
    public void testConvertRequestTagsToMap_Empty() {
        assertThat(cfnTagHelper.convertRequestTagsToMap(new ArrayList<>())).isEqualTo(new HashMap<>());
    }

    @Test
    public void testConvertRequestTagsToMap_NullTag() {
        List<Tag> input = Arrays.asList(
                Tag.builder().key("foo").value("bar").build(),
                null
        );
        try {
            cfnTagHelper.convertRequestTagsToMap(input);
            Assertions.fail("Should have thrown an exception");
        } catch (SsmCfnClientSideException e) {
            assertThat(e.getMessage()).isEqualTo(TAG_NULL);
        }
    }

    @Test
    public void testConvertRequestTagsToMap_Duplicates() {
        List<Tag> input = Arrays.asList(
                 Tag.builder().key("foo").value("bar").build(),
                 Tag.builder().key("foo").value("blah").build()
        );

        try {
            cfnTagHelper.convertRequestTagsToMap(input);
            Assertions.fail("Should have thrown an exception");
        } catch (SsmCfnClientSideException e) {
            assertThat(e.getMessage()).isEqualTo(NO_DUPLICATE_TAGS);
        }
    }

    @Test
    public void testConvertRequestTagsToMap_NullKey() {
        List<Tag> input = Arrays.asList(
                 Tag.builder().key("foo").value("bar").build(),
                 Tag.builder().key(null).value("blah").build()
        );

        try {
            cfnTagHelper.convertRequestTagsToMap(input);
            Assertions.fail("Should have thrown an exception");
        } catch (SsmCfnClientSideException e) {
            assertThat(e.getMessage()).isEqualTo(TAG_KEY_NULL);
        }
    }

    @Test
    public void testConvertRequestTagsToMap_SystemTag() {
        List<Tag> input = Arrays.asList(
                 Tag.builder().key("aws:foo").value("bar").build(),
                 Tag.builder().key("foo").value("blah").build()
        );

        try {
            cfnTagHelper.convertRequestTagsToMap(input);
            Assertions.fail("Should have thrown an exception");
        } catch (SsmCfnClientSideException e) {
            assertThat(e.getMessage()).isEqualTo(NO_SYSTEM_TAGS);
        }
    }

    @Test
    public void testValidateCustomerSuppliedTags_Nominal() {
        Map<String, String> input = new HashMap<>();
        input.put("foo", "bar");
        input.put("smash", "pow");

        cfnTagHelper.validateCustomerSuppliedTags(input);
    }

    @Test
    public void testValidateCustomerSuppliedTags_Null() {
        cfnTagHelper.validateCustomerSuppliedTags(null);
    }

    @Test
    public void testValidateCustomerSuppliedTags_NullKey() {
        Map<String, String> input = new HashMap<>();
        input.put("foo", "bar");
        input.put(null, "pow");

        try {
            cfnTagHelper.validateCustomerSuppliedTags(input);
            Assertions.fail("Should have thrown an exception");
        } catch (SsmCfnClientSideException e) {
            assertThat(e.getMessage()).isEqualTo(TAG_KEY_NULL);
        }
    }

    @Test
    public void testValidateCustomerSuppliedTags_SystemTag() {
        Map<String, String> input = new HashMap<>();
        input.put("aws:foo", "bar");
        input.put("smash", "pow");

        try {
            cfnTagHelper.validateCustomerSuppliedTags(input);
            Assertions.fail("Should have thrown an exception");
        } catch (SsmCfnClientSideException e) {
            assertThat(e.getMessage()).isEqualTo(NO_SYSTEM_TAGS);
        }
    }

    @Test
    public void testConvertToTagList_Nominal() {
        Map<String, String> input = new HashMap<>();
        input.put("aws:foo", "bar");
        input.put("smash", "pow");
        input.put("fizz", "buzz");

        List<Tag> expected = Arrays.asList(
                 Tag.builder().key("aws:foo").value("bar").build(),
                 Tag.builder().key("smash").value("pow").build(),
                 Tag.builder().key("fizz").value("buzz").build()
        );

        List<Tag> actual = TagHelper.convertToTagList(input);

        // Because internally we use a map, list ordering will be unpredictable, so we'll
        // just confirm that lists contain same elements
        Collections.sort(expected, Comparator.comparing(Tag::key));
        Collections.sort(actual, Comparator.comparing(Tag::key));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testConvertToTagList_NullKey() {
        Map<String, String> input = new HashMap<>();
        input.put(null, "buzz");

        List<Tag> expected = Arrays.asList(
                 Tag.builder().key(null).value("buzz").build()
        );

        List<Tag> actual = TagHelper.convertToTagList(input);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testConvertToTagList_Empty() {
        assertThat(TagHelper.convertToTagList(new HashMap<>())).isEqualTo(new ArrayList<>());
    }

    @Test
    public void testValidateAndMergeTagsForCreate_Nominal() {
        Map<String, String> resourceTags = new HashMap<>();
        resourceTags.put("foo", "bar");
        resourceTags.put("smash", "pow");
        resourceTags.put("fizz", "buzz");

        Map<String, String> stackTags = new HashMap<>();
        stackTags.put("stackkey", "stackvalue");

        Map<String, String> systemTags = new HashMap<>();
        systemTags.put("aws:somekey", "somevalue");
        systemTags.put("aws:anotherkey", "anothervalue");

        ResourceHandlerRequest<ResourceModel> request = new ResourceHandlerRequest<ResourceModel>();
        request.setDesiredResourceTags(stackTags);
        request.setSystemTags(systemTags);
        request.setLogicalResourceIdentifier(TEST_RESOURCE_ID);

        List<Tag> expected = buildRequestTagList(resourceTags);
        List<Tag> tagListStackTag = buildRequestTagList(stackTags);
        List<Tag> tagListSystemTag = buildRequestTagList(systemTags);
        expected.addAll(tagListStackTag);
        expected.addAll(tagListSystemTag);

        List<Tag> actual = cfnTagHelper.validateAndMergeTagsForCreate(request, buildCfnTagList(resourceTags));

        // Because internally we use a map, list ordering will be unpredictable, so we'll
        // just confirm that lists contain same elements
        Collections.sort(expected, Comparator.comparing(Tag::key));
        Collections.sort(actual, Comparator.comparing(Tag::key));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testValidateAndMergeTagsForCreate_SystemTagResource() {
        Map<String, String> resourceTags = new HashMap<>();
        resourceTags.put("aws:foo", "bar");

        try {
            cfnTagHelper.validateAndMergeTagsForCreate(new ResourceHandlerRequest<ResourceModel>(), buildCfnTagList(resourceTags));
            Assertions.fail("Should have thrown an exception");
        } catch (SsmCfnClientSideException e) {
            assertThat(e.getMessage()).isEqualTo(NO_SYSTEM_TAGS);
        }
    }

    @Test
    public void testValidateAndMergeTagsForCreate_SystemTagStack() {
        Map<String, String> stackTags = new HashMap<>();
        stackTags.put("aws:stackkey", "stackvalue");

        ResourceHandlerRequest<ResourceModel> request = new ResourceHandlerRequest<ResourceModel>();
        request.setDesiredResourceTags(stackTags);

        try {
            cfnTagHelper.validateAndMergeTagsForCreate(request, new ArrayList<>());
            Assertions.fail("Should have thrown an exception");
        } catch (SsmCfnClientSideException e) {
            assertThat(e.getMessage()).isEqualTo(NO_SYSTEM_TAGS);
        }
    }

    @Test
    public void testValidateAndMergeTagsForCreate_TagPrecedence() {
        Map<String, String> resourceTags = new HashMap<>();
        resourceTags.put("resourceAndStack", "resource");
        resourceTags.put("resourceAndCloudformation", "resource");
        resourceTags.put("allThree", "resource");

        Map<String, String> stackTags = new HashMap<>();
        stackTags.put("resourceAndStack", "stack");
        stackTags.put("stackAndCloudformation", "stack");
        stackTags.put("allThree", "stack");

        Map<String, String> systemTags = new HashMap<>();
        systemTags.put("stackAndCloudformation", "cloudformation");
        systemTags.put("resourceAndCloudformation", "cloudformation");
        systemTags.put("allThree", "cloudformation");

        ResourceHandlerRequest<ResourceModel> request = new ResourceHandlerRequest<ResourceModel>();
        request.setDesiredResourceTags(stackTags);
        request.setSystemTags(systemTags);
        request.setLogicalResourceIdentifier(TEST_RESOURCE_ID);

        List<Tag> expected = Arrays.asList(
                 Tag.builder().key("resourceAndStack").value("resource").build(),
                 Tag.builder().key("resourceAndCloudformation").value("resource").build(),
                 Tag.builder().key("allThree").value("resource").build(),
                 Tag.builder().key("stackAndCloudformation").value("stack").build());

        List<Tag> actual = cfnTagHelper.validateAndMergeTagsForCreate(request, buildCfnTagList(resourceTags));

        // Because internally we use a map, list ordering will be unpredictable, so we'll
        // just confirm that lists contain same elements
        Collections.sort(expected, Comparator.comparing(Tag::key));
        Collections.sort(actual, Comparator.comparing(Tag::key));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testValidateAndMergeTagsForCreate_NullLists() {
        assertThat(cfnTagHelper.validateAndMergeTagsForCreate(new ResourceHandlerRequest<ResourceModel>(), null)).isEqualTo(new ArrayList<>());
    }

    @Test
    public void testUpdateTagsForResource_Nominal() {
        listTagsForResourceRequest = ListTagsForResourceRequest.builder().resourceType(TEST_RESOURCE_TYPE).resourceId(TEST_RESOURCE_ID).build();
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

        ResourceModel newResourceModel = ResourceModel.builder().tags(buildCfnTagList(newResourceTags)).id(TEST_RESOURCE_ID).build();

        ResourceHandlerRequest<ResourceModel> request = new ResourceHandlerRequest<ResourceModel>();
        request.setDesiredResourceState(newResourceModel);
        request.setDesiredResourceTags(newStackTags);
        request.setLogicalResourceIdentifier(TEST_RESOURCE_ID);

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
        // resource tag in ResourceModel
        Map<String, String> newResourceTags = new HashMap<>();
        newResourceTags.put("aws:foo", "bar");

        ResourceModel newResourceModel = ResourceModel.builder().tags(buildCfnTagList(newResourceTags)).id(TEST_RESOURCE_ID).build();

        ResourceHandlerRequest<ResourceModel> request = new ResourceHandlerRequest<ResourceModel>();
        request.setDesiredResourceState(newResourceModel);
        request.setLogicalResourceIdentifier(TEST_RESOURCE_ID);

        try {
            cfnTagHelper.updateTagsForResource(request, TEST_RESOURCE_TYPE, ssmClient, proxy);
            Assertions.fail("Should have thrown an exception");
        } catch (SsmCfnClientSideException e) {
            assertThat(e.getMessage()).isEqualTo(NO_SYSTEM_TAGS);
            verifyZeroInteractions(proxy);
        }
    }

    @Test
    public void testUpdateTagsForResource_SystemTagStack() {
        // stack tag in getDesiredResourceTag
        Map<String, String> newStackTags = new HashMap<>();
        newStackTags.put("aws:foo", "bar");

        ResourceModel newResourceModel = ResourceModel.builder().id(TEST_RESOURCE_ID).build();

        ResourceHandlerRequest<ResourceModel> request = new ResourceHandlerRequest<ResourceModel>();
        request.setDesiredResourceState(newResourceModel);
        request.setDesiredResourceTags(newStackTags);
        request.setLogicalResourceIdentifier(TEST_RESOURCE_ID);

        try {
            cfnTagHelper.updateTagsForResource(request, TEST_RESOURCE_TYPE, ssmClient, proxy);
            Assertions.fail("Should have thrown an exception");
        } catch (SsmCfnClientSideException e) {
            assertThat(e.getMessage()).isEqualTo(NO_SYSTEM_TAGS);
            verifyZeroInteractions(proxy);
        }
    }

    @Test
    public void testUpdateTagsForResource_EmptyLists() {
        listTagsForResourceRequest = ListTagsForResourceRequest.builder().resourceType(TEST_RESOURCE_TYPE).resourceId(TEST_RESOURCE_ID).build();
        listTagsForResourceResponse = ListTagsForResourceResponse.builder().build();
        when(proxy.injectCredentialsAndInvokeV2(
                eq(listTagsForResourceRequest),
                ArgumentMatchers.<Function<ListTagsForResourceRequest, ListTagsForResourceResponse>>any()))
                .thenReturn(listTagsForResourceResponse);

        ResourceModel newResourceModel = ResourceModel.builder().id(TEST_RESOURCE_ID).build();

        ResourceHandlerRequest<ResourceModel> request = new ResourceHandlerRequest<ResourceModel>();
        request.setLogicalResourceIdentifier(TEST_RESOURCE_ID);
        request.setDesiredResourceState(newResourceModel);

        cfnTagHelper.updateTagsForResource(request, TEST_RESOURCE_TYPE, ssmClient, proxy);

        verify(proxy)
                .injectCredentialsAndInvokeV2(
                        eq(listTagsForResourceRequest),
                        ArgumentMatchers.<Function<ListTagsForResourceRequest, ListTagsForResourceResponse>>any());
        verifyNoMoreInteractions(proxy);
    }

    @Test
    public void testUpdateTagsForResource_EmptyRequest() {
        cfnTagHelper.updateTagsForResource(new ResourceHandlerRequest<ResourceModel>(), TEST_RESOURCE_TYPE, ssmClient, proxy);
        verifyZeroInteractions(proxy);
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
