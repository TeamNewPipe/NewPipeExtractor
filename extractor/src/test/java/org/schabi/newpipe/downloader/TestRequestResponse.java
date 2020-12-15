package org.schabi.newpipe.downloader;

import org.schabi.newpipe.extractor.downloader.Request;
import org.schabi.newpipe.extractor.downloader.Response;

final class TestRequestResponse {
    private final String comment;
    private final Request request;
    private final Response response;

    public TestRequestResponse(Request request, Response response) {
        this.comment = "Auto-generated for tests. See RecordingDownloader";
        this.request = request;
        this.response = response;
    }

    public String getComment() {
        return comment;
    }

    public Request getRequest() {
        return request;
    }

    public Response getResponse() {
        return response;
    }
}
