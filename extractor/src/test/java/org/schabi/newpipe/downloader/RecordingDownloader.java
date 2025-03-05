package org.schabi.newpipe.downloader;

import com.google.gson.GsonBuilder;

import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Request;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.Nonnull;

/**
 * <p>
 * Relays requests to {@link DownloaderTestImpl} and saves the request/response pair into a json
 * file.
 * </p>
 * <p>
 * Those files are used by {@link MockDownloader}.
 * </p>
 * <p>
 * The files <b>must</b> be created on the local dev environment
 * and recreated when the requests made by a test class change.
 * </p>
 * <p>
 * Run the test class as a whole and not each test separately.
 * Make sure the requests made by a class are unique.
 * </p>
 */
class RecordingDownloader extends Downloader {

    public static final String FILE_NAME_PREFIX = "generated_mock_";

    // From https://stackoverflow.com/a/15875500/13516981
    private static final String IP_V4_PATTERN =
            "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

    private int index = 0;
    private final Path path;

    /**
     * Creates the folder described by {@code stringPath} if it does not exist.
     * Deletes existing files starting with {@link RecordingDownloader#FILE_NAME_PREFIX}.
     * @param stringPath Path to the folder where the json files will be saved to.
     */
    public RecordingDownloader(final String stringPath) {
        this.path = Paths.get(stringPath);
        
        if (Files.exists(path)) {
            try (final var directoryStream = Files.newDirectoryStream(path,
                    entry -> entry.getFileName().toString()
                            .startsWith(RecordingDownloader.FILE_NAME_PREFIX))) {
                for (final Path entry : directoryStream) {
                    Files.delete(entry);
                }
            } catch (final IOException ioe) {
                throw new UncheckedIOException(ioe);
            }
        } else {
            try {
                Files.createDirectories(path);
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    @Override
    public Response execute(@Nonnull final Request request) throws IOException,
            ReCaptchaException {
        final Downloader downloader = DownloaderTestImpl.getInstance();
        Response response = downloader.execute(request);
        response = new Response(
            response.responseCode(),
            response.responseMessage(),
            response.responseHeaders(),
            response.responseBody().replaceAll(IP_V4_PATTERN, "127.0.0.1"),
            response.latestUrl()
        );

        final Path outputPath = path.resolve(FILE_NAME_PREFIX + index + ".json");
        index++;
        try (final var writer = Files.newBufferedWriter(outputPath)) {
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create()
                    .toJson(new TestRequestResponse(request, response), writer);
        }

        return response;
    }
}
