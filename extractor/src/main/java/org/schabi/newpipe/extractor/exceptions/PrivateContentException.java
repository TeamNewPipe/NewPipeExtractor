package org.schabi.newpipe.extractor.exceptions;

public class PrivateContentException extends ContentNotAvailableException {
    public PrivateContentException(String message) {
        super(message);
    }

    public PrivateContentException(String message, Throwable cause) {
        super(message, cause);
    }
}
