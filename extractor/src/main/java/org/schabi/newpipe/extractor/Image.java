package org.schabi.newpipe.extractor;

public class Image {
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
