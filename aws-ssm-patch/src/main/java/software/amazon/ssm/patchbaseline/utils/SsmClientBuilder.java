package software.amazon.ssm.patchbaseline.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.cloudformation.LambdaWrapper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SsmClientBuilder {
    public static SsmClient getClient() {
        return SsmClient.builder()
                //.httpClient(LambdaWrapper.HTTP_CLIENT)
                .build();
    }
}
