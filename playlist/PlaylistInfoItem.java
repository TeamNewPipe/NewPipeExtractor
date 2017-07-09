package org.schabi.newpipe.extractor.playlist;

import org.schabi.newpipe.extractor.InfoItem;

public class PlaylistInfoItem implements InfoItem {

    public int serviceId = -1;
    public String name = "";
    public String thumbnailUrl = "";
    public String webPageUrl = "";

    public InfoType infoType() {
        return InfoType.PLAYLIST;
    }

    public String getTitle() {
        return name;
    }

    public String getLink() {
        return webPageUrl;
    }
}
