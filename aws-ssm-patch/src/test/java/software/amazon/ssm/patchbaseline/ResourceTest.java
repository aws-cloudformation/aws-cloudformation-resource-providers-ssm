package software.amazon.ssm.patchbaseline;

import com.amazonaws.AmazonServiceException;
import static software.amazon.ssm.patchbaseline.TestConstants.BASELINE_ID;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static junit.framework.Assert.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ResourceTest {

    private static final String EXCEPTION_MESSAGE = "Ruh Roh, Raggy";
    private ResourceModel model;

    @Mock
    private Logger logger;

    @BeforeEach
    public void setup() {
        model = ResourceModel.builder().id(BASELINE_ID).build();
    }

    @Test
    public void testKnownNonretriableException() {
        ProgressEvent<ResourceModel, CallbackContext> response =
                Resource.handleException(new IllegalArgumentException(EXCEPTION_MESSAGE), model, BASELINE_ID, logger);

        assertThat(response.getMessage()).isEqualTo(EXCEPTION_MESSAGE);
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
    }

    @Test
    public void testUnknownException() {
        ProgressEvent<ResourceModel, CallbackContext> response =
                Resource.handleException(new RuntimeException(EXCEPTION_MESSAGE), model, BASELINE_ID, logger);

        assertThat(response.getMessage()).isEqualTo(EXCEPTION_MESSAGE);
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);

    }

    @Test
    public void testAmazonServiceException400() {
        AmazonServiceException ex = new AmazonServiceException(EXCEPTION_MESSAGE);
        ex.setStatusCode(Resource.STATUS_CODE_400);

        ProgressEvent<ResourceModel, CallbackContext> response =
                Resource.handleException(ex, model, BASELINE_ID, logger);

        assertTrue(response.getMessage().contains(EXCEPTION_MESSAGE));
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);

    }

    @Test
    public void testAmazonServiceException400HttpTimeout() {
        AmazonServiceException ex = new AmazonServiceException(EXCEPTION_MESSAGE);
        ex.setStatusCode(Resource.STATUS_CODE_400);
        ex.setErrorCode(Resource.RETRIABLE_400_ERROR_CODES.get(0));

        ProgressEvent<ResourceModel, CallbackContext> response =
                Resource.handleException(ex, model, BASELINE_ID, logger);

        assertTrue(response.getMessage().contains(EXCEPTION_MESSAGE));
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
    }

    @Test
    public void testAmazonServiceException500() {
        AmazonServiceException ex = new AmazonServiceException(EXCEPTION_MESSAGE);
        ex.setStatusCode(Resource.STATUS_CODE_500);

        ProgressEvent<ResourceModel, CallbackContext> response =
                Resource.handleException(ex, model, BASELINE_ID, logger);

        assertTrue(response.getMessage().contains(EXCEPTION_MESSAGE));
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
    }

}
