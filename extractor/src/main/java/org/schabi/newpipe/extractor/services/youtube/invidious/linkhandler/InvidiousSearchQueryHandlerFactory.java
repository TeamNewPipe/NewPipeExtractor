package org.schabi.newpipe.extractor.services.youtube.invidious.linkhandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;

import java.util.List;

public class InvidiousSearchQueryHandlerFactory extends SearchQueryHandlerFactory {

    public static final String ALL = "all";
    public static final String VIDEOS = "videos";
    public static final String CHANNELS = "channels";
    public static final String PLAYLISTS = "playlists";

    private static String baseUrl;

    private InvidiousSearchQueryHandlerFactory(final String baseUrl) {
        InvidiousSearchQueryHandlerFactory.baseUrl = baseUrl;
    }

    public static InvidiousSearchQueryHandlerFactory getInstance(final String baseUrl) {
        return new InvidiousSearchQueryHandlerFactory(baseUrl);
    }

    @Override
    public String getUrl(String query, List<String> contentFilter, String sortFilter) throws ParsingException {
        String url = baseUrl + "/api/v1/search?q=" + query;

        if (contentFilter.size() > 0) {
            switch (contentFilter.get(0)) {
                case VIDEOS:
                    return url; // + "&type=video" it's the default type provided by Invidious
                case CHANNELS:
                    return url + "&type=channel";
                case PLAYLISTS:
                    return url + "&type=playlist";
                case ALL:
                default:
                    break;
            }
        }

        return url + "&type=all";
    }


    @Override
    public String[] getAvailableContentFilter() {
        return new String[]{
                ALL,
                VIDEOS,
                CHANNELS,
                PLAYLISTS
        };
    }

}
