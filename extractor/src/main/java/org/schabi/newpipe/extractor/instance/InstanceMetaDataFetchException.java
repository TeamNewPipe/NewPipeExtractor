package org.schabi.newpipe.extractor.instance;

/**
 * Thrown when an {@link Instance} couldn't be validated.
 */
public class InstanceMetaDataFetchException extends RuntimeException {
    public InstanceMetaDataFetchException(final String message) {
        super(message);
    }

    public InstanceMetaDataFetchException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public InstanceMetaDataFetchException(final Throwable cause) {
        super(cause);
    }
}
