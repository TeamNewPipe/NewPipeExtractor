package org.schabi.newpipe.extractor.playlist;

import org.schabi.newpipe.extractor.InfoItem;

public class PlaylistInfoItem extends InfoItem {

    public String uploader_name;
    /**
     * How many streams this playlist have
     */
    public long stream_count = 0;

    public PlaylistInfoItem() {
        super(InfoType.PLAYLIST);
    }
}
