package org.schabi.newpipe.extractor.exceptions;

/**
 * Exception for contents not supported in a country.
 *
 * <p>
 * Unsupported content means content is not intentionally geographically restricted such as for
 * distribution rights, for which {@link GeographicRestrictionException} should be used instead.
 * </p>
 */
public class UnsupportedContentInCountryException extends ContentNotAvailableException {

    public UnsupportedContentInCountryException(final String message) {
        super(message);
    }

    public UnsupportedContentInCountryException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
