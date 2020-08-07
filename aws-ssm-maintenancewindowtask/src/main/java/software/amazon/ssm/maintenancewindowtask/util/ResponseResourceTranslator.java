package software.amazon.ssm.maintenancewindowtask.util;

import com.amazonaws.util.Base64;
import com.amazonaws.util.CollectionUtils;
import software.amazon.ssm.maintenancewindowtask.CloudWatchOutputConfig;
import software.amazon.ssm.maintenancewindowtask.LoggingInfo;
import software.amazon.ssm.maintenancewindowtask.MaintenanceWindowAutomationParameters;
import software.amazon.ssm.maintenancewindowtask.MaintenanceWindowLambdaParameters;
import software.amazon.ssm.maintenancewindowtask.MaintenanceWindowRunCommandParameters;
import software.amazon.ssm.maintenancewindowtask.MaintenanceWindowStepFunctionsParameters;
import software.amazon.awssdk.services.ssm.model.MaintenanceWindowTaskParameterValueExpression;
import software.amazon.ssm.maintenancewindowtask.NotificationConfig;
import software.amazon.ssm.maintenancewindowtask.Target;
import software.amazon.ssm.maintenancewindowtask.TaskInvocationParameters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Translate Service Model properties to Resource Model properties
 */
public class ResponseResourceTranslator {

    /**
     * Translate Response Targets to ResourceModel Targets
     */
    public static Optional<List<Target>> translateToResourceModelTargets(final List<software.amazon.awssdk.services.ssm.model.Target> responseTargets) {

        if (!CollectionUtils.isNullOrEmpty(responseTargets)) {
            List<Target> resourceModelTargets = responseTargets.stream().map(entry ->
                    Target.builder()
                            .key(entry.key())
                            .values(entry.values())
                            .build()).collect(Collectors.toList());
            return Optional.of(resourceModelTargets);
        }
        return Optional.empty();
    }

    /**
     * Translate Service Model Response LoggingInfo to ResourceModel LoggingInfo
     */
    public static Optional<LoggingInfo> translateToResourceModelLoggingInfo(final software.amazon.awssdk.services.ssm.model.LoggingInfo responseLoggingInfo) {
        if (responseLoggingInfo == null) {
            return Optional.empty();
        } else {
            LoggingInfo resourceModelLoggingInfo = LoggingInfo.builder()
                    .s3Bucket(responseLoggingInfo.s3BucketName())
                    .s3Prefix(responseLoggingInfo.s3KeyPrefix())
                    .region(responseLoggingInfo.s3Region())
                    .build();
            return Optional.of(resourceModelLoggingInfo);
        }
    }

    /**
     * Translate Service Model Response NotificationConfig to ResourceModel NotificationConfig
     */
    private static Optional<NotificationConfig> translateToResourceModelNotificationConfig(final software.amazon.awssdk.services.ssm.model.NotificationConfig responseNotificationConfig) {
        if (responseNotificationConfig == null) {
            return Optional.empty();
        } else {
            NotificationConfig resourceModelNotificationConfig = NotificationConfig.builder()
                    .notificationArn(responseNotificationConfig.notificationArn())
                    .notificationType(responseNotificationConfig.notificationTypeAsString())
                    .notificationEvents(responseNotificationConfig.notificationEventsAsStrings())
                    .build();
            return Optional.of(resourceModelNotificationConfig);
        }
    }

    /**
     * Translate Service Model Response CloudWatchOutputConfig to ResourceModel CloudWatchOutputConfig
     */
    private static Optional<CloudWatchOutputConfig> translateToResourceModelCloudWatchOutputConfig(final software.amazon.awssdk.services.ssm.model.CloudWatchOutputConfig responseCloudWatchOutputConfig) {
        if (responseCloudWatchOutputConfig == null) {
            return Optional.empty();
        } else {
            CloudWatchOutputConfig resourceModelCloudWatchOutputConfig = CloudWatchOutputConfig.builder()
                    .cloudWatchLogGroupName(responseCloudWatchOutputConfig.cloudWatchLogGroupName())
                    .cloudWatchOutputEnabled(responseCloudWatchOutputConfig.cloudWatchOutputEnabled())
                    .build();
            return Optional.of(resourceModelCloudWatchOutputConfig);
        }
    }

    /**
     * Translate Service Model MaintenanceWindowAutomationParameters to ResourceModel MaintenanceWindowAutomationParameters
     */
    private static Optional<MaintenanceWindowAutomationParameters> translateToResourceModelAutomationParameters(final software.amazon.awssdk.services.ssm.model.MaintenanceWindowAutomationParameters responseAutomation) {
        if (responseAutomation == null) {
            return Optional.empty();
        } else {
            MaintenanceWindowAutomationParameters resourceModelAutomationParameters = MaintenanceWindowAutomationParameters.builder()
                    .documentVersion(responseAutomation.documentVersion())
                    .parameters(responseAutomation.parameters())
                    .build();
            return Optional.of(resourceModelAutomationParameters);
        }
    }

    /**
     * Translate Service Model MaintenanceWindowLambdaParameters to ResourceModel MaintenanceWindowLambdaParameters
     */
    private static Optional<MaintenanceWindowLambdaParameters> translateToResourceModelLambdaParameters(final software.amazon.awssdk.services.ssm.model.MaintenanceWindowLambdaParameters responseLambda) {
        if (responseLambda == null) {
            return Optional.empty();
        } else {

            MaintenanceWindowLambdaParameters resourceModelLambdaParameters = MaintenanceWindowLambdaParameters.builder()
                    .clientContext(responseLambda.clientContext())
                    .payload(new String(Base64.encode(responseLambda.payload().asByteArray())))
                    .qualifier(responseLambda.qualifier())
                    .build();
            return Optional.of(resourceModelLambdaParameters);
        }
    }

    /**
     * Translate Service Model MaintenanceWindowRunCommandParameters to ResourceModel MaintenanceWindowRunCommandParameters
     */
    private static Optional<MaintenanceWindowRunCommandParameters> translateToResourceModelRunCommandParameters(final software.amazon.awssdk.services.ssm.model.MaintenanceWindowRunCommandParameters responseRunCommand) {
        if (responseRunCommand == null) {
            return Optional.empty();
        } else {
            MaintenanceWindowRunCommandParameters.MaintenanceWindowRunCommandParametersBuilder resourceModelRunCommandParametersBuilder =
                    MaintenanceWindowRunCommandParameters.builder()
                            .comment(responseRunCommand.comment())
                            .documentHash(responseRunCommand.documentHash())
                            .documentHashType(responseRunCommand.documentHashTypeAsString())
                            .outputS3BucketName(responseRunCommand.outputS3BucketName())
                            .outputS3KeyPrefix(responseRunCommand.outputS3KeyPrefix())
                            .serviceRoleArn(responseRunCommand.serviceRoleArn())
                            .timeoutSeconds(responseRunCommand.timeoutSeconds())
                            .parameters(responseRunCommand.parameters());
            translateToResourceModelNotificationConfig(responseRunCommand.notificationConfig())
                    .ifPresent(resourceModelRunCommandParametersBuilder::notificationConfig);
            translateToResourceModelCloudWatchOutputConfig(responseRunCommand.cloudWatchOutputConfig())
                    .ifPresent(resourceModelRunCommandParametersBuilder::cloudWatchOutputConfig);
            return Optional.of(resourceModelRunCommandParametersBuilder.build());
        }
    }

    /**
     * Translate Service Model MaintenanceWindowStepFunctionsParameters to ResourceModel MaintenanceWindowStepFunctionsParameters
     */
    private static Optional<MaintenanceWindowStepFunctionsParameters> translateToResourceModelStepFunctionsParameters(final software.amazon.awssdk.services.ssm.model.MaintenanceWindowStepFunctionsParameters responseStepFunctions) {
        if (responseStepFunctions == null) {
            return Optional.empty();
        } else {
            MaintenanceWindowStepFunctionsParameters resourceModelStepFunctionsParameters = MaintenanceWindowStepFunctionsParameters.builder()
                    .input(responseStepFunctions.input())
                    .name(responseStepFunctions.name())
                    .build();
            return Optional.of(resourceModelStepFunctionsParameters);
        }
    }

    /**
     * Translate Service Model TaskInvocationParameters to ResourceModel TaskInvocationParameters
     */
    public static Optional<TaskInvocationParameters> translateToResourceModelTaskInvocationParameters(final software.amazon.awssdk.services.ssm.model.MaintenanceWindowTaskInvocationParameters responseTaskInvocationParameters) {
        if (responseTaskInvocationParameters == null) {
            return Optional.empty();
        } else {
            TaskInvocationParameters.TaskInvocationParametersBuilder resourceModelTaskInvocationParametersBuilder = TaskInvocationParameters.builder();
            translateToResourceModelAutomationParameters(responseTaskInvocationParameters.automation())
                    .ifPresent(resourceModelTaskInvocationParametersBuilder::maintenanceWindowAutomationParameters);
            translateToResourceModelLambdaParameters(responseTaskInvocationParameters.lambda())
                    .ifPresent(resourceModelTaskInvocationParametersBuilder::maintenanceWindowLambdaParameters);
            translateToResourceModelRunCommandParameters(responseTaskInvocationParameters.runCommand())
                    .ifPresent(resourceModelTaskInvocationParametersBuilder::maintenanceWindowRunCommandParameters);
            translateToResourceModelStepFunctionsParameters(responseTaskInvocationParameters.stepFunctions())
                    .ifPresent(resourceModelTaskInvocationParametersBuilder::maintenanceWindowStepFunctionsParameters);
            return Optional.of(resourceModelTaskInvocationParametersBuilder.build());
        }
    }

    /**
     * Translate Service Model TaskParameters to ResourceModel TaskParameters
     */
    public static Optional<Map<String, List<String>>> translateToResourceModelTaskParameters(final Map<String, MaintenanceWindowTaskParameterValueExpression> responseTaskParameters) {
        if (responseTaskParameters == null || responseTaskParameters.isEmpty()) {
            return Optional.empty();
        } else {
            Map<String, List<String>> resourceModelTaskParams = new HashMap<String, List<String>>();
            for (Map.Entry<String, MaintenanceWindowTaskParameterValueExpression> entry : responseTaskParameters.entrySet())
                resourceModelTaskParams.put(entry.getKey(), entry.getValue().values());
            return Optional.of(resourceModelTaskParams);
        }
    }

}
