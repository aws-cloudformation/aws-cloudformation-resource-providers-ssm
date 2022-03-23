package software.amazon.ssm.resourcepolicies;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.PutResourcePolicyRequest;
import software.amazon.awssdk.services.ssm.model.PutResourcePolicyResponse;
import software.amazon.awssdk.services.ssm.model.ResourcePolicyInvalidParameterException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

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
        final UpdateHandler handler = new UpdateHandler();

        ResourceModel model = ResourceModel.builder()
                .id("T1")
                .policy("{\"Version\": \"2012-10-17\",\n" +
                        "    \"Statement\": [{\n" +
                        "        \"Sid\": \"AllowSecurityTeam\",\n" +
                        "        \"Effect\":\"Allow\",\n" +
                        "        \"Principal\": {\n" +
                        "            \"AWS\": \"arn:aws:iam::123456787901:root/Security\"\n" +
                        "        },\n" +
                        "        \"Action\": [\n" +
                        "            \"ssm:GetOpsItem\"\n" +
                        "        ],\n" +
                        "        \"Resource\": [\"arn:aws:ssm:us-east-1:712868411371:opsitemgroup/default\"],\n" +
                        "        \"Condition\": {\n" +
                        "                \"StringEquals\": {\n" +
                        "                    \"aws:ResourceTag/Security\": \"TRUE\",\n" +
                        "                    \"aws:PrincipalOrgID\":[\"MyOrgId\"]\"\n" +
                        "                }\n" +
                        "        }\n" +
                        "    }]}")
                .resourceArn("arn:aws:ssm:us-east-1:1234567890:opsitemgroup/default")
                .policyHash("some hash")
                .policyId("12345")
                .build();

        PutResourcePolicyResponse putResourcePolicyResponse = PutResourcePolicyResponse.builder()
                .policyId("12345")
                .policyHash("12345678")
                .build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        Mockito.when(proxyClient.client().putResourcePolicy(any(PutResourcePolicyRequest.class))).thenReturn(putResourcePolicyResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_Failure_ResourceArnNotExist() {
        final UpdateHandler handler = new UpdateHandler();

        final ResourceModel model = ResourceModel.builder()
                .id("123")
                .policy("{\"Version\": \"2012-10-17\",\n" +
                        "    \"Statement\": [{\n" +
                        "        \"Sid\": \"AllowSecurityTeam\",\n" +
                        "        \"Effect\":\"Allow\",\n" +
                        "        \"Principal\": {\n" +
                        "            \"AWS\": \"arn:aws:iam::123456787901:root/Security\"\n" +
                        "        },\n" +
                        "        \"Action\": [\n" +
                        "            \"ssm:GetOpsItem\"\n" +
                        "        ],\n" +
                        "        \"Resource\": [\"arn:aws:ssm:us-east-1:712868411371:notExist\"],\n" +
                        "        \"Condition\": {\n" +
                        "                \"StringEquals\": {\n" +
                        "                    \"aws:ResourceTag/Security\": \"TRUE\",\n" +
                        "                    \"aws:PrincipalOrgID\":[\"MyOrgId\"]\"\n" +
                        "                }\n" +
                        "        }\n" +
                        "    }]}")
                .resourceArn("arn:aws:ssm:us-east-1:712868411371:notExist")
                .policyHash("some hash")
                .policyId("12345")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        Mockito.when((this.proxyClient.client()).putResourcePolicy(ArgumentMatchers.any(PutResourcePolicyRequest.class))).thenThrow(ResourcePolicyInvalidParameterException.class);

        try {
            handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        }
        catch (CfnGeneralServiceException ex) {
            assertThat(ex).isInstanceOf(CfnGeneralServiceException.class);
        }
    }

    @Test
    public void handleRequest_Failure_InvalidPolicy() {
        final UpdateHandler handler = new UpdateHandler();

        final ResourceModel model = ResourceModel.builder()
                .id("T3")
                .policy("Invalid policy")
                .resourceArn("arn:aws:ssm:us-east-1:1234567890:opsitemgroup/default")
                .policyHash("some hash")
                .policyId("12345")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        Mockito.when((this.proxyClient.client()).putResourcePolicy(ArgumentMatchers.any(PutResourcePolicyRequest.class))).thenThrow(ResourcePolicyInvalidParameterException.class);

        try {
            handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        }
        catch (CfnGeneralServiceException ex) {
            assertThat(ex).isInstanceOf(CfnGeneralServiceException.class);
        }
    }

}
