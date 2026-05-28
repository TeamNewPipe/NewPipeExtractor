package org.schabi.newpipe.extractor.stream.deliverysource.uri;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Class describing a URI.
 *
 * <p>
 * URIs are stored as strings and can be eventually refreshed with a {@link Refresher}.
 * </p>
 */
public final class UriObject {

    /**
     * Constant representing the expiration of this URI is not known.
     */
    public static final long EXPIRATION_TIMESTAMP_UNKNOWN = -1;

    /**
     * Constant representing there is no planned expiration of this URI.
     */
    public static final long EXPIRATION_TIMESTAMP_NO_EXPIRY = -2;

    @Nonnull
    private final String uri;
    private final long expirationTimestamp;
    @Nullable
    private final Refresher refresher;

    /**
     * Construct a new {@link UriObject instance}.
     *
     * @param uri                 a non-null URI, as a string
     * @param refresher           a {@link Refresher} to eventually refresh the given URI, which
     *                            may be null
     * @param expirationTimestamp the expiration Unix timestamp of this URI in UTC timezone and
     *                            seconds
     * @throws NullPointerException     if the URI is null
     * @throws IllegalArgumentException if the URI is empty or blank
     * @see #EXPIRATION_TIMESTAMP_UNKNOWN
     * @see #EXPIRATION_TIMESTAMP_NO_EXPIRY
     */
    public UriObject(@Nonnull final String uri,
                     final long expirationTimestamp,
                     @Nullable final Refresher refresher) {
        if (uri.isBlank()) {
            throw new IllegalArgumentException("Empty or blank URI");
        }

        this.uri = uri;
        this.refresher = refresher;
        this.expirationTimestamp = expirationTimestamp;
    }

    /**
     * Get the URI associated with this {@link UriObject}.
     *
     * @return the URI
     */
    @Nonnull
    public String getUri() {
        return uri;
    }

    /**
     * Refresh the URI, if possible, with the given {@link Refresher}.
     *
     * @return a {@link UriObject} containing the new URI
     * @throws UnsupportedOperationException if there is no refresher set
     * @throws RefreshException              if something went when refreshing the URI while it
     *                                       shouldn't
     * @throws IllegalArgumentException      if the refresher returns an empty or a blank URI
     */
    @Nonnull
    public UriObject refreshUri() throws RefreshException {
        if (refresher == null) {
            throw new UnsupportedOperationException("Cannot refresh a URI without a refresher");
        }

        final UriObject newUriObject = refresher.refresh(uri);
        if (newUriObject.uri.isBlank()) {
            throw new IllegalArgumentException("Refresher returned a blank URI");
        }

        return newUriObject;
    }

    /**
     * Get the expiration Unix timestamp of this URI in UTC timezone and in seconds if it is known
     * or {@link #EXPIRATION_TIMESTAMP_UNKNOWN} otherwise.
     *
     * @return the expiration Unix timestamp of this stream in UTC timezone and seconds as a long
     * or {@link #EXPIRATION_TIMESTAMP_UNKNOWN}
     * @see #EXPIRATION_TIMESTAMP_UNKNOWN
     * @see #EXPIRATION_TIMESTAMP_NO_EXPIRY
     */
    public long getExpirationTimestamp() {
        return expirationTimestamp;
    }

    /**
     * Interface describing the ability to refresh a stream URI.
     *
     * <p>
     * Refreshing a stream URI should be focused to refresh it only and not the entire stream
     * info. For such cases, no refresher may be provided and clients should refresh stream info
     * instead.
     * </p>
     */
    public interface Refresher {

        /**
         * Refreshes a URI to a new one, using optionally this URI.
         *
         * <p>
         * If the refresher cannot refresh this URI as something went wrong when
         * getting it (stream quality now unavailable, content removed, invalid server response,
         * ...), a {@link RefreshException} must be thrown.
         * </p>
         *
         * @param uri the current URI to be refreshed
         * @return a new {@link UriObject} with the new URI and eventually a new {@link Refresher}
         * @throws RefreshException if something went when refreshing the URI while it shouldn't
         */
        UriObject refresh(@Nonnull String uri) throws RefreshException;
    }

    /**
     * Exception thrown when a refresh failed while it shouldn't.
     */
    public static final class RefreshException extends Exception {

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

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof UriObject that)) {
            return false;
        }

        return expirationTimestamp == that.expirationTimestamp
                && uri.equals(that.uri)
                && Objects.equals(refresher, that.refresher);
    }

    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + Long.hashCode(expirationTimestamp);
        result = 31 * result + Objects.hashCode(refresher);
        return result;
    }

    @Override
    public String toString() {
        return "UriObject{"
                + "uri=" + uri
                + ", expirationTimestamp=" + expirationTimestamp
                + ", refresher=" + refresher
                + "}";
    }
}
