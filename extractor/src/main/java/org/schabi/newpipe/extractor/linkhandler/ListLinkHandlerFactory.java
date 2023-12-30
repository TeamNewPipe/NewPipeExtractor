package org.schabi.newpipe.extractor.linkhandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.extractor.utils.Utils;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ListLinkHandlerFactory extends LinkHandlerFactory {

    ///////////////////////////////////
    // To Override
    ///////////////////////////////////

    public abstract String getUrl(String id,
                                  @Nonnull List<FilterItem> contentFilter,
                                  @Nullable List<FilterItem> sortFilter)
            throws ParsingException, UnsupportedOperationException;

    public String getUrl(final String id,
                         final List<FilterItem> contentFilter,
                         final List<FilterItem> sortFilter,
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

    public ListLinkHandler fromQuery(final String query,
                                     final List<FilterItem> contentFilter,
                                     final List<FilterItem> sortFilter) throws ParsingException {
        final String url = getUrl(query, contentFilter, sortFilter);
        return new ListLinkHandler(url, url, query, contentFilter, sortFilter);
    }

    public ListLinkHandler fromQuery(final String id,
                                     final List<FilterItem> contentFilters,
                                     final List<FilterItem> sortFilter,
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
        return getUrl(id, List.of(), List.of());
    }

    @Override
    public String getUrl(final String id, final String baseUrl)
            throws ParsingException, UnsupportedOperationException {
        return getUrl(id, List.of(), List.of(), baseUrl);
    }
}
