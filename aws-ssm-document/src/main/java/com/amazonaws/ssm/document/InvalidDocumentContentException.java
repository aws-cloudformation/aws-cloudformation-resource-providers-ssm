package com.amazonaws.ssm.document;

/**
 * Exception thrown when Document Content is invalid and cannot be processed.
 */
//compiler complain that this class needs to have serialVersionUID but this class is never going to be serialized.
@SuppressWarnings("serial")
class InvalidDocumentContentException extends RuntimeException {
    InvalidDocumentContentException(final String message, final Throwable cause) {
        super(message, cause);
    }

    InvalidDocumentContentException(final String message) {
        super(message);
    }
}
