package software.amazon.ssm.patchbaseline.utils;

public class SsmCfnClientSideException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public SsmCfnClientSideException(final String message) {
        super(message);
    }

    public SsmCfnClientSideException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
