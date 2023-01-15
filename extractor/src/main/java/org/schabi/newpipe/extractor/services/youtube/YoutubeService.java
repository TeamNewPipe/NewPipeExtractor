package org.schabi.newpipe.extractor.services.youtube;

import static org.schabi.newpipe.extractor.StreamingService.ServiceInfo.MediaCapability.AUDIO;
import static org.schabi.newpipe.extractor.StreamingService.ServiceInfo.MediaCapability.COMMENTS;
import static org.schabi.newpipe.extractor.StreamingService.ServiceInfo.MediaCapability.LIVE;
import static org.schabi.newpipe.extractor.StreamingService.ServiceInfo.MediaCapability.VIDEO;
import static java.util.Arrays.asList;

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
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeChannelExtractor;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeCommentsExtractor;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeFeedExtractor;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeMixPlaylistExtractor;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeMusicSearchExtractor;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubePlaylistExtractor;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeSearchExtractor;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeSubscriptionExtractor;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeSuggestionExtractor;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeTrendingExtractor;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeCommentsLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubePlaylistLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeTrendingLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor;
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor;

import java.util.List;

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

public class YoutubeService extends StreamingService {

    public YoutubeService(final int id) {
        super(id, "YouTube", asList(AUDIO, VIDEO, LIVE, COMMENTS));
    }

    @Override
    public String getBaseUrl() {
        return "https://youtube.com";
    }

    @Override
    public LinkHandlerFactory getStreamLHFactory() {
        return YoutubeStreamLinkHandlerFactory.getInstance();
    }

    @Override
    public ListLinkHandlerFactory getChannelLHFactory() {
        return YoutubeChannelLinkHandlerFactory.getInstance();
    }

    @Override
    public ListLinkHandlerFactory getPlaylistLHFactory() {
        return YoutubePlaylistLinkHandlerFactory.getInstance();
    }

    @Override
    public SearchQueryHandlerFactory getSearchQHFactory() {
        return YoutubeSearchQueryHandlerFactory.getInstance();
    }

    @Override
    public StreamExtractor getStreamExtractor(final LinkHandler linkHandler) {
        return new YoutubeStreamExtractor(this, linkHandler);
    }

    @Override
    public ChannelExtractor getChannelExtractor(final ListLinkHandler linkHandler) {
        return new YoutubeChannelExtractor(this, linkHandler);
    }

    @Override
    public PlaylistExtractor getPlaylistExtractor(final ListLinkHandler linkHandler) {
        if (YoutubeParsingHelper.isYoutubeMixId(linkHandler.getId())) {
            return new YoutubeMixPlaylistExtractor(this, linkHandler);
        } else {
            return new YoutubePlaylistExtractor(this, linkHandler);
        }
    }

    @Override
    public SearchExtractor getSearchExtractor(final SearchQueryHandler query) {
        final List<String> contentFilters = query.getContentFilters();

        if (!contentFilters.isEmpty() && contentFilters.get(0).startsWith("music_")) {
            return new YoutubeMusicSearchExtractor(this, query);
        } else {
            return new YoutubeSearchExtractor(this, query);
        }
    }

    @Override
    public SuggestionExtractor getSuggestionExtractor() {
        return new YoutubeSuggestionExtractor(this);
    }

    @Override
    public KioskList getKioskList() throws ExtractionException {
        final KioskList list = new KioskList(this);

        // add kiosks here e.g.:
        try {
            list.addKioskEntry(
                    (streamingService, url, id) -> new YoutubeTrendingExtractor(
                            YoutubeService.this,
                            new YoutubeTrendingLinkHandlerFactory().fromUrl(url),
                            id
                    ),
                    new YoutubeTrendingLinkHandlerFactory(),
                    "Trending"
            );
            list.setDefaultKiosk("Trending");
        } catch (final Exception e) {
            throw new ExtractionException(e);
        }

        return list;
    }

    @Override
    public SubscriptionExtractor getSubscriptionExtractor() {
        return new YoutubeSubscriptionExtractor(this);
    }

    @Nonnull
    @Override
    public FeedExtractor getFeedExtractor(final String channelUrl) throws ExtractionException {
        return new YoutubeFeedExtractor(this, getChannelLHFactory().fromUrl(channelUrl));
    }

    @Override
    public ListLinkHandlerFactory getCommentsLHFactory() {
        return YoutubeCommentsLinkHandlerFactory.getInstance();
    }

    @Override
    public CommentsExtractor getCommentsExtractor(final ListLinkHandler urlIdHandler)
            throws ExtractionException {
        return new YoutubeCommentsExtractor(this, urlIdHandler);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Localization
    //////////////////////////////////////////////////////////////////////////*/

    // https://www.youtube.com/picker_ajax?action_language_json=1
    private static final List<Localization> SUPPORTED_LANGUAGES = Localization.listFrom(
            "en-GB"
            /*"af", "am", "ar", "az", "be", "bg", "bn", "bs", "ca", "cs", "da", "de",
            "el", "en", "en-GB", "es", "es-419", "es-US", "et", "eu", "fa", "fi", "fil", "fr",
            "fr-CA", "gl", "gu", "hi", "hr", "hu", "hy", "id", "is", "it", "iw", "ja",
            "ka", "kk", "km", "kn", "ko", "ky", "lo", "lt", "lv", "mk", "ml", "mn",
            "mr", "ms", "my", "ne", "nl", "no", "pa", "pl", "pt", "pt-PT", "ro", "ru",
            "si", "sk", "sl", "sq", "sr", "sr-Latn", "sv", "sw", "ta", "te", "th", "tr",
            "uk", "ur", "uz", "vi", "zh-CN", "zh-HK", "zh-TW", "zu"*/
    );

    // https://www.youtube.com/picker_ajax?action_country_json=1
    private static final List<ContentCountry> SUPPORTED_COUNTRIES = ContentCountry.listFrom(
            "DZ", "AR", "AU", "AT", "AZ", "BH", "BD", "BY", "BE", "BO", "BA", "BR", "BG", "KH",
            "CA", "CL", "CO", "CR", "HR", "CY", "CZ", "DK", "DO", "EC", "EG", "SV", "EE", "FI",
            "FR", "GE", "DE", "GH", "GR", "GT", "HN", "HK", "HU", "IS", "IN", "ID", "IQ", "IE",
            "IL", "IT", "JM", "JP", "JO", "KZ", "KE", "KW", "LA", "LV", "LB", "LY", "LI", "LT",
            "LU", "MY", "MT", "MX", "ME", "MA", "NP", "NL", "NZ", "NI", "NG", "MK", "NO", "OM",
            "PK", "PA", "PG", "PY", "PE", "PH", "PL", "PT", "PR", "QA", "RO", "RU", "SA", "SN",
            "RS", "SG", "SK", "SI", "ZA", "KR", "ES", "LK", "SE", "CH", "TW", "TZ", "TH", "TN",
            "TR", "UG", "UA", "AE", "GB", "US", "UY", "VE", "VN", "YE", "ZW"
    );

    @Override
    public List<Localization> getSupportedLocalizations() {
        return SUPPORTED_LANGUAGES;
    }

    @Override
    public List<ContentCountry> getSupportedCountries() {
        return SUPPORTED_COUNTRIES;
    }
}
