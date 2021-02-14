package org.schabi.newpipe.extractor.exceptions;

public class PaidContentException extends ContentNotAvailableException {
    public PaidContentException(String message) {
        super(message);
    }

    public PaidContentException(String message, Throwable cause) {
        super(message, cause);
    }
}
