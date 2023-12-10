package org.schabi.newpipe.extractor.playlist;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.stream.Description;

import javax.annotation.Nullable;

public class PlaylistInfoItem extends InfoItem {

    private String uploaderName;
    private String uploaderUrl;
    private boolean uploaderVerified;
    /**
     * How many streams this playlist have
     */
    private long streamCount = 0;
    private Description description;
    private PlaylistInfo.PlaylistType playlistType;

    public PlaylistInfoItem(final int serviceId, final String url, final String name) {
        super(InfoType.PLAYLIST, serviceId, url, name);
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(final String uploaderName) {
        this.uploaderName = uploaderName;
    }

    @Nullable
    public String getUploaderUrl() {
        return uploaderUrl;
    }

    public void setUploaderUrl(@Nullable final String uploaderUrl) {
        this.uploaderUrl = uploaderUrl;
    }

    public boolean isUploaderVerified() {
        return uploaderVerified;
    }

    public void setUploaderVerified(final boolean uploaderVerified) {
        this.uploaderVerified = uploaderVerified;
    }

    public long getStreamCount() {
        return streamCount;
    }

    public void setStreamCount(final long streamCount) {
        this.streamCount = streamCount;
    }

    public Description getDescription() {
        return description;
    }

    public void setDescription(final Description description) {
        this.description = description;
    }

    public PlaylistInfo.PlaylistType getPlaylistType() {
        return playlistType;
    }

    public void setPlaylistType(final PlaylistInfo.PlaylistType playlistType) {
        this.playlistType = playlistType;
    }
}
