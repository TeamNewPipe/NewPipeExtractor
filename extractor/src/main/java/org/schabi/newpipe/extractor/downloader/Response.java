package org.schabi.newpipe.extractor.downloader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A Data class used to hold the results from requests made by the Downloader implementation.
 */
public class Response {
    private final int responseCode;
    private final String responseMessage;
    private final Map<String, List<String>> responseHeaders;
    private final String responseBody;

    public Response(int responseCode, String responseMessage, Map<String, List<String>> responseHeaders, @Nullable String responseBody) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.responseHeaders = responseHeaders != null ? responseHeaders : Collections.<String, List<String>>emptyMap();

        this.responseBody = responseBody == null ? "" : responseBody;
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

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/

    /**
     * For easy access to some header value that (usually) don't repeat itself.
     * <p>For getting all the values associated to the header, use {@link #responseHeaders()} (e.g. {@code Set-Cookie}).
     *
     * @param name the name of the header
     * @return the first value assigned to this header
     */
    @Nullable
    public String getHeader(String name) {
        for (Map.Entry<String, List<String>> headerEntry : responseHeaders.entrySet()) {
            if (headerEntry.getKey().equalsIgnoreCase(name)) {
                if (headerEntry.getValue().size() > 0) {
                    return headerEntry.getValue().get(0);
                }
            }
        }

        return null;
    }
}
