package org.schabi.newpipe.extractor.exceptions;

public class InvalidInstanceException extends ExtractionException {
    public InvalidInstanceException(String message) {
        super(message);
    }

    public InvalidInstanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
