package org.schabi.newpipe.extractor.streamdata.stream.quality;

public class VideoQualityData {
    public static int UNKNOWN = -1;

    private final int height;
    private final int width;
    private final int fps;

    public VideoQualityData(final int height, final int width, final int fps) {
        this.height = height;
        this.width = width;
        this.fps = fps;
    }

    public VideoQualityData(final int height, final int fps) {
        this(height, UNKNOWN, fps);
    }

    public VideoQualityData(final int height) {
        this(height, UNKNOWN);
    }

    public VideoQualityData() {
        this(UNKNOWN);
    }


    public int height() {
        return height;
    }

    public int width() {
        return width;
    }

    public int fps() {
        return fps;
    }

    public boolean equalsVideoQualityData(final VideoQualityData other) {
        return height() == other.height()
                && width() == other.width()
                && fps() == other.fps();
    }
}
