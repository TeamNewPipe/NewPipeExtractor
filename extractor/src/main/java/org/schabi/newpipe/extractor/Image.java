package org.schabi.newpipe.extractor;

public class Image {
    // Make it so that HIGH > LOW
    public final static int LOW = -2;
    public final static int HIGH = -1;

    private final String url;
    private final int width;
    private final int height;

    public Image(final String url, final int width, final int height) {
        this.url = url;
        this.width = width;
        this.height = height;
    }

    public String getUrl() {
        return url;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
