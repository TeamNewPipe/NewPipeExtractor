package org.schabi.newpipe.extractor.services.youtube.extractors;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.DISABLE_PRETTY_PRINT_PARAMETER;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.fixThumbnailUrl;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getUrlFromNavigationEndpoint;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getValidJsonResponseBody;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getYoutubeMusicHeaders;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.MUSIC_ALBUMS;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.MUSIC_ARTISTS;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.MUSIC_PLAYLISTS;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.MUSIC_SONGS;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.MUSIC_VIDEOS;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.grack.nanojson.JsonWriter;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.MetaInfo;
import org.schabi.newpipe.extractor.MultiInfoItemsCollector;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class YoutubeMusicSearchExtractor extends SearchExtractor {
    private JsonObject initialData;

    public YoutubeMusicSearchExtractor(final StreamingService service,
                                       final SearchQueryHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        final String[] youtubeMusicKeys = YoutubeParsingHelper.getYoutubeMusicKey();

        final String url = "https://music.youtube.com/youtubei/v1/search?alt=json&key="
                + youtubeMusicKeys[0] + DISABLE_PRETTY_PRINT_PARAMETER;

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
        final byte[] json = JsonWriter.string()
            .object()
                .object("context")
                    .object("client")
                        .value("clientName", "WEB_REMIX")
                        .value("clientVersion", youtubeMusicKeys[2])
                        .value("hl", "en-GB")
                        .value("gl", getExtractorContentCountry().getCountryCode())
                        .array("experimentIds").end()
                        .value("experimentsToken", "")
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
                        // TO DO: provide a way to enable restricted mode with:
                        .value("enableSafetyMode", false)
                    .end()
                .end()
                .value("query", getSearchString())
                .value("params", params)
            .end().done().getBytes(StandardCharsets.UTF_8);
        // @formatter:on

        final String responseBody = getValidJsonResponseBody(
                getDownloader().postWithContentTypeJson(url, getYoutubeMusicHeaders(), json));

        try {
            initialData = JsonParser.object().from(responseBody);
        } catch (final JsonParserException e) {
            throw new ParsingException("Could not parse JSON", e);
        }
    }

    private List<JsonObject> getItemSectionRendererContents() {
        return initialData
                .getObject("contents")
                .getObject("tabbedSearchResultsRenderer")
                .getArray("tabs")
                .getObject(0)
                .getObject("tabRenderer")
                .getObject("content")
                .getObject("sectionListRenderer")
                .getArray("contents")
                .stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .map(c -> c.getObject("itemSectionRenderer"))
                .filter(isr -> !isr.isEmpty())
                .map(isr -> isr
                        .getArray("contents")
                        .getObject(0))
                .collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public String getSearchSuggestion() throws ParsingException {
        for (final JsonObject obj : getItemSectionRendererContents()) {
            final JsonObject didYouMeanRenderer = obj
                    .getObject("didYouMeanRenderer");
            final JsonObject showingResultsForRenderer = obj
                    .getObject("showingResultsForRenderer");

            if (!didYouMeanRenderer.isEmpty()) {
                return getTextFromObject(didYouMeanRenderer.getObject("correctedQuery"));
            } else if (!showingResultsForRenderer.isEmpty()) {
                return JsonUtils.getString(showingResultsForRenderer,
                        "correctedQueryEndpoint.searchEndpoint.query");
            }
        }

        return "";
    }

    @Override
    public boolean isCorrectedSearch() throws ParsingException {
        return getItemSectionRendererContents()
                .stream()
                .anyMatch(obj -> obj.has("showingResultsForRenderer"));
    }

    @Nonnull
    @Override
    public List<MetaInfo> getMetaInfo() {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws IOException, ExtractionException {
        final MultiInfoItemsCollector collector = new MultiInfoItemsCollector(getServiceId());

        final JsonArray contents = JsonUtils.getArray(JsonUtils.getArray(initialData,
                "contents.tabbedSearchResultsRenderer.tabs").getObject(0),
                "tabRenderer.content.sectionListRenderer.contents");

        Page nextPage = null;

        for (final Object content : contents) {
            if (((JsonObject) content).has("musicShelfRenderer")) {
                final JsonObject musicShelfRenderer = ((JsonObject) content)
                        .getObject("musicShelfRenderer");

                collectMusicStreamsFrom(collector, musicShelfRenderer.getArray("contents"));

                nextPage = getNextPageFrom(musicShelfRenderer.getArray("continuations"));
            }
        }

        return new InfoItemsPage<>(collector, nextPage);
    }

    @Override
    public InfoItemsPage<InfoItem> getPage(final Page page)
            throws IOException, ExtractionException {
        if (page == null || isNullOrEmpty(page.getUrl())) {
            throw new IllegalArgumentException("Page doesn't contain an URL");
        }

        final MultiInfoItemsCollector collector = new MultiInfoItemsCollector(getServiceId());

        final String[] youtubeMusicKeys = YoutubeParsingHelper.getYoutubeMusicKey();

        // @formatter:off
        final byte[] json = JsonWriter.string()
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
            .end().done().getBytes(StandardCharsets.UTF_8);
        // @formatter:on

        final String responseBody = getValidJsonResponseBody(
                getDownloader().postWithContentTypeJson(
                        page.getUrl(), getYoutubeMusicHeaders(), json));

        final JsonObject ajaxJson;
        try {
            ajaxJson = JsonParser.object().from(responseBody);
        } catch (final JsonParserException e) {
            throw new ParsingException("Could not parse JSON", e);
        }

        final JsonObject musicShelfContinuation = ajaxJson.getObject("continuationContents")
                .getObject("musicShelfContinuation");

        collectMusicStreamsFrom(collector, musicShelfContinuation.getArray("contents"));
        final JsonArray continuations = musicShelfContinuation.getArray("continuations");

        return new InfoItemsPage<>(collector, getNextPageFrom(continuations));
    }

    @SuppressWarnings("MethodLength")
    private void collectMusicStreamsFrom(final MultiInfoItemsCollector collector,
                                         @Nonnull final JsonArray videos) {
        final TimeAgoParser timeAgoParser = getTimeAgoParser();

        for (final Object item : videos) {
            final JsonObject info = ((JsonObject) item)
                    .getObject("musicResponsiveListItemRenderer", null);
            if (info != null) {
                final String displayPolicy = info.getString("musicItemRendererDisplayPolicy",
                        "");
                if (displayPolicy.equals("MUSIC_ITEM_RENDERER_DISPLAY_POLICY_GREY_OUT")) {
                    continue; // No info about video URL available
                }

                final JsonObject flexColumnRenderer = info.getArray("flexColumns")
                        .getObject(1)
                        .getObject("musicResponsiveListItemFlexColumnRenderer");
                final JsonArray descriptionElements = flexColumnRenderer.getObject("text")
                        .getArray("runs");
                final String searchType = getLinkHandler().getContentFilters().get(0);
                if (searchType.equals(MUSIC_SONGS) || searchType.equals(MUSIC_VIDEOS)) {
                    collector.commit(new YoutubeStreamInfoItemExtractor(info, timeAgoParser) {
                        @Override
                        public String getUrl() throws ParsingException {
                            final String id = info.getObject("playlistItemData")
                                    .getString("videoId");
                            if (!isNullOrEmpty(id)) {
                                return "https://music.youtube.com/watch?v=" + id;
                            }
                            throw new ParsingException("Could not get url");
                        }

                        @Override
                        public String getName() throws ParsingException {
                            final String name = getTextFromObject(info.getArray("flexColumns")
                                    .getObject(0)
                                    .getObject("musicResponsiveListItemFlexColumnRenderer")
                                    .getObject("text"));
                            if (!isNullOrEmpty(name)) {
                                return name;
                            }
                            throw new ParsingException("Could not get name");
                        }

                        @Override
                        public long getDuration() throws ParsingException {
                            final String duration = descriptionElements
                                    .getObject(descriptionElements.size() - 1)
                                    .getString("text");
                            if (!isNullOrEmpty(duration)) {
                                return YoutubeParsingHelper.parseDurationString(duration);
                            }
                            throw new ParsingException("Could not get duration");
                        }

                        @Override
                        public String getUploaderName() throws ParsingException {
                            final String name = descriptionElements.getObject(0).getString("text");
                            if (!isNullOrEmpty(name)) {
                                return name;
                            }
                            throw new ParsingException("Could not get uploader name");
                        }

                        @Override
                        public String getUploaderUrl() throws ParsingException {
                            if (searchType.equals(MUSIC_VIDEOS)) {
                                final JsonArray items = info.getObject("menu")
                                        .getObject("menuRenderer")
                                        .getArray("items");
                                for (final Object item : items) {
                                    final JsonObject menuNavigationItemRenderer =
                                            ((JsonObject) item).getObject(
                                                    "menuNavigationItemRenderer");
                                    if (menuNavigationItemRenderer.getObject("icon")
                                            .getString("iconType", "")
                                            .equals("ARTIST")) {
                                        return getUrlFromNavigationEndpoint(
                                                menuNavigationItemRenderer
                                                        .getObject("navigationEndpoint"));
                                    }
                                }

                                return null;
                            } else {
                                final JsonObject navigationEndpointHolder = info
                                        .getArray("flexColumns")
                                        .getObject(1)
                                        .getObject("musicResponsiveListItemFlexColumnRenderer")
                                        .getObject("text").getArray("runs").getObject(0);

                                if (!navigationEndpointHolder.has("navigationEndpoint")) {
                                    return null;
                                }

                                final String url = getUrlFromNavigationEndpoint(
                                        navigationEndpointHolder.getObject("navigationEndpoint"));

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
                            final String viewCount = descriptionElements
                                    .getObject(descriptionElements.size() - 3)
                                    .getString("text");
                            if (!isNullOrEmpty(viewCount)) {
                                try {
                                    return Utils.mixedNumberWordToLong(viewCount);
                                } catch (final Parser.RegexException e) {
                                    // probably viewCount == "No views" or similar
                                    return 0;
                                }
                            }
                            throw new ParsingException("Could not get view count");
                        }

                        @Override
                        public String getThumbnailUrl() throws ParsingException {
                            try {
                                final JsonArray thumbnails = info.getObject("thumbnail")
                                        .getObject("musicThumbnailRenderer")
                                        .getObject("thumbnail").getArray("thumbnails");
                                // the last thumbnail is the one with the highest resolution
                                final String url = thumbnails.getObject(thumbnails.size() - 1)
                                        .getString("url");

                                return fixThumbnailUrl(url);
                            } catch (final Exception e) {
                                throw new ParsingException("Could not get thumbnail url", e);
                            }
                        }
                    });
                } else if (searchType.equals(MUSIC_ARTISTS)) {
                    collector.commit(new YoutubeChannelInfoItemExtractor(info) {
                        @Override
                        public String getThumbnailUrl() throws ParsingException {
                            try {
                                final JsonArray thumbnails = info.getObject("thumbnail")
                                        .getObject("musicThumbnailRenderer")
                                        .getObject("thumbnail").getArray("thumbnails");
                                // the last thumbnail is the one with the highest resolution
                                final String url = thumbnails.getObject(thumbnails.size() - 1)
                                        .getString("url");

                                return fixThumbnailUrl(url);
                            } catch (final Exception e) {
                                throw new ParsingException("Could not get thumbnail url", e);
                            }
                        }

                        @Override
                        public String getName() throws ParsingException {
                            final String name = getTextFromObject(info.getArray("flexColumns")
                                    .getObject(0)
                                    .getObject("musicResponsiveListItemFlexColumnRenderer")
                                    .getObject("text"));
                            if (!isNullOrEmpty(name)) {
                                return name;
                            }
                            throw new ParsingException("Could not get name");
                        }

                        @Override
                        public String getUrl() throws ParsingException {
                            final String url = getUrlFromNavigationEndpoint(info
                                    .getObject("navigationEndpoint"));
                            if (!isNullOrEmpty(url)) {
                                return url;
                            }
                            throw new ParsingException("Could not get url");
                        }

                        @Override
                        public long getSubscriberCount() throws ParsingException {
                            final String subscriberCount = getTextFromObject(info
                                    .getArray("flexColumns").getObject(2)
                                    .getObject("musicResponsiveListItemFlexColumnRenderer")
                                    .getObject("text"));
                            if (!isNullOrEmpty(subscriberCount)) {
                                try {
                                    return Utils.mixedNumberWordToLong(subscriberCount);
                                } catch (final Parser.RegexException ignored) {
                                    // probably subscriberCount == "No subscribers" or similar
                                    return 0;
                                }
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
                                final JsonArray thumbnails = info.getObject("thumbnail")
                                        .getObject("musicThumbnailRenderer")
                                        .getObject("thumbnail").getArray("thumbnails");
                                // the last thumbnail is the one with the highest resolution
                                final String url = thumbnails.getObject(thumbnails.size() - 1)
                                        .getString("url");

                                return fixThumbnailUrl(url);
                            } catch (final Exception e) {
                                throw new ParsingException("Could not get thumbnail url", e);
                            }
                        }

                        @Override
                        public String getName() throws ParsingException {
                            final String name = getTextFromObject(info.getArray("flexColumns")
                                    .getObject(0)
                                    .getObject("musicResponsiveListItemFlexColumnRenderer")
                                    .getObject("text"));
                            if (!isNullOrEmpty(name)) {
                                return name;
                            }
                            throw new ParsingException("Could not get name");
                        }

                        @Override
                        public String getUrl() throws ParsingException {
                            String playlistId = info.getObject("menu")
                                    .getObject("menuRenderer")
                                    .getArray("items")
                                    .getObject(4)
                                    .getObject("toggleMenuServiceItemRenderer")
                                    .getObject("toggledServiceEndpoint")
                                    .getObject("likeEndpoint")
                                    .getObject("target")
                                    .getString("playlistId");

                            if (isNullOrEmpty(playlistId)) {
                                playlistId = info.getObject("overlay")
                                        .getObject("musicItemThumbnailOverlayRenderer")
                                        .getObject("content")
                                        .getObject("musicPlayButtonRenderer")
                                        .getObject("playNavigationEndpoint")
                                        .getObject("watchPlaylistEndpoint")
                                        .getString("playlistId");
                            }
                            if (!isNullOrEmpty(playlistId)) {
                                return "https://music.youtube.com/playlist?list=" + playlistId;
                            }
                            throw new ParsingException("Could not get url");
                        }

                        @Override
                        public String getUploaderName() throws ParsingException {
                            final String name;
                            if (searchType.equals(MUSIC_ALBUMS)) {
                                name = descriptionElements.getObject(2).getString("text");
                            } else {
                                name = descriptionElements.getObject(0).getString("text");
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
                            final String count = descriptionElements.getObject(2)
                                    .getString("text");
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

    @Nullable
    private Page getNextPageFrom(final JsonArray continuations)
            throws IOException, ParsingException, ReCaptchaException {
        if (isNullOrEmpty(continuations)) {
            return null;
        }

        final JsonObject nextContinuationData = continuations.getObject(0)
                .getObject("nextContinuationData");
        final String continuation = nextContinuationData.getString("continuation");

        return new Page("https://music.youtube.com/youtubei/v1/search?ctoken=" + continuation
                + "&continuation=" + continuation + "&alt=json" + "&key="
                + YoutubeParsingHelper.getYoutubeMusicKey()[0]);
    }
}
