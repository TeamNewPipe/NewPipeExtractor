package org.schabi.newpipe.downloader;

import org.schabi.newpipe.extractor.downloader.Downloader;

import java.util.Locale;

public class DownloaderFactory {

    private static final DownloaderType DEFAULT_DOWNLOADER = DownloaderType.REAL;

    private static DownloaderType cachedDownloaderType;

    private static DownloaderType getDownloaderType() {
        if (cachedDownloaderType == null) {
            cachedDownloaderType = determineDownloaderType();
        }

        return cachedDownloaderType;
    }

    private static DownloaderType determineDownloaderType() {
        String propValue = System.getProperty("downloader");
        if (propValue == null) {
            return DEFAULT_DOWNLOADER;
        }
        propValue = propValue.toUpperCase();
        // Use shortcut because RECORDING is quite long
        if (propValue.equals("REC")) {
            return DownloaderType.RECORDING;
        }
        try {
            return DownloaderType.valueOf(propValue);
        } catch (final Exception e) {
            return DEFAULT_DOWNLOADER;
        }
    }

    public static Downloader getDownloader(final Class<?> clazz) {
        return getDownloader(clazz, null);
    }

    public static Downloader getDownloader(final Class<?> clazz, final String specificUseCase) {
        String baseName = clazz.getName();
        if (specificUseCase != null) {
            baseName += "." + specificUseCase;
        }
        return getDownloader("src/test/resources/mocks/v1/"
            + baseName
            .toLowerCase(Locale.ENGLISH)
            .replace('$', '.')
            .replace("test", "")
            .replace('.', '/'));
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
     */
    protected static Downloader getDownloader(final String path) {
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
