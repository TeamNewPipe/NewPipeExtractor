package org.schabi.newpipe.extractor;

import java.util.Collections;
import java.util.List;

import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.comments.CommentsExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
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
    public abstract LinkHandlerFactory getStreamLHFactory();
    public abstract ListLinkHandlerFactory getChannelLHFactory();
    public abstract ListLinkHandlerFactory getPlaylistLHFactory();
    public abstract SearchQueryHandlerFactory getSearchQHFactory();
    public abstract ListLinkHandlerFactory getCommentsLHFactory();


    ////////////////////////////////////////////
    // Extractor
    ////////////////////////////////////////////
    public abstract SearchExtractor getSearchExtractor(SearchQueryHandler queryHandler, String contentCountry);
    public abstract SuggestionExtractor getSuggestionExtractor();
    public abstract SubscriptionExtractor getSubscriptionExtractor();
    public abstract KioskList getKioskList() throws ExtractionException;

    public abstract ChannelExtractor getChannelExtractor(ListLinkHandler urlIdHandler) throws ExtractionException;
    public abstract PlaylistExtractor getPlaylistExtractor(ListLinkHandler urlIdHandler) throws ExtractionException;
    public abstract StreamExtractor getStreamExtractor(LinkHandler UIHFactory) throws ExtractionException;
    public abstract CommentsExtractor getCommentsExtractor(ListLinkHandler urlIdHandler) throws ExtractionException;
    public abstract boolean isCommentsSupported();
    
    public SearchExtractor getSearchExtractor(String query, List<String> contentFilter, String sortFilter, String contentCountry) throws ExtractionException {
        return getSearchExtractor(getSearchQHFactory().fromQuery(query, contentFilter, sortFilter), contentCountry);
    }

    public ChannelExtractor getChannelExtractor(String id, List<String> contentFilter, String sortFilter) throws ExtractionException {
        return getChannelExtractor(getChannelLHFactory().fromQuery(id, contentFilter, sortFilter));
    }

    public PlaylistExtractor getPlaylistExtractor(String id, List<String> contentFilter, String sortFilter) throws ExtractionException {
        return getPlaylistExtractor(getPlaylistLHFactory().fromQuery(id, contentFilter, sortFilter));
    }

    public SearchExtractor getSearchExtractor(String query, String contentCountry) throws ExtractionException {
        return getSearchExtractor(getSearchQHFactory().fromQuery(query), contentCountry);
    }

    public ChannelExtractor getChannelExtractor(String url) throws ExtractionException {
        return getChannelExtractor(getChannelLHFactory().fromUrl(url));
    }

    public PlaylistExtractor getPlaylistExtractor(String url) throws ExtractionException {
        return getPlaylistExtractor(getPlaylistLHFactory().fromUrl(url));
    }

    public StreamExtractor getStreamExtractor(String url) throws ExtractionException {
        return getStreamExtractor(getStreamLHFactory().fromUrl(url));
    }
    
    public CommentsExtractor getCommentsExtractor(String url) throws ExtractionException {
        ListLinkHandlerFactory llhf = getCommentsLHFactory();
        if(null == llhf) {
            return null;
        }
        return getCommentsExtractor(llhf.fromUrl(url));
    }



 

	/**
     * figure out where the link is pointing to (a channel, video, playlist, etc.)
     */
    public final LinkType getLinkTypeByUrl(String url) throws ParsingException {
        LinkHandlerFactory sH = getStreamLHFactory();
        LinkHandlerFactory cH = getChannelLHFactory();
        LinkHandlerFactory pH = getPlaylistLHFactory();

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
