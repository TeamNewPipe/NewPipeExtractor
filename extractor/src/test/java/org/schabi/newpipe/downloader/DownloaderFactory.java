package org.schabi.newpipe.downloader;

import org.schabi.newpipe.extractor.downloader.Downloader;

import java.io.IOException;

public class DownloaderFactory {

    public final static String RESOURCE_PATH = "src/test/resources/org/schabi/newpipe/extractor/";

    private final static DownloaderType DEFAULT_DOWNLOADER = DownloaderType.REAL;

    /**
     * @param path The path to the folder where mocks are saved/retrieved.
     *             Preferably starting with {@link DownloaderFactory#RESOURCE_PATH}
     */
    public Downloader getDownloader(String path) throws IOException {
        DownloaderType type;
        try {
            type = DownloaderType.valueOf(System.getProperty("downloader"));
        } catch (Exception e) {
            type = DEFAULT_DOWNLOADER;
        }

        switch (type) {
            case REAL:
                return DownloaderTestImpl.getInstance();
            case MOCK:
                return new MockDownloader(path);
            case RECORDING:
                return new RecordingDownloader(path);
            default:
                throw new UnsupportedOperationException("Unknown downloader type: " + type.toString());
        }
    }
}
