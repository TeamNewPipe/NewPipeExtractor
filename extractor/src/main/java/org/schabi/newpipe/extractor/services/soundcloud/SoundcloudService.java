package org.schabi.newpipe.extractor.services.soundcloud;

import org.schabi.newpipe.extractor.*;
import org.schabi.newpipe.extractor.ListUIHFactory;
import org.schabi.newpipe.extractor.UIHFactory;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.kiosk.KioskList;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.search.SearchEngine;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.search.SearchQIHFactory;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor;

import static java.util.Collections.singletonList;
import static org.schabi.newpipe.extractor.StreamingService.ServiceInfo.MediaCapability.AUDIO;

public class SoundcloudService extends StreamingService {

    public SoundcloudService(int id) {
        super(id, "SoundCloud", singletonList(AUDIO));
    }

    @Override
    public SearchExtractor getSearchExtractor(SearchQIHFactory queryHandler, String contentCountry) {
        return new SoundcloudSearchExtractor(this, queryHandler, contentCountry);
    }

    @Override
    public SearchQIHFactory getSearchQIHFactory() {
        return new SoundcloudSearchQIHFactory();
    }

    @Override
    public UIHFactory getStreamUIHFactory() {
        return SoundcloudStreamUIHFactory.getInstance();
    }

    @Override
    public ListUIHFactory getChannelUIHFactory() {
        return SoundcloudChannelUIHFactory.getInstance();
    }

    @Override
    public ListUIHFactory getPlaylistUIHFactory() {
        return SoundcloudPlaylistUIHFactory.getInstance();
    }


    @Override
    public StreamExtractor getStreamExtractor(UIHFactory UIHFactory) {
        return new SoundcloudStreamExtractor(this, UIHFactory);
    }

    @Override
    public ChannelExtractor getChannelExtractor(ListUIHFactory urlIdHandler) {
        return new SoundcloudChannelExtractor(this, urlIdHandler);
    }

    @Override
    public PlaylistExtractor getPlaylistExtractor(ListUIHFactory urlIdHandler) {
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
                        new SoundcloudChartsUIHFactory().setUrl(url), id);
            }
        };

        KioskList list = new KioskList(getServiceId());

        // add kiosks here e.g.:
        final SoundcloudChartsUIHFactory h = new SoundcloudChartsUIHFactory();
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
