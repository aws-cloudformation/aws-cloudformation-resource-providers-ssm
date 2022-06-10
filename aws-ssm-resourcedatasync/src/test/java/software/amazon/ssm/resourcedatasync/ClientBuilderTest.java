package software.amazon.ssm.resourcedatasync;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.SsmClient;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ClientBuilderTest extends TestBase {
    @Test
    public void getClient_success() {
        final SsmClient client = ClientBuilder.getClient();
        assertThat(client).isNotNull();
    }
}
