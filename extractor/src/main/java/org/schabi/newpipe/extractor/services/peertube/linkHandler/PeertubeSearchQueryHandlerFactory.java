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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class PeertubeSearchQueryHandlerFactory extends SearchQueryHandlerFactory {

    private static final PeertubeSearchQueryHandlerFactory INSTANCE =
            new PeertubeSearchQueryHandlerFactory();

    public static final String VIDEOS = "videos";
    // sepia is the global index
    public static final String SEPIA_BASE_URL = "https://sepiasearch.org";
    public static final String SEARCH_ENDPOINT_PLAYLISTS = "/api/v1/search/video-playlists";
    public static final String SEARCH_ENDPOINT_VIDEOS = "/api/v1/search/videos";
    public static final String SEARCH_ENDPOINT_CHANNELS = "/api/v1/search/video-channels";

    private PeertubeSearchQueryHandlerFactory() {
        super(new PeertubeFilters());
    }

    public static PeertubeSearchQueryHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public String getUrl(final String searchString,
                         @Nonnull final List<FilterItem> selectedContentFilter,
                         @Nullable final List<FilterItem> selectedSortFilters)
            throws ParsingException, UnsupportedOperationException {
        final String baseUrl;
        if (isSepiaContentFilterPresent(selectedContentFilter)) {
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

            final String endpoint = getContentFilterDependingEndpoint(selectedContentFilter);
            return baseUrl + endpoint + "?search=" + Utils.encodeUrlUtf8(searchString)
                    + filterQuery;
        } catch (final UnsupportedEncodingException e) {
            throw new ParsingException("Could not encode query", e);
        }
    }

    private boolean isSepiaContentFilterPresent(
            @Nullable final List<FilterItem> selectedContentFilter) {
        boolean isSepiaFilterPresent = false;
        if (selectedContentFilter != null) {
            final Optional<FilterItem> sepiaFilter =
                    PeertubeHelpers.getSepiaFilter(selectedContentFilter);
            if (sepiaFilter.isPresent()) {
                isSepiaFilterPresent = true;
            }
        }

        return isSepiaFilterPresent;
    }

    private String getContentFilterDependingEndpoint(
            @Nullable final List<FilterItem> selectedContentFilter) {
        // default to video search endpoint
        String endpoint = SEARCH_ENDPOINT_VIDEOS;
        if (selectedContentFilter != null
                && // SepiaFilter only supports SEARCH_ENDPOINT_VIDEOS
                !isSepiaContentFilterPresent(selectedContentFilter)) {
            final Optional<FilterItem> contentFilter = selectedContentFilter.stream()
                    .filter(PeertubeFilters.PeertubeContentFilterItem.class::isInstance)
                    .findFirst();
            if (contentFilter.isPresent()) {
                endpoint = ((PeertubeFilters.PeertubeContentFilterItem) contentFilter.get())
                        .getEndpoint();
            }
        }
        return endpoint;
    }
}
