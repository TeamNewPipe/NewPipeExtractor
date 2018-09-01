package org.schabi.newpipe.extractor;

import java.util.List;
import java.util.Map;

public class DownloadResponse {
    private final String responseBody;
    private final Map<String, List<String>> responseHeaders;

    public DownloadResponse(String responseBody, Map<String, List<String>> headers) {
        super();
        this.responseBody = responseBody;
        this.responseHeaders = headers;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }

}
