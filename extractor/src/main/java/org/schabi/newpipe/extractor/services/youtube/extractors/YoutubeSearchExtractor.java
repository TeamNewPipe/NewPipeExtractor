package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.grack.nanojson.JsonWriter;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.search.InfoItemsSearchCollector;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper.fixThumbnailUrl;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper.getJsonResponse;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper.getUrlFromNavigationEndpoint;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.MUSIC_ALBUMS;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.MUSIC_ARTISTS;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.MUSIC_PLAYLISTS;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.MUSIC_SONGS;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.MUSIC_VIDEOS;

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
        if (isMusicSearch()) {
            final String[] youtubeMusicKeys = YoutubeParsingHelper.getYoutubeMusicKeys();

            final String url = "https://music.youtube.com/youtubei/v1/search?alt=json&key=" + youtubeMusicKeys[0];

            String params = null;

            switch (getLinkHandler().getContentFilters().get(0)) {
                case MUSIC_SONGS:
                    params = "Eg-KAQwIARAAGAAgACgAMABqChAEEAUQAxAKEAk%3D";
                    break;
                case MUSIC_VIDEOS:
                    params = "Eg-KAQwIABABGAAgACgAMABqChAEEAUQAxAKEAk%3D";
                    break;
                case MUSIC_ALBUMS:
                    params = "Eg-KAQwIABAAGAEgACgAMABqChAEEAUQAxAKEAk%3D";
                    break;
                case MUSIC_PLAYLISTS:
                    params = "Eg-KAQwIABAAGAAgACgBMABqChAEEAUQAxAKEAk%3D";
                    break;
                case MUSIC_ARTISTS:
                    params = "Eg-KAQwIABAAGAAgASgAMABqChAEEAUQAxAKEAk%3D";
                    break;
            }

            // @formatter:off
            byte[] json = JsonWriter.string()
                .object()
                    .object("context")
                        .object("client")
                            .value("clientName", "WEB_REMIX")
                            .value("clientVersion", youtubeMusicKeys[2])
                            .value("hl", "en")
                            .value("gl", getExtractorContentCountry().getCountryCode())
                            .array("experimentIds").end()
                            .value("experimentsToken", "")
                            .value("utcOffsetMinutes", 0)
                            .object("locationInfo").end()
                            .object("musicAppInfo").end()
                        .end()
                        .object("capabilities").end()
                        .object("request")
                            .array("internalExperimentFlags").end()
                            .object("sessionIndex").end()
                        .end()
                        .object("activePlayers").end()
                        .object("user")
                            .value("enableSafetyMode", false)
                        .end()
                    .end()
                    .value("query", getSearchString())
                    .value("params", params)
                .end().done().getBytes("UTF-8");
            // @formatter:on

            Map<String, List<String>> headers = new HashMap<>();
            headers.put("X-YouTube-Client-Name", Collections.singletonList(youtubeMusicKeys[1]));
            headers.put("X-YouTube-Client-Version", Collections.singletonList(youtubeMusicKeys[2]));
            headers.put("Origin", Collections.singletonList("https://music.youtube.com"));
            headers.put("Referer", Collections.singletonList("music.youtube.com"));
            headers.put("Content-Type", Collections.singletonList("application/json"));

            Response response = getDownloader().post(url, headers, json);

            if (response.responseCode() == 404) {
                throw new ContentNotAvailableException("Not found" +
                        " (\"" + response.responseCode() + " " + response.responseMessage() + "\")");
            }

            final String responseBody = response.responseBody();
            if (responseBody.length() < 50) { // ensure to have a valid response
                throw new ParsingException("JSON response is too short");
            }

            final String responseContentType = response.getHeader("Content-Type");
            if (responseContentType != null && responseContentType.toLowerCase().contains("text/html")) {
                throw new ParsingException("Got HTML document, expected JSON response" +
                        " (latest url was: \"" + response.latestUrl() + "\")");
            }

            try {
                initialData = JsonParser.object().from(responseBody);
            } catch (JsonParserException e) {
                throw new ParsingException("Could not parse JSON", e);
            }
        } else {
            final String url = getUrl() + "&pbj=1";

            final JsonArray ajaxJson = getJsonResponse(url, getExtractorLocalization());

            initialData = ajaxJson.getObject(1).getObject("response");
        }
    }

    @Nonnull
    @Override
    public String getUrl() throws ParsingException {
        if (isMusicSearch()) return super.getUrl();
        return super.getUrl() + "&gl=" + getExtractorContentCountry().getCountryCode();
    }

    @Override
    public String getSearchSuggestion() throws ParsingException {
        if (isMusicSearch()) {
            final JsonObject itemSectionRenderer = initialData.getObject("contents").getObject("sectionListRenderer")
                    .getArray("contents").getObject(0).getObject("itemSectionRenderer");
            if (itemSectionRenderer == null) return "";
            final JsonObject didYouMeanRenderer = itemSectionRenderer.getArray("contents")
                    .getObject(0).getObject("didYouMeanRenderer");
            if (didYouMeanRenderer == null) return "";
            return getTextFromObject(didYouMeanRenderer.getObject("correctedQuery"));
        } else {
            JsonObject showingResultsForRenderer = initialData.getObject("contents")
                    .getObject("twoColumnSearchResultsRenderer").getObject("primaryContents")
                    .getObject("sectionListRenderer").getArray("contents").getObject(0)
                    .getObject("itemSectionRenderer").getArray("contents").getObject(0)
                    .getObject("showingResultsForRenderer");
            if (showingResultsForRenderer == null) return "";
            return getTextFromObject(showingResultsForRenderer.getObject("correctedQuery"));
        }
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws ExtractionException, IOException {
        final InfoItemsSearchCollector collector = new InfoItemsSearchCollector(getServiceId());

        if (isMusicSearch()) {
            final JsonArray contents = initialData.getObject("contents").getObject("sectionListRenderer").getArray("contents");

            for (Object content : contents) {
                if (((JsonObject) content).getObject("musicShelfRenderer") != null) {
                    collectMusicStreamsFrom(collector, ((JsonObject) content).getObject("musicShelfRenderer").getArray("contents"));
                }
            }
        } else {
            JsonArray sections = initialData.getObject("contents").getObject("twoColumnSearchResultsRenderer")
                    .getObject("primaryContents").getObject("sectionListRenderer").getArray("contents");

            for (Object section : sections) {
                collectStreamsFrom(collector, ((JsonObject) section).getObject("itemSectionRenderer").getArray("contents"));
            }
        }

        return new InfoItemsPage<>(collector, getNextPageUrl());
    }

    @Override
    public String getNextPageUrl() throws ExtractionException, IOException {
        if (isMusicSearch()) {
            final JsonArray contents = initialData.getObject("contents").getObject("sectionListRenderer").getArray("contents");

            for (Object content : contents) {
                if (((JsonObject) content).getObject("musicShelfRenderer") != null) {
                    return getNextPageUrlFrom(((JsonObject) content).getObject("musicShelfRenderer").getArray("continuations"));
                }
            }

            return "";
        } else {
            return getNextPageUrlFrom(initialData.getObject("contents").getObject("twoColumnSearchResultsRenderer")
                    .getObject("primaryContents").getObject("sectionListRenderer").getArray("contents")
                    .getObject(0).getObject("itemSectionRenderer").getArray("continuations"));
        }
    }

    @Override
    public InfoItemsPage<InfoItem> getPage(String pageUrl) throws IOException, ExtractionException {
        if (pageUrl == null || pageUrl.isEmpty()) {
            throw new ExtractionException(new IllegalArgumentException("Page url is empty or null"));
        }

        final InfoItemsSearchCollector collector = new InfoItemsSearchCollector(getServiceId());

        JsonArray continuations;

        if (isMusicSearch()) {
            final String[] youtubeMusicKeys = YoutubeParsingHelper.getYoutubeMusicKeys();

            // @formatter:off
            byte[] json = JsonWriter.string()
                .object()
                    .object("context")
                        .object("client")
                            .value("clientName", "WEB_REMIX")
                            .value("clientVersion", youtubeMusicKeys[2])
                            .value("hl", "en")
                            .value("gl", getExtractorContentCountry().getCountryCode())
                            .array("experimentIds").end()
                            .value("experimentsToken", "")
                            .value("utcOffsetMinutes", 0)
                            .object("locationInfo").end()
                            .object("musicAppInfo").end()
                        .end()
                        .object("capabilities").end()
                        .object("request")
                            .array("internalExperimentFlags").end()
                            .object("sessionIndex").end()
                        .end()
                        .object("activePlayers").end()
                        .object("user")
                            .value("enableSafetyMode", false)
                        .end()
                    .end()
                .end().done().getBytes("UTF-8");
            // @formatter:on

            Map<String, List<String>> headers = new HashMap<>();
            headers.put("X-YouTube-Client-Name", Collections.singletonList(youtubeMusicKeys[1]));
            headers.put("X-YouTube-Client-Version", Collections.singletonList(youtubeMusicKeys[2]));
            headers.put("Origin", Collections.singletonList("https://music.youtube.com"));
            headers.put("Referer", Collections.singletonList("music.youtube.com"));
            headers.put("Content-Type", Collections.singletonList("application/json"));

            Response response = getDownloader().post(pageUrl, headers, json);

            if (response.responseCode() == 404) {
                throw new ContentNotAvailableException("Not found" +
                        " (\"" + response.responseCode() + " " + response.responseMessage() + "\")");
            }

            final String responseBody = response.responseBody();
            if (responseBody.length() < 50) { // ensure to have a valid response
                throw new ParsingException("JSON response is too short");
            }

            final String responseContentType = response.getHeader("Content-Type");
            if (responseContentType != null && responseContentType.toLowerCase().contains("text/html")) {
                throw new ParsingException("Got HTML document, expected JSON response" +
                        " (latest url was: \"" + response.latestUrl() + "\")");
            }

            final JsonObject ajaxJson;
            try {
                ajaxJson = JsonParser.object().from(responseBody);
            } catch (JsonParserException e) {
                throw new ParsingException("Could not parse JSON", e);
            }

            if (ajaxJson.getObject("continuationContents") == null) {
                return InfoItemsPage.emptyPage();
            }

            JsonObject musicShelfContinuation = ajaxJson.getObject("continuationContents").getObject("musicShelfContinuation");

            collectMusicStreamsFrom(collector, musicShelfContinuation.getArray("contents"));
            continuations = musicShelfContinuation.getArray("continuations");
        } else {
            final JsonArray ajaxJson = getJsonResponse(pageUrl, getExtractorLocalization());

            JsonObject itemSectionRenderer = ajaxJson.getObject(1).getObject("response")
                    .getObject("continuationContents").getObject("itemSectionContinuation");

            collectStreamsFrom(collector, itemSectionRenderer.getArray("contents"));
            continuations = itemSectionRenderer.getArray("continuations");
        }

        return new InfoItemsPage<>(collector, getNextPageUrlFrom(continuations));
    }

    private boolean isMusicSearch() {
        final List<String> contentFilters = getLinkHandler().getContentFilters();
        return contentFilters.size() > 0 && contentFilters.get(0).startsWith("music_");
    }

    private void collectStreamsFrom(InfoItemsSearchCollector collector, JsonArray videos) throws NothingFoundException, ParsingException {
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

    private void collectMusicStreamsFrom(InfoItemsSearchCollector collector, JsonArray videos) {
        final TimeAgoParser timeAgoParser = getTimeAgoParser();

        for (Object item : videos) {
            final JsonObject info = ((JsonObject) item).getObject("musicResponsiveListItemRenderer");
            if (info != null) {
                final String searchType = getLinkHandler().getContentFilters().get(0);
                if (searchType.equals(MUSIC_SONGS) || searchType.equals(MUSIC_VIDEOS)) {
                    collector.commit(new YoutubeStreamInfoItemExtractor(info, timeAgoParser) {
                        @Override
                        public String getUrl() throws ParsingException {
                            String url = getUrlFromNavigationEndpoint(info.getObject("doubleTapCommand"));
                            if (url != null && !url.isEmpty()) return url;
                            throw new ParsingException("Could not get url");
                        }

                        @Override
                        public String getName() throws ParsingException {
                            String name = getTextFromObject(info.getArray("flexColumns").getObject(0)
                                    .getObject("musicResponsiveListItemFlexColumnRenderer").getObject("text"));
                            if (name != null && !name.isEmpty()) return name;
                            throw new ParsingException("Could not get name");
                        }

                        @Override
                        public long getDuration() throws ParsingException {
                            String duration = getTextFromObject(info.getArray("flexColumns").getObject(3)
                                    .getObject("musicResponsiveListItemFlexColumnRenderer").getObject("text"));
                            if (duration != null && !duration.isEmpty())
                                return YoutubeParsingHelper.parseDurationString(duration);
                            throw new ParsingException("Could not get duration");
                        }

                        @Override
                        public String getUploaderName() throws ParsingException {
                            String name = getTextFromObject(info.getArray("flexColumns").getObject(1)
                                    .getObject("musicResponsiveListItemFlexColumnRenderer").getObject("text"));
                            if (name != null && !name.isEmpty()) return name;
                            throw new ParsingException("Could not get uploader name");
                        }

                        @Override
                        public String getUploaderUrl() throws ParsingException {
                            String url = null;

                            if (searchType.equals(MUSIC_VIDEOS)) {
                                JsonArray items = info.getObject("menu").getObject("menuRenderer").getArray("items");
                                for (Object item : items) {
                                    JsonObject menuNavigationItemRenderer = ((JsonObject) item).getObject("menuNavigationItemRenderer");
                                    if (menuNavigationItemRenderer != null && menuNavigationItemRenderer.getObject("icon").getString("iconType").equals("ARTIST")) {
                                        url = getUrlFromNavigationEndpoint(menuNavigationItemRenderer.getObject("navigationEndpoint"));
                                        break;
                                    }
                                }
                            } else {
                                JsonObject navigationEndpoint = info.getArray("flexColumns")
                                        .getObject(1).getObject("musicResponsiveListItemFlexColumnRenderer")
                                        .getObject("text").getArray("runs").getObject(0).getObject("navigationEndpoint");
                                if (navigationEndpoint == null) return null;
                                url = getUrlFromNavigationEndpoint(navigationEndpoint);
                            }

                            if (url != null && !url.isEmpty()) return url;
                            throw new ParsingException("Could not get uploader url");
                        }

                        @Override
                        public String getTextualUploadDate() {
                            return null;
                        }

                        @Override
                        public DateWrapper getUploadDate() {
                            return null;
                        }

                        @Override
                        public long getViewCount() throws ParsingException {
                            if (searchType.equals(MUSIC_SONGS)) return -1;
                            String viewCount = getTextFromObject(info.getArray("flexColumns").getObject(2)
                                    .getObject("musicResponsiveListItemFlexColumnRenderer").getObject("text"));
                            if (viewCount != null && !viewCount.isEmpty()) return Utils.mixedNumberWordToLong(viewCount);
                            throw new ParsingException("Could not get view count");
                        }

                        @Override
                        public String getThumbnailUrl() throws ParsingException {
                            try {
                                JsonArray thumbnails = info.getObject("thumbnail").getObject("musicThumbnailRenderer")
                                        .getObject("thumbnail").getArray("thumbnails");
                                // the last thumbnail is the one with the highest resolution
                                String url = thumbnails.getObject(thumbnails.size() - 1).getString("url");

                                return fixThumbnailUrl(url);
                            } catch (Exception e) {
                                throw new ParsingException("Could not get thumbnail url", e);
                            }
                        }
                    });
                } else if (searchType.equals(MUSIC_ARTISTS)) {
                    collector.commit(new YoutubeChannelInfoItemExtractor(info) {
                        @Override
                        public String getThumbnailUrl() throws ParsingException {
                            try {
                                JsonArray thumbnails = info.getObject("thumbnail").getObject("musicThumbnailRenderer")
                                        .getObject("thumbnail").getArray("thumbnails");
                                // the last thumbnail is the one with the highest resolution
                                String url = thumbnails.getObject(thumbnails.size() - 1).getString("url");

                                return fixThumbnailUrl(url);
                            } catch (Exception e) {
                                throw new ParsingException("Could not get thumbnail url", e);
                            }
                        }

                        @Override
                        public String getName() throws ParsingException {
                            String name = getTextFromObject(info.getArray("flexColumns").getObject(0)
                                    .getObject("musicResponsiveListItemFlexColumnRenderer").getObject("text"));
                            if (name != null && !name.isEmpty()) return name;
                            throw new ParsingException("Could not get name");
                        }

                        @Override
                        public String getUrl() throws ParsingException {
                            String url = getUrlFromNavigationEndpoint(info.getObject("navigationEndpoint"));
                            if (url != null && !url.isEmpty()) return url;
                            throw new ParsingException("Could not get url");
                        }

                        @Override
                        public long getSubscriberCount() throws ParsingException {
                            String viewCount = getTextFromObject(info.getArray("flexColumns").getObject(2)
                                    .getObject("musicResponsiveListItemFlexColumnRenderer").getObject("text"));
                            if (viewCount != null && !viewCount.isEmpty()) return Utils.mixedNumberWordToLong(viewCount);
                            throw new ParsingException("Could not get subscriber count");
                        }

                        @Override
                        public long getStreamCount() {
                            return -1;
                        }

                        @Override
                        public String getDescription() {
                            return null;
                        }
                    });
                } else if (searchType.equals(MUSIC_ALBUMS) || searchType.equals(MUSIC_PLAYLISTS)) {
                    collector.commit(new YoutubePlaylistInfoItemExtractor(info) {
                        @Override
                        public String getThumbnailUrl() throws ParsingException {
                            try {
                                JsonArray thumbnails = info.getObject("thumbnail").getObject("musicThumbnailRenderer")
                                        .getObject("thumbnail").getArray("thumbnails");
                                // the last thumbnail is the one with the highest resolution
                                String url = thumbnails.getObject(thumbnails.size() - 1).getString("url");

                                return fixThumbnailUrl(url);
                            } catch (Exception e) {
                                throw new ParsingException("Could not get thumbnail url", e);
                            }
                        }

                        @Override
                        public String getName() throws ParsingException {
                            String name = getTextFromObject(info.getArray("flexColumns").getObject(0)
                                    .getObject("musicResponsiveListItemFlexColumnRenderer").getObject("text"));
                            if (name != null && !name.isEmpty()) return name;
                            throw new ParsingException("Could not get name");
                        }

                        @Override
                        public String getUrl() throws ParsingException {
                            String url = getUrlFromNavigationEndpoint(info.getObject("doubleTapCommand"));
                            if (url != null && !url.isEmpty()) return url;
                            throw new ParsingException("Could not get url");
                        }

                        @Override
                        public String getUploaderName() throws ParsingException {
                            String name;
                            if (searchType.equals(MUSIC_ALBUMS)) {
                                name = getTextFromObject(info.getArray("flexColumns").getObject(2)
                                        .getObject("musicResponsiveListItemFlexColumnRenderer").getObject("text"));
                            } else {
                                name = getTextFromObject(info.getArray("flexColumns").getObject(1)
                                        .getObject("musicResponsiveListItemFlexColumnRenderer").getObject("text"));
                            }
                            if (name != null && !name.isEmpty()) return name;
                            throw new ParsingException("Could not get uploader name");
                        }

                        @Override
                        public long getStreamCount() throws ParsingException {
                            if (searchType.equals(MUSIC_ALBUMS)) return ITEM_COUNT_UNKNOWN;
                            String count = getTextFromObject(info.getArray("flexColumns").getObject(2)
                                    .getObject("musicResponsiveListItemFlexColumnRenderer").getObject("text"));
                            if (count != null && !count.isEmpty()) {
                                if (count.contains("100+")) {
                                    return ITEM_COUNT_MORE_THAN_100;
                                } else {
                                    return Long.parseLong(Utils.removeNonDigitCharacters(count));
                                }
                            }
                            throw new ParsingException("Could not get count");
                        }
                    });
                }
            }
        }
    }

    private String getNextPageUrlFrom(JsonArray continuations) throws ParsingException, IOException, ReCaptchaException {
        if (continuations == null) return "";

        JsonObject nextContinuationData = continuations.getObject(0).getObject("nextContinuationData");
        String continuation = nextContinuationData.getString("continuation");
        String clickTrackingParams = nextContinuationData.getString("clickTrackingParams");

        if (isMusicSearch()) {
            return "https://music.youtube.com/youtubei/v1/search?ctoken=" + continuation + "&continuation=" + continuation
                    + "&itct=" + clickTrackingParams + "&alt=json&key=" + YoutubeParsingHelper.getYoutubeMusicKeys()[0];
        } else {
            return getUrl() + "&pbj=1&ctoken=" + continuation + "&continuation=" + continuation
                    + "&itct=" + clickTrackingParams;
        }
    }
}
