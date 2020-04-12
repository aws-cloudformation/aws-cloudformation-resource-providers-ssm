package software.amazon.ssm.patchbaseline;

import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.awssdk.services.ssm.model.ResourceLimitExceededException;
import software.amazon.awssdk.services.ssm.model.ResourceInUseException;
import software.amazon.awssdk.services.ssm.model.InvalidResourceIdException;
import software.amazon.awssdk.services.ssm.model.AlreadyExistsException;
import software.amazon.ssm.patchbaseline.utils.SsmCfnClientSideException;
import com.amazonaws.AmazonServiceException;

import java.util.Arrays;
import java.util.List;

public class Resource {
    static final int STATUS_CODE_400 = 400;
    static final int STATUS_CODE_500 = 500;
    static final List<String> RETRIABLE_400_ERROR_CODES = Arrays.asList(
            "HttpTimeoutException",
            "HttpConnectionTimeoutException");

    private static final int MAX_EXCEPTION_CHAIN_LENGTH = 5;

    /**
     * Handle an exception.
     * @param ex                    Exception object
     * @param model                 ResourceModel
     * @param baselineID            Baseline ID
     * @param logger                log
     * @return                      Response to CloudFormation
     */
    public static ProgressEvent<ResourceModel, CallbackContext> handleException (Exception ex,
                                                                                 ResourceModel model,
                                                                                 String baselineID,
                                                                                 Logger logger) {

        if (ex instanceof IllegalArgumentException) {
            logger.log(String.format("WARN Handlers were unable to parse CloudFormation request properly. "
                    + "Exception details: %s %n", ex.getMessage()));
        } else if (ex instanceof ResourceInUseException) {
            logger.log(String.format("WARN Patch baseline resource %s was in use while deleting. "
                    + "Exception details: %s %n", baselineID, ex.getMessage()));
        } else if (ex instanceof InvalidResourceIdException) {
            logger.log(String.format("WARN CloudFormation provided invalid patch baseline ID %s. "
                    + "Exception details: %s %n", baselineID, ex.getMessage()));
        } else if (ex instanceof ResourceLimitExceededException) {
            logger.log(String.format("WARN User tried to create patch baseline but exceeded their resource limits. "
                    + "Exception details: %s %n", ex.getMessage()));
        } else if (ex instanceof AlreadyExistsException) {
            logger.log(String.format("WARN User tried to register baseline %s to patch group that already has a baseline. "
                    + "Exception details: %s %n", baselineID, ex.getMessage()));
        } else if (ex instanceof SsmCfnClientSideException) {
            logger.log(String.format("WARN Client-side error in CloudFormation request. "
                    + "Exception details: %s %n", ex.getMessage()));
        } else if (ex instanceof AmazonServiceException) {
            AmazonServiceException ase = (AmazonServiceException)ex;
            logExceptionChain(ase, logger);
            int errorStatus = ase.getStatusCode();
            if (errorStatus >= STATUS_CODE_400 && errorStatus < STATUS_CODE_500) {
                // 400s default to FAILURE
                logger.log(String.format("WARN SSM returned a 4xx error code! Exception details: %s %n", ex.getMessage()));

                if (RETRIABLE_400_ERROR_CODES.contains(ase.getErrorCode())) {
                    // a lot of HTTP timeout exceptions will appear as an
                    // AmazonServiceException with Status Code 400, but we want to retry these, as they aren't actually
                    // client-side errors.
                    logger.log(String.format("INFO Detected HttpTimeoutException. Please RETRY"));
                }
            } else {
                // >= 500s default to RETRY
                logger.log(String.format("ERROR SSM returned a 5xx error code! Please RETRY! Exception details: %s %n", ex.getMessage()));
            }
        } else {
            // response.setMessage(String.format("Internal Failure: %s", ex.getMessage()));
            logger.log(String.format("ERROR Encountered an unknown exception while processing patch baseline request. Exception details: %s %n", ex.getMessage()));
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.FAILED)
                .message(ex.getMessage())
                .build();
    }

    /**
     * Log the exception chain of an exception. This was introduced to help diagnose the issue
     * where DNS timeouts are wrapped in an AmazonServiceException, so we don't have enough information to handle these in a type-safe
     * way.
     *
     * @param thrown   Exception object
     * @param logger  log
     */
    private static void logExceptionChain(Throwable thrown, Logger logger) {
        StringBuilder exceptionChain = new StringBuilder(String.format("%s : %s",
                thrown.getClass().getCanonicalName(), thrown.getMessage()));

        // Limit the traversal steps to limit log length and prevent cycles.
        Throwable current = thrown;
        int numLogs = 1;
        while (null != current.getCause() && numLogs < MAX_EXCEPTION_CHAIN_LENGTH) {
            current = current.getCause();
            exceptionChain.append(String.format(" -> %s : %s", current.getClass().getCanonicalName(), current.getMessage()));
            numLogs++;
        }

        logger.log(String.format("WARN Exception Chain: %s %n", exceptionChain));
    }

}
