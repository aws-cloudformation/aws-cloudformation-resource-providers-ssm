package software.amazon.ssm.resourcepolicies;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.cloudformation.LambdaWrapper;


public class ClientBuilder {

  public static SsmClient getClient() {
    return SsmClient.builder()
            .httpClient(LambdaWrapper.HTTP_CLIENT)
            .build();
  }
}
