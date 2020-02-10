package org.schabi.newpipe.extractor.linkhandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public abstract class ListLinkHandlerFactory extends LinkHandlerFactory {

    ///////////////////////////////////
    // To Override
    ///////////////////////////////////

    public List<String> getContentFilter(String url) throws ParsingException {
        return new ArrayList<>(0);
    }

    public String getSortFilter(String url) throws ParsingException {
        return "";
    }

    public abstract String getUrl(String id, List<String> contentFilter, String sortFilter) throws ParsingException;

    public String getUrl(String id, List<String> contentFilter, String sortFilter, String baseUrl) throws ParsingException {
        return getUrl(id, contentFilter, sortFilter);
    }

    ///////////////////////////////////
    // Logic
    ///////////////////////////////////

    @Override
    public ListLinkHandler fromUrl(String url) throws ParsingException {
        String baseUrl = Utils.getBaseUrl(url);
        return fromUrl(url, baseUrl);
    }

    @Override
    public ListLinkHandler fromUrl(String url, String baseUrl) throws ParsingException {
        if (url == null) throw new IllegalArgumentException("url may not be null");

        return new ListLinkHandler(super.fromUrl(url, baseUrl), getContentFilter(url), getSortFilter(url));
    }

    @Override
    public ListLinkHandler fromId(String id) throws ParsingException {
        return new ListLinkHandler(super.fromId(id), new ArrayList<String>(0), "");
    }

    @Override
    public ListLinkHandler fromId(String id, String baseUrl) throws ParsingException {
        return new ListLinkHandler(super.fromId(id, baseUrl), new ArrayList<String>(0), "");
    }

    public ListLinkHandler fromQuery(String id,
                                     List<String> contentFilters,
                                     String sortFilter) throws ParsingException {
        final String url = getUrl(id, contentFilters, sortFilter);
        return new ListLinkHandler(url, url, id, contentFilters, sortFilter);
    }

    public ListLinkHandler fromQuery(String id,
                                     List<String> contentFilters,
                                     String sortFilter, String baseUrl) throws ParsingException {
        final String url = getUrl(id, contentFilters, sortFilter, baseUrl);
        return new ListLinkHandler(url, url, id, contentFilters, sortFilter);
    }


    /**
     * For making ListLinkHandlerFactory compatible with LinkHandlerFactory we need to override this,
     * however it should not be overridden by the actual implementation.
     *
     * @param id
     * @return the url coresponding to id without any filters applied
     */
    public String getUrl(String id) throws ParsingException {
        return getUrl(id, new ArrayList<String>(0), "");
    }

    @Override
    public String getUrl(String id, String baseUrl) throws ParsingException {
        return getUrl(id, new ArrayList<String>(0), "", baseUrl);
    }

    /**
     * Will returns content filter the corresponding extractor can handle like "channels", "videos", "music", etc.
     *
     * @return filter that can be applied when building a query for getting a list
     */
    public String[] getAvailableContentFilter() {
        return new String[0];
    }

    /**
     * Will returns sort filter the corresponding extractor can handle like "A-Z", "oldest first", "size", etc.
     *
     * @return filter that can be applied when building a query for getting a list
     */
    public String[] getAvailableSortFilter() {
        return new String[0];
    }
}
