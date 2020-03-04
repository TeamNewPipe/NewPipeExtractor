package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.search.InfoItemsSearchCollector;
import org.schabi.newpipe.extractor.search.SearchExtractor;

import java.io.IOException;

import javax.annotation.Nonnull;

import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper.getJsonResponse;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper.getTextFromObject;

/*
 * Created by Christian Schabesberger on 22.07.2018
 *
 * Copyright (C) Christian Schabesberger 2018 <chris.schabesberger@mailbox.org>
 * YoutubeSearchExtractor.java is part of NewPipe.
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

public class YoutubeSearchExtractor extends SearchExtractor {
    private JsonObject initialData;

    public YoutubeSearchExtractor(StreamingService service, SearchQueryHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        final String url = getUrl() + "&pbj=1";

        final JsonArray ajaxJson = getJsonResponse(url, getExtractorLocalization());

        initialData = ajaxJson.getObject(1).getObject("response");
    }

    @Nonnull
    @Override
    public String getUrl() throws ParsingException {
        return super.getUrl() + "&gl=" + getExtractorContentCountry().getCountryCode();
    }

    @Override
    public String getSearchSuggestion() throws ParsingException {
        JsonObject showingResultsForRenderer = initialData.getObject("contents")
                .getObject("twoColumnSearchResultsRenderer").getObject("primaryContents")
                .getObject("sectionListRenderer").getArray("contents").getObject(0)
                .getObject("itemSectionRenderer").getArray("contents").getObject(0)
                .getObject("showingResultsForRenderer");
        if (showingResultsForRenderer == null) {
            return "";
        } else {
            return getTextFromObject(showingResultsForRenderer.getObject("correctedQuery"));
        }
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws ExtractionException {
        InfoItemsSearchCollector collector = getInfoItemSearchCollector();
        JsonArray sections = initialData.getObject("contents").getObject("twoColumnSearchResultsRenderer")
                .getObject("primaryContents").getObject("sectionListRenderer").getArray("contents");

        for (Object section : sections) {
            collectStreamsFrom(collector, ((JsonObject) section).getObject("itemSectionRenderer").getArray("contents"));
        }

        return new InfoItemsPage<>(collector, getNextPageUrl());
    }

    @Override
    public String getNextPageUrl() throws ExtractionException {
        return getNextPageUrlFrom(initialData.getObject("contents").getObject("twoColumnSearchResultsRenderer")
                .getObject("primaryContents").getObject("sectionListRenderer").getArray("contents")
                .getObject(0).getObject("itemSectionRenderer").getArray("continuations"));
    }

    @Override
    public InfoItemsPage<InfoItem> getPage(String pageUrl) throws IOException, ExtractionException {
        if (pageUrl == null || pageUrl.isEmpty()) {
            throw new ExtractionException(new IllegalArgumentException("Page url is empty or null"));
        }

        InfoItemsSearchCollector collector = getInfoItemSearchCollector();
        final JsonArray ajaxJson = getJsonResponse(pageUrl, getExtractorLocalization());

        if (ajaxJson.getObject(1).getObject("response").getObject("continuationContents") == null)
            return null;

        JsonObject itemSectionRenderer = ajaxJson.getObject(1).getObject("response")
                .getObject("continuationContents").getObject("itemSectionContinuation");

        collectStreamsFrom(collector, itemSectionRenderer.getArray("contents"));

        return new InfoItemsPage<>(collector, getNextPageUrlFrom(itemSectionRenderer.getArray("continuations")));
    }

    private void collectStreamsFrom(InfoItemsSearchCollector collector, JsonArray videos) throws NothingFoundException, ParsingException {
        collector.reset();

        final TimeAgoParser timeAgoParser = getTimeAgoParser();

        for (Object item : videos) {
            if (((JsonObject) item).getObject("backgroundPromoRenderer") != null) {
                throw new NothingFoundException(getTextFromObject(((JsonObject) item)
                        .getObject("backgroundPromoRenderer").getObject("bodyText")));
            } else if (((JsonObject) item).getObject("videoRenderer") != null) {
                collector.commit(new YoutubeStreamInfoItemExtractor(((JsonObject) item).getObject("videoRenderer"), timeAgoParser));
            } else if (((JsonObject) item).getObject("channelRenderer") != null) {
                collector.commit(new YoutubeChannelInfoItemExtractor(((JsonObject) item).getObject("channelRenderer")));
            } else if (((JsonObject) item).getObject("playlistRenderer") != null) {
                collector.commit(new YoutubePlaylistInfoItemExtractor(((JsonObject) item).getObject("playlistRenderer")));
            }
        }
    }

    private String getNextPageUrlFrom(JsonArray continuations) throws ParsingException {
        if (continuations == null) {
            return "";
        }

        JsonObject nextContinuationData = continuations.getObject(0).getObject("nextContinuationData");
        String continuation = nextContinuationData.getString("continuation");
        String clickTrackingParams = nextContinuationData.getString("clickTrackingParams");
        return getUrl() + "&pbj=1&ctoken=" + continuation + "&continuation=" + continuation
                + "&itct=" + clickTrackingParams;
    }
}
