package com.aws.ssm.association;

import com.aws.cfn.proxy.AmazonWebServicesClientProxy;
import com.aws.cfn.proxy.HandlerErrorCode;
import com.aws.cfn.proxy.Logger;
import com.aws.cfn.proxy.OperationStatus;
import com.aws.cfn.proxy.ProgressEvent;
import com.aws.cfn.proxy.ResourceHandlerRequest;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import software.amazon.awssdk.services.ssm.model.SsmResponseMetadata;
import software.amazon.awssdk.services.ssm.model.UpdateAssociationRequest;
import software.amazon.awssdk.services.ssm.model.UpdateAssociationResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdateHandlerTest {

    @Mock
    protected Logger logger;

    @Mock
    private UpdateAssociationResponse response;

    @Mock
    protected CallbackContext callbackContext;

    @Mock
    private SsmResponseMetadata metadata;

    @Mock
    protected AmazonWebServicesClientProxy proxy;

    @Mock
    protected ResourceHandlerRequest<ResourceModel> resourceHandlerRequest;


    private UpdateHandler updateHandler;
    private ResourceModel resourceModel;
    private ResourceModel resourceModelUpdate;
    private ResourceModel resourceModelImmutableUpdate;
    @Before
    public void setup() {
        updateHandler = new UpdateHandler();
        resourceModel = new ResourceModel("AssociationId", "AssociationName", "DocumentVersion", "InstanceId", "Name", null, Maps.newHashMap(), "ScheduleExpression", null);
        resourceModelUpdate = new ResourceModel("AssociationId", "AssociationName", "DocumentVersion", "InstanceId", "Name", null, Maps.newHashMap(), "ScheduleExpressionUpdate", null);
        resourceModelImmutableUpdate = new ResourceModel("AssociationId", "AssociationNameUpdate", "DocumentVersion", "InstanceIdUpdate", "Name", null, Maps.newHashMap(), "ScheduleExpressionUpdate", null);
        when(resourceHandlerRequest.getDesiredResourceState()).thenReturn(resourceModelUpdate);

    }

    @Test
    public void testSuccessfulUpdate() {
        final UpdateAssociationRequest updateAssociationRequest = UpdateAssociationRequest
                .builder()
                .associationId(resourceModelUpdate.getAssociationId())
                .associationName(resourceModelUpdate.getAssociationName())
                .documentVersion(resourceModelUpdate.getDocumentVersion())
                .outputLocation(Utils.translateInstanceAssociationOutputLocation(resourceModelUpdate.getOutputLocation()))
                .parameters(Utils.getMapFromParameters(resourceModelUpdate.getParameters()))
                .scheduleExpression(resourceModelUpdate.getScheduleExpression())
                .build();
        when(resourceHandlerRequest.getPreviousResourceState()).thenReturn(resourceModel);
        when(proxy.injectCredentialsAndInvokeV2(eq(updateAssociationRequest), any())).thenReturn(response);
        when(response.responseMetadata()).thenReturn(metadata);
        when(metadata.requestId()).thenReturn("RequestID");
        final ProgressEvent pe = updateHandler.handleRequest(proxy, resourceHandlerRequest, callbackContext, logger);

        verify(proxy).injectCredentialsAndInvokeV2(eq(updateAssociationRequest), any());
        assertThat(pe, is(equalTo(Utils.defaultSuccessHandler(resourceModelUpdate))));
    }

    @Test
    public void testImmutableUpdate() {
        when(resourceHandlerRequest.getPreviousResourceState()).thenReturn(resourceModelImmutableUpdate);
        final ProgressEvent pe = updateHandler.handleRequest(proxy, resourceHandlerRequest, callbackContext, logger);
        final ProgressEvent expectedPE = ProgressEvent.<ResourceModel, CallbackContext>builder()
                .errorCode(HandlerErrorCode.NotUpdatable)
                .status(OperationStatus.FAILED)
                .build();
        assertThat(pe, is(equalTo(expectedPE)));
    }
}
