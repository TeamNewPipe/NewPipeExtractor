package org.schabi.newpipe.extractor;

import com.sun.org.apache.xerces.internal.xs.StringList;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.kiosk.KioskList;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.search.SearchEngine;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.search.SearchQueryUrlHandler;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor;

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

    ////////////////////////////////////////////
    // Url Id handler
    ////////////////////////////////////////////
    public abstract UrlIdHandler getStreamUrlIdHandler();
    public abstract ListUrlIdHandler getChannelUrlIdHandler();
    public abstract ListUrlIdHandler getPlaylistUrlIdHandler();
    public abstract SearchQueryUrlHandler getSearchQueryHandler();


    ////////////////////////////////////////////
    // Extractor
    ////////////////////////////////////////////
    public abstract SearchEngine getSearchEngine();
    public abstract SearchExtractor getSearchExtractor(SearchQueryUrlHandler queryHandler, String contentCountry);
    public abstract SuggestionExtractor getSuggestionExtractor();
    public abstract SubscriptionExtractor getSubscriptionExtractor();
    public abstract KioskList getKioskList() throws ExtractionException;

    public abstract ChannelExtractor getChannelExtractor(ListUrlIdHandler urlIdHandler) throws ExtractionException;
    public abstract PlaylistExtractor getPlaylistExtractor(ListUrlIdHandler urlIdHandler) throws ExtractionException;
    public abstract StreamExtractor getStreamExtractor(UrlIdHandler urlIdHandler) throws ExtractionException;

    public SearchExtractor getSearchExtractor(String query, List<String> contentFilter, String sortFilter, String contentCountry) throws ExtractionException {
        return getSearchExtractor(getSearchQueryHandler().setQuery(query, contentFilter, sortFilter), contentCountry);
    }

    public ChannelExtractor getChannelExtractor(String id, List<String> contentFilter, String sortFilter) throws ExtractionException {
        return getChannelExtractor(getChannelUrlIdHandler().setQuery(id, contentFilter, sortFilter));
    }

    public PlaylistExtractor getPlaylistExtractor(String id, List<String> contentFilter, String sortFilter) throws ExtractionException {
        return getPlaylistExtractor(getPlaylistUrlIdHandler().setQuery(id, contentFilter, sortFilter));
    }

    public SearchExtractor getSearchExtractor(String query, String contentCountry) throws ExtractionException {
        return getSearchExtractor(getSearchQueryHandler().setQuery(query), contentCountry);
    }

    public ChannelExtractor getChannelExtractor(String url) throws ExtractionException {
        return getChannelExtractor(getChannelUrlIdHandler().setUrl(url));
    }

    public PlaylistExtractor getPlaylistExtractor(String url) throws ExtractionException {
        return getPlaylistExtractor(getPlaylistUrlIdHandler().setUrl(url));
    }

    public StreamExtractor getStreamExtractor(String url) throws ExtractionException {
        return getStreamExtractor(getStreamUrlIdHandler().setUrl(url));
    }



    /**
     * figure out where the link is pointing to (a channel, video, playlist, etc.)
     */
    public final LinkType getLinkTypeByUrl(String url) throws ParsingException {
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
