package org.schabi.newpipe.extractor.utils;

import java.util.Arrays;

import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.HttpResponseException;

public final class HttpUtils {

    private HttpUtils() {
        // Utility class, no instances allowed
    }

    // CHECKSTYLE:OFF
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
}
