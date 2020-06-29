package software.amazon.ssm.maintenancewindow.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.cloudformation.LambdaWrapper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClientBuilder {
    /**
     * Builds and returns SsmClient with configuration overrides.
     *
     * @return Configured SsmClient.
     */
    public static SsmClient getClient() {
        return SsmClient.builder()
                .httpClient(LambdaWrapper.HTTP_CLIENT)
                .build();
    }
}
