package org.schabi.newpipe.extractor.linkhandler;

import org.schabi.newpipe.extractor.search.filter.FilterItem;

import java.util.List;

public class SearchQueryHandler extends ListLinkHandler {

    public SearchQueryHandler(final String originalUrl,
                              final String url,
                              final String searchString,
                              final List<FilterItem> contentFilters,
                              final List<FilterItem> sortFilter) {
        super(originalUrl, url, searchString, contentFilters, sortFilter);
    }

    public SearchQueryHandler(final ListLinkHandler handler) {
        this(handler.originalUrl,
                handler.url,
                handler.id,
                handler.contentFilters,
                handler.sortFilter);
    }


    /**
     * Returns the search string. Since ListQIHandler is based on ListLinkHandler
     * getSearchString() is equivalent to calling getId().
     *
     * @return the search string
     */
    public String getSearchString() {
        return getId();
    }
}
