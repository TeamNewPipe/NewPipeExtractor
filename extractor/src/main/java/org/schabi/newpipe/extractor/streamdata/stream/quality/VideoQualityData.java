package org.schabi.newpipe.extractor.streamdata.stream.quality;

public class VideoQualityData {
    public static final int UNKNOWN = -1;

    private final int height;
    private final int width;
    private final int fps;

    public VideoQualityData(final int height, final int width, final int fps) {
        this.height = height;
        this.width = width;
        this.fps = fps;
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

    public static VideoQualityData fromHeightWidth(final int height, final int width) {
        return new VideoQualityData(height, width, UNKNOWN);
    }

    public static VideoQualityData fromHeightFps(final int height, final int fps) {
        return new VideoQualityData(height, UNKNOWN, fps);
    }

    public static VideoQualityData fromHeight(final int height) {
        return new VideoQualityData(height, UNKNOWN, UNKNOWN);
    }

    public static VideoQualityData fromUnknown() {
        return new VideoQualityData(UNKNOWN, UNKNOWN, UNKNOWN);
    }
}
