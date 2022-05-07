package org.schabi.newpipe.extractor.services.youtube.invidious.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousService;

import java.util.List;

public class InvidiousSearchQueryHandlerFactory extends SearchQueryHandlerFactory {

    public static final String ALL = "all";
    public static final String VIDEOS = "videos";
    public static final String CHANNELS = "channels";
    public static final String PLAYLISTS = "playlists";

    protected final String baseUrl;

    public InvidiousSearchQueryHandlerFactory(final InvidiousService service) {
        this.baseUrl = service.getInstance().getUrl();
    }

    @Override
    public String getUrl(
            final String query,
            final List<String> contentFilter,
            final String sortFilter
    ) throws ParsingException {
        final String url = baseUrl + "/api/v1/search?q=" + query;

        if (!contentFilter.isEmpty()) {
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
