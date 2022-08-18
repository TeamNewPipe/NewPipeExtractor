package org.schabi.newpipe.extractor.linkhandler;

import org.schabi.newpipe.extractor.search.filter.FilterItem;

import java.util.Collections;
import java.util.List;

public class ListLinkHandler extends LinkHandler {
    protected final List<FilterItem> contentFilters;
    protected final List<FilterItem> sortFilter;

    public ListLinkHandler(final String originalUrl,
                           final String url,
                           final String id,
                           final List<FilterItem> selectedContentFilters,
                           final List<FilterItem> selectedSortFilter) {
        super(originalUrl, url, id);
        this.contentFilters = Collections.unmodifiableList(selectedContentFilters);
        this.sortFilter = selectedSortFilter;
    }

    public ListLinkHandler(final ListLinkHandler handler) {
        this(handler.originalUrl,
                handler.url,
                handler.id,
                handler.contentFilters,
                handler.sortFilter);
    }

    public ListLinkHandler(final LinkHandler handler) {
        this(handler.originalUrl,
                handler.url,
                handler.id,
                Collections.emptyList(),
                Collections.emptyList());
    }

    public List<FilterItem> getContentFilters() {
        return contentFilters;
    }

    public List<FilterItem> getSortFilter() {
        return sortFilter;
    }
}
