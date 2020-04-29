package software.amazon.ssm.patchbaseline.utils;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.HashMap;

public class TagUtilsTest {

    @Test
    public void testConsolidateTagsTags_Nominal() {
        Map<String, String> resourceTags = new HashMap<>();
        resourceTags.put("foo", "bar");

        Map<String, String> stackTags = new HashMap<>();
        stackTags.put("stackkey", "stackvalue");

        Map<String, String> systemTags = new HashMap<>();
        systemTags.put("aws:somekey", "somevalue");

        Map<String, String> consolidatedTags = TagUtils.consolidateTags(stackTags, systemTags, resourceTags);

        Map<String, String> expected = new HashMap<>();
        expected.put("foo", "bar");
        expected.put("stackkey", "stackvalue");
        expected.put("aws:somekey", "somevalue");

        assertThat(consolidatedTags).isEqualTo(expected);
    }

    @Test
    public void testConsolidateTagsTags_TagPrecedence() {
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

        Map<String, String> consolidatedTags = TagUtils.consolidateTags(stackTags, systemTags, resourceTags);

        Map<String, String> expected = new HashMap<>();
        expected.put("resourceAndStack", "resource");
        expected.put("resourceAndCloudformation", "resource");
        expected.put("allThree", "resource");
        expected.put("stackAndCloudformation", "stack");

        assertThat(consolidatedTags).isEqualTo(expected);
    }

    @Test
    public void testGetTagsToCreate(){

        Map<String, String> newTags = new HashMap<>();
        newTags.put("Key1", "Value1");
        newTags.put("Key2", "Value2");
        newTags.put("Key4", "Value4") ;

        Map<String, String> oldTags = new HashMap<>();
        oldTags.put("Key1", "Value1");
        oldTags.put("Key2", "Value4");
        oldTags.put("Key3", "Value3");

        Map<String, String> expected = new HashMap<>();
        expected.put("Key2", "Value2");
        expected.put("Key4", "Value4");

        Map<String, String> tagsToCreate = TagUtils.getTagsToCreate(newTags, oldTags);
        assertThat(tagsToCreate).isEqualTo(expected);
    }

    @Test
    public void testGetTagsToDelete(){

        Map<String, String> newTags = new HashMap<>();
        newTags.put("Key1", "Value1");
        newTags.put("Key2", "Value2");
        newTags.put("Key4", "Value4") ;

        Map<String, String> oldTags = new HashMap<>();
        oldTags.put("Key1", "Value1");
        oldTags.put("Key2", "Value4");
        oldTags.put("Key3", "Value3");

        Map<String, String> expected = new HashMap<>();
        expected.put("Key3", "Value3");

        Map<String, String> tagsToDelete = TagUtils.getTagsToDelete(newTags, oldTags);
        assertThat(tagsToDelete).isEqualTo(expected);
    }

}
