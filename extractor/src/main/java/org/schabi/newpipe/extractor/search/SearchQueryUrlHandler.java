package org.schabi.newpipe.extractor.search;

import org.schabi.newpipe.extractor.ListUrlIdHandler;
import org.schabi.newpipe.extractor.UrlIdHandler;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.util.List;

public abstract class SearchQueryUrlHandler extends ListUrlIdHandler {

    @Override
    public String onGetIdFromUrl(String url) {
        return "Search";
    }

    public String getSearchString() {
        return getId();
    }

    @Override
    public SearchQueryUrlHandler setQuery(String querry,
                                          List<String> contentFilter,
                                          String sortFilter) throws ParsingException {
        return (SearchQueryUrlHandler) super.setQuery(querry, contentFilter, sortFilter);
    }


    @Override
    public SearchQueryUrlHandler setQuery(String querry) throws ParsingException {
        return (SearchQueryUrlHandler) super.setQuery(querry);
    }


    @Override
    public boolean onAcceptUrl(String url) {
        return false;
    }

    public SearchQueryUrlHandler setId(String query) throws ParsingException {
        if(query == null) throw new IllegalArgumentException("id can not be null");
        this.id = query;
        return this;
    }

}
