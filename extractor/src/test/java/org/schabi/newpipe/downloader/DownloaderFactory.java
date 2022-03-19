package org.schabi.newpipe.downloader;

import org.schabi.newpipe.extractor.downloader.Downloader;

import java.io.IOException;

public class DownloaderFactory {

    public static final String RESOURCE_PATH = "src/test/resources/org/schabi/newpipe/extractor/";

    private static final DownloaderType DEFAULT_DOWNLOADER = DownloaderType.REAL;

    public static DownloaderType getDownloaderType() {
        try {
            return DownloaderType.valueOf(System.getProperty("downloader"));
        } catch (final Exception e) {
            return DEFAULT_DOWNLOADER;
        }
    }

    /**
     * <p>
     * Returns a implementation of a {@link Downloader}.
     * </p>
     * <p>
     * If the system property "downloader" is set and is one of {@link DownloaderType},
     * then a downloader of that type is returned.
     * It can be passed in with gradle by adding the argument -Ddownloader=abcd,
     * where abcd is one of {@link DownloaderType}
     * </p>
     * <p>
     * Otherwise it falls back to {@link DownloaderFactory#DEFAULT_DOWNLOADER}.
     * Change this during development on the local machine to use a different downloader.
     * </p>
     *
     * @param path The path to the folder where mocks are saved/retrieved.
     *             Preferably starting with {@link DownloaderFactory#RESOURCE_PATH}
     */
    public static Downloader getDownloader(final String path) throws IOException {
        final DownloaderType type = getDownloaderType();
        switch (type) {
            case REAL:
                return DownloaderTestImpl.getInstance();
            case MOCK:
                return new MockDownloader(path);
            case RECORDING:
                return new RecordingDownloader(path);
            default:
                throw new UnsupportedOperationException("Unknown downloader type: " + type);
        }
    }
}
