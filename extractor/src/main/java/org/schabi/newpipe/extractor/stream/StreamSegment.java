package org.schabi.newpipe.extractor.stream;


import javax.annotation.Nullable;
import java.io.Serializable;

public class StreamSegment implements Serializable {
    /**
     * Title of this segment
     */
    private String title;

    /**
     * The channel or creator linked to this segment
     */
    private String channelName;

    /**
     * Timestamp of the starting point in seconds
     */
    private int startTimeSeconds;

    /**
     * Direct url to this segment. This can be null if the service doesn't provide such function.
     */
    @Nullable
    public String url;

    /**
     * Preview url for this segment. This can be null if the service doesn't provide such function
     * or there is no resource found.
     */
    @Nullable
    private String previewUrl = null;

    public StreamSegment(final String title, final int startTimeSeconds) {
        this.title = title;
        this.startTimeSeconds = startTimeSeconds;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public int getStartTimeSeconds() {
        return startTimeSeconds;
    }

    public void setStartTimeSeconds(final int startTimeSeconds) {
        this.startTimeSeconds = startTimeSeconds;
    }

    @Nullable
    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(@Nullable final String channelName) {
        this.channelName = channelName;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    public void setUrl(@Nullable final String url) {
        this.url = url;
    }

    @Nullable
    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(@Nullable final String previewUrl) {
        this.previewUrl = previewUrl;
    }
}
