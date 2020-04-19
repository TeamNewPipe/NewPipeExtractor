package org.schabi.newpipe.extractor.exceptions;

public class ContentNotSupportedException extends ParsingException {
    public ContentNotSupportedException(String message) {
        super(message);
    }

    public ContentNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }
}
