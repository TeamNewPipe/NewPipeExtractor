package org.schabi.newpipe.extractor.playlist;

import org.schabi.newpipe.extractor.InfoItem;

public class PlaylistInfoItem extends InfoItem {

    private String uploaderName;
    /**
     * How many streams this playlist have
     */
    private long streamCount = 0;

    public PlaylistInfoItem(int serviceId, String url, String name) {
        super(InfoType.PLAYLIST, serviceId, url, name);
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(String uploader_name) {
        this.uploaderName = uploader_name;
    }

    public long getStreamCount() {
        return streamCount;
    }

    public void setStreamCount(long stream_count) {
        this.streamCount = stream_count;
    }
}
