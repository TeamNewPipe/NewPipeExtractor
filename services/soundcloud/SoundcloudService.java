package org.schabi.newpipe.extractor.services.soundcloud;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.SuggestionExtractor;
import org.schabi.newpipe.extractor.UrlIdHandler;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.search.SearchEngine;
import org.schabi.newpipe.extractor.stream.StreamExtractor;

import java.io.IOException;

public class SoundcloudService extends StreamingService {

    public SoundcloudService(int id) {
        super(id);
    }

    @Override
    public ServiceInfo getServiceInfo() {
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.name = "Soundcloud";
        return serviceInfo;
    }

    @Override
    public StreamExtractor getStreamExtractorInstance(String url)
            throws ExtractionException, IOException {
        UrlIdHandler urlIdHandler = SoundcloudStreamUrlIdHandler.getInstance();
        if (urlIdHandler.acceptUrl(url)) {
            return new SoundcloudStreamExtractor(urlIdHandler, url, getServiceId());
        } else {
            throw new IllegalArgumentException("supplied String is not a valid Soundcloud URL");
        }
    }

    @Override
    public SearchEngine getSearchEngineInstance() {
        return new SoundcloudSearchEngine(getServiceId());
    }

    @Override
    public UrlIdHandler getStreamUrlIdHandlerInstance() {
        return SoundcloudStreamUrlIdHandler.getInstance();
    }

    @Override
    public UrlIdHandler getChannelUrlIdHandlerInstance() {
        return SoundcloudChannelUrlIdHandler.getInstance();
    }


    @Override
    public UrlIdHandler getPlaylistUrlIdHandlerInstance() {
        return SoundcloudPlaylistUrlIdHandler.getInstance();
    }

    @Override
    public ChannelExtractor getChannelExtractorInstance(String url) throws ExtractionException, IOException {
        return new SoundcloudChannelExtractor(getChannelUrlIdHandlerInstance(), url, getServiceId());
    }

    @Override
    public PlaylistExtractor getPlaylistExtractorInstance(String url) throws ExtractionException, IOException {
        return new SoundcloudPlaylistExtractor(getPlaylistUrlIdHandlerInstance(), url, getServiceId());
    }

    @Override
    public SuggestionExtractor getSuggestionExtractorInstance() {
        return new SoundcloudSuggestionExtractor(getServiceId());
    }
}
