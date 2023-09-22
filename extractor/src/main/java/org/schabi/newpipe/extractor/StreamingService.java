package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor;
import org.schabi.newpipe.extractor.comments.CommentsExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.feed.FeedExtractor;
import org.schabi.newpipe.extractor.kiosk.KioskList;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.localization.ContentCountry;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.localization.TimeAgoPatternsManager;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor;
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/*
 * Copyright (C) 2018 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * StreamingService.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor.  If not, see <http://www.gnu.org/licenses/>.
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
        public ServiceInfo(final String name, final List<MediaCapability> mediaCapabilities) {
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
     * LinkType will be used to determine which type of URL you are handling, and therefore which
     * part of NewPipe should handle a certain URL.
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
     * set the id when you put the extractor into {@link ServiceList}
     * All other parameters can be set directly from the overriding constructor.
     * @param id the number of the service to identify him within the NewPipe frontend
     * @param name the name of the service
     * @param capabilities the type of media this service can handle
     */
    public StreamingService(final int id,
                            final String name,
                            final List<ServiceInfo.MediaCapability> capabilities) {
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

    public abstract String getBaseUrl();

    /*//////////////////////////////////////////////////////////////////////////
    // Url Id handler
    //////////////////////////////////////////////////////////////////////////*/

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
     * Must return a new instance of an implementation of ListLinkHandlerFactory for channel tabs.
     * If support for channel tabs is not given null must be returned.
     *
     * @return an instance of a ListLinkHandlerFactory for channels or null
     */
    public abstract ListLinkHandlerFactory getChannelTabLHFactory();

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

    /*//////////////////////////////////////////////////////////////////////////
    // Extractors
    //////////////////////////////////////////////////////////////////////////*/

    /**
     * Must create a new instance of a SearchExtractor implementation.
     * @param queryHandler specifies the keyword lock for, and the filters which should be applied.
     * @return a new SearchExtractor instance
     */
    public abstract SearchExtractor getSearchExtractor(SearchQueryHandler queryHandler);

    /**
     * Must create a new instance of a SuggestionExtractor implementation.
     * @return a new SuggestionExtractor instance
     */
    public abstract SuggestionExtractor getSuggestionExtractor();

    /**
     * Outdated or obsolete. null can be returned.
     * @return just null
     */
    public abstract SubscriptionExtractor getSubscriptionExtractor();

    /**
     * This method decides which strategy will be chosen to fetch the feed. In YouTube, for example,
     * a separate feed exists which is lightweight and made specifically to be used like this.
     * <p>
     * In services which there's no other way to retrieve them, null should be returned.
     *
     * @return a {@link FeedExtractor} instance or null.
     */
    @Nullable
    public FeedExtractor getFeedExtractor(final String url) throws ExtractionException {
        return null;
    }

    /**
     * Must create a new instance of a KioskList implementation.
     * @return a new KioskList instance
     */
    public abstract KioskList getKioskList() throws ExtractionException;

    /**
     * Must create a new instance of a ChannelExtractor implementation.
     * @param linkHandler is pointing to the channel which should be handled by this new instance.
     * @return a new ChannelExtractor
     */
    public abstract ChannelExtractor getChannelExtractor(ListLinkHandler linkHandler)
            throws ExtractionException;

    /**
     * Must create a new instance of a ChannelTabExtractor implementation.
     *
     * @param linkHandler is pointing to the channel which should be handled by this new instance.
     * @return a new ChannelTabExtractor
     */
    public abstract ChannelTabExtractor getChannelTabExtractor(ListLinkHandler linkHandler)
            throws ExtractionException;

    /**
     * Must crete a new instance of a PlaylistExtractor implementation.
     * @param linkHandler is pointing to the playlist which should be handled by this new instance.
     * @return a new PlaylistExtractor
     */
    public abstract PlaylistExtractor getPlaylistExtractor(ListLinkHandler linkHandler)
            throws ExtractionException;

    /**
     * Must create a new instance of a StreamExtractor implementation.
     * @param linkHandler is pointing to the stream which should be handled by this new instance.
     * @return a new StreamExtractor
     */
    public abstract StreamExtractor getStreamExtractor(LinkHandler linkHandler)
            throws ExtractionException;

    public abstract CommentsExtractor getCommentsExtractor(ListLinkHandler linkHandler)
            throws ExtractionException;

    /*//////////////////////////////////////////////////////////////////////////
    // Extractors without link handler
    //////////////////////////////////////////////////////////////////////////*/

    public SearchExtractor getSearchExtractor(final String query,
                                              final List<String> contentFilter,
                                              final String sortFilter) throws ExtractionException {
        return getSearchExtractor(getSearchQHFactory()
                .fromQuery(query, contentFilter, sortFilter));
    }

    public ChannelExtractor getChannelExtractor(final String id,
                                                final List<String> contentFilter,
                                                final String sortFilter)
            throws ExtractionException {
        return getChannelExtractor(getChannelLHFactory()
                .fromQuery(id, contentFilter, sortFilter));
    }

    public PlaylistExtractor getPlaylistExtractor(final String id,
                                                  final List<String> contentFilter,
                                                  final String sortFilter)
            throws ExtractionException {
        return getPlaylistExtractor(getPlaylistLHFactory()
                .fromQuery(id, contentFilter, sortFilter));
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Short extractors overloads
    //////////////////////////////////////////////////////////////////////////*/

    public SearchExtractor getSearchExtractor(final String query) throws ExtractionException {
        return getSearchExtractor(getSearchQHFactory().fromQuery(query));
    }

    public ChannelExtractor getChannelExtractor(final String url) throws ExtractionException {
        return getChannelExtractor(getChannelLHFactory().fromUrl(url));
    }

    public ChannelTabExtractor getChannelTabExtractorFromId(final String id, final String tab)
            throws ExtractionException {
        return getChannelTabExtractor(getChannelTabLHFactory().fromQuery(
                id, Collections.singletonList(tab), ""));
    }

    public ChannelTabExtractor getChannelTabExtractorFromIdAndBaseUrl(final String id,
                                                                      final String tab,
                                                                      final String baseUrl)
            throws ExtractionException {
        return getChannelTabExtractor(getChannelTabLHFactory().fromQuery(
                id, Collections.singletonList(tab), "", baseUrl));
    }

    public PlaylistExtractor getPlaylistExtractor(final String url) throws ExtractionException {
        return getPlaylistExtractor(getPlaylistLHFactory().fromUrl(url));
    }

    public StreamExtractor getStreamExtractor(final String url) throws ExtractionException {
        return getStreamExtractor(getStreamLHFactory().fromUrl(url));
    }

    public CommentsExtractor getCommentsExtractor(final String url) throws ExtractionException {
        final ListLinkHandlerFactory listLinkHandlerFactory = getCommentsLHFactory();
        if (listLinkHandlerFactory == null) {
            return null;
        }
        return getCommentsExtractor(listLinkHandlerFactory.fromUrl(url));
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/

    /**
     * Figures out where the link is pointing to (a channel, a video, a playlist, etc.)
     * @param url the url on which it should be decided of which link type it is
     * @return the link type of url
     */
    public final LinkType getLinkTypeByUrl(final String url) throws ParsingException {
        final String polishedUrl = Utils.followGoogleRedirectIfNeeded(url);

        final LinkHandlerFactory sH = getStreamLHFactory();
        final LinkHandlerFactory cH = getChannelLHFactory();
        final LinkHandlerFactory pH = getPlaylistLHFactory();

        if (sH != null && sH.acceptUrl(polishedUrl)) {
            return LinkType.STREAM;
        } else if (cH != null && cH.acceptUrl(polishedUrl)) {
            return LinkType.CHANNEL;
        } else if (pH != null && pH.acceptUrl(polishedUrl)) {
            return LinkType.PLAYLIST;
        } else {
            return LinkType.NONE;
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Localization
    //////////////////////////////////////////////////////////////////////////*/

    /**
     * Returns a list of localizations that this service supports.
     */
    public List<Localization> getSupportedLocalizations() {
        return Collections.singletonList(Localization.DEFAULT);
    }

    /**
     * Returns a list of countries that this service supports.<br>
     */
    public List<ContentCountry> getSupportedCountries() {
        return Collections.singletonList(ContentCountry.DEFAULT);
    }

    /**
     * Returns the localization that should be used in this service. It will get which localization
     * the user prefer (using {@link NewPipe#getPreferredLocalization()}), then it will:
     * <ul>
     * <li>Check if the exactly localization is supported by this service.</li>
     * <li>If not, check if a less specific localization is available, using only the language
     * code.</li>
     * <li>Fallback to the {@link Localization#DEFAULT default} localization.</li>
     * </ul>
     */
    public Localization getLocalization() {
        final Localization preferredLocalization = NewPipe.getPreferredLocalization();

        // Check the localization's language and country
        if (getSupportedLocalizations().contains(preferredLocalization)) {
            return preferredLocalization;
        }

        // Fallback to the first supported language that matches the preferred language
        for (final Localization supportedLanguage : getSupportedLocalizations()) {
            if (supportedLanguage.getLanguageCode()
                    .equals(preferredLocalization.getLanguageCode())) {
                return supportedLanguage;
            }
        }

        return Localization.DEFAULT;
    }

    /**
     * Returns the country that should be used to fetch content in this service. It will get which
     * country the user prefer (using {@link NewPipe#getPreferredContentCountry()}), then it will:
     * <ul>
     * <li>Check if the country is supported by this service.</li>
     * <li>If not, fallback to the {@link ContentCountry#DEFAULT default} country.</li>
     * </ul>
     */
    public ContentCountry getContentCountry() {
        final ContentCountry preferredContentCountry = NewPipe.getPreferredContentCountry();

        if (getSupportedCountries().contains(preferredContentCountry)) {
            return preferredContentCountry;
        }

        return ContentCountry.DEFAULT;
    }

    /**
     * Get an instance of the time ago parser using the patterns related to the passed localization.
     * <br><br>
     * Just like {@link #getLocalization()}, it will also try to fallback to a less specific
     * localization if the exact one is not available/supported.
     *
     * @throws IllegalArgumentException if the localization is not supported (parsing patterns are
     *                                  not present).
     */
    public TimeAgoParser getTimeAgoParser(final Localization localization) {
        final TimeAgoParser targetParser = TimeAgoPatternsManager.getTimeAgoParserFor(localization);

        if (targetParser != null) {
            return targetParser;
        }

        if (!localization.getCountryCode().isEmpty()) {
            final Localization lessSpecificLocalization
                    = new Localization(localization.getLanguageCode());
            final TimeAgoParser lessSpecificParser
                    = TimeAgoPatternsManager.getTimeAgoParserFor(lessSpecificLocalization);

            if (lessSpecificParser != null) {
                return lessSpecificParser;
            }
        }

        throw new IllegalArgumentException(
                "Localization is not supported (\"" + localization + "\")");
    }

}
