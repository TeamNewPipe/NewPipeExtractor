package org.schabi.newpipe.extractor.services.soundcloud;

import static java.util.Collections.singletonList;
import static org.schabi.newpipe.extractor.StreamingService.ServiceInfo.MediaCapability.AUDIO;

import org.schabi.newpipe.extractor.StreamingService;
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
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor;

public class SoundcloudService extends StreamingService {

    public SoundcloudService(int id) {
        super(id, "SoundCloud", singletonList(AUDIO));
    }
    
    @Override
    public String getBaseUrl() {
        return "https://soundcloud.com";
    }

    @Override
    public SearchQueryHandlerFactory getSearchQHFactory() {
        return new SoundcloudSearchQueryHandlerFactory();
    }

    @Override
    public LinkHandlerFactory getStreamLHFactory() {
        return SoundcloudStreamLinkHandlerFactory.getInstance();
    }

    @Override
    public ListLinkHandlerFactory getChannelLHFactory() {
        return SoundcloudChannelLinkHandlerFactory.getInstance();
    }

    @Override
    public ListLinkHandlerFactory getPlaylistLHFactory() {
        return SoundcloudPlaylistLinkHandlerFactory.getInstance();
    }


    @Override
    public StreamExtractor getStreamExtractor(LinkHandler LinkHandler) {
        return new SoundcloudStreamExtractor(this, LinkHandler);
    }

    @Override
    public ChannelExtractor getChannelExtractor(ListLinkHandler linkHandler) {
        return new SoundcloudChannelExtractor(this, linkHandler);
    }

    @Override
    public PlaylistExtractor getPlaylistExtractor(ListLinkHandler linkHandler) {
        return new SoundcloudPlaylistExtractor(this, linkHandler);
    }

    @Override
    public SearchExtractor getSearchExtractor(SearchQueryHandler queryHandler) {
        return new SoundcloudSearchExtractor(this, queryHandler);
    }

    @Override
    public SoundcloudSuggestionExtractor getSuggestionExtractor() {
        return new SoundcloudSuggestionExtractor(this);
    }

    @Override
    public KioskList getKioskList() throws ExtractionException {
        KioskList.KioskExtractorFactory chartsFactory = new KioskList.KioskExtractorFactory() {
            @Override
            public KioskExtractor createNewKiosk(StreamingService streamingService,
                                                 String url,
                                                 String id)
                    throws ExtractionException {
                return new SoundcloudChartsExtractor(SoundcloudService.this,
                        new SoundcloudChartsLinkHandlerFactory().fromUrl(url), id);
            }
        };

        KioskList list = new KioskList(this);

        // add kiosks here e.g.:
        final SoundcloudChartsLinkHandlerFactory h = new SoundcloudChartsLinkHandlerFactory();
        try {
            list.addKioskEntry(chartsFactory, h, "Top 50");
            list.addKioskEntry(chartsFactory, h, "New & hot");
            list.setDefaultKiosk("New & hot");
        } catch (Exception e) {
            throw new ExtractionException(e);
        }

        return list;
    }

    @Override
    public SubscriptionExtractor getSubscriptionExtractor() {
        return new SoundcloudSubscriptionExtractor(this);
    }

	@Override
	public ListLinkHandlerFactory getCommentsLHFactory() {
		return null;
	}

	@Override
    public CommentsExtractor getCommentsExtractor(ListLinkHandler linkHandler)
            throws ExtractionException {
        return null;
    }
    
}
