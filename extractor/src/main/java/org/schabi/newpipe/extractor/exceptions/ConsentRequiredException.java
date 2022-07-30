package org.schabi.newpipe.extractor.exceptions;

public class ConsentRequiredException extends ParsingException {

    public ConsentRequiredException(final String message) {
        super(message);
    }

    public ConsentRequiredException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
