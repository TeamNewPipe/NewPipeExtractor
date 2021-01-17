package org.schabi.newpipe.downloader;

import com.google.gson.GsonBuilder;

import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Request;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.Nonnull;

/**
 * <p>
 * Relays requests to {@link DownloaderTestImpl} and saves the request/response pair into a json file.
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

    public final static String FILE_NAME_PREFIX = "generated_mock_";

    private int index = 0;
    private final String path;

    /**
     * Creates the folder described by {@code stringPath} if it does not exists.
     * Deletes existing files starting with {@link RecordingDownloader#FILE_NAME_PREFIX}.
     * @param stringPath Path to the folder where the json files will be saved to.
     */
    public RecordingDownloader(String stringPath) throws IOException {
        this.path = stringPath;
        Path path = Paths.get(stringPath);
        File folder = path.toFile();
        if (folder.exists()) {
            for (File file : folder.listFiles()) {
                if (file.getName().startsWith(RecordingDownloader.FILE_NAME_PREFIX)) {
                    file.delete();
                }
            }
        } else {
            Files.createDirectories(path);
        }
    }

    @Override
    public Response execute(@Nonnull Request request) throws IOException, ReCaptchaException {
        Downloader downloader = DownloaderTestImpl.getInstance();
        Response response = downloader.execute(request);

        File outputFile = new File(path + File.separator + FILE_NAME_PREFIX + index + ".json");
        index++;
        outputFile.createNewFile();
        FileWriter writer = new FileWriter(outputFile);
        new GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(new TestRequestResponse(request, response), writer);
        writer.flush();
        writer.close();

        return response;
    }
}
