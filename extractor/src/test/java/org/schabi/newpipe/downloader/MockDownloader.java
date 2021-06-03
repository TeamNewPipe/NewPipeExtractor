package org.schabi.newpipe.downloader;

import com.google.gson.GsonBuilder;

import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Request;
import org.schabi.newpipe.extractor.downloader.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * <p>
 * Mocks requests by using json files created by {@link RecordingDownloader}
 * </p>
 */
class MockDownloader extends Downloader {

    private final String path;
    private final Map<Request, Response> mocks;

    public MockDownloader(@Nonnull final String path) throws IOException {
        this.path = path;
        this.mocks = new HashMap<>();
        final File[] files = new File(path).listFiles();
        if (files != null) {
            for (final File file : files) {
                if (file.getName().startsWith(RecordingDownloader.FILE_NAME_PREFIX)) {
                    final InputStreamReader reader = new InputStreamReader(new FileInputStream(
                            file), StandardCharsets.UTF_8);
                    final TestRequestResponse response = new GsonBuilder()
                            .create()
                            .fromJson(reader, TestRequestResponse.class);
                    reader.close();
                    mocks.put(response.getRequest(), response.getResponse());
                }
            }
        }
    }

    @Override
    public Response execute(@Nonnull final Request request) {
        final Response result = mocks.get(request);
        if (result == null) {
            throw new NullPointerException("No mock response for request with url '" + request
                    .url() + "' exists in path '" + path + "'.\nPlease make sure to run the tests "
                    + "with the RecordingDownloader first after changes.");
        }
        return result;
    }
}
