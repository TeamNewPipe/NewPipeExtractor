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
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.utils.Parser;

import javax.annotation.Nonnull;
import java.io.IOException;

public class YoutubeTrendingExtractor extends KioskExtractor<StreamInfoItem> {

    private Document doc;
    private JsonObject initialData;

    public YoutubeTrendingExtractor(StreamingService service,
                                    ListLinkHandler linkHandler,
                                    String kioskId) {
        super(service, linkHandler, kioskId);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        final String url = getUrl() +
                "?gl=" + getExtractorContentCountry().getCountryCode();

        final Response response = downloader.get(url, getExtractorLocalization());
        doc = YoutubeParsingHelper.parseAndCheckPage(url, response);
        initialData = getInitialData();
    }

    private JsonObject getInitialData() throws ParsingException {
        try {
            String initialData = Parser.matchGroup1("window\\[\"ytInitialData\"\\]\\s*=\\s*(\\{.*?\\});", doc.toString());
            return JsonParser.object().from(initialData);
        } catch (JsonParserException | Parser.RegexException e) {
            throw new ParsingException("Could not get ytInitialData", e);
        }
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
            name = initialData.getObject("header").getObject("feedTabbedHeaderRenderer").getObject("title")
                    .getArray("runs").getObject(0).getString("text");
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
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws ParsingException {
        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        JsonArray firstPageElements = initialData.getObject("contents").getObject("twoColumnBrowseResultsRenderer")
                .getArray("tabs").getObject(0).getObject("tabRenderer").getObject("content")
                .getObject("sectionListRenderer").getArray("contents").getObject(0).getObject("itemSectionRenderer")
                .getArray("contents").getObject(0).getObject("shelfRenderer").getObject("content")
                .getObject("expandedShelfContentsRenderer").getArray("items");

        final TimeAgoParser timeAgoParser = getTimeAgoParser();

        for (Object ul : firstPageElements) {
            final JsonObject videoInfo = ((JsonObject) ul).getObject("videoRenderer");
            collector.commit(new YoutubeStreamInfoItemExtractor(videoInfo, timeAgoParser) {
                @Override
                public String getUrl() throws ParsingException {
                    try {
                        String videoId = videoInfo.getString("videoId");
                        return YoutubeStreamLinkHandlerFactory.getInstance().getUrl(videoId);
                    } catch (Exception e) {
                        throw new ParsingException("Could not get web page url for the video", e);
                    }
                }

                @Override
                public String getName() throws ParsingException {
                    String name = null;
                    try {
                        name = videoInfo.getObject("title").getObject("accessibility")
                                .getObject("accessibilityData").getString("label");
                    } catch (Exception ignored) {
                    }
                    if (name != null && !name.isEmpty()) return name;
                    try {
                        name = videoInfo.getObject("title").getArray("runs").getObject(0).getString("text");
                    } catch (Exception e) {
                        throw new ParsingException("Could not get web page url for the video", e);
                    }
                    if (name != null && !name.isEmpty()) return name;
                    throw new ParsingException("Could not get web page url for the video");
                }

                @Override
                public String getUploaderUrl() throws ParsingException {
                    try {
                        String id = videoInfo.getObject("ownerText").getArray("runs").getObject(0).
                                getObject("browseEndpoint").getString("browseId");
                        if (id == null || id.isEmpty()) {
                            throw new IllegalArgumentException("is empty");
                        }
                        return YoutubeChannelLinkHandlerFactory.getInstance().getUrl(id);
                    } catch (Exception e) {
                        throw new ParsingException("Could not get Uploader url");
                    }
                }

                @Override
                public String getUploaderName() throws ParsingException {
                    try {
                        return videoInfo.getObject("ownerText").getArray("runs").getObject(0).getString("text");
                    } catch (Exception e) {
                        throw new ParsingException("Could not get uploader name");
                    }
                }

                @Override
                public String getThumbnailUrl() throws ParsingException {
                    try {
                        return videoInfo.getObject("thumbnail").getArray("thumbnails").getObject(0).getString("url");
                    } catch (Exception e) {
                        throw new ParsingException("Could not get thumbnail url", e);
                    }
                }
            });
        }
        return new InfoItemsPage<>(collector, getNextPageUrl());

    }
}
