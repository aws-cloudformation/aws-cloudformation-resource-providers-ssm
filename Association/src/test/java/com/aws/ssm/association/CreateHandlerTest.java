package com.aws.ssm.association;

import com.aws.cfn.proxy.AmazonWebServicesClientProxy;
import com.aws.cfn.proxy.Logger;
import com.aws.cfn.proxy.ProgressEvent;
import com.aws.cfn.proxy.ResourceHandlerRequest;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import software.amazon.awssdk.services.ssm.model.AssociationDescription;
import software.amazon.awssdk.services.ssm.model.CreateAssociationRequest;
import software.amazon.awssdk.services.ssm.model.CreateAssociationResponse;
import software.amazon.awssdk.services.ssm.model.SsmResponseMetadata;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreateHandlerTest {

    @Mock
    protected Logger logger;

    @Mock
    private CreateAssociationResponse response;

    @Mock
    private AssociationDescription associationDescription;

    @Mock
    protected CallbackContext callbackContext;

    @Mock
    private SsmResponseMetadata metadata;

    @Mock
    protected AmazonWebServicesClientProxy proxy;

    @Mock
    protected ResourceHandlerRequest<ResourceModel> resourceHandlerRequest;

    private CreateHandler createHandler;

    private ResourceModel resourceModel;

    @Before
    public void setup() {
        createHandler = new CreateHandler();
        resourceModel = new ResourceModel(null, "AssociationName", "DocumentVersion", "InstanceId", "Name", null, Maps.newHashMap(), "ScheduleExpression", null);
        when(resourceHandlerRequest.getDesiredResourceState()).thenReturn(resourceModel);
    }

    @Test
    public void testSuccessfulCreate() throws Exception {
        final CreateAssociationRequest createAssociationRequest = CreateAssociationRequest
                .builder()
                .associationName(resourceModel.getAssociationName())
                .documentVersion(resourceModel.getDocumentVersion())
                .instanceId(resourceModel.getInstanceId())
                .name(resourceModel.getName())
                .outputLocation(Utils.translateInstanceAssociationOutputLocation(resourceModel.getOutputLocation()))
                .parameters(Utils.getMapFromParameters(resourceModel.getParameters()))
                .scheduleExpression(resourceModel.getScheduleExpression())
                .targets(Utils.translateTargetCollection(resourceModel.getTargets()))
                .build();
        when(proxy.injectCredentialsAndInvokeV2(eq(createAssociationRequest), any())).thenReturn(response);
        when(response.responseMetadata()).thenReturn(metadata);
        when(response.associationDescription()).thenReturn(associationDescription);
        when(associationDescription.associationId()).thenReturn("AssociationId");
        when(metadata.requestId()).thenReturn("RequestID");
        final ProgressEvent pe = createHandler.handleRequest(proxy, resourceHandlerRequest, callbackContext, logger);

        verify(proxy).injectCredentialsAndInvokeV2(eq(createAssociationRequest), any());
        assertThat(pe, is(equalTo(Utils.defaultSuccessHandler(resourceModel))));
    }
}
