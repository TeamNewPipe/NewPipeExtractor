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
import org.schabi.newpipe.extractor.utils.Localization;

/*
 * Copyright (C) Christian Schabesberger 2018 <chris.schabesberger@mailbox.org>
 * StreamingService.java is part of NewPipe.
 *
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

public abstract class StreamingService {

    /**
     * This class holds meta information about the service implementation.
     */
    public static class ServiceInfo {
        private final String name;
        private final List<MediaCapability> mediaCapabilities;

        /**
         * Creates a new instance of a ServiceInfo
         * @param name the name of the service
         * @param mediaCapabilities the type of media this service can handle
         */
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
            AUDIO, VIDEO, LIVE, COMMENTS
        }
    }

    /**
     * LinkType will be used to determine which type of URL you are handling, and therefore which part
     * of NewPipe should handle a certain URL.
     */
    public enum LinkType {
        NONE,
        STREAM,
        CHANNEL,
        PLAYLIST
    }

    private final int serviceId;
    private final ServiceInfo serviceInfo;

    /**
     * Creates a new Streaming service.
     * If you Implement one do not set id within your implementation of this extractor, instead
     * set the id when you put the extractor into
     * <a href="https://teamnewpipe.github.io/NewPipeExtractor/javadoc/org/schabi/newpipe/extractor/ServiceList.html">ServiceList</a>.
     * All other parameters can be set directly from the overriding constructor.
     * @param id the number of the service to identify him within the NewPipe frontend
     * @param name the name of the service
     * @param capabilities the type of media this service can handle
     */
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

    /**
     * Must return a new instance of an implementation of LinkHandlerFactory for streams.
     * @return an instance of a LinkHandlerFactory for streams
     */
    public abstract LinkHandlerFactory getStreamLHFactory();

    /**
     * Must return a new instance of an implementation of ListLinkHandlerFactory for channels.
     * If support for channels is not given null must be returned.
     * @return an instance of a ListLinkHandlerFactory for channels or null
     */
    public abstract ListLinkHandlerFactory getChannelLHFactory();

    /**
     * Must return a new instance of an implementation of ListLinkHandlerFactory for playlists.
     * If support for playlists is not given null must be returned.
     * @return an instance of a ListLinkHandlerFactory for playlists or null
     */
    public abstract ListLinkHandlerFactory getPlaylistLHFactory();

    /**
     * Must return an instance of an implementation of SearchQueryHandlerFactory.
     * @return an instance of a SearchQueryHandlerFactory
     */
    public abstract SearchQueryHandlerFactory getSearchQHFactory();
    public abstract ListLinkHandlerFactory getCommentsLHFactory();


    ////////////////////////////////////////////
    // Extractor
    ////////////////////////////////////////////

    /**
     * Must create a new instance of a SearchExtractor implementation.
     * @param queryHandler specifies the keyword lock for, and the filters which should be applied.
     * @param localization specifies the language/country for the extractor.
     * @return a new SearchExtractor instance
     */
    public abstract SearchExtractor getSearchExtractor(SearchQueryHandler queryHandler, Localization localization);

    /**
     * Must create a new instance of a SuggestionExtractor implementation.
     * @param localization specifies the language/country for the extractor.
     * @return a new SuggestionExtractor instance
     */
    public abstract SuggestionExtractor getSuggestionExtractor(Localization localization);

    /**
     * Outdated or obsolete. null can be returned.
     * @return just null
     */
    public abstract SubscriptionExtractor getSubscriptionExtractor();

    /**
     * Must create a new instance of a KioskList implementation.
     * @return a new KioskList instance
     * @throws ExtractionException
     */
    public abstract KioskList getKioskList() throws ExtractionException;

    /**
     * Must create a new instance of a ChannelExtractor implementation.
     * @param linkHandler is pointing to the channel which should be handled by this new instance.
     * @param localization specifies the language used for the request.
     * @return a new ChannelExtractor
     * @throws ExtractionException
     */
    public abstract ChannelExtractor getChannelExtractor(ListLinkHandler linkHandler,
                                                         Localization localization) throws ExtractionException;

    /**
     * Must crete a new instance of a PlaylistExtractor implementation.
     * @param linkHandler is pointing to the playlist which should be handled by this new instance.
     * @param localization specifies the language used for the request.
     * @return a new PlaylistExtractor
     * @throws ExtractionException
     */
    public abstract PlaylistExtractor getPlaylistExtractor(ListLinkHandler linkHandler,
                                                           Localization localization) throws ExtractionException;

    /**
     * Must create a new instance of a StreamExtractor implementation.
     * @param linkHandler is pointing to the stream which should be handled by this new instance.
     * @param localization specifies the language used for the request.
     * @return a new StreamExtractor
     * @throws ExtractionException
     */
    public abstract StreamExtractor getStreamExtractor(LinkHandler linkHandler,
                                                       Localization localization) throws ExtractionException;
    public abstract CommentsExtractor getCommentsExtractor(ListLinkHandler linkHandler,
                                                           Localization localization) throws ExtractionException;
    ////////////////////////////////////////////
    // Extractor with default localization
    ////////////////////////////////////////////

    public SearchExtractor getSearchExtractor(SearchQueryHandler queryHandler) {
        return getSearchExtractor(queryHandler, NewPipe.getPreferredLocalization());
    }

    public SuggestionExtractor getSuggestionExtractor() {
        return getSuggestionExtractor(NewPipe.getPreferredLocalization());
    }

    public ChannelExtractor getChannelExtractor(ListLinkHandler linkHandler) throws ExtractionException {
        return getChannelExtractor(linkHandler, NewPipe.getPreferredLocalization());
    }
    
    public PlaylistExtractor getPlaylistExtractor(ListLinkHandler linkHandler) throws ExtractionException {
        return getPlaylistExtractor(linkHandler, NewPipe.getPreferredLocalization());
    }

    public StreamExtractor getStreamExtractor(LinkHandler linkHandler) throws ExtractionException {
        return getStreamExtractor(linkHandler, NewPipe.getPreferredLocalization());
    }
    
    public CommentsExtractor getCommentsExtractor(ListLinkHandler urlIdHandler) throws ExtractionException {
        return getCommentsExtractor(urlIdHandler, NewPipe.getPreferredLocalization());
    }

    ////////////////////////////////////////////
    // Extractor without link handler
    ////////////////////////////////////////////

    public SearchExtractor getSearchExtractor(String query,
                                              List<String> contentFilter,
                                              String sortFilter,
                                              Localization localization) throws ExtractionException {
        return getSearchExtractor(getSearchQHFactory()
                .fromQuery(query,
                        contentFilter,
                        sortFilter),
                localization);
    }

    public ChannelExtractor getChannelExtractor(String id,
                                                List<String> contentFilter,
                                                String sortFilter,
                                                Localization localization) throws ExtractionException {
        return getChannelExtractor(getChannelLHFactory().fromQuery(id, contentFilter, sortFilter), localization);
    }

    public PlaylistExtractor getPlaylistExtractor(String id,
                                                  List<String> contentFilter,
                                                  String sortFilter,
                                                  Localization localization) throws ExtractionException {
        return getPlaylistExtractor(getPlaylistLHFactory()
                .fromQuery(id,
                        contentFilter,
                        sortFilter),
                localization);
    }

    ////////////////////////////////////////////
    // Short extractor without localization
    ////////////////////////////////////////////

    public SearchExtractor getSearchExtractor(String query) throws ExtractionException {
        return getSearchExtractor(getSearchQHFactory().fromQuery(query), NewPipe.getPreferredLocalization());
    }

    public ChannelExtractor getChannelExtractor(String url) throws ExtractionException {
        return getChannelExtractor(getChannelLHFactory().fromUrl(url), NewPipe.getPreferredLocalization());
    }

    public PlaylistExtractor getPlaylistExtractor(String url) throws ExtractionException {
        return getPlaylistExtractor(getPlaylistLHFactory().fromUrl(url), NewPipe.getPreferredLocalization());
    }

    public StreamExtractor getStreamExtractor(String url) throws ExtractionException {
        return getStreamExtractor(getStreamLHFactory().fromUrl(url), NewPipe.getPreferredLocalization());
    }
    
    public CommentsExtractor getCommentsExtractor(String url) throws ExtractionException {
        ListLinkHandlerFactory llhf = getCommentsLHFactory();
        if(null == llhf) {
            return null;
        }
        return getCommentsExtractor(llhf.fromUrl(url), NewPipe.getPreferredLocalization());
    }


    /**
     * Figures out where the link is pointing to (a channel, a video, a playlist, etc.)
     * @param url the url on which it should be decided of which link type it is
     * @return the link type of url
     * @throws ParsingException
     */
    public final LinkType getLinkTypeByUrl(String url) throws ParsingException {
        LinkHandlerFactory sH = getStreamLHFactory();
        LinkHandlerFactory cH = getChannelLHFactory();
        LinkHandlerFactory pH = getPlaylistLHFactory();

        if (sH != null && sH.acceptUrl(url)) {
            return LinkType.STREAM;
        } else if (cH != null && cH.acceptUrl(url)) {
            return LinkType.CHANNEL;
        } else if (pH != null && pH.acceptUrl(url)) {
            return LinkType.PLAYLIST;
        } else {
            return LinkType.NONE;
        }
    }
}
