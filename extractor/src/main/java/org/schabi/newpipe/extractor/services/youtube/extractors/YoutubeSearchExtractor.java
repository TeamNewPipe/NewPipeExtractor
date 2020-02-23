package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
import org.schabi.newpipe.extractor.utils.Parser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

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
        final Element el = doc.select("div[class*=\"spell-correction\"]").first();
        if (el != null) {
            return el.select("a").first().text();
        } else {
            return "";
        }
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws ExtractionException {
        return new InfoItemsPage<>(collectItems(doc), getNextPageUrl());
    }

    @Override
    public String getNextPageUrl() throws ExtractionException {
        return getUrl() + "&page=" + 2;
    }

    @Override
    public InfoItemsPage<InfoItem> getPage(String pageUrl) throws IOException, ExtractionException {
        // TODO: Get extracting next pages working
        final String response = getDownloader().get(pageUrl, getExtractorLocalization()).responseBody();
        doc = Jsoup.parse(response, pageUrl);

        return new InfoItemsPage<>(collectItems(doc), getNextPageUrlFromCurrentUrl(pageUrl));
    }

    private String getNextPageUrlFromCurrentUrl(String currentUrl)
            throws MalformedURLException, UnsupportedEncodingException {
        final int pageNr = Integer.parseInt(
                Parser.compatParseMap(
                        new URL(currentUrl)
                                .getQuery())
                        .get("page"));

        return currentUrl.replace("&page=" + pageNr,
                "&page=" + Integer.toString(pageNr + 1));
    }

    private InfoItemsSearchCollector collectItems(Document doc) throws NothingFoundException, ParsingException {
        InfoItemsSearchCollector collector = getInfoItemSearchCollector();
        collector.reset();

        final TimeAgoParser timeAgoParser = getTimeAgoParser();

        if (initialData == null) initialData = YoutubeParsingHelper.getInitialData(doc.toString());
        JsonArray list = initialData.getObject("contents").getObject("twoColumnSearchResultsRenderer")
                .getObject("primaryContents").getObject("sectionListRenderer").getArray("contents")
                .getObject(0).getObject("itemSectionRenderer").getArray("contents");

        for (Object item : list) {
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
        return collector;
    }

}
