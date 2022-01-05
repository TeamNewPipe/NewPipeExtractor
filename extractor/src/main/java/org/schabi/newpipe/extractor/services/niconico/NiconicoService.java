package org.schabi.newpipe.extractor.services.niconico;

import static org.schabi.newpipe.extractor.StreamingService.ServiceInfo.MediaCapability.VIDEO;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.comments.CommentsExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.kiosk.KioskList;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.niconico.extractors.NiconicoSearchExtractor;
import org.schabi.newpipe.extractor.services.niconico.extractors.NiconicoStreamExtractor;
import org.schabi.newpipe.extractor.services.niconico.extractors.NiconicoSuggestionExtractor;
import org.schabi.newpipe.extractor.services.niconico.extractors.NiconicoTrendExtractor;
import org.schabi.newpipe.extractor.services.niconico.extractors.NiconicoUserExtractor;
import org.schabi.newpipe.extractor.services.niconico.linkHandler.NiconicoSearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.services.niconico.linkHandler.NiconicoStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.niconico.linkHandler.NiconicoTrendLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.niconico.linkHandler.NiconicoUserLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor;
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor;

import java.util.Collections;

public class NiconicoService extends StreamingService {
    public NiconicoService(final int id)
    {
        super(id, "Niconico", Collections.singletonList(VIDEO));
    }
    public static final String BASE_URL = "https://www.nicovideo.jp";
    public static final String USER_URL = "https://www.nicovideo.jp/user/";
    public static final String CHANNEL_URL = "https://ch.nicovideo.jp/";
    public static final String DAILY_TREND_URL = "https://www.nicovideo.jp/ranking/genre/all?term=24h&rss=2.0";
    public static final String SUGGESTION_URL = "https://sug.search.nicovideo.jp/suggestion/expand/";
    public static final String RELATION_URL = "https://flapi.nicovideo.jp/api/getrelation?video=";
    public static final String TRENDING_RSS_STR = "^第\\d+位：(.*)$";
    public static final String SMILEVIDEO = "(nicovideo\\.jp\\/watch|nico\\.ms)\\/((?:sm|so)\\d+)(.+)?";
    public static final String USER_UPLOAD_LIST = "(?:www|sp).nicovideo.jp/user/(\\d+)(?:/video)?";
    public static final String APP_NAME = "NewPipe";
    // generally, Niconico uses Japanese, but some videos have multiple language texts.
    // Use ja-JP locale to get original information of video.
    public static final Localization LOCALE = Localization.fromLocalizationCode("ja-JP");

    @Override
    public String getBaseUrl()
    {
        return BASE_URL;
    }

    @Override
    public LinkHandlerFactory getStreamLHFactory() {
        return new NiconicoStreamLinkHandlerFactory();
    }

    @Override
    public ListLinkHandlerFactory getChannelLHFactory() {
        return new NiconicoUserLinkHandlerFactory();
    }

    @Override
    public ListLinkHandlerFactory getPlaylistLHFactory() {
        return null;
    }

    @Override
    public SearchQueryHandlerFactory getSearchQHFactory() {
        return new NiconicoSearchQueryHandlerFactory();
    }

    @Override
    public ListLinkHandlerFactory getCommentsLHFactory() {
        return null;
    }

    @Override
    public SearchExtractor getSearchExtractor(SearchQueryHandler queryHandler) {
        return new NiconicoSearchExtractor(this, queryHandler);
    }

    @Override
    public SuggestionExtractor getSuggestionExtractor() {
        return new NiconicoSuggestionExtractor(this);
    }

    @Override
    public SubscriptionExtractor getSubscriptionExtractor() {
        return null;
    }

    @Override
    public KioskList getKioskList() throws ExtractionException {
        final KioskList.KioskExtractorFactory kioskFactory = (streamingService, url, id) ->
                new NiconicoTrendExtractor(this,
                        new NiconicoTrendLinkHandlerFactory().fromUrl(url), id);

        KioskList kioskList = new KioskList(this);

        final NiconicoTrendLinkHandlerFactory h = new NiconicoTrendLinkHandlerFactory();

        try {
            kioskList.addKioskEntry(kioskFactory, h, "Trending");
            kioskList.setDefaultKiosk("Trending");
        } catch (Exception e) {
            throw new ExtractionException(e);
        }

        return kioskList;
    }

    @Override
    public ChannelExtractor getChannelExtractor(ListLinkHandler linkHandler) throws ExtractionException {
        return new NiconicoUserExtractor(this, linkHandler);
    }

    @Override
    public PlaylistExtractor getPlaylistExtractor(ListLinkHandler linkHandler) throws ExtractionException {
        return null;
    }

    @Override
    public StreamExtractor getStreamExtractor(LinkHandler linkHandler) throws ExtractionException {
        return new NiconicoStreamExtractor(this, linkHandler);
    }

    @Override
    public CommentsExtractor getCommentsExtractor(ListLinkHandler linkHandler) throws ExtractionException {
        return null;
    }
}
