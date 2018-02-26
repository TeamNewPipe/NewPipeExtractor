package org.schabi.newpipe.extractor;

import java.util.List;

public abstract class ListInfo extends Info {
    public List<InfoItem> related_streams;
    public String nextPageUrl = null;

    public ListInfo(int serviceId, String id, String url, String name) {
        super(serviceId, id, url, name);
    }

    public List<InfoItem> getRelatedStreams() {
        return related_streams;
    }

    public void setRelatedStreams(List<InfoItem> related_streams) {
        this.related_streams = related_streams;
    }

    public boolean hasNextPage() {
        return nextPageUrl != null && !nextPageUrl.isEmpty();
    }

    public String getNextPageUrl() {
        return nextPageUrl;
    }

    public void setNextPageUrl(String pageUrl) {
        this.nextPageUrl = pageUrl;
    }
}
