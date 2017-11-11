package org.schabi.newpipe.extractor;

import java.util.List;

public abstract class ListInfo extends Info {
    public List<InfoItem> related_streams;
    public boolean has_more_streams;
    public String next_streams_url;

    public ListInfo(int serviceId, String id, String url, String name) {
        super(serviceId, id, url, name);
    }

    public List<InfoItem> getRelatedStreams() {
        return related_streams;
    }

    public void setRelatedStreams(List<InfoItem> related_streams) {
        this.related_streams = related_streams;
    }

    public boolean hasMoreStreams() {
        return has_more_streams;
    }

    public void setHasMoreStreams(boolean has_more_streams) {
        this.has_more_streams = has_more_streams;
    }

    public String getNextStreamsUrl() {
        return next_streams_url;
    }

    public void setNextStreamsUrl(String next_streams_url) {
        this.next_streams_url = next_streams_url;
    }
}
