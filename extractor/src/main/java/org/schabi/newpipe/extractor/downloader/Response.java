package org.schabi.newpipe.extractor.downloader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.schabi.newpipe.extractor.exceptions.HttpResponseException;

/**
 * A Data class used to hold the results from requests made by the Downloader implementation.
 */
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

    /**
     * Validates the response codes for the given {@link Response}, and throws
     * a {@link HttpResponseException} if the code is invalid
     * @param response The response to validate
     * @param validResponseCodes Expected valid response codes
     * @throws HttpResponseException Thrown when the response code is not in {@code validResponseCodes},
     * or when {@code  validResponseCodes} is empty and the code is a 4xx or 5xx error.
     */
    // CHECKSTYLE:ON
    public static void validateResponseCode(final Response response,
                                            final int... validResponseCodes)
        throws HttpResponseException {
        final int code = response.responseCode();
        final var throwError = (validResponseCodes == null || validResponseCodes.length == 0)
            ? code >= 400 && code <= 599
            : Arrays.stream(validResponseCodes).noneMatch(c -> c == code);

        if (throwError) {
            throw new HttpResponseException(response);
        }
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

    // CHECKSTYLE:OFF
    /**
     * Helper function simply to make it easier to validate response code inline
     * before getting the code/body/latestUrl/etc.
     * Validates the response codes for the given {@link Response}, and throws a {@link HttpResponseException} if the code is invalid
     * @see Response#validateResponseCode(Response, int...)
     * @param validResponseCodes Expected valid response codes
     * @return {@link this} response
     * @throws HttpResponseException Thrown when the response code is not in {@code validResponseCodes},
     * or when {@code  validResponseCodes} is empty and the code is a 4xx or 5xx error.
     */
    // CHECKSTYLE:ON
    public Response validateResponseCode(final int... validResponseCodes)
        throws HttpResponseException {
        validateResponseCode(this, validResponseCodes);
        return this;
    }
}
