package org.schabi.newpipe.extractor.services.youtube.extractors;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.DISABLE_PRETTY_PRINT_PARAMETER;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.YOUTUBEI_V1_URL;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getJsonPostResponse;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.ALL;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.CHANNELS;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.PLAYLISTS;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.VIDEOS;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.getSearchParameter;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonBuilder;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonWriter;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.MetaInfo;
import org.schabi.newpipe.extractor.MultiInfoItemsCollector;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.youtube.YoutubeMetaInfoHelper;
import org.schabi.newpipe.extractor.utils.JsonUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/*
 * Created by Christian Schabesberger on 22.07.2018
 *
 * Copyright (C) 2018 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * YoutubeSearchExtractor.java is part of NewPipe Extractor.
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

public class YoutubeSearchExtractor extends SearchExtractor {

    @Nullable
    private final String searchType;
    private final boolean extractVideoResults;
    private final boolean extractChannelResults;
    private final boolean extractPlaylistResults;

    private JsonObject initialData;

    public YoutubeSearchExtractor(final StreamingService service,
                                  final SearchQueryHandler linkHandler) {
        super(service, linkHandler);
        final List<String> contentFilters = linkHandler.getContentFilters();
        searchType = isNullOrEmpty(contentFilters) ? null : contentFilters.get(0);
        // Save whether we should extract video, channel and playlist results depending on the
        // requested search type, as YouTube returns sometimes videos inside channel search results
        // If no search type is provided or ALL filter is requested, extract everything
        extractVideoResults = searchType == null || ALL.equals(searchType)
                || VIDEOS.equals(searchType);
        extractChannelResults = searchType == null || ALL.equals(searchType)
                || CHANNELS.equals(searchType);
        extractPlaylistResults = searchType == null || ALL.equals(searchType)
                || PLAYLISTS.equals(searchType);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) throws IOException,
            ExtractionException {
        final String query = super.getSearchString();
        final Localization localization = getExtractorLocalization();
        final String params = getSearchParameter(searchType);

        final JsonBuilder<JsonObject> jsonBody = prepareDesktopJsonBuilder(localization,
                getExtractorContentCountry())
                .value("query", query);
        if (!isNullOrEmpty(params)) {
            jsonBody.value("params", params);
        }

        final byte[] body = JsonWriter.string(jsonBody.done()).getBytes(StandardCharsets.UTF_8);

        initialData = getJsonPostResponse("search", body, localization);
    }

    @Nonnull
    @Override
    public String getUrl() throws ParsingException {
        return super.getUrl() + "&gl=" + getExtractorContentCountry().getCountryCode();
    }

    @Nonnull
    @Override
    public String getSearchSuggestion() throws ParsingException {
        final JsonObject itemSectionRenderer = initialData.getObject("contents")
                .getObject("twoColumnSearchResultsRenderer")
                .getObject("primaryContents")
                .getObject("sectionListRenderer")
                .getArray("contents")
                .getObject(0)
                .getObject("itemSectionRenderer");
        final JsonObject didYouMeanRenderer = itemSectionRenderer.getArray("contents")
                .getObject(0)
                .getObject("didYouMeanRenderer");

        if (!didYouMeanRenderer.isEmpty()) {
            return JsonUtils.getString(didYouMeanRenderer,
                    "correctedQueryEndpoint.searchEndpoint.query");
        }

        return Objects.requireNonNullElse(
                getTextFromObject(itemSectionRenderer.getArray("contents")
                        .getObject(0)
                        .getObject("showingResultsForRenderer")
                        .getObject("correctedQuery")), "");
    }

    @Override
    public boolean isCorrectedSearch() {
        final JsonObject showingResultsForRenderer = initialData.getObject("contents")
                .getObject("twoColumnSearchResultsRenderer").getObject("primaryContents")
                .getObject("sectionListRenderer").getArray("contents").getObject(0)
                .getObject("itemSectionRenderer").getArray("contents").getObject(0)
                .getObject("showingResultsForRenderer");
        return !showingResultsForRenderer.isEmpty();
    }

    @Nonnull
    @Override
    public List<MetaInfo> getMetaInfo() throws ParsingException {
        return YoutubeMetaInfoHelper.getMetaInfo(
                initialData.getObject("contents")
                        .getObject("twoColumnSearchResultsRenderer")
                        .getObject("primaryContents")
                        .getObject("sectionListRenderer")
                        .getArray("contents"));
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws IOException, ExtractionException {
        final MultiInfoItemsCollector collector = new MultiInfoItemsCollector(getServiceId());

        final JsonArray sections = initialData.getObject("contents")
                .getObject("twoColumnSearchResultsRenderer")
                .getObject("primaryContents")
                .getObject("sectionListRenderer")
                .getArray("contents");

        Page nextPage = null;

        for (final Object section : sections) {
            final JsonObject sectionJsonObject = (JsonObject) section;
            if (sectionJsonObject.has("itemSectionRenderer")) {
                final JsonObject itemSectionRenderer =
                        sectionJsonObject.getObject("itemSectionRenderer");

                collectStreamsFrom(collector, itemSectionRenderer.getArray("contents"));
            } else if (sectionJsonObject.has("continuationItemRenderer")) {
                nextPage = getNextPageFrom(
                        sectionJsonObject.getObject("continuationItemRenderer"));
            }
        }

        return new InfoItemsPage<>(collector, nextPage);
    }

    @Override
    public InfoItemsPage<InfoItem> getPage(final Page page) throws IOException,
            ExtractionException {
        if (page == null || isNullOrEmpty(page.getUrl())) {
            throw new IllegalArgumentException("Page doesn't contain an URL");
        }

        final Localization localization = getExtractorLocalization();
        final MultiInfoItemsCollector collector = new MultiInfoItemsCollector(getServiceId());

        // @formatter:off
        final byte[] json = JsonWriter.string(prepareDesktopJsonBuilder(localization,
                getExtractorContentCountry())
                .value("continuation", page.getId())
                .done())
                .getBytes(StandardCharsets.UTF_8);
        // @formatter:on

        final JsonObject ajaxJson = getJsonPostResponse("search", json, localization);

        final JsonArray continuationItems = ajaxJson.getArray("onResponseReceivedCommands")
                .getObject(0)
                .getObject("appendContinuationItemsAction")
                .getArray("continuationItems");

        final JsonArray contents = continuationItems.getObject(0)
                .getObject("itemSectionRenderer")
                .getArray("contents");
        collectStreamsFrom(collector, contents);

        return new InfoItemsPage<>(collector, getNextPageFrom(continuationItems.getObject(1)
                .getObject("continuationItemRenderer")));
    }

    private void collectStreamsFrom(final MultiInfoItemsCollector collector,
                                    @Nonnull final JsonArray contents)
            throws NothingFoundException {
        final TimeAgoParser timeAgoParser = getTimeAgoParser();

        for (final Object content : contents) {
            final JsonObject item = (JsonObject) content;
            if (item.has("backgroundPromoRenderer")) {
                throw new NothingFoundException(
                        getTextFromObject(item.getObject("backgroundPromoRenderer")
                                .getObject("bodyText")));
            } else if (extractVideoResults && item.has("videoRenderer")) {
                collector.commit(new YoutubeStreamInfoItemExtractor(
                        item.getObject("videoRenderer"), timeAgoParser));
            } else if (extractChannelResults && item.has("channelRenderer")) {
                collector.commit(new YoutubeChannelInfoItemExtractor(
                        item.getObject("channelRenderer")));
            } else if (extractPlaylistResults) {
                if (item.has("playlistRenderer")) {
                    collector.commit(new YoutubePlaylistInfoItemExtractor(
                            item.getObject("playlistRenderer")));
                } else if (item.has("showRenderer")) {
                    collector.commit(new YoutubeShowRendererInfoItemExtractor(
                            item.getObject("showRenderer")));
                }
            }
        }
    }

    @Nullable
    private Page getNextPageFrom(final JsonObject continuationItemRenderer) {
        if (isNullOrEmpty(continuationItemRenderer)) {
            return null;
        }

        final String token = continuationItemRenderer.getObject("continuationEndpoint")
                .getObject("continuationCommand")
                .getString("token");

        final String url = YOUTUBEI_V1_URL + "search?" + DISABLE_PRETTY_PRINT_PARAMETER;

        return new Page(url, token);
    }
}
