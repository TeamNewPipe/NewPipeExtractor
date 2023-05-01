package org.schabi.newpipe.extractor.playlist;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.localization.DateWrapper;

import javax.annotation.Nullable;

public class PlaylistInfoItem extends InfoItem {

    private String uploaderName;
    private String uploaderUrl;
    private boolean uploaderVerified;
    /**
     * How many streams this playlist have
     */
    private long streamCount = 0;
    private PlaylistInfo.PlaylistType playlistType;
    @Nullable
    private String textualUploadDate;
    @Nullable
    private DateWrapper uploadDate;

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

    public PlaylistInfo.PlaylistType getPlaylistType() {
        return playlistType;
    }

    public void setPlaylistType(final PlaylistInfo.PlaylistType playlistType) {
        this.playlistType = playlistType;
    }

    public String getTextualUploadDate() {
        return textualUploadDate;
    }

    public void setTextualUploadDate(final String textualUploadDate) {
        this.textualUploadDate = textualUploadDate;
    }

    public DateWrapper getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(final DateWrapper uploadDate) {
        this.uploadDate = uploadDate;
    }
}
