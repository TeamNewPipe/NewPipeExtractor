package org.schabi.newpipe.extractor.stream.deliverysource.uri;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A {@link UriDeliverySource} source for streams using HTTP and HTTPS requests.
 */
public class HttpDeliverySource implements UriDeliverySource, Serializable {

    @Nonnull
    private final UriObject url;
    @Nonnull
    private final List<UriObject> alternateUrls;
    @Nonnull
    private final Map<String, List<String>> httpHeaders;
    @Nonnull
    private final HttpMethod httpMethod;
    @Nullable
    private final byte[] requestBody;

    /**
     * Construct a new {@link HttpDeliverySource} instance.
     *
     * @param url           the URL to the content
     * @param alternateUrls alternate URLs to the content
     * @param httpHeaders   the HTTP headers to use with this content, as an unmodifiable map for
     *                      which keys are names of HTTP headers and unmodifiable lists as values
     *                      of HTTP headers
     * @param httpMethod    the HTTP method to use with this content
     * @param requestBody   an optional request body to use as a byte array, which should be only
     *                      provided for relevant HTTP methods
     */
    public HttpDeliverySource(@Nonnull final UriObject url,
                              @Nonnull final List<UriObject> alternateUrls,
                              @Nonnull final Map<String, List<String>> httpHeaders,
                              @Nonnull final HttpMethod httpMethod,
                              @Nullable final byte[] requestBody) {
        this.url = Objects.requireNonNull(url);
        this.alternateUrls = Objects.requireNonNull(alternateUrls);
        this.httpHeaders = Objects.requireNonNull(httpHeaders);
        this.httpMethod = Objects.requireNonNull(httpMethod);
        this.requestBody = requestBody;
    }

    @Nonnull
    @Override
    public UriObject getUri() {
        return url;
    }

    @Nonnull
    @Override
    public List<UriObject> getAlternateUris() {
        return alternateUrls;
    }

    /**
     * Get the HTTP headers to use with this content, as an unmodifiable map for which keys are
     * names of HTTP headers and unmodifiable lists as values of HTTP headers.
     *
     * @return the HTTP headers
     */
    @Nonnull
    public Map<String, List<String>> getHttpHeaders() {
        return httpHeaders;
    }

    /**
     * Get the {@link HttpMethod} to use with this content.
     *
     * @return the HTTP method
     */
    @Nonnull
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    /**
     * Get the HTTP request body to use as a byte array, if there is a unique one, i.e. it is
     * guaranteed to not change between requests.
     *
     * @return the HTTP request body or {@code null} if there isn't a unique one
     */
    @Nullable
    public byte[] getRequestBody() {
        return requestBody;
    }

    /**
     * Enum describing supported HTTP methods for HTTP requests of {@link HttpDeliverySource}.
     */
    public enum HttpMethod {
        GET,
        POST
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof HttpDeliverySource that)) {
            return false;
        }

        return url.equals(that.url) && alternateUrls.equals(that.alternateUrls)
                && httpHeaders.equals(that.httpHeaders)
                && httpMethod == that.httpMethod
                && Arrays.equals(requestBody, that.requestBody);
    }

    @Override
    public int hashCode() {
        int result = url.hashCode();
        result = 31 * result + alternateUrls.hashCode();
        result = 31 * result + httpHeaders.hashCode();
        result = 31 * result + httpMethod.hashCode();
        result = 31 * result + Arrays.hashCode(requestBody);
        return result;
    }

    @Override
    public String toString() {
        return "HttpDeliverySource{"
                + "url=" + url
                + ", alternateUrls=" + alternateUrls
                + ", httpHeaders=" + httpHeaders
                + ", httpMethod=" + httpMethod
                + ", requestBody=" + Arrays.toString(requestBody)
                + "}";
    }
}
