package software.amazon.ssm.maintenancewindowtask.util;

import com.amazonaws.util.CollectionUtils;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.ssm.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;



/**
 * Translates Resource Model to Request
 */
public class ResourceRequestTranslator {


    /**
     * Translate Resource Model Targets to Request Targets
     */
    public  static Optional<List<Target>> translateToRequestTargets(final List<software.amazon.ssm.maintenancewindowtask.Target> resourceModelTargets) {

        if (!CollectionUtils.isNullOrEmpty(resourceModelTargets)) {
            List<Target> requestTargets = resourceModelTargets.stream().map(entry ->
                    Target.builder()
                            .key(entry.getKey())
                            .values(entry.getValues())
                            .build()).collect(Collectors.toList());
            return Optional.of(requestTargets);
        }
        return Optional.empty();
    }

    /**
     * Translate Resource Model LoggingInfo to Request LoggingInfo
     */
    public static Optional<LoggingInfo> translateToRequestLoggingInfo(final software.amazon.ssm.maintenancewindowtask.LoggingInfo resourceModelLoggingInfo) {
        if (resourceModelLoggingInfo == null) {
            return Optional.empty();
        } else {
            LoggingInfo requestLoggingInfo = LoggingInfo.builder()
                    .s3BucketName(resourceModelLoggingInfo.getS3Bucket())
                    .s3KeyPrefix(resourceModelLoggingInfo.getS3Prefix())
                    .s3Region(resourceModelLoggingInfo.getRegion()).build();
            return Optional.of(requestLoggingInfo);
        }
    }

    /**
     * Translate Resource Model NotificationConfig to Request NotificationConfig
     */
    public static Optional<NotificationConfig> translateToRequestNotificationConfig(final software.amazon.ssm.maintenancewindowtask.NotificationConfig resourceModelNotificationConfig) {
        if(resourceModelNotificationConfig == null) {
            return Optional.empty();
        } else {
            NotificationConfig requestNotificationConfig = NotificationConfig.builder()
                    .notificationArn(resourceModelNotificationConfig.getNotificationArn())
                    .notificationType(resourceModelNotificationConfig.getNotificationType())
                    .notificationEventsWithStrings(resourceModelNotificationConfig.getNotificationEvents())
                    .build();
            return Optional.of(requestNotificationConfig);
        }
    }

    /**
     * Translate Resource Model CloudWatchOutputConfig to Request CloudWatchOutputConfig
     */
    public static Optional<CloudWatchOutputConfig> translateToRequestCloudWatchOutputConfig(final software.amazon.ssm.maintenancewindowtask.CloudWatchOutputConfig resourceModelCloudWatchOutputConfig) {
        if(resourceModelCloudWatchOutputConfig == null) {
            return Optional.empty();
        } else {
            CloudWatchOutputConfig requestCloudWatchOutputConfig = CloudWatchOutputConfig.builder()
                    .cloudWatchLogGroupName(resourceModelCloudWatchOutputConfig.getCloudWatchLogGroupName())
                    .cloudWatchOutputEnabled(resourceModelCloudWatchOutputConfig.getCloudWatchOutputEnabled())
                    .build();
            return Optional.of(requestCloudWatchOutputConfig);
        }
    }

    /**
     * Translate Resource Model MaintenanceWindowAutomationParameters to Request MaintenanceWindowAutomationParameters
     */
    public static Optional<MaintenanceWindowAutomationParameters> translateToRequestAutomationParameters(final software.amazon.ssm.maintenancewindowtask.MaintenanceWindowAutomationParameters resourceModelAutomation) {
        if (resourceModelAutomation == null) {
            return Optional.empty();
        } else {
            //OBJECT_MAPPER.addMixIn(MaintenanceWindowAutomationParameters.class, FixMaintenanceWindowAutomationParameters.class);
            MaintenanceWindowAutomationParameters requestAutomationParameters = MaintenanceWindowAutomationParameters.builder()
                    .documentVersion(resourceModelAutomation.getDocumentVersion())
                    .parameters(resourceModelAutomation.getParameters())
                    .build();
            return Optional.of(requestAutomationParameters);
        }
    }

    /**
     * Translate Resource Model MaintenanceWindowLambdaParameters to Request MaintenanceWindowLambdaParameters
     */
    public static Optional<MaintenanceWindowLambdaParameters> translateToRequestLambdaParameters(final software.amazon.ssm.maintenancewindowtask.MaintenanceWindowLambdaParameters resourceModelLambda) {
        if (resourceModelLambda == null) {
            return Optional.empty();
        } else {

            MaintenanceWindowLambdaParameters requestLambdaParameters = MaintenanceWindowLambdaParameters.builder()
                    .clientContext(resourceModelLambda.getClientContext())
                    .payload(SdkBytes.fromUtf8String(resourceModelLambda.getPayload()))
                    .qualifier(resourceModelLambda.getQualifier())
                    .build();
            return Optional.of(requestLambdaParameters);
        }
    }

    /**
     * Translate Resource Model MaintenanceWindowRunCommandParameters to Request MaintenanceWindowRunCommandParameters
     */
    public static Optional<MaintenanceWindowRunCommandParameters> translateToRequestRunCommandParameters(final software.amazon.ssm.maintenancewindowtask.MaintenanceWindowRunCommandParameters resourceModelRunCommand) {
        if (resourceModelRunCommand == null) {
            return Optional.empty();
        } else {
            MaintenanceWindowRunCommandParameters requestRunCommandParameters = MaintenanceWindowRunCommandParameters.builder()
                    .comment(resourceModelRunCommand.getComment())
                    .documentHash(resourceModelRunCommand.getDocumentHash())
                    .documentHashType(resourceModelRunCommand.getDocumentHashType())
                    .notificationConfig(translateToRequestNotificationConfig(resourceModelRunCommand.getNotificationConfig()).get())
                    .outputS3BucketName(resourceModelRunCommand.getOutputS3BucketName())
                    .outputS3KeyPrefix(resourceModelRunCommand.getOutputS3KeyPrefix())
                    .serviceRoleArn(resourceModelRunCommand.getServiceRoleArn())
                    .timeoutSeconds(resourceModelRunCommand.getTimeoutSeconds())
                    .parameters(resourceModelRunCommand.getParameters())
                    .cloudWatchOutputConfig(translateToRequestCloudWatchOutputConfig(resourceModelRunCommand.getCloudWatchOutputConfig()).get())
                    .build();
            return Optional.of(requestRunCommandParameters);
        }
    }

    /**
     * Translate Resource Model MaintenanceWindowStepFunctionsParameters to Request MaintenanceWindowStepFunctionsParameters
     */
    public static Optional<MaintenanceWindowStepFunctionsParameters> translateToRequestStepFunctionsParameters(final software.amazon.ssm.maintenancewindowtask.MaintenanceWindowStepFunctionsParameters resourceModelStepFunctions) {
        if (resourceModelStepFunctions == null) {
            return Optional.empty();
        } else {
            MaintenanceWindowStepFunctionsParameters requestStepFunctionsParameters = MaintenanceWindowStepFunctionsParameters.builder()
                    .input(resourceModelStepFunctions.getInput())
                    .name(resourceModelStepFunctions.getName())
                    .build();
            return Optional.of(requestStepFunctionsParameters);
        }
    }

    /**
     * Translate Resource Model TaskInvocationParameters to Request TaskInvocationParameters
     */
    public static Optional<MaintenanceWindowTaskInvocationParameters> translateToRequestTaskInvocationParameters(final software.amazon.ssm.maintenancewindowtask.TaskInvocationParameters resourceModelTaskInvocationParameters) {
        if (resourceModelTaskInvocationParameters == null) {
            return Optional.empty();
        } else {
            MaintenanceWindowTaskInvocationParameters requestTaskInvocationParameters = MaintenanceWindowTaskInvocationParameters.builder()
                    .automation(translateToRequestAutomationParameters(resourceModelTaskInvocationParameters.getMaintenanceWindowAutomationParameters()).get())
                    .lambda(translateToRequestLambdaParameters(resourceModelTaskInvocationParameters.getMaintenanceWindowLambdaParameters()).get())
                    .runCommand(translateToRequestRunCommandParameters(resourceModelTaskInvocationParameters.getMaintenanceWindowRunCommandParameters()).get())
                    .stepFunctions(translateToRequestStepFunctionsParameters(resourceModelTaskInvocationParameters.getMaintenanceWindowStepFunctionsParameters()).get())
                    .build();
            return Optional.of(requestTaskInvocationParameters);
        }
    }

    /**
     * Translate Resource Model TaskParameters to Request TaskParameters
     */
    public static Optional<Map<String, MaintenanceWindowTaskParameterValueExpression>> translateToRequestTaskParameters(final Map<String, List<String>> resourceModelTaskParameters) {
        if(resourceModelTaskParameters == null || resourceModelTaskParameters.isEmpty()){
            return Optional.empty();
        } else {
            Map<String, MaintenanceWindowTaskParameterValueExpression> requestTaskParams = new HashMap<String,MaintenanceWindowTaskParameterValueExpression>();
            for (Map.Entry<String,List<String>> entry : resourceModelTaskParameters.entrySet())
                requestTaskParams.put(entry.getKey(),MaintenanceWindowTaskParameterValueExpression.builder().values(entry.getValue()).build());
            return Optional.of(requestTaskParams);
        }
    }
}