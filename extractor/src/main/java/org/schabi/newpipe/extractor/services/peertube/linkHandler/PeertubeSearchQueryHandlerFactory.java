package org.schabi.newpipe.extractor.services.peertube.linkHandler;

import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.util.List;

public final class PeertubeSearchQueryHandlerFactory extends SearchQueryHandlerFactory {

    public static final String VIDEOS = "videos";
    public static final String SEPIA_VIDEOS = "sepia_videos"; // sepia is the global index
    public static final String PLAYLISTS = "playlists";
    public static final String CHANNELS = "channels";
    public static final String SEPIA_BASE_URL = "https://sepiasearch.org";
    public static final String SEARCH_ENDPOINT_PLAYLISTS = "/api/v1/search/video-playlists";
    public static final String SEARCH_ENDPOINT_VIDEOS = "/api/v1/search/videos";
    public static final String SEARCH_ENDPOINT_CHANNELS = "/api/v1/search/video-channels";

    private PeertubeSearchQueryHandlerFactory() {
    }

    public static PeertubeSearchQueryHandlerFactory getInstance() {
        return new PeertubeSearchQueryHandlerFactory();
    }

    @Override
    public String getUrl(final String searchString,
                         final List<String> contentFilters,
                         final String sortFilter) throws ParsingException {
        final String baseUrl;
        if (!contentFilters.isEmpty() && contentFilters.get(0).startsWith("sepia_")) {
            baseUrl = SEPIA_BASE_URL;
        } else {
            baseUrl = ServiceList.PeerTube.getBaseUrl();
        }
        return getUrl(searchString, contentFilters, sortFilter, baseUrl);
    }

    @Override
    public String getUrl(final String searchString,
                         final List<String> contentFilters,
                         final String sortFilter,
                         final String baseUrl) throws ParsingException {
        try {
            final String endpoint;
            if (contentFilters.isEmpty()
                    || contentFilters.get(0).equals(VIDEOS)
                    || contentFilters.get(0).equals(SEPIA_VIDEOS)) {
                endpoint = SEARCH_ENDPOINT_VIDEOS;
            } else if (contentFilters.get(0).equals(CHANNELS)) {
                endpoint = SEARCH_ENDPOINT_CHANNELS;
            } else {
                endpoint = SEARCH_ENDPOINT_PLAYLISTS;
            }
            return baseUrl + endpoint + "?search=" + Utils.encodeUrlUtf8(searchString);
        } catch (final UnsupportedEncodingException e) {
            throw new ParsingException("Could not encode query", e);
        }
    }

    @Override
    public String[] getAvailableContentFilter() {
        return new String[]{
                VIDEOS,
                PLAYLISTS,
                CHANNELS,
                SEPIA_VIDEOS,
        };
    }
}
