package org.schabi.newpipe.extractor.stream;

import java.util.List;

public final class Frameset {

    private List<String> urls;
    private int frameWidth;
    private int frameHeight;
    private int totalCount;
    private int framesPerPageX;
    private int framesPerPageY;

    public Frameset(List<String> urls, int frameWidth, int frameHeight, int totalCount, int framesPerPageX, int framesPerPageY) {
        this.urls = urls;
        this.totalCount = totalCount;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.framesPerPageX = framesPerPageX;
        this.framesPerPageY = framesPerPageY;
    }

    /**
     * @return list of urls to images with frames
     */
    public List<String> getUrls() {
        return urls;
    }

    /**
     * @return total count of frames
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * @return maximum frames count by x
     */
    public int getFramesPerPageX() {
        return framesPerPageX;
    }

    /**
     * @return maximum frames count by y
     */
    public int getFramesPerPageY() {
        return framesPerPageY;
    }

    /**
     * @return width of a one frame, in pixels
     */
    public int getFrameWidth() {
        return frameWidth;
    }

    /**
     * @return height of a one frame, in pixels
     */
    public int getFrameHeight() {
        return frameHeight;
    }
}