package org.schabi.newpipe.extractor.exceptions;

import java.io.IOException;
import org.schabi.newpipe.extractor.downloader.Response;

public class HttpResponseException extends IOException {
    public HttpResponseException(final Response response) {
        this("Error in HTTP Response for " + response.latestUrl() + "\n\t"
            + response.responseCode() + " - " + response.responseMessage());
    }

    public HttpResponseException(final String message) {
        super(message);
    }
}
