package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.channel.FeedExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.kiosk.KioskList;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.search.SearchEngine;
import org.schabi.newpipe.extractor.stream.StreamExtractor;

import java.io.IOException;

public abstract class StreamingService {

    public class ServiceInfo {
        public final String name;

        public ServiceInfo(String name) {
            this.name = name;
        }
    }

    public enum LinkType {
        NONE,
        STREAM,
        CHANNEL,
        PLAYLIST,
        CHANNEL_FEED
    }

    private final int serviceId;
    private final ServiceInfo serviceInfo;

    public StreamingService(int id, String name) {
        this.serviceId = id;
        this.serviceInfo = new ServiceInfo(name);
    }

    public final int getServiceId() {
        return serviceId;
    }

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    public abstract UrlIdHandler getStreamUrlIdHandler();
    public abstract UrlIdHandler getChannelUrlIdHandler();
    public abstract UrlIdHandler getPlaylistUrlIdHandler();
    public abstract UrlIdHandler getFeedUrlIdHandler();
    public abstract SearchEngine getSearchEngine();
    public abstract SuggestionExtractor getSuggestionExtractor();
    public abstract StreamExtractor getStreamExtractor(String url) throws IOException, ExtractionException;
    public abstract ChannelExtractor getChannelExtractor(String url, String nextStreamsUrl) throws IOException, ExtractionException;
    public abstract FeedExtractor getFeedExtractor(String url, String nextStreamsUrl) throws IOException, ExtractionException;
    public abstract PlaylistExtractor getPlaylistExtractor(String url, String nextStreamsUrl) throws IOException, ExtractionException;
    public abstract KioskList getKioskList() throws ExtractionException;

    public FeedExtractor getFeedExtractor(String url) throws IOException, ExtractionException {
        return getFeedExtractor(url, null);
    }

    public ChannelExtractor getChannelExtractor(String url) throws IOException, ExtractionException {
        return getChannelExtractor(url, null);
    }

    public PlaylistExtractor getPlaylistExtractor(String url) throws IOException, ExtractionException {
        return getPlaylistExtractor(url, null);
    }

    /**
     * figure out where the link is pointing to (a channel, video, playlist, etc.)
     */
    public final LinkType getLinkTypeByUrl(String url) {
        UrlIdHandler sH = getStreamUrlIdHandler();
        UrlIdHandler cH = getChannelUrlIdHandler();
        UrlIdHandler pH = getPlaylistUrlIdHandler();
        UrlIdHandler fH = getFeedUrlIdHandler();

        if (sH.acceptUrl(url)) {
            return LinkType.STREAM;
        } else if (cH.acceptUrl(url)) {
            return LinkType.CHANNEL;
        } else if (pH.acceptUrl(url)) {
            return LinkType.PLAYLIST;
        } else if (fH.acceptUrl(url)) {
            return LinkType.CHANNEL_FEED;
        } else {
            return LinkType.NONE;
        }
    }
}
