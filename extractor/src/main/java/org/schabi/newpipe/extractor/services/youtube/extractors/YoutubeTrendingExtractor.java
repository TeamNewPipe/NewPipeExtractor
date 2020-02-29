package org.schabi.newpipe.extractor.services.youtube.extractors;

/*
 * Created by Christian Schabesberger on 12.08.17.
 *
 * Copyright (C) Christian Schabesberger 2018 <chris.schabesberger@mailbox.org>
 * YoutubeTrendingExtractor.java is part of NewPipe.
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

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import java.io.IOException;

import javax.annotation.Nonnull;

import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper.getJsonResponse;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper.getTextFromObject;

public class YoutubeTrendingExtractor extends KioskExtractor<StreamInfoItem> {
    private JsonObject initialData;

    public YoutubeTrendingExtractor(StreamingService service,
                                    ListLinkHandler linkHandler,
                                    String kioskId) {
        super(service, linkHandler, kioskId);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        final String url = getUrl() + "?pbj=1&gl="
                + getExtractorContentCountry().getCountryCode();

        final JsonArray ajaxJson = getJsonResponse(url);

        initialData = ajaxJson.getObject(1).getObject("response");
    }

    @Override
    public String getNextPageUrl() {
        return "";
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(String pageUrl) {
        return null;
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        String name;
        try {
            name = getTextFromObject(initialData.getObject("header").getObject("feedTabbedHeaderRenderer").getObject("title"));
        } catch (Exception e) {
            throw new ParsingException("Could not get Trending name", e);
        }
        if (name != null && !name.isEmpty()) {
            return name;
        }
        throw new ParsingException("Could not get Trending name");
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() {
        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        final TimeAgoParser timeAgoParser = getTimeAgoParser();
        JsonArray itemSectionRenderers = initialData.getObject("contents").getObject("twoColumnBrowseResultsRenderer")
                .getArray("tabs").getObject(0).getObject("tabRenderer").getObject("content")
                .getObject("sectionListRenderer").getArray("contents");

        for (Object itemSectionRenderer : itemSectionRenderers) {
            JsonObject expandedShelfContentsRenderer = ((JsonObject) itemSectionRenderer).getObject("itemSectionRenderer")
                    .getArray("contents").getObject(0).getObject("shelfRenderer").getObject("content")
                    .getObject("expandedShelfContentsRenderer");
            if (expandedShelfContentsRenderer != null) {
                for (Object ul : expandedShelfContentsRenderer.getArray("items")) {
                    final JsonObject videoInfo = ((JsonObject) ul).getObject("videoRenderer");
                    collector.commit(new YoutubeStreamInfoItemExtractor(videoInfo, timeAgoParser));
                }
            }
        }
        return new InfoItemsPage<>(collector, getNextPageUrl());

    }
}
