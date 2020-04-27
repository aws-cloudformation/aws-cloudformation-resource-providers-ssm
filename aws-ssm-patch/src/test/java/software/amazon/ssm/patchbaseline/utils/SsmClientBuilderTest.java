package software.amazon.ssm.patchbaseline.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit test to verify that SSMClientFactory can successfully create SSMClient
 */
@ExtendWith(MockitoExtension.class)
public class SsmClientBuilderTest {

    @Test
    public void testSuccess() {
        //This test will throw an exception if it fails
        SsmClientBuilder.getClient();
    }
}
