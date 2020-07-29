package software.amazon.ssm.maintenancewindow;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import software.amazon.awssdk.services.ssm.model.CreateMaintenanceWindowRequest;
import software.amazon.awssdk.services.ssm.model.CreateMaintenanceWindowResponse;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.AlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.ProgressEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.ssm.maintenancewindow.translator.request.CreateMaintenanceWindowTranslator;
import software.amazon.ssm.maintenancewindow.translator.ExceptionTranslator;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static software.amazon.ssm.maintenancewindow.TestConstants.RESOURCE_MODEL_TAGS;
import static software.amazon.ssm.maintenancewindow.TestConstants.SCHEDULE_OFFSET;
import static software.amazon.ssm.maintenancewindow.TestConstants.SCHEDULE_OFFSET_SCHEDULE;
import static software.amazon.ssm.maintenancewindow.TestConstants.SERVICE_MODEL_TAG_WITHOUT_RESOURCE_TAGS;
import static software.amazon.ssm.maintenancewindow.TestConstants.SERVICE_MODEL_TAG_WITH_RESOURCE_TAGS;
import static software.amazon.ssm.maintenancewindow.TestConstants.SYSTEM_TAGS;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest {

    private static final Boolean ALLOWED_UNASSOCIATED_TARGETS = false;
    private static final Integer CUTOFF = 1;
    private static final String SCHEDULE = "cron(0 4 ? * SUN *)";
    private static final Integer DURATION = 2;
    private static final String NAME = "TestMaintenanceWindow";
    private static final String WINDOW_ID = "mw-1234567890";
    private static final ResourceModel model = ResourceModel.builder()
            .name(NAME)
            .allowUnassociatedTargets(ALLOWED_UNASSOCIATED_TARGETS)
            .schedule(SCHEDULE)
            .duration(DURATION)
            .cutoff(CUTOFF)
            .build();
    private static final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .desiredResourceTags(RESOURCE_MODEL_TAGS)
            .systemTags(SYSTEM_TAGS)
            .build();

    private static final CreateMaintenanceWindowRequest createMaintenanceWindowRequest =
            CreateMaintenanceWindowRequest.builder()
                    .name(model.getName())
                    .allowUnassociatedTargets(model.getAllowUnassociatedTargets())
                    .schedule(model.getSchedule())
                    .duration(model.getDuration())
                    .cutoff(model.getCutoff())
                    .tags(SERVICE_MODEL_TAG_WITH_RESOURCE_TAGS)
                    .build();

    private CreateHandler handler;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @Mock
    private ExceptionTranslator exceptionTranslator;

    @Mock
    private CreateMaintenanceWindowTranslator createMaintenanceWindowTranslator;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        exceptionTranslator = mock(ExceptionTranslator.class);
        createMaintenanceWindowTranslator = mock(CreateMaintenanceWindowTranslator.class);
        handler = new CreateHandler(createMaintenanceWindowTranslator, exceptionTranslator);
    }

    @Test
    public void handleCreateRequestForSuccessWithResourceTags() {

        when(createMaintenanceWindowTranslator.resourceModelToRequest(model, RESOURCE_MODEL_TAGS, SYSTEM_TAGS))
                .thenReturn(createMaintenanceWindowRequest);

        final CreateMaintenanceWindowResponse result =
                CreateMaintenanceWindowResponse.builder()
                        .windowId(WINDOW_ID)
                        .build();

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(createMaintenanceWindowRequest),
                        ArgumentMatchers.<Function<CreateMaintenanceWindowRequest, CreateMaintenanceWindowResponse>>any()))
                .thenReturn(result);

        final ResourceModel expectedModel = request.getDesiredResourceState();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
                ProgressEvent.defaultSuccessHandler(expectedModel);

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(exceptionTranslator);

    }

    @Test
    public void handleCreateRequestForSuccessWithScheduleOffset() {

        ResourceModel scheduleOffsetModel = ResourceModel.builder()
                .name(NAME)
                .allowUnassociatedTargets(ALLOWED_UNASSOCIATED_TARGETS)
                .schedule(SCHEDULE_OFFSET_SCHEDULE)
                .scheduleOffset(SCHEDULE_OFFSET)
                .duration(DURATION)
                .cutoff(CUTOFF)
                .build();

        CreateMaintenanceWindowRequest scheduleOffsetCreateMaintenanceWindowRequest =
                CreateMaintenanceWindowRequest.builder()
                        .name(scheduleOffsetModel.getName())
                        .allowUnassociatedTargets(scheduleOffsetModel.getAllowUnassociatedTargets())
                        .schedule(scheduleOffsetModel.getSchedule())
                        .scheduleOffset(scheduleOffsetModel.getScheduleOffset())
                        .duration(scheduleOffsetModel.getDuration())
                        .cutoff(scheduleOffsetModel.getCutoff())
                        .tags(SERVICE_MODEL_TAG_WITHOUT_RESOURCE_TAGS)
                        .build();

        ResourceHandlerRequest<ResourceModel> scheduleOffsetRequest = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(scheduleOffsetModel)
                .systemTags(SYSTEM_TAGS)
                .build();
        when(createMaintenanceWindowTranslator.resourceModelToRequest(scheduleOffsetModel, null, SYSTEM_TAGS))
                .thenReturn(scheduleOffsetCreateMaintenanceWindowRequest);

        final CreateMaintenanceWindowResponse result =
                CreateMaintenanceWindowResponse.builder()
                        .windowId(WINDOW_ID)
                        .build();

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(scheduleOffsetCreateMaintenanceWindowRequest),
                        ArgumentMatchers.<Function<CreateMaintenanceWindowRequest, CreateMaintenanceWindowResponse>>any()))
                .thenReturn(result);

        final ResourceModel expectedModel = scheduleOffsetRequest.getDesiredResourceState();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, scheduleOffsetRequest, null, logger);

        final ProgressEvent<ResourceModel, CallbackContext> expectedProgressEvent =
                ProgressEvent.defaultSuccessHandler(expectedModel);

        assertThat(response).isEqualTo(expectedProgressEvent);
        verifyZeroInteractions(exceptionTranslator);

    }

    @Test
    void handleCreateRequestThrowsTranslatedServiceException() {

        when(createMaintenanceWindowTranslator.resourceModelToRequest(model,RESOURCE_MODEL_TAGS, SYSTEM_TAGS))
                .thenReturn(createMaintenanceWindowRequest);

        final InternalServerErrorException serviceException = InternalServerErrorException.builder().build();

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(createMaintenanceWindowRequest),
                        ArgumentMatchers.<Function<CreateMaintenanceWindowRequest, CreateMaintenanceWindowResponse>>any()))
                .thenThrow(serviceException);

        when(exceptionTranslator.translateFromServiceException(serviceException, createMaintenanceWindowRequest))
                .thenReturn(new CfnServiceInternalErrorException("CreateMaintenanceWindow", serviceException));

        Assertions.assertThrows(CfnServiceInternalErrorException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });

        verify(exceptionTranslator)
                .translateFromServiceException(serviceException, createMaintenanceWindowRequest);

    }

    @Test
    void handleCreateRequestThrowsAlreadyExistException() {

        when(createMaintenanceWindowTranslator.resourceModelToRequest(model,RESOURCE_MODEL_TAGS, SYSTEM_TAGS))
                .thenReturn(createMaintenanceWindowRequest);

        final AlreadyExistsException alreadyExistsException = AlreadyExistsException.builder().build();

        when(
                proxy.injectCredentialsAndInvokeV2(
                        eq(createMaintenanceWindowRequest),
                        ArgumentMatchers.<Function<CreateMaintenanceWindowRequest, CreateMaintenanceWindowResponse>>any()))
                .thenThrow(alreadyExistsException);

        when(exceptionTranslator.translateFromServiceException(alreadyExistsException, createMaintenanceWindowRequest))
                .thenReturn(new CfnAlreadyExistsException(alreadyExistsException));

        Assertions.assertThrows(CfnAlreadyExistsException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });

        verify(exceptionTranslator)
                .translateFromServiceException(alreadyExistsException, createMaintenanceWindowRequest);

    }
}
