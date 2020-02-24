package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.jsoup.nodes.Document;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.search.InfoItemsSearchCollector;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

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

    private Document doc;
    private JsonObject initialData;

    public YoutubeSearchExtractor(StreamingService service, SearchQueryHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        final String url = getUrl();
        final Response response = downloader.get(url, getExtractorLocalization());
        doc = YoutubeParsingHelper.parseAndCheckPage(url, response);
        initialData = YoutubeParsingHelper.getInitialData(response.responseBody());
    }

    @Nonnull
    @Override
    public String getUrl() throws ParsingException {
        return super.getUrl() + "&gl=" + getExtractorContentCountry().getCountryCode();
    }

    @Override
    public String getSearchSuggestion() {
        JsonObject showingResultsForRenderer = initialData.getObject("contents")
                .getObject("twoColumnSearchResultsRenderer").getObject("primaryContents")
                .getObject("sectionListRenderer").getArray("contents").getObject(0)
                .getObject("itemSectionRenderer").getArray("contents").getObject(0)
                .getObject("showingResultsForRenderer");
        if (showingResultsForRenderer == null) {
            return "";
        } else {
            return showingResultsForRenderer.getObject("correctedQuery").getArray("runs")
                    .getObject(0).getString("text");
        }
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws ExtractionException {
        InfoItemsSearchCollector collector = getInfoItemSearchCollector();
        JsonArray videos = initialData.getObject("contents").getObject("twoColumnSearchResultsRenderer")
                .getObject("primaryContents").getObject("sectionListRenderer").getArray("contents")
                .getObject(0).getObject("itemSectionRenderer").getArray("contents");

        collectStreamsFrom(collector, videos);
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
        JsonArray ajaxJson;
        try {
            Map<String, List<String>> headers = new HashMap<>();
            headers.put("X-YouTube-Client-Name", Collections.singletonList("1"));
            headers.put("X-YouTube-Client-Version", Collections.singletonList("2.20200221.03.00")); // TODO: Automatically get YouTube client version somehow
            final String response = getDownloader().get(pageUrl, headers, getExtractorLocalization()).responseBody();
            ajaxJson = JsonParser.array().from(response);
        } catch (JsonParserException pe) {
            throw new ParsingException("Could not parse json data for next streams", pe);
        }

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
                throw new NothingFoundException(((JsonObject) item).getObject("backgroundPromoRenderer")
                        .getObject("bodyText").getArray("runs").getObject(0).getString("text"));
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
