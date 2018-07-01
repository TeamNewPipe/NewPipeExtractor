package org.schabi.newpipe.extractor.uih;

import java.util.List;

public class SearchQIHandler extends ListUIHandler {

    public SearchQIHandler(String originalUrl,
                           String url,
                           String searchString,
                           List<String> contentFilters,
                           String sortFilter) {
        super(originalUrl, url, searchString, contentFilters, sortFilter);
    }

    public SearchQIHandler(ListUIHandler handler) {
        this(handler.originalUrl,
                handler.url,
                handler.id,
                handler.contentFilters,
                handler.sortFilter);
    }


    /**
     * Returns the search string. Since ListQIHandler is based on ListUIHandler
     * getSearchString() is equivalent to calling getId().
     * @return the search string
     */
    public String getSearchString() {
        return getId();
    }
}
