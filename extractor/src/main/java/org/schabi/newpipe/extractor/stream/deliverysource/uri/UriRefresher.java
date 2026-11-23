package org.schabi.newpipe.extractor.stream.deliverysource.uri;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface describing the ability to refresh a stream URI.
 *
 * <p>
 * Refreshing a stream URI should be focused to refresh it only and not the entire stream
 * info. For such cases, no refresher may be provided and clients should refresh stream info
 * instead.
 * </p>
 */
public interface UriRefresher {

    /**
     * Refreshes a URI to a new one, using optionally this URI.
     *
     * <p>
     * If the refresher cannot refresh this URI as something went wrong when
     * getting it (stream quality now unavailable, content removed, invalid server response, ...),
     * a {@link RefreshException} must be thrown.
     * </p>
     *
     * @param uri the current URI to be refreshed
     * @return a new {@link UriObject} with the new URI and eventually a new {@link UriRefresher}
     * @throws RefreshException if something went when refreshing the URI while it shouldn't
     */
    UriObject refresh(@Nonnull String uri) throws RefreshException;

    /**
     * Exception thrown when a refresh failed while it shouldn't.
     */
    final class RefreshException extends Exception {

        /**
         * Construct a new {@link RefreshException} instance.
         *
         * @param message the message of the exception, which may not be null
         * @param cause   the original cause, which can be null
         */
        public RefreshException(@Nonnull final String message, @Nullable final Throwable cause) {
            super(message, cause);
        }
    }
}
