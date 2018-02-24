package org.schabi.newpipe.extractor.services.soundcloud;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.SuggestionExtractor;
import org.schabi.newpipe.extractor.UrlIdHandler;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.kiosk.KioskList;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.search.SearchEngine;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor;

import java.io.IOException;

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
    public UrlIdHandler getStreamUrlIdHandler() {
        return SoundcloudStreamUrlIdHandler.getInstance();
    }

    @Override
    public UrlIdHandler getChannelUrlIdHandler() {
        return SoundcloudChannelUrlIdHandler.getInstance();
    }

    @Override
    public UrlIdHandler getPlaylistUrlIdHandler() {
        return SoundcloudPlaylistUrlIdHandler.getInstance();
    }


    @Override
    public StreamExtractor getStreamExtractor(String url) throws IOException, ExtractionException {
        return new SoundcloudStreamExtractor(this, url);
    }

    @Override
    public ChannelExtractor getChannelExtractor(String url, String nextPageUrl) throws IOException, ExtractionException {
        return new SoundcloudChannelExtractor(this, url, nextPageUrl);
    }

    @Override
    public PlaylistExtractor getPlaylistExtractor(String url, String nextPageUrl) throws IOException, ExtractionException {
        return new SoundcloudPlaylistExtractor(this, url, nextPageUrl);
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
                                                 String nextPageUrl,
                                                 String id)
                    throws ExtractionException, IOException {
                return new SoundcloudChartsExtractor(SoundcloudService.this,
                        url,
                        nextPageUrl,
                        id);
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
