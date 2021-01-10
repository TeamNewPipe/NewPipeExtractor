package org.schabi.newpipe.extractor.exceptions;

public class GeographicRestrictionException extends ContentNotAvailableException {
    public GeographicRestrictionException(String message) {
        super(message);
    }

    public GeographicRestrictionException(String message, Throwable cause) {
        super(message, cause);
    }
}
