package org.schabi.newpipe.extractor.playlist;

import org.schabi.newpipe.extractor.InfoItem;

public class PlaylistInfoItem extends InfoItem {

    private String uploaderName;
    /**
     * How many streams this playlist have
     */
    private long streamCount = 0;
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
}
