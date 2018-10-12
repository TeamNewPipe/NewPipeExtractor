package org.schabi.newpipe.extractor.services.peertube;

import static java.util.Collections.singletonList;
import static org.schabi.newpipe.extractor.StreamingService.ServiceInfo.MediaCapability.VIDEO;

import java.io.IOException;
import java.util.List;

import org.jsoup.helper.StringUtil;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.StreamingService.ServiceInfo.MediaCapability;
import org.schabi.newpipe.extractor.SuggestionExtractor;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.comments.CommentsExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.kiosk.KioskList;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeChannelExtractor;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeCommentsExtractor;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeSearchExtractor;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeStreamExtractor;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeSuggestionExtractor;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeTrendingExtractor;
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeChannelLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeCommentsLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeSearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeTrendingLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor;
import org.schabi.newpipe.extractor.utils.Localization;

public class PeertubeService extends StreamingService {
    
    private PeertubeInstance instance;
    
    public PeertubeService(int id) {
        this(id, PeertubeInstance.defaultInstance);
    }
    
    public PeertubeService(int id, PeertubeInstance instance) {
        super(id, instance.getName(), singletonList(VIDEO));
        this.instance  = instance;
    }

    public PeertubeService(int id, String name, List<MediaCapability> capabilities) {
        super(id, name, capabilities);
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
        // TODO Auto-generated method stub
        return null;
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
    public SearchExtractor getSearchExtractor(SearchQueryHandler queryHandler, Localization localization) {
        return new PeertubeSearchExtractor(this, queryHandler, localization);
    }

    @Override
    public SuggestionExtractor getSuggestionExtractor(Localization localization) {
        return new PeertubeSuggestionExtractor(this.getServiceId(), localization);
    }

    @Override
    public SubscriptionExtractor getSubscriptionExtractor() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KioskList getKioskList(Localization localization) throws ExtractionException {
        KioskList.KioskExtractorFactory kioskFactory = new KioskList.KioskExtractorFactory() {
            @Override
            public KioskExtractor createNewKiosk(StreamingService streamingService,
                                                 String url,
                                                 String id,
                                                 Localization local)
                    throws ExtractionException {
                return new PeertubeTrendingExtractor(PeertubeService.this,
                        new PeertubeTrendingLinkHandlerFactory().fromId(id), id, local);
            }
        };

        KioskList list = new KioskList(getServiceId(), localization);

        // add kiosks here e.g.:
        final PeertubeTrendingLinkHandlerFactory h = new PeertubeTrendingLinkHandlerFactory();
        try {
            list.addKioskEntry(kioskFactory, h, PeertubeTrendingLinkHandlerFactory.KIOSK_TRENDING);
            list.addKioskEntry(kioskFactory, h, PeertubeTrendingLinkHandlerFactory.KIOSK_RECENT);
            list.addKioskEntry(kioskFactory, h, PeertubeTrendingLinkHandlerFactory.KIOSK_LOCAL);
            list.setDefaultKiosk(PeertubeTrendingLinkHandlerFactory.KIOSK_TRENDING);
        } catch (Exception e) {
            throw new ExtractionException(e);
        }

        return list;
    }

    @Override
    public ChannelExtractor getChannelExtractor(ListLinkHandler linkHandler, Localization localization)
            throws ExtractionException {
        return new PeertubeChannelExtractor(this, linkHandler, localization);
    }

    @Override
    public PlaylistExtractor getPlaylistExtractor(ListLinkHandler linkHandler, Localization localization)
            throws ExtractionException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StreamExtractor getStreamExtractor(LinkHandler linkHandler, Localization localization)
            throws ExtractionException {
        return new PeertubeStreamExtractor(this, linkHandler, localization);
    }

    @Override
    public CommentsExtractor getCommentsExtractor(ListLinkHandler linkHandler, Localization localization)
            throws ExtractionException {
        return new PeertubeCommentsExtractor(this, linkHandler, localization);
    }

    @Override
    public boolean isCommentsSupported() {
        return true;
    }

    @Override
    public String getBaseUrl() {
        return instance.getUrl();
    }
    
    public void setInstance(String url) throws IOException {
        this.instance = new PeertubeInstance(url);
        if(!StringUtil.isBlank(instance.getName())) {
            this.getServiceInfo().setName(instance.getName());
        }else {
            this.getServiceInfo().setName("PeerTube");
        }
    }
    

}
