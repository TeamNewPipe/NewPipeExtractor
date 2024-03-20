package org.schabi.newpipe.downloader;

import com.google.gson.GsonBuilder;

import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Request;
import org.schabi.newpipe.extractor.downloader.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        try (final var directoryStream = Files.newDirectoryStream(Paths.get(path),
                entry -> entry.getFileName().toString()
                        .startsWith(RecordingDownloader.FILE_NAME_PREFIX))) {
            for (final var entry : directoryStream) {
                try (final var reader = Files.newBufferedReader(entry)) {
                    final var response = new GsonBuilder()
                            .create()
                            .fromJson(reader, TestRequestResponse.class);
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
