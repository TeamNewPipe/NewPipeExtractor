package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.util.List;

public abstract class ListInfo<T extends InfoItem> extends Info {
    private List<T> relatedItems;
    private String nextPageUrl = null;
    private String[] contentFilter = {};
    private String sortFilter = "";

    public ListInfo(int serviceId,
                    String id,
                    String url,
                    String originalUrl,
                    String name,
                    String[] contentFilter,
                    String sortFilter) {
        super(serviceId, id, url, originalUrl, name);
        this.contentFilter = contentFilter;
        this.sortFilter = sortFilter;
    }

    public ListInfo(int serviceId, ListUrlIdHandler listUrlIdHandler, String name) throws ParsingException {
        super(serviceId, listUrlIdHandler, name);
        this.contentFilter = listUrlIdHandler.getContentFilter();
        this.sortFilter = listUrlIdHandler.getSortFilter();
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

    public String[] getContentFilter() {
        return contentFilter;
    }

    public String getSortFilter() {
        return sortFilter;
    }
}
