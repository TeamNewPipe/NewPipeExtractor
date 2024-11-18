package org.schabi.newpipe.downloader;

import com.google.gson.GsonBuilder;

import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Request;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

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
    private final String path;

    // try to prevent ReCaptchaExceptions / rate limits by tracking and throttling the requests
    /**
     * The maximum number of requests per 20 seconds which are executed
     * by the {@link RecordingDownloader}.
     * 20 seconds is used as upper bound because the rate limit can be triggered within 30 seconds
     * and hitting the rate limit should be prevented because it comes with a bigger delay.
     * The values can be adjusted when executing the downloader and running into problems.
     * <p>TODO: Allow adjusting the value by setting a param in the gradle command</p>
     */
    private static final int MAX_REQUESTS_PER_20_SECONDS = 30;
    private static final long[] requestTimes = new long[MAX_REQUESTS_PER_20_SECONDS];
    private static int requestTimesCursor = -1;
    private static final Random throttleRandom = new Random();

    /**
     * Creates the folder described by {@code stringPath} if it does not exist.
     * Deletes existing files starting with {@link RecordingDownloader#FILE_NAME_PREFIX}.
     * @param stringPath Path to the folder where the json files will be saved to.
     */
    public RecordingDownloader(final String stringPath) throws IOException {
        this.path = stringPath;
        final Path path = Paths.get(stringPath);
        final File folder = path.toFile();
        if (folder.exists()) {
            for (final File file : folder.listFiles()) {
                if (file.getName().startsWith(RecordingDownloader.FILE_NAME_PREFIX)) {
                    file.delete();
                }
            }
        } else {
            Files.createDirectories(path);
        }
    }

    @Override
    public Response execute(@Nonnull final Request request) throws IOException,
            ReCaptchaException {

        // Delay the execution if the max number of requests per minute is reached
        final long currentTime = System.currentTimeMillis();
        // the cursor points to the latest request time and the next position is the oldest one
        final int oldestRequestTimeCursor = (requestTimesCursor + 1) % requestTimes.length;
        final long oldestRequestTime = requestTimes[oldestRequestTimeCursor];
        if (oldestRequestTime + 20_000 >= currentTime) {
            try {
                // sleep at least until the oldest request is 20s old, but not more than 20s
                final int minSleepTime = (int) (currentTime - oldestRequestTime);
                Thread.sleep(minSleepTime + throttleRandom.nextInt(20_000 - minSleepTime));
            } catch (InterruptedException e) {
                // handle the exception gracefully because it's not critical for the test
                System.err.println("Error while throttling the RecordingDownloader.");
                e.printStackTrace();
            }
        }
        requestTimesCursor = oldestRequestTimeCursor; // the oldest value needs to be overridden
        requestTimes[requestTimesCursor] = System.currentTimeMillis();

        // Handle ReCaptchaExceptions by retrying the request once after a while
        try {
            return executeRequest(request);
        } catch (ReCaptchaException e) {
            try {
                // sleep for 35-60 seconds to circumvent the rate limit
                System.out.println("Throttling the RecordingDownloader to handle a ReCaptcha."
                        + " Sleeping for 35-60 seconds.");
                Thread.sleep(35_000 + throttleRandom.nextInt(25_000));
            } catch (InterruptedException ie) {
                // handle the exception gracefully because it's not critical for the test
                System.err.println("Error while throttling the RecordingDownloader.");
                ie.printStackTrace();
                e.printStackTrace();
            }
            return executeRequest(request);
        }
    }

    @Nonnull
    private Response executeRequest(@Nonnull final Request request) throws IOException,
            ReCaptchaException {
        final Downloader downloader = DownloaderTestImpl.getInstance();
        Response response = downloader.execute(request);
        String cleanedResponseBody = response.responseBody().replaceAll(IP_V4_PATTERN, "127.0.0.1");
        response = new Response(
                response.responseCode(),
                response.responseMessage(),
                response.responseHeaders(),
                cleanedResponseBody,
                response.latestUrl()
        );

        final File outputFile = new File(path + File.separator + FILE_NAME_PREFIX + index
                + ".json");
        index++;
        outputFile.createNewFile();
        final OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputFile),
                StandardCharsets.UTF_8);
        new GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(new TestRequestResponse(request, response), writer);
        writer.flush();
        writer.close();

        return response;
    }
}
