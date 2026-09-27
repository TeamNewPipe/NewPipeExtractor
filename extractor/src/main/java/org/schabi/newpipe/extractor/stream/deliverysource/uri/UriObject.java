package org.schabi.newpipe.extractor.stream.deliverysource.uri;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Class describing a URI.
 *
 * <p>
 * URIs are stored as strings and can be eventually refreshed with a {@link UriRefresher}.
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
    private final UriRefresher refresher;

    /**
     * Construct a new {@link UriObject instance}.
     *
     * @param uri                 a non-null URI, as a string
     * @param refresher           a {@link UriRefresher} to eventually refresh the given URI, which
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
                     @Nullable final UriRefresher refresher) {
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
     * Get the {@link UriRefresher} of this stream to eventually refresh this URI.
     *
     * @return the {@link UriRefresher} of this stream or {@code null}
     */
    @Nullable
    public UriRefresher getRefresher() {
        return refresher;
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
