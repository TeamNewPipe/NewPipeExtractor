package org.schabi.newpipe.extractor.playlist;

import org.schabi.newpipe.extractor.InfoItem;

public class PlaylistInfoItem extends InfoItem {

    public String uploader_name;
    /**
     * How many streams this playlist have
     */
    public long stream_count = 0;

    public PlaylistInfoItem(int serviceId, String url, String name) {
        super(InfoType.PLAYLIST, serviceId, url, name);
    }

    public String getUploaderName() {
        return uploader_name;
    }

    public void setUploaderName(String uploader_name) {
        this.uploader_name = uploader_name;
    }

    public long getStreamCount() {
        return stream_count;
    }

    public void setStreamCount(long stream_count) {
        this.stream_count = stream_count;
    }
}
