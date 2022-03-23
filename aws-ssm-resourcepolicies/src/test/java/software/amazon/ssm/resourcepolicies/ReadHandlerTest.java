package software.amazon.ssm.resourcepolicies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetResourcePoliciesRequest;
import software.amazon.awssdk.services.ssm.model.GetResourcePoliciesResponse;
import software.amazon.awssdk.services.ssm.model.GetResourcePoliciesResponseEntry;
import software.amazon.awssdk.services.ssm.model.ResourcePolicyNotFoundException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<SsmClient> proxyClient;

    @Mock
    SsmClient sdkClient;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(SsmClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ReadHandler handler = new ReadHandler();

        final ResourceModel model = ResourceModel.builder()
                .resourceArn("arn:aws:ssm:us-east-1:712868411371:opsitemgroup/default")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final GetResourcePoliciesResponseEntry getResourcePoliciesResponseEntry = GetResourcePoliciesResponseEntry.builder()
                .policy("some policy")
                .policyId("some policy id")
                .policyHash("some policy hash")
                .build();

        final GetResourcePoliciesResponse getResourcePoliciesResponse = GetResourcePoliciesResponse.builder()
                .policies(getResourcePoliciesResponseEntry)
                .nextToken("some next token")
                .build();

        when(proxyClient.client().getResourcePolicies(any(GetResourcePoliciesRequest.class))).thenReturn(getResourcePoliciesResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_SimpleSuccess_NoPolicies() {
        final ReadHandler handler = new ReadHandler();

        final ResourceModel model = ResourceModel.builder()
                .resourceArn("arn:aws:ssm:us-east-1:712868411371:opsitemgroup/default")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final GetResourcePoliciesResponse readResourcePolicyResponse = GetResourcePoliciesResponse.builder()
                .nextToken("None")
                .build();

        when(proxyClient.client().getResourcePolicies(any(GetResourcePoliciesRequest.class))).thenReturn(readResourcePolicyResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        //assertThat(response.getResourceModel().getPolicyId()).isEqualTo("No policy");
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_Failure_ResourceNotExist() {
        final ReadHandler handler = new ReadHandler();

        final ResourceModel model = ResourceModel.builder()
                .resourceArn("arn:aws:ssm:us-east-1:712868411371:NotExist")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        Mockito.when((this.proxyClient.client()).getResourcePolicies(ArgumentMatchers.any(GetResourcePoliciesRequest.class))).thenThrow(ResourcePolicyNotFoundException.class);

        try {
            handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        }
        catch (CfnGeneralServiceException ex) {
            assertThat(ex).isInstanceOf(CfnGeneralServiceException.class);
        }

    }
}
