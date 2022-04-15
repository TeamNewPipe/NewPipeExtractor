package org.schabi.newpipe.extractor.services.youtube.invidious;

import static org.schabi.newpipe.extractor.StreamingService.ServiceInfo.MediaCapability.AUDIO;
import static org.schabi.newpipe.extractor.StreamingService.ServiceInfo.MediaCapability.COMMENTS;
import static org.schabi.newpipe.extractor.StreamingService.ServiceInfo.MediaCapability.LIVE;
import static org.schabi.newpipe.extractor.StreamingService.ServiceInfo.MediaCapability.VIDEO;
import static java.util.Arrays.asList;

import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.comments.CommentsExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.feed.FeedExtractor;
import org.schabi.newpipe.extractor.kiosk.KioskList;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.localization.ContentCountry;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.youtube.YoutubeLikeStreamingService;
import org.schabi.newpipe.extractor.services.youtube.invidious.extractors.InvidiousChannelExtractor;
import org.schabi.newpipe.extractor.services.youtube.invidious.extractors.InvidiousCommentsExtractor;
import org.schabi.newpipe.extractor.services.youtube.invidious.extractors.InvidiousFeedExtractor;
import org.schabi.newpipe.extractor.services.youtube.invidious.extractors.InvidiousMixPlaylistExtractor;
import org.schabi.newpipe.extractor.services.youtube.invidious.extractors.InvidiousPlaylistExtractor;
import org.schabi.newpipe.extractor.services.youtube.invidious.extractors.InvidiousSearchExtractor;
import org.schabi.newpipe.extractor.services.youtube.invidious.extractors.InvidiousStreamExtractor;
import org.schabi.newpipe.extractor.services.youtube.invidious.extractors.InvidiousSuggestionExtractor;
import org.schabi.newpipe.extractor.services.youtube.invidious.extractors.InvidiousTrendingExtractor;
import org.schabi.newpipe.extractor.services.youtube.invidious.linkHandler.InvidiousChannelLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.invidious.linkHandler.InvidiousCommentLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.invidious.linkHandler.InvidiousPlaylistLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.invidious.linkHandler.InvidiousSearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.invidious.linkHandler.InvidiousStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.invidious.linkHandler.InvidiousTrendingLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.shared.YoutubePlaylistHelper;
import org.schabi.newpipe.extractor.services.youtube.shared.extractors.YoutubeTakeoutSubscriptionExtractor;
import org.schabi.newpipe.extractor.services.youtube.youtube.YoutubeDirectService;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor;
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor;

import java.util.List;

import javax.annotation.Nonnull;

public class InvidiousService extends YoutubeLikeStreamingService {
    protected InvidiousInstance instance;

    public InvidiousService(final int id) {
        super(id, "Invidious", asList(AUDIO, VIDEO, LIVE, COMMENTS));
    }

    public InvidiousService(final int id, final InvidiousInstance instance) {
        super(id, "Invidious", asList(AUDIO, VIDEO, LIVE, COMMENTS));
        this.instance = instance;
    }

    @Override
    public String getBaseUrl() {
        return instance.getUrl();
    }

    @Override
    public LinkHandlerFactory getStreamLHFactory() {
        return new InvidiousStreamLinkHandlerFactory(this);
    }

    @Override
    public ListLinkHandlerFactory getChannelLHFactory() {
        return new InvidiousChannelLinkHandlerFactory(this);
    }

    @Override
    public ListLinkHandlerFactory getPlaylistLHFactory() {
        return new InvidiousPlaylistLinkHandlerFactory(this);
    }

    @Override
    public SearchQueryHandlerFactory getSearchQHFactory() {
        return new InvidiousSearchQueryHandlerFactory(this);
    }

    @Override
    public ListLinkHandlerFactory getCommentsLHFactory() {
        return new InvidiousCommentLinkHandlerFactory(this);
    }

    @Override
    public SearchExtractor getSearchExtractor(final SearchQueryHandler queryHandler) {
        return new InvidiousSearchExtractor(this, queryHandler);
    }

    @Override
    public SuggestionExtractor getSuggestionExtractor() {
        return new InvidiousSuggestionExtractor(this);
    }

    @Override
    public SubscriptionExtractor getSubscriptionExtractor() {
        // Invidious provides also exports
        return new YoutubeTakeoutSubscriptionExtractor(this);
    }

    @Override
    public KioskList getKioskList() {

        final KioskList.KioskExtractorFactory kioskFactory = ((streamingService, url, id) ->
                new InvidiousTrendingExtractor(
                        InvidiousService.this,
                        new InvidiousTrendingLinkHandlerFactory(InvidiousService.this).fromId(id),
                        id
                )
        );

        final KioskList list = new KioskList(this);

        final InvidiousTrendingLinkHandlerFactory lhf =
                new InvidiousTrendingLinkHandlerFactory(InvidiousService.this);
        list.addKioskEntry(kioskFactory, lhf, InvidiousTrendingLinkHandlerFactory.KIOSK_POPULAR);
        list.addKioskEntry(kioskFactory, lhf, InvidiousTrendingLinkHandlerFactory.KIOSK_TRENDING);
        list.setDefaultKiosk(InvidiousTrendingLinkHandlerFactory.KIOSK_TRENDING);

        return list;
    }

    @Override
    public ChannelExtractor getChannelExtractor(final ListLinkHandler linkHandler) {
        return new InvidiousChannelExtractor(this, linkHandler);
    }

    @Override
    public PlaylistExtractor getPlaylistExtractor(final ListLinkHandler linkHandler) {
        return YoutubePlaylistHelper.isYoutubeMixId(linkHandler.getId())
                ? new InvidiousMixPlaylistExtractor(this, linkHandler)
                : new InvidiousPlaylistExtractor(this, linkHandler);
    }

    @Nonnull
    @Override
    public FeedExtractor getFeedExtractor(final String url) throws ExtractionException {
        return new InvidiousFeedExtractor(this, getChannelLHFactory().fromUrl(url));
    }

    @Override
    public StreamExtractor getStreamExtractor(final LinkHandler linkHandler) {
        return new InvidiousStreamExtractor(this, linkHandler);
    }

    @Override
    public CommentsExtractor getCommentsExtractor(final ListLinkHandler linkHandler) {
        return new InvidiousCommentsExtractor(this, linkHandler);
    }

    public InvidiousInstance getInstance() {
        return instance;
    }

    public void setInstance(final InvidiousInstance instance) {
        this.instance = instance;
    }

    @Override
    public List<Localization> getSupportedLocalizations() {
        return YoutubeDirectService.SUPPORTED_LANGUAGES;
    }

    @Override
    public List<ContentCountry> getSupportedCountries() {
        return YoutubeDirectService.SUPPORTED_COUNTRIES;
    }
}
