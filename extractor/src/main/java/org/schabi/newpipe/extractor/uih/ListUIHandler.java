package org.schabi.newpipe.extractor.uih;

import java.util.Collections;
import java.util.List;

public class ListUIHandler extends UIHandler {
    protected final List<String> contentFilters;
    protected final String sortFilter;

    public ListUIHandler(String originalUrl,
                         String url,
                         String id,
                         List<String> contentFilters,
                         String sortFilter) {
        super(originalUrl, url, id);
        this.contentFilters = Collections.unmodifiableList(contentFilters);
        this.sortFilter = sortFilter;
    }

    public ListUIHandler(ListUIHandler handler) {
        this(handler.originalUrl,
                handler.url,
                handler.id,
                handler.contentFilters,
                handler.sortFilter);
    }

    public ListUIHandler(UIHandler handler,
                         List<String> contentFilters,
                         String sortFilter) {
        this(handler.originalUrl,
                handler.url,
                handler.id,
                contentFilters,
                sortFilter);
    }

    public List<String> getContentFilters() {
        return contentFilters;
    }

    public String getSortFilter() {
        return sortFilter;
    }
}
