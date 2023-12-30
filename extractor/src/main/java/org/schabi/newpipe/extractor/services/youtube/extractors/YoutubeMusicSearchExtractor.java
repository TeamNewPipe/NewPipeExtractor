package org.schabi.newpipe.extractor.services.youtube.extractors;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.DISABLE_PRETTY_PRINT_PARAMETER;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObjectOrThrow;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getValidJsonResponseBody;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getYoutubeMusicHeaders;
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
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.search.filter.FilterContainer;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.search.filter.YoutubeFilters;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class YoutubeMusicSearchExtractor extends SearchExtractor {
    private JsonObject initialData;

    public YoutubeMusicSearchExtractor(final StreamingService service,
                                       final SearchQueryHandler linkHandler) {
        super(service, linkHandler);
    }

    private int getSearchTypeId() {
        final YoutubeFilters.MusicYoutubeContentFilterItem contentFilterItem =
                Utils.getFirstContentFilterItem(getLinkHandler());
        if (contentFilterItem != null) {
            return contentFilterItem.getIdentifier();
        }
        return FilterContainer.ITEM_IDENTIFIER_UNKNOWN;
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        final String[] youtubeMusicKeys = YoutubeParsingHelper.getYoutubeMusicKey();

        final String url = "https://music.youtube.com/youtubei/v1/search?key="
                + youtubeMusicKeys[0] + DISABLE_PRETTY_PRINT_PARAMETER;


        final YoutubeFilters.MusicYoutubeContentFilterItem contentFilterItem =
                Utils.getFirstContentFilterItem(getLinkHandler());
        // Get the search parameter for the request. If getParams() be null
        // (which should never happen - only in test cases), JsonWriter.string() can handle it
        final String params = (contentFilterItem != null) ? contentFilterItem.getParams() : null;

        // @formatter:off
        final byte[] json = JsonWriter.string()
            .object()
                .object("context")
                    .object("client")
                        .value("clientName", "WEB_REMIX")
                        .value("clientVersion", youtubeMusicKeys[2])
                        .value("hl", "en-GB")
                        .value("gl", getExtractorContentCountry().getCountryCode())
                        .value("platform", "DESKTOP")
                        .value("utcOffsetMinutes", 0)
                    .end()
                    .object("request")
                        .array("internalExperimentFlags")
                        .end()
                        .value("useSsl", true)
                    .end()
                    .object("user")
                        // TODO: provide a way to enable restricted mode with:
                        //  .value("enableSafetyMode", boolean)
                        .value("lockedSafetyMode", false)
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
                return getTextFromObjectOrThrow(
                        didYouMeanRenderer.getObject("correctedQuery"),
                        "getting search suggestion from didYouMeanRenderer.correctedQuery");
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
                        .value("hl", "en-GB")
                        .value("gl", getExtractorContentCountry().getCountryCode())
                        .value("platform", "DESKTOP")
                        .value("utcOffsetMinutes", 0)
                    .end()
                    .object("request")
                        .array("internalExperimentFlags")
                        .end()
                        .value("useSsl", true)
                    .end()
                    .object("user")
                        // TODO: provide a way to enable restricted mode with:
                        //  .value("enableSafetyMode", boolean)
                        .value("lockedSafetyMode", false)
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

    private void collectMusicStreamsFrom(final MultiInfoItemsCollector collector,
                                         @Nonnull final JsonArray videos) {
        final int searchTypeId = getSearchTypeId();

        videos.stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .map(item -> item.getObject("musicResponsiveListItemRenderer", null))
                .filter(Objects::nonNull)
                .forEachOrdered(infoItem -> {
                    final String displayPolicy = infoItem.getString(
                            "musicItemRendererDisplayPolicy", "");
                    if (displayPolicy.equals("MUSIC_ITEM_RENDERER_DISPLAY_POLICY_GREY_OUT")) {
                        // No info about URL available
                        return;
                    }

                    final JsonArray descriptionElements = infoItem.getArray("flexColumns")
                            .getObject(1)
                            .getObject("musicResponsiveListItemFlexColumnRenderer")
                            .getObject("text")
                            .getArray("runs");

                    switch (searchTypeId) {
                        case YoutubeFilters.ID_CF_MAIN_YOUTUBE_MUSIC_SONGS:
                        case YoutubeFilters.ID_CF_MAIN_YOUTUBE_MUSIC_VIDEOS:
                            collector.commit(new YoutubeMusicSongOrVideoInfoItemExtractor(
                                    infoItem, descriptionElements, searchTypeId));
                            break;
                        case YoutubeFilters.ID_CF_MAIN_YOUTUBE_MUSIC_ARTISTS:
                            collector.commit(new YoutubeMusicArtistInfoItemExtractor(infoItem));
                            break;
                        case YoutubeFilters.ID_CF_MAIN_YOUTUBE_MUSIC_ALBUMS:
                        case YoutubeFilters.ID_CF_MAIN_YOUTUBE_MUSIC_PLAYLISTS:
                            collector.commit(new YoutubeMusicAlbumOrPlaylistInfoItemExtractor(
                                    infoItem, descriptionElements, searchTypeId));
                            break;
                    }
                });
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
                + "&continuation=" + continuation + "&key="
                + YoutubeParsingHelper.getYoutubeMusicKey()[0] + DISABLE_PRETTY_PRINT_PARAMETER);
    }
}
