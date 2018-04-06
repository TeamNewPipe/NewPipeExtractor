package org.schabi.newpipe.extractor;

import java.util.List;

public abstract class ListInfo<T extends InfoItem> extends Info {
    private List<T> relatedItems;
    private String nextPageUrl = null;

    public ListInfo(int serviceId, String id, String url, String originalUrl, String name) {
        super(serviceId, id, url, originalUrl, name);
    }

    public List<T> getRelatedItems() {
        return relatedItems;
    }

    public void setRelatedItems(List<T> relatedItems) {
        this.relatedItems = relatedItems;
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
