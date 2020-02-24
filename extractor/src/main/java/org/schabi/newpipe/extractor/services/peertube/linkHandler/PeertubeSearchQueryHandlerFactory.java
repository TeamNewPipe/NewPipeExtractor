package org.schabi.newpipe.extractor.services.peertube.linkHandler;

import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class PeertubeSearchQueryHandlerFactory extends SearchQueryHandlerFactory {

    public static final String CHARSET_UTF_8 = "UTF-8";
    public static final String VIDEOS = "videos";
    public static final String SEARCH_ENDPOINT = "/api/v1/search/videos";

    public static PeertubeSearchQueryHandlerFactory getInstance() {
        return new PeertubeSearchQueryHandlerFactory();
    }

    @Override
    public String getUrl(String searchString, List<String> contentFilters, String sortFilter) throws ParsingException {
        String baseUrl = ServiceList.PeerTube.getBaseUrl();
        return getUrl(searchString, contentFilters, sortFilter, baseUrl);
    }

    @Override
    public String getUrl(String searchString, List<String> contentFilters, String sortFilter, String baseUrl) throws ParsingException {
        try {
            final String url = baseUrl + SEARCH_ENDPOINT
                    + "?search=" + URLEncoder.encode(searchString, CHARSET_UTF_8);

            return url;
        } catch (UnsupportedEncodingException e) {
            throw new ParsingException("Could not encode query", e);
        }
    }

    @Override
    public String[] getAvailableContentFilter() {
        return new String[]{VIDEOS};
    }
}
