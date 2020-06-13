package org.schabi.newpipe.extractor.linkhandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.util.ArrayList;
import java.util.List;

public abstract class SearchQueryHandlerFactory extends ListLinkHandlerFactory {

    ///////////////////////////////////
    // To Override
    ///////////////////////////////////

    @Override
    public abstract String getUrl(String query, List<String> contentFilter, String sortFilter) throws ParsingException;

    public String getSearchString(String url) {
        return "";
    }

    ///////////////////////////////////
    // Logic
    ///////////////////////////////////

    @Override
    public String getId(String url) {
        return getSearchString(url);
    }

    @Override
    public SearchQueryHandler fromQuery(String query,
                                        List<String> contentFilter,
                                        String sortFilter) throws ParsingException {
        return new SearchQueryHandler(super.fromQuery(query, contentFilter, sortFilter));
    }

    public SearchQueryHandler fromQuery(String query) throws ParsingException {
        return fromQuery(query, new ArrayList<String>(0), "");
    }

    /**
     * It's not mandatory for NewPipe to handle the Url
     *
     * @param url
     * @return
     */
    @Override
    public boolean onAcceptUrl(String url) {
        return false;
    }
}
