package software.amazon.ssm.patchbaseline;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.loggers.LambdaLogPublisher;
import software.amazon.cloudformation.proxy.*;
import software.amazon.cloudformation.resource.ResourceTypeSchema;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HandlerWrapperImpl extends HandlerWrapper{

    private final static TypeReference<ResourceHandlerTestPayload<ResourceModel, CallbackContext>> TEST_ENTRY_TYPE_REFERENCE =
            new TypeReference<ResourceHandlerTestPayload<ResourceModel, CallbackContext>>() {};
    @Override
    public void testEntrypoint(
            final InputStream inputStream,
            final OutputStream outputStream,
            final Context context) throws IOException {

        this.loggerProxy = new LoggerProxy();
        this.loggerProxy.addLogPublisher(new LambdaLogPublisher(context.getLogger()));

        ProgressEvent<ResourceModel, CallbackContext> response = ProgressEvent.failed(null, null,
                HandlerErrorCode.InternalFailure, "Uninitialized");
        try {
            final String input = IOUtils.toString(inputStream, "UTF-8");
            final ResourceHandlerTestPayload<ResourceModel, CallbackContext> payload =
                    this.serializer.deserialize(
                            input,
                            TEST_ENTRY_TYPE_REFERENCE);

            final AmazonWebServicesClientProxy proxy = new AmazonWebServicesClientProxy(
                    loggerProxy, payload.getCredentials(), () -> (long) context.getRemainingTimeInMillis());

            response = invokeHandler(proxy, payload.getRequest(), payload.getAction(), payload.getCallbackContext());
        } catch (final BaseHandlerException e) {
            response = ProgressEvent.defaultFailureHandler(e, e.getErrorCode());
        } catch (final AmazonServiceException | AwsServiceException e) {
            response = ProgressEvent.defaultFailureHandler(e, HandlerErrorCode.GeneralServiceException);
        } catch (final Throwable e) {
            e.printStackTrace();
            response = ProgressEvent.defaultFailureHandler(e, HandlerErrorCode.InternalFailure);
        } finally {
            writeResponse(outputStream, response);


            // outputStream.close();
        }
    }

    private void writeResponse(final OutputStream outputStream, final ProgressEvent<ResourceModel, CallbackContext> response)
            throws IOException {
        ResourceModel model = response.getResourceModel();
        if (model != null) {
            JSONObject modelObject = new JSONObject(this.serializer.serialize(model));
            ResourceTypeSchema.load(provideResourceSchemaJSONObject()).removeWriteOnlyProperties(modelObject);
            ResourceModel sanitizedModel = this.serializer.deserializeStrict(modelObject.toString(), getModelTypeReference());

            response.setResourceModel(sanitizedModel);
        }

        String output = this.serializer.serialize(response);
        outputStream.write(output.getBytes(StandardCharsets.UTF_8));
        outputStream.close();
    }
}
