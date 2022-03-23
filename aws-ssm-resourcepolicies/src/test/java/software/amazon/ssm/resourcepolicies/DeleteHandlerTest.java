package software.amazon.ssm.resourcepolicies;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DeleteResourcePolicyRequest;
import software.amazon.awssdk.services.ssm.model.DeleteResourcePolicyResponse;
import software.amazon.awssdk.services.ssm.model.ResourcePolicyInvalidParameterException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest extends AbstractTestBase {

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

    @AfterEach
    public void tear_down() {
        verify(sdkClient, atLeastOnce()).serviceName();
        verifyNoMoreInteractions(sdkClient);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final DeleteHandler handler = new DeleteHandler();

        final ResourceModel model = ResourceModel.builder()
                .id("T1")
                .resourceArn("arn:aws:ssm:us-east-1:712868411371:opsitemgroup/default")
                .policyId("123456789")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        DeleteResourcePolicyResponse deleteResourcePolicyResponse = DeleteResourcePolicyResponse.builder()
                .resourceArn(model.getResourceArn())
                .policyId("12345")
                .policyHash("12345678")
                .build();

        Mockito.when(proxyClient.client().deleteResourcePolicy(any(DeleteResourcePolicyRequest.class))).thenReturn(deleteResourcePolicyResponse);
        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_SimpleSuccess_NoPolicyId() {
        final DeleteHandler handler = new DeleteHandler();

        final ResourceModel model = ResourceModel.builder()
                .id("T1")
                .resourceArn("arn:aws:ssm:us-east-1:712868411371:opsitemgroup/default")
                .policyId("123456789")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        DeleteResourcePolicyResponse deleteResourcePolicyResponse = DeleteResourcePolicyResponse.builder()
                .resourceArn(model.getResourceArn())
                .policyId("12345")
                .policyHash("12345678")
                .build();

        Mockito.when(proxyClient.client().deleteResourcePolicy(any(DeleteResourcePolicyRequest.class))).thenThrow(ResourcePolicyInvalidParameterException.class);
        try {
            handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        }
        catch (CfnGeneralServiceException ex) {
            assertThat(ex).isInstanceOf(CfnGeneralServiceException.class);
        }
    }
}
