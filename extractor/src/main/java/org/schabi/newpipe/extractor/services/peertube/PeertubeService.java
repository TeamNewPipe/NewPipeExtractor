package org.schabi.newpipe.extractor.services.peertube;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.comments.CommentsExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.kiosk.KioskList;
import org.schabi.newpipe.extractor.linkhandler.*;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.peertube.extractors.*;
import org.schabi.newpipe.extractor.services.peertube.linkHandler.*;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor;
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor;

import static java.util.Arrays.asList;
import static org.schabi.newpipe.extractor.StreamingService.ServiceInfo.MediaCapability.COMMENTS;
import static org.schabi.newpipe.extractor.StreamingService.ServiceInfo.MediaCapability.VIDEO;

public class PeertubeService extends StreamingService {

    private PeertubeInstance instance;

    public PeertubeService(int id) {
        this(id, PeertubeInstance.defaultInstance);
    }

    public PeertubeService(int id, PeertubeInstance instance) {
        super(id, "PeerTube", asList(VIDEO, COMMENTS));
        this.instance = instance;
    }

    @Override
    public LinkHandlerFactory getStreamLHFactory() {
        return PeertubeStreamLinkHandlerFactory.getInstance();
    }

    @Override
    public ListLinkHandlerFactory getChannelLHFactory() {
        return PeertubeChannelLinkHandlerFactory.getInstance();
    }

    @Override
    public ListLinkHandlerFactory getPlaylistLHFactory() {
        return PeertubePlaylistLinkHandlerFactory.getInstance();
    }

    @Override
    public SearchQueryHandlerFactory getSearchQHFactory() {
        return PeertubeSearchQueryHandlerFactory.getInstance();
    }

    @Override
    public ListLinkHandlerFactory getCommentsLHFactory() {
        return PeertubeCommentsLinkHandlerFactory.getInstance();
    }

    @Override
    public SearchExtractor getSearchExtractor(SearchQueryHandler queryHandler) {
        return new PeertubeSearchExtractor(this, queryHandler);
    }

    @Override
    public SuggestionExtractor getSuggestionExtractor() {
        return new PeertubeSuggestionExtractor(this);
    }

    @Override
    public SubscriptionExtractor getSubscriptionExtractor() {
        return null;
    }

    @Override
    public ChannelExtractor getChannelExtractor(ListLinkHandler linkHandler)
            throws ExtractionException {

        if (linkHandler.getUrl().contains("/video-channels/")) {
            return new PeertubeChannelExtractor(this, linkHandler);
        } else {
            return new PeertubeAccountExtractor(this, linkHandler);
        }
    }

    @Override
    public PlaylistExtractor getPlaylistExtractor(ListLinkHandler linkHandler)
            throws ExtractionException {
        return new PeertubePlaylistExtractor(this, linkHandler);
    }

    @Override
    public StreamExtractor getStreamExtractor(LinkHandler linkHandler)
            throws ExtractionException {
        return new PeertubeStreamExtractor(this, linkHandler);
    }

    @Override
    public CommentsExtractor getCommentsExtractor(ListLinkHandler linkHandler)
            throws ExtractionException {
        return new PeertubeCommentsExtractor(this, linkHandler);
    }

    @Override
    public String getBaseUrl() {
        return instance.getUrl();
    }

    public PeertubeInstance getInstance() {
        return this.instance;
    }

    public void setInstance(PeertubeInstance instance) {
        this.instance = instance;
    }

    @Override
    public KioskList getKioskList() throws ExtractionException {
        KioskList.KioskExtractorFactory kioskFactory = new KioskList.KioskExtractorFactory() {
            @Override
            public KioskExtractor createNewKiosk(StreamingService streamingService,
                                                 String url,
                                                 String id)
                    throws ExtractionException {
                return new PeertubeTrendingExtractor(PeertubeService.this,
                        new PeertubeTrendingLinkHandlerFactory().fromId(id), id);
            }
        };

        KioskList list = new KioskList(this);

        // add kiosks here e.g.:
        final PeertubeTrendingLinkHandlerFactory h = new PeertubeTrendingLinkHandlerFactory();
        try {
            list.addKioskEntry(kioskFactory, h, PeertubeTrendingLinkHandlerFactory.KIOSK_TRENDING);
            list.addKioskEntry(kioskFactory, h, PeertubeTrendingLinkHandlerFactory.KIOSK_MOST_LIKED);
            list.addKioskEntry(kioskFactory, h, PeertubeTrendingLinkHandlerFactory.KIOSK_RECENT);
            list.addKioskEntry(kioskFactory, h, PeertubeTrendingLinkHandlerFactory.KIOSK_LOCAL);
            list.setDefaultKiosk(PeertubeTrendingLinkHandlerFactory.KIOSK_TRENDING);
        } catch (Exception e) {
            throw new ExtractionException(e);
        }

        return list;
    }


}
