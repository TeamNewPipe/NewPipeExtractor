package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.grack.nanojson.JsonWriter;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.search.InfoItemsSearchCollector;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.*;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.MUSIC_ALBUMS;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.MUSIC_ARTISTS;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.MUSIC_PLAYLISTS;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.MUSIC_SONGS;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.MUSIC_VIDEOS;
import static org.schabi.newpipe.extractor.utils.JsonUtils.EMPTY_STRING;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public class YoutubeMusicSearchExtractor extends SearchExtractor {
    private JsonObject initialData;

    public YoutubeMusicSearchExtractor(final StreamingService service, final SearchQueryHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) throws IOException, ExtractionException {
        final String[] youtubeMusicKeys = YoutubeParsingHelper.getYoutubeMusicKeys();

        final String url = "https://music.youtube.com/youtubei/v1/search?alt=json&key=" + youtubeMusicKeys[0];

        final String params;

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
            default:
                params = null;
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

        final Map<String, List<String>> headers = new HashMap<>();
        headers.put("X-YouTube-Client-Name", Collections.singletonList(youtubeMusicKeys[1]));
        headers.put("X-YouTube-Client-Version", Collections.singletonList(youtubeMusicKeys[2]));
        headers.put("Origin", Collections.singletonList("https://music.youtube.com"));
        headers.put("Referer", Collections.singletonList("music.youtube.com"));
        headers.put("Content-Type", Collections.singletonList("application/json"));

        final String responseBody = getValidJsonResponseBody(getDownloader().post(url, headers, json));

        try {
            initialData = JsonParser.object().from(responseBody);
        } catch (JsonParserException e) {
            throw new ParsingException("Could not parse JSON", e);
        }
    }

    @Nonnull
    @Override
    public String getUrl() throws ParsingException {
        return super.getUrl();
    }

    @Nonnull
    @Override
    public String getSearchSuggestion() throws ParsingException {
        final JsonObject itemSectionRenderer = initialData.getObject("contents").getObject("sectionListRenderer")
                .getArray("contents").getObject(0).getObject("itemSectionRenderer");
        if (itemSectionRenderer.isEmpty()) {
            return "";
        }

        final JsonObject didYouMeanRenderer = itemSectionRenderer.getArray("contents")
                .getObject(0).getObject("didYouMeanRenderer");
        final JsonObject showingResultsForRenderer = itemSectionRenderer.getArray("contents").getObject(0)
                .getObject("showingResultsForRenderer");

        if (!didYouMeanRenderer.isEmpty()) {
            return getTextFromObject(didYouMeanRenderer.getObject("correctedQuery"));
        } else if (!showingResultsForRenderer.isEmpty()) {
            return JsonUtils.getString(showingResultsForRenderer, "correctedQueryEndpoint.searchEndpoint.query");
        } else {
            return "";
        }
    }

    @Override
    public boolean isCorrectedSearch() {
        final JsonObject itemSectionRenderer = initialData.getObject("contents").getObject("sectionListRenderer")
                .getArray("contents").getObject(0).getObject("itemSectionRenderer");
        if (itemSectionRenderer.isEmpty()) {
            return false;
        }

        final JsonObject showingResultsForRenderer = itemSectionRenderer.getArray("contents").getObject(0)
                .getObject("showingResultsForRenderer");
        return !showingResultsForRenderer.isEmpty();
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws ExtractionException, IOException {
        final InfoItemsSearchCollector collector = new InfoItemsSearchCollector(getServiceId());

        final JsonArray contents = initialData.getObject("contents").getObject("sectionListRenderer").getArray("contents");

        Page nextPage = null;

        for (Object content : contents) {
            if (((JsonObject) content).has("musicShelfRenderer")) {
                final JsonObject musicShelfRenderer = ((JsonObject) content).getObject("musicShelfRenderer");

                collectMusicStreamsFrom(collector, musicShelfRenderer.getArray("contents"));

                nextPage = getNextPageFrom(musicShelfRenderer.getArray("continuations"));
            }
        }

        return new InfoItemsPage<>(collector, nextPage);
    }

    @Override
    public InfoItemsPage<InfoItem> getPage(final Page page) throws IOException, ExtractionException {
        if (page == null || isNullOrEmpty(page.getUrl())) {
            throw new IllegalArgumentException("Page doesn't contain an URL");
        }

        final InfoItemsSearchCollector collector = new InfoItemsSearchCollector(getServiceId());

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

        final Map<String, List<String>> headers = new HashMap<>();
        headers.put("X-YouTube-Client-Name", Collections.singletonList(youtubeMusicKeys[1]));
        headers.put("X-YouTube-Client-Version", Collections.singletonList(youtubeMusicKeys[2]));
        headers.put("Origin", Collections.singletonList("https://music.youtube.com"));
        headers.put("Referer", Collections.singletonList("music.youtube.com"));
        headers.put("Content-Type", Collections.singletonList("application/json"));

        final String responseBody = getValidJsonResponseBody(getDownloader().post(page.getUrl(), headers, json));

        final JsonObject ajaxJson;
        try {
            ajaxJson = JsonParser.object().from(responseBody);
        } catch (JsonParserException e) {
            throw new ParsingException("Could not parse JSON", e);
        }

        final JsonObject musicShelfContinuation = ajaxJson.getObject("continuationContents").getObject("musicShelfContinuation");

        collectMusicStreamsFrom(collector, musicShelfContinuation.getArray("contents"));
        final JsonArray continuations = musicShelfContinuation.getArray("continuations");

        return new InfoItemsPage<>(collector, getNextPageFrom(continuations));
    }

    private void collectMusicStreamsFrom(final InfoItemsSearchCollector collector, final JsonArray videos) {
        final TimeAgoParser timeAgoParser = getTimeAgoParser();

        for (Object item : videos) {
            final JsonObject info = ((JsonObject) item).getObject("musicResponsiveListItemRenderer", null);
            if (info != null) {
                final String searchType = getLinkHandler().getContentFilters().get(0);
                if (searchType.equals(MUSIC_SONGS) || searchType.equals(MUSIC_VIDEOS)) {
                    collector.commit(new YoutubeStreamInfoItemExtractor(info, timeAgoParser) {
                        @Override
                        public String getUrl() throws ParsingException {
                            final String url = getUrlFromNavigationEndpoint(info.getObject("doubleTapCommand"));
                            if (!isNullOrEmpty(url)) {
                                return url;
                            }
                            throw new ParsingException("Could not get url");
                        }

                        @Override
                        public String getName() throws ParsingException {
                            final String name = getTextFromObject(info.getArray("flexColumns").getObject(0)
                                    .getObject("musicResponsiveListItemFlexColumnRenderer").getObject("text"));
                            if (!isNullOrEmpty(name)) {
                                return name;
                            }
                            throw new ParsingException("Could not get name");
                        }

                        @Override
                        public long getDuration() throws ParsingException {
                            final String duration = getTextFromObject(info.getArray("flexColumns").getObject(3)
                                    .getObject("musicResponsiveListItemFlexColumnRenderer").getObject("text"));
                            if (!isNullOrEmpty(duration)) {
                                return YoutubeParsingHelper.parseDurationString(duration);
                            }
                            throw new ParsingException("Could not get duration");
                        }

                        @Override
                        public String getUploaderName() throws ParsingException {
                            final String name = getTextFromObject(info.getArray("flexColumns").getObject(1)
                                    .getObject("musicResponsiveListItemFlexColumnRenderer").getObject("text"));
                            if (!isNullOrEmpty(name)) {
                                return name;
                            }
                            throw new ParsingException("Could not get uploader name");
                        }

                        @Override
                        public String getUploaderUrl() throws ParsingException {
                            if (searchType.equals(MUSIC_VIDEOS)) {
                                JsonArray items = info.getObject("menu").getObject("menuRenderer").getArray("items");
                                for (Object item : items) {
                                    final JsonObject menuNavigationItemRenderer = ((JsonObject) item).getObject("menuNavigationItemRenderer");
                                    if (menuNavigationItemRenderer.getObject("icon").getString("iconType", EMPTY_STRING).equals("ARTIST")) {
                                        return getUrlFromNavigationEndpoint(menuNavigationItemRenderer.getObject("navigationEndpoint"));
                                    }
                                }

                                return null;
                            } else {
                                final JsonObject navigationEndpointHolder = info.getArray("flexColumns")
                                        .getObject(1).getObject("musicResponsiveListItemFlexColumnRenderer")
                                        .getObject("text").getArray("runs").getObject(0);

                                if (!navigationEndpointHolder.has("navigationEndpoint")) return null;

                                final String url = getUrlFromNavigationEndpoint(navigationEndpointHolder.getObject("navigationEndpoint"));

                                if (!isNullOrEmpty(url)) {
                                    return url;
                                }

                                throw new ParsingException("Could not get uploader URL");
                            }
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
                            if (searchType.equals(MUSIC_SONGS)) {
                                return -1;
                            }
                            final String viewCount = getTextFromObject(info.getArray("flexColumns").getObject(2)
                                    .getObject("musicResponsiveListItemFlexColumnRenderer").getObject("text"));
                            if (!isNullOrEmpty(viewCount)) {
                                return Utils.mixedNumberWordToLong(viewCount);
                            }
                            throw new ParsingException("Could not get view count");
                        }

                        @Override
                        public String getThumbnailUrl() throws ParsingException {
                            try {
                                final JsonArray thumbnails = info.getObject("thumbnail").getObject("musicThumbnailRenderer")
                                        .getObject("thumbnail").getArray("thumbnails");
                                // the last thumbnail is the one with the highest resolution
                                final String url = thumbnails.getObject(thumbnails.size() - 1).getString("url");

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
                                final JsonArray thumbnails = info.getObject("thumbnail").getObject("musicThumbnailRenderer")
                                        .getObject("thumbnail").getArray("thumbnails");
                                // the last thumbnail is the one with the highest resolution
                                final String url = thumbnails.getObject(thumbnails.size() - 1).getString("url");

                                return fixThumbnailUrl(url);
                            } catch (Exception e) {
                                throw new ParsingException("Could not get thumbnail url", e);
                            }
                        }

                        @Override
                        public String getName() throws ParsingException {
                            final String name = getTextFromObject(info.getArray("flexColumns").getObject(0)
                                    .getObject("musicResponsiveListItemFlexColumnRenderer").getObject("text"));
                            if (!isNullOrEmpty(name)) {
                                return name;
                            }
                            throw new ParsingException("Could not get name");
                        }

                        @Override
                        public String getUrl() throws ParsingException {
                            final String url = getUrlFromNavigationEndpoint(info.getObject("navigationEndpoint"));
                            if (!isNullOrEmpty(url)) {
                                return url;
                            }
                            throw new ParsingException("Could not get url");
                        }

                        @Override
                        public long getSubscriberCount() throws ParsingException {
                            final String viewCount = getTextFromObject(info.getArray("flexColumns").getObject(2)
                                    .getObject("musicResponsiveListItemFlexColumnRenderer").getObject("text"));
                            if (!isNullOrEmpty(viewCount)) {
                                return Utils.mixedNumberWordToLong(viewCount);
                            }
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
                                final JsonArray thumbnails = info.getObject("thumbnail").getObject("musicThumbnailRenderer")
                                        .getObject("thumbnail").getArray("thumbnails");
                                // the last thumbnail is the one with the highest resolution
                                final String url = thumbnails.getObject(thumbnails.size() - 1).getString("url");

                                return fixThumbnailUrl(url);
                            } catch (Exception e) {
                                throw new ParsingException("Could not get thumbnail url", e);
                            }
                        }

                        @Override
                        public String getName() throws ParsingException {
                            final String name = getTextFromObject(info.getArray("flexColumns").getObject(0)
                                    .getObject("musicResponsiveListItemFlexColumnRenderer").getObject("text"));
                            if (!isNullOrEmpty(name)) {
                                return name;
                            }
                            throw new ParsingException("Could not get name");
                        }

                        @Override
                        public String getUrl() throws ParsingException {
                            final String url = getUrlFromNavigationEndpoint(info.getObject("doubleTapCommand"));
                            if (!isNullOrEmpty(url)) {
                                return url;
                            }
                            throw new ParsingException("Could not get url");
                        }

                        @Override
                        public String getUploaderName() throws ParsingException {
                            final String name;
                            if (searchType.equals(MUSIC_ALBUMS)) {
                                name = getTextFromObject(info.getArray("flexColumns").getObject(2)
                                        .getObject("musicResponsiveListItemFlexColumnRenderer").getObject("text"));
                            } else {
                                name = getTextFromObject(info.getArray("flexColumns").getObject(1)
                                        .getObject("musicResponsiveListItemFlexColumnRenderer").getObject("text"));
                            }
                            if (!isNullOrEmpty(name)) {
                                return name;
                            }
                            throw new ParsingException("Could not get uploader name");
                        }

                        @Override
                        public long getStreamCount() throws ParsingException {
                            if (searchType.equals(MUSIC_ALBUMS)) {
                                return ITEM_COUNT_UNKNOWN;
                            }
                            final String count = getTextFromObject(info.getArray("flexColumns").getObject(2)
                                    .getObject("musicResponsiveListItemFlexColumnRenderer").getObject("text"));
                            if (!isNullOrEmpty(count)) {
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

    private Page getNextPageFrom(final JsonArray continuations) throws ParsingException, IOException, ReCaptchaException {
        if (isNullOrEmpty(continuations)) {
            return null;
        }

        final JsonObject nextContinuationData = continuations.getObject(0).getObject("nextContinuationData");
        final String continuation = nextContinuationData.getString("continuation");
        final String clickTrackingParams = nextContinuationData.getString("clickTrackingParams");

        return new Page("https://music.youtube.com/youtubei/v1/search?ctoken=" + continuation
                + "&continuation=" + continuation + "&itct=" + clickTrackingParams + "&alt=json"
                + "&key=" + YoutubeParsingHelper.getYoutubeMusicKeys()[0]);
    }
}
