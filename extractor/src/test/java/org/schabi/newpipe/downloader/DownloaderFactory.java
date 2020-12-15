package org.schabi.newpipe.downloader;

import org.schabi.newpipe.extractor.downloader.Downloader;

import java.io.IOException;

public class DownloaderFactory {

    private final static DownloaderType DEFAULT_DOWNLOADER = DownloaderType.REAL;

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
