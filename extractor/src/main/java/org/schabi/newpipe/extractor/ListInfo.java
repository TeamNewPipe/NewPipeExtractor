package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.utils.Pair;

import java.util.List;
import java.util.Map;

public abstract class ListInfo<T extends InfoItem> extends Info {
    private List<T> relatedItems;
    private Page nextPage = null;
    private Map<String, Page> sortOptionPages;
    private final List<String> contentFilters;
    private final String sortFilter;

    public ListInfo(final int serviceId,
                    final String id,
                    final String url,
                    final String originalUrl,
                    final String name,
                    final List<String> contentFilter,
                    final String sortFilter) {
        super(serviceId, id, url, originalUrl, name);
        this.contentFilters = contentFilter;
        this.sortFilter = sortFilter;
    }

    public ListInfo(final int serviceId,
                    final ListLinkHandler listUrlIdHandler,
                    final String name) {
        super(serviceId, listUrlIdHandler, name);
        this.contentFilters = listUrlIdHandler.getContentFilters();
        this.sortFilter = listUrlIdHandler.getSortFilter();
    }

    public List<T> getRelatedItems() {
        return relatedItems;
    }

    public void setRelatedItems(final List<T> relatedItems) {
        this.relatedItems = relatedItems;
    }

    public boolean hasNextPage() {
        return Page.isValid(nextPage);
    }

    public Page getNextPage() {
        return nextPage;
    }

    public void setNextPage(final Page page) {
        this.nextPage = page;
    }

    public void setSortOptionPages(final Map<String, Page> pages) {
        this.sortOptionPages = pages;
    }

    public Map<String, Page> getSortOptionPages() {
        return sortOptionPages;
    }

    public List<String> getContentFilters() {
        return contentFilters;
    }

    public String getSortFilter() {
        return sortFilter;
    }
}
