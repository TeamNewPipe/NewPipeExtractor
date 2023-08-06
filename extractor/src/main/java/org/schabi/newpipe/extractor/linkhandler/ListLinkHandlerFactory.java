package org.schabi.newpipe.extractor.linkhandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class ListLinkHandlerFactory extends LinkHandlerFactory {

    ///////////////////////////////////
    // To Override
    ///////////////////////////////////

    public abstract String getUrl(String id, List<String> contentFilter, String sortFilter)
            throws ParsingException, UnsupportedOperationException;

    public String getUrl(final String id,
                         final List<String> contentFilter,
                         final String sortFilter,
                         final String baseUrl)
            throws ParsingException, UnsupportedOperationException {
        return getUrl(id, contentFilter, sortFilter);
    }

    ///////////////////////////////////
    // Logic
    ///////////////////////////////////

    @Override
    public ListLinkHandler fromUrl(final String url) throws ParsingException {
        final String polishedUrl = Utils.followGoogleRedirectIfNeeded(url);
        final String baseUrl = Utils.getBaseUrl(polishedUrl);
        return fromUrl(polishedUrl, baseUrl);
    }

    @Override
    public ListLinkHandler fromUrl(final String url, final String baseUrl) throws ParsingException {
        Objects.requireNonNull(url, "URL may not be null");
        return new ListLinkHandler(super.fromUrl(url, baseUrl));
    }

    @Override
    public ListLinkHandler fromId(final String id) throws ParsingException {
        return new ListLinkHandler(super.fromId(id));
    }

    @Override
    public ListLinkHandler fromId(final String id, final String baseUrl) throws ParsingException {
        return new ListLinkHandler(super.fromId(id, baseUrl));
    }

    public ListLinkHandler fromQuery(final String id,
                                     final List<String> contentFilters,
                                     final String sortFilter) throws ParsingException {
        final String url = getUrl(id, contentFilters, sortFilter);
        return new ListLinkHandler(url, url, id, contentFilters, sortFilter);
    }

    public ListLinkHandler fromQuery(final String id,
                                     final List<String> contentFilters,
                                     final String sortFilter,
                                     final String baseUrl) throws ParsingException {
        final String url = getUrl(id, contentFilters, sortFilter, baseUrl);
        return new ListLinkHandler(url, url, id, contentFilters, sortFilter);
    }


    /**
     * For making ListLinkHandlerFactory compatible with LinkHandlerFactory we need to override
     * this, however it should not be overridden by the actual implementation.
     *
     * @return the url corresponding to id without any filters applied
     */
    public String getUrl(final String id) throws ParsingException, UnsupportedOperationException {
        return getUrl(id, new ArrayList<>(0), "");
    }

    @Override
    public String getUrl(final String id, final String baseUrl) throws ParsingException {
        return getUrl(id, new ArrayList<>(0), "", baseUrl);
    }

    /**
     * Will returns content filter the corresponding extractor can handle like "channels", "videos",
     * "music", etc.
     *
     * @return filter that can be applied when building a query for getting a list
     */
    public String[] getAvailableContentFilter() {
        return new String[0];
    }

    /**
     * Will returns sort filter the corresponding extractor can handle like "A-Z", "oldest first",
     * "size", etc.
     *
     * @return filter that can be applied when building a query for getting a list
     */
    public String[] getAvailableSortFilter() {
        return new String[0];
    }
}
