package org.schabi.newpipe.downloader;

import com.google.gson.GsonBuilder;

import org.apache.commons.io.FileUtils;
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

class RecordingDownloader extends Downloader {

    public final static String FILE_NAME_PREFIX = "generated_mock_";

    private int index = 0;
    private final String path;

    public RecordingDownloader(String stringPath) throws IOException {
        this.path = stringPath;
        Path path = Paths.get(stringPath);
        File directory = path.toFile();
        if (directory.exists()) {
            FileUtils.cleanDirectory(directory);
        }
        Files.createDirectories(path);
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
