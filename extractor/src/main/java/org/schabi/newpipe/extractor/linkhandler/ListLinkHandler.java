package org.schabi.newpipe.extractor.linkhandler;

import java.util.Collections;
import java.util.List;

public class ListLinkHandler extends LinkHandler {
    protected final List<String> contentFilters;
    protected final String sortFilter;

    public ListLinkHandler(final String originalUrl,
                           final String url,
                           final String id,
                           final List<String> contentFilters,
                           final String sortFilter) {
        super(originalUrl, url, id);
        this.contentFilters = Collections.unmodifiableList(contentFilters);
        this.sortFilter = sortFilter;
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
                "");
    }

    public List<String> getContentFilters() {
        return contentFilters;
    }

    public String getSortFilter() {
        return sortFilter;
    }
}
