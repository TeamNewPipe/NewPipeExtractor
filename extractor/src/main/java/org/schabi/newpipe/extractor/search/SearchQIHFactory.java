package org.schabi.newpipe.extractor.search;

import org.schabi.newpipe.extractor.ListUIHFactory;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.util.List;

public abstract class SearchQIHFactory extends ListUIHFactory {

    @Override
    public String onGetIdFromUrl(String url) {
        return "Search";
    }

    public String getSearchString() {
        return getId();
    }

    @Override
    public SearchQIHFactory setQuery(String querry,
                                     List<String> contentFilter,
                                     String sortFilter) throws ParsingException {
        return (SearchQIHFactory) super.setQuery(querry, contentFilter, sortFilter);
    }


    @Override
    public SearchQIHFactory setQuery(String querry) throws ParsingException {
        return (SearchQIHFactory) super.setQuery(querry);
    }


    @Override
    public boolean onAcceptUrl(String url) {
        return false;
    }

    public SearchQIHFactory setId(String query) throws ParsingException {
        if(query == null) throw new IllegalArgumentException("id can not be null");
        this.id = query;
        return this;
    }

}
