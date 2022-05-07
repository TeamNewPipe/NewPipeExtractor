package org.schabi.newpipe.extractor.services.youtube;

import static org.schabi.newpipe.extractor.StreamingService.ServiceInfo.MediaCapability.AUDIO;
import static org.schabi.newpipe.extractor.StreamingService.ServiceInfo.MediaCapability.COMMENTS;
import static org.schabi.newpipe.extractor.StreamingService.ServiceInfo.MediaCapability.LIVE;
import static org.schabi.newpipe.extractor.StreamingService.ServiceInfo.MediaCapability.VIDEO;
import static java.util.Arrays.asList;

import org.schabi.newpipe.extractor.InstanceBasedStreamingService;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.comments.CommentsExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
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
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.youtube.youtube.YoutubeInstance;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor;
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

/*
 * Created by Christian Schabesberger on 23.08.15.
 *
 * Copyright (C) Christian Schabesberger 2018 <chris.schabesberger@mailbox.org>
 * YoutubeService.java is part of NewPipe.
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

public class YoutubeService extends StreamingService
        implements InstanceBasedStreamingService<YoutubeLikeInstance<? extends StreamingService>> {

    @Nonnull
    protected YoutubeLikeInstance<? extends StreamingService> instance;
    @Nonnull
    protected YoutubeLikeStreamingService subService;

    public YoutubeService(final int id) {
        this(id, YoutubeInstance.YOUTUBE);
    }

    public YoutubeService(
            final int id,
            final YoutubeLikeInstance<? extends StreamingService> instance) {
        super(id, "YouTube", asList(AUDIO, VIDEO, LIVE, COMMENTS));
        this.setInstance(instance);
    }

    @Override
    @Nonnull
    public YoutubeLikeInstance<? extends StreamingService> getInstance() {
        return instance;
    }

    @Override
    public void setInstance(final YoutubeLikeInstance<? extends StreamingService> instance) {
        this.instance = Objects.requireNonNull(instance);
        subService = Objects.requireNonNull(instance.getNewStreamingService(getServiceId()));
    }

    @Override
    public String getBaseUrl() {
        return subService.getBaseUrl();
    }

    @Override
    public LinkHandlerFactory getStreamLHFactory() {
        return subService.getStreamLHFactory();
    }

    @Override
    public ListLinkHandlerFactory getChannelLHFactory() {
        return subService.getChannelLHFactory();
    }

    @Override
    public ListLinkHandlerFactory getPlaylistLHFactory() {
        return subService.getPlaylistLHFactory();
    }

    @Override
    public SearchQueryHandlerFactory getSearchQHFactory() {
        return subService.getSearchQHFactory();
    }

    @Override
    public StreamExtractor getStreamExtractor(final LinkHandler linkHandler) {
        return subService.getStreamExtractor(linkHandler);
    }

    @Override
    public ChannelExtractor getChannelExtractor(final ListLinkHandler linkHandler) {
        return subService.getChannelExtractor(linkHandler);
    }

    @Override
    public PlaylistExtractor getPlaylistExtractor(final ListLinkHandler linkHandler) {
        return subService.getPlaylistExtractor(linkHandler);
    }

    @Override
    public SearchExtractor getSearchExtractor(final SearchQueryHandler query) {
        return subService.getSearchExtractor(query);
    }

    @Override
    public SuggestionExtractor getSuggestionExtractor() {
        return subService.getSuggestionExtractor();
    }

    @Override
    public KioskList getKioskList() {
        return subService.getKioskList();
    }

    @Override
    public SubscriptionExtractor getSubscriptionExtractor() {
        return subService.getSubscriptionExtractor();
    }

    @Nonnull
    @Override
    public FeedExtractor getFeedExtractor(final String channelUrl) throws ExtractionException {
        return subService.getFeedExtractor(channelUrl);
    }

    @Override
    public ListLinkHandlerFactory getCommentsLHFactory() {
        return subService.getCommentsLHFactory();
    }

    @Override
    public CommentsExtractor getCommentsExtractor(final ListLinkHandler urlIdHandler)
            throws ExtractionException {
        return subService.getCommentsExtractor(urlIdHandler);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Localization
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public List<Localization> getSupportedLocalizations() {
        return subService.getSupportedLocalizations();
    }

    @Override
    public List<ContentCountry> getSupportedCountries() {
        return subService.getSupportedCountries();
    }
}
