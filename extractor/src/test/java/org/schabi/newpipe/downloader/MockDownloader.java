package org.schabi.newpipe.downloader;

import com.google.gson.GsonBuilder;

import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Request;
import org.schabi.newpipe.extractor.downloader.Response;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

class MockDownloader extends Downloader {

    private final String path;
    private final Map<Request, Response> mocks;

    public MockDownloader(@Nonnull String path) throws IOException {
        this.path = path;
        this.mocks = new HashMap<>();
        File folder = new File(path);
        for (File file : folder.listFiles()) {
            final FileReader reader = new FileReader(file);
            final TestRequestResponse response = new GsonBuilder()
                    .create()
                    .fromJson(reader, TestRequestResponse.class);
            reader.close();
            mocks.put(response.getRequest(), response.getResponse());
        }
    }

    @Override
    public Response execute(@Nonnull Request request) {
        Response result = mocks.get(request);
        if (result == null) {
            throw new NullPointerException("No mock response for request with url '" + request.url()
                    + "' exists in path '" + path + "'.\nPlease make sure to run the tests with " +
                    "the RecordingDownloader first after changes.");
        }
        return result;
    }
}
