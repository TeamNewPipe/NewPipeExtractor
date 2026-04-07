package org.schabi.newpipe.extractor.downloader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.schabi.newpipe.extractor.exceptions.HttpResponseException;

/**
 * A Data class used to hold the results from requests made by the Downloader implementation.
 */
@SuppressWarnings("checkstyle:NeedBraces")
public class Response {
    private final int responseCode;
    private final String responseMessage;
    private final Map<String, List<String>> responseHeaders;
    private final String responseBody;

    private final String latestUrl;

    public Response(final int responseCode,
                    final String responseMessage,
                    @Nullable final Map<String, List<String>> responseHeaders,
                    @Nullable final String responseBody,
                    @Nullable final String latestUrl) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.responseHeaders = responseHeaders == null ? Collections.emptyMap() : responseHeaders;

        this.responseBody = responseBody == null ? "" : responseBody;
        this.latestUrl = latestUrl;
    }

    public int responseCode() {
        return responseCode;
    }

    public String responseMessage() {
        return responseMessage;
    }

    public Map<String, List<String>> responseHeaders() {
        return responseHeaders;
    }

    @Nonnull
    public String responseBody() {
        return responseBody;
    }

    /**
     * Used for detecting a possible redirection, limited to the latest one.
     *
     * @return latest url known right before this response object was created
     */
    @Nonnull
    public String latestUrl() {
        return latestUrl;
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/

    /**
     * For easy access to some header value that (usually) don't repeat itself.
     * <p>For getting all the values associated to the header, use {@link #responseHeaders()} (e.g.
     * {@code Set-Cookie}).
     *
     * @param name the name of the header
     * @return the first value assigned to this header
     */
    @Nullable
    public String getHeader(final String name) {
        for (final Map.Entry<String, List<String>> headerEntry : responseHeaders.entrySet()) {
            final String key = headerEntry.getKey();
            if (key != null && key.equalsIgnoreCase(name) && !headerEntry.getValue().isEmpty()) {
                return headerEntry.getValue().get(0);
            }
        }

        return null;
    }

    /**
     * Ensure the response code is 2xx
     * @return this {@code Response}
     * @throws HttpResponseException if the response code is not 2xx
     */
    public Response ensureSuccessResponseCode() throws HttpResponseException {
        return ensureResponseCodeInRange(200, 299);
    }

    /**
     * Ensure the response code is 3xx
     * @return this {@code Response}
     * @throws HttpResponseException if the response code is not 3xx
     */
    public Response ensureRedirectResponseCode() throws HttpResponseException {
        return ensureResponseCodeInRange(300, 399);
    }

    /**
     * Ensure the response code is not 4xx or 5xx
     * @return this {@code Response}
     * @throws HttpResponseException if the response code is client or server error
     */
    public Response ensureResponseCodeIsNotError() throws HttpResponseException {
        return ensureResponseCodeNotInRange(400, 599);
    }

    /**
     * Ensure the HTTP response code is within range of min and max inclusive
     * @return this Response
     * @throws HttpResponseException if the code is outside the range
     */
    public Response ensureResponseCodeInRange(final int min, final int max)
        throws HttpResponseException {
        if (responseCode() < min || responseCode() > max) {
            throw new HttpResponseException(this);
        }
        return this;
    }

    public Response ensureResponseCodeNotInRange(
        final int min,
        final int max,
        final Function<Response, HttpResponseException> errorSupplier
    )
        throws HttpResponseException {
        if (min > max) throw new RuntimeException("min must be less than max");
        if (responseCode() >= min && responseCode() <= max) {
            throw errorSupplier.apply(this);
        }
        return this;
    }

    public Response ensureResponseCodeNotInRange(final int min, final int max)
        throws HttpResponseException {
        return ensureResponseCodeNotInRange(min, max, HttpResponseException::new);
    }

    /**
     * Throw exception if response code is a 4xx client error
     * @return this {@code Response}
     * @throws HttpResponseException if the response code is 4xx
     */
    public Response throwIfClientError(
        final Function<Response, HttpResponseException> errorSupplier
    )
        throws HttpResponseException {
        return throwIfResponseCodeInRange(400, 499, errorSupplier);
    }

    /**
     * Throw exception if response code is a 4xx client error
     * @return this {@code Response}
     * @throws HttpResponseException if the response code is 4xx
     */
    public Response throwIfClientError()
        throws HttpResponseException {
        return throwIfClientError(HttpResponseException::new);
    }

    /**
     * Throw exception if response code is a 5xx server error
     * @return this {@code Response}
     * @throws HttpResponseException if the response code is 4xx
     */
    public Response throwIfServerError(
        final Function<Response, HttpResponseException> errorSupplier
    )
        throws HttpResponseException {
        return throwIfResponseCodeInRange(500, 599, errorSupplier);
    }

    public Response throwIfServerError()
        throws HttpResponseException {
        return throwIfServerError(HttpResponseException::new);
    }

    public Response throwIfResponseCode(
        final int errorCode,
        final Function<Response, HttpResponseException> errorSupplier
    )
        throws HttpResponseException {
        if (responseCode() == errorCode) {
            throw errorSupplier.apply(this);
        }
        return this;
    }

    public Response throwIfResponseCode(final int errorCode) throws HttpResponseException {
        return throwIfResponseCode(errorCode, HttpResponseException::new);
    }

    public Response throwIfResponseCodeInRange(final int min,
                                               final int max,
                                               final Function<Response,
                                                              HttpResponseException> errorSupplier)
        throws HttpResponseException {
        return ensureResponseCodeNotInRange(min, max, errorSupplier);
    }

    public Response throwIfResponseCodeInRange(final int min,
                                               final int max) throws HttpResponseException {
        return throwIfResponseCodeInRange(min, max, HttpResponseException::new);
    }
}
