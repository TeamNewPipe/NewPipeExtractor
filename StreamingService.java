package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.search.SearchEngine;
import org.schabi.newpipe.extractor.stream.StreamExtractor;

import java.io.IOException;

public abstract class StreamingService {
    public class ServiceInfo {
        public String name = "";
    }

    public enum LinkType {
        NONE,
        STREAM,
        CHANNEL,
        PLAYLIST
    }

    private int serviceId;

    public StreamingService(int id) {
        serviceId = id;
    }

    public abstract ServiceInfo getServiceInfo();
    public abstract StreamExtractor getExtractorInstance(String url) throws IOException, ExtractionException;
    public abstract SearchEngine getSearchEngineInstance();
    public abstract UrlIdHandler getStreamUrlIdHandlerInstance();
    public abstract UrlIdHandler getChannelUrlIdHandlerInstance();
    public abstract UrlIdHandler getPlaylistUrlIdHandlerInstance();
    public abstract ChannelExtractor getChannelExtractorInstance(String url) throws ExtractionException, IOException;
    public abstract PlaylistExtractor getPlaylistExtractorInstance(String url) throws ExtractionException, IOException;
    public abstract SuggestionExtractor getSuggestionExtractorInstance();


    public final int getServiceId() {
        return serviceId;
    }

    /**
     * figure out where the link is pointing to (a channel, video, playlist, etc.)
     */
    public final LinkType getLinkTypeByUrl(String url) {
        UrlIdHandler sH = getStreamUrlIdHandlerInstance();
        UrlIdHandler cH = getChannelUrlIdHandlerInstance();
        UrlIdHandler pH = getPlaylistUrlIdHandlerInstance();

        if (sH.acceptUrl(url)) {
            return LinkType.STREAM;
        } else if (cH.acceptUrl(url)) {
            return LinkType.CHANNEL;
        } else if (pH.acceptUrl(url)) {
            return LinkType.PLAYLIST;
        } else {
            return LinkType.NONE;
        }
    }
}
