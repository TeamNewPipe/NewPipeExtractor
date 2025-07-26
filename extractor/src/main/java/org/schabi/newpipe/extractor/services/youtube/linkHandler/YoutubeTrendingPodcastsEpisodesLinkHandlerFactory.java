package org.schabi.newpipe.extractor.services.youtube.linkHandler;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isInvidiousURL;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isYoutubeURL;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public final class YoutubeTrendingPodcastsEpisodesLinkHandlerFactory
        extends ListLinkHandlerFactory {

    public static final String KIOSK_ID = "trending_podcasts_episodes";

    public static final YoutubeTrendingPodcastsEpisodesLinkHandlerFactory INSTANCE =
            new YoutubeTrendingPodcastsEpisodesLinkHandlerFactory();

    private static final String PATH = "/podcasts/popularepisodes";

    private YoutubeTrendingPodcastsEpisodesLinkHandlerFactory() {
    }

    @Override
    public String getUrl(final String id,
                         final List<String> contentFilters,
                         final String sortFilter)
            throws ParsingException, UnsupportedOperationException {
        return "https://www.youtube.com" + PATH;
    }

    @Override
    public String getId(final String url) throws ParsingException, UnsupportedOperationException {
        return KIOSK_ID;
    }

    @Override
    public boolean onAcceptUrl(final String url) {
        final URL urlObj;
        try {
            urlObj = Utils.stringToURL(url);
        } catch (final MalformedURLException e) {
            return false;
        }

        return Utils.isHTTP(urlObj) && (isYoutubeURL(urlObj) || isInvidiousURL(urlObj))
                && PATH.equals(urlObj.getPath());
    }
}
