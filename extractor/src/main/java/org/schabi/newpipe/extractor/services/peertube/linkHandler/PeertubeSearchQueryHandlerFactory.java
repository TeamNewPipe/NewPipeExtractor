package org.schabi.newpipe.extractor.services.peertube.linkHandler;

import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.utils.Utils;
import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.extractor.services.peertube.PeertubeHelpers;
import org.schabi.newpipe.extractor.services.peertube.search.filter.PeertubeFilters;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;

public final class PeertubeSearchQueryHandlerFactory extends SearchQueryHandlerFactory {

    private static final PeertubeSearchQueryHandlerFactory INSTANCE =
            new PeertubeSearchQueryHandlerFactory();

    public static final String VIDEOS = "videos";
    // sepia is the global index
    public static final String SEPIA_BASE_URL = "https://sepiasearch.org";
    public static final String SEARCH_ENDPOINT = "/api/v1/search/videos";

    private PeertubeSearchQueryHandlerFactory() {
        super(new PeertubeFilters());
    }

    public static PeertubeSearchQueryHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public String getUrl(final String searchString,
                         final List<FilterItem> selectedContentFilter,
                         final List<FilterItem> selectedSortFilters)
            throws ParsingException, UnsupportedOperationException {
        final String baseUrl;
        final Optional<FilterItem> sepiaFilter =
                PeertubeHelpers.getSepiaFilter(selectedContentFilter);
        if (sepiaFilter.isPresent()) {
            baseUrl = SEPIA_BASE_URL;
        } else {
            baseUrl = ServiceList.PeerTube.getBaseUrl();
        }

        return getUrl(searchString, selectedContentFilter, selectedSortFilters, baseUrl);
    }

    @Override
    public String getUrl(final String searchString,
                         final List<FilterItem> selectedContentFilter,
                         final List<FilterItem> selectedSortFilter,
                         final String baseUrl)
            throws ParsingException, UnsupportedOperationException {
        try {
            searchFilters.setSelectedSortFilter(selectedSortFilter);
            searchFilters.setSelectedContentFilter(selectedContentFilter);

            final String filterQuery = searchFilters.evaluateSelectedFilters(null);

            return baseUrl + SEARCH_ENDPOINT + "?search=" + Utils.encodeUrlUtf8(searchString)
                    + filterQuery;
        } catch (final UnsupportedEncodingException e) {
            throw new ParsingException("Could not encode query", e);
        }
    }
}
