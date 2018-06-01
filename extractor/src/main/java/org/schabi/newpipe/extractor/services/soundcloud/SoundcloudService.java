package org.schabi.newpipe.extractor.services.soundcloud;

import org.schabi.newpipe.extractor.ListUrlIdHandler;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.SuggestionExtractor;
import org.schabi.newpipe.extractor.UrlIdHandler;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.kiosk.KioskList;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.search.SearchEngine;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.search.SearchQueryUrlHandler;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor;

import static java.util.Collections.singletonList;
import static org.schabi.newpipe.extractor.StreamingService.ServiceInfo.MediaCapability.AUDIO;

public class SoundcloudService extends StreamingService {

    public SoundcloudService(int id) {
        super(id, "SoundCloud", singletonList(AUDIO));
    }

    @Override
    public SearchEngine getSearchEngine() {
        return new SoundcloudSearchEngine(getServiceId());
    }

    @Override
    public SearchExtractor getSearchExtractor(SearchQueryUrlHandler queryHandler, String contentCountry) {
        return new SoundcloudSearchExtractor(this, queryHandler, contentCountry);
    }

    @Override
    public SearchQueryUrlHandler getSearchQueryHandler() {
        return new SoundcloudSearchQueryUrlHandler();
    }

    @Override
    public UrlIdHandler getStreamUrlIdHandler() {
        return SoundcloudStreamUrlIdHandler.getInstance();
    }

    @Override
    public ListUrlIdHandler getChannelUrlIdHandler() {
        return SoundcloudChannelUrlIdHandler.getInstance();
    }

    @Override
    public ListUrlIdHandler getPlaylistUrlIdHandler() {
        return SoundcloudPlaylistUrlIdHandler.getInstance();
    }


    @Override
    public StreamExtractor getStreamExtractor(UrlIdHandler urlIdHandler) {
        return new SoundcloudStreamExtractor(this, urlIdHandler);
    }

    @Override
    public ChannelExtractor getChannelExtractor(ListUrlIdHandler urlIdHandler) {
        return new SoundcloudChannelExtractor(this, urlIdHandler);
    }

    @Override
    public PlaylistExtractor getPlaylistExtractor(ListUrlIdHandler urlIdHandler) {
        return new SoundcloudPlaylistExtractor(this, urlIdHandler);
    }

    @Override
    public SuggestionExtractor getSuggestionExtractor() {
        return new SoundcloudSuggestionExtractor(getServiceId());
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
                        new SoundcloudChartsUrlIdHandler().setUrl(url), id);
            }
        };

        KioskList list = new KioskList(getServiceId());

        // add kiosks here e.g.:
        final SoundcloudChartsUrlIdHandler h = new SoundcloudChartsUrlIdHandler();
        try {
            list.addKioskEntry(chartsFactory, h, "Top 50");
            list.addKioskEntry(chartsFactory, h, "New & hot");
        } catch (Exception e) {
            throw new ExtractionException(e);
        }

        return list;
    }


    @Override
    public SubscriptionExtractor getSubscriptionExtractor() {
        return new SoundcloudSubscriptionExtractor(this);
    }
}
