package org.schabi.newpipe.extractor.exceptions;

public class GeographicRestrictionException extends ContentNotAvailableException {
    public GeographicRestrictionException(final String message) {
        super(message);
    }

    public GeographicRestrictionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
