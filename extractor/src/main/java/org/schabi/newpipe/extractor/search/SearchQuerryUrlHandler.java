package org.schabi.newpipe.extractor.search;

import org.schabi.newpipe.extractor.ListUrlIdHandler;

public abstract class SearchQuerryUrlHandler extends ListUrlIdHandler {
    String searchQuerry;

    public String getSearchQuerry() {
        return searchQuerry;
    }
}
