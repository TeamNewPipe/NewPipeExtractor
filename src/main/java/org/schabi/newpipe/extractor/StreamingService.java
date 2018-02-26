package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.kiosk.KioskList;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.search.SearchEngine;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public abstract class StreamingService {
    public static class ServiceInfo {
        private final String name;
        private final List<MediaCapability> mediaCapabilities;

        public ServiceInfo(String name, List<MediaCapability> mediaCapabilities) {
            this.name = name;
            this.mediaCapabilities = Collections.unmodifiableList(mediaCapabilities);
        }

        public String getName() {
            return name;
        }

        public List<MediaCapability> getMediaCapabilities() {
            return mediaCapabilities;
        }

        public enum MediaCapability {
            AUDIO, VIDEO, LIVE
        }
    }

    public enum LinkType {
        NONE,
        STREAM,
        CHANNEL,
        PLAYLIST
    }

    private final int serviceId;
    private final ServiceInfo serviceInfo;

    public StreamingService(int id, String name, List<ServiceInfo.MediaCapability> capabilities) {
        this.serviceId = id;
        this.serviceInfo = new ServiceInfo(name, capabilities);
    }

    public final int getServiceId() {
        return serviceId;
    }

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    @Override
    public String toString() {
        return serviceId + ":" + serviceInfo.getName();
    }

    public abstract UrlIdHandler getStreamUrlIdHandler();
    public abstract UrlIdHandler getChannelUrlIdHandler();
    public abstract UrlIdHandler getPlaylistUrlIdHandler();
    public abstract SearchEngine getSearchEngine();
    public abstract SuggestionExtractor getSuggestionExtractor();
    public abstract StreamExtractor getStreamExtractor(String url) throws ExtractionException;
    public abstract KioskList getKioskList() throws ExtractionException;
    public abstract ChannelExtractor getChannelExtractor(String url) throws ExtractionException;
    public abstract PlaylistExtractor getPlaylistExtractor(String url) throws ExtractionException;
    public abstract SubscriptionExtractor getSubscriptionExtractor();

    /**
     * figure out where the link is pointing to (a channel, video, playlist, etc.)
     */
    public final LinkType getLinkTypeByUrl(String url) {
        UrlIdHandler sH = getStreamUrlIdHandler();
        UrlIdHandler cH = getChannelUrlIdHandler();
        UrlIdHandler pH = getPlaylistUrlIdHandler();

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
