package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;

import java.util.List;

import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public abstract class ListInfo<T extends InfoItem> extends Info {
    private List<T> relatedItems;
    private Page nextPage = null;
    private final List<String> contentFilters;
    private final String sortFilter;

    public ListInfo(int serviceId,
                    String id,
                    String url,
                    String originalUrl,
                    String name,
                    List<String> contentFilter,
                    String sortFilter) {
        super(serviceId, id, url, originalUrl, name);
        this.contentFilters = contentFilter;
        this.sortFilter = sortFilter;
    }

    public ListInfo(int serviceId, ListLinkHandler listUrlIdHandler, String name) {
        super(serviceId, listUrlIdHandler, name);
        this.contentFilters = listUrlIdHandler.getContentFilters();
        this.sortFilter = listUrlIdHandler.getSortFilter();
    }

    public List<T> getRelatedItems() {
        return relatedItems;
    }

    public void setRelatedItems(List<T> relatedItems) {
        this.relatedItems = relatedItems;
    }

    public boolean hasNextPage() {
        return nextPage != null && (!isNullOrEmpty(nextPage.getUrl())
                || !isNullOrEmpty(nextPage.getIds()));
    }

    public Page getNextPage() {
        return nextPage;
    }

    public void setNextPage(Page page) {
        this.nextPage = page;
    }

    public List<String> getContentFilters() {
        return contentFilters;
    }

    public String getSortFilter() {
        return sortFilter;
    }
}
