package org.schabi.newpipe.extractor.services.youtube.extractors;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.DISABLE_PRETTY_PRINT_PARAMETER;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.YOUTUBEI_V1_URL;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.extractPlaylistTypeFromPlaylistUrl;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getJsonPostResponse;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getKey;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getImagesFromThumbnailsArray;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getUrlFromNavigationEndpoint;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonWriter;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.playlist.PlaylistInfo;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class YoutubePlaylistExtractor extends PlaylistExtractor {
    // Names of some objects in JSON response frequently used in this class
    private static final String PLAYLIST_VIDEO_RENDERER = "playlistVideoRenderer";
    private static final String PLAYLIST_VIDEO_LIST_RENDERER = "playlistVideoListRenderer";
    private static final String RICH_GRID_RENDERER = "richGridRenderer";
    private static final String RICH_ITEM_RENDERER = "richItemRenderer";
    private static final String REEL_ITEM_RENDERER = "reelItemRenderer";
    private static final String SIDEBAR = "sidebar";
    private static final String VIDEO_OWNER_RENDERER = "videoOwnerRenderer";

    private JsonObject browseResponse;

    private JsonObject playlistInfo;
    private JsonObject uploaderInfo;
    private JsonObject playlistHeader;

    private boolean isNewPlaylistInterface;

    public YoutubePlaylistExtractor(final StreamingService service,
                                    final ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) throws IOException,
            ExtractionException {
        final Localization localization = getExtractorLocalization();
        final byte[] body = JsonWriter.string(prepareDesktopJsonBuilder(localization,
                        getExtractorContentCountry())
                        .value("browseId", "VL" + getId())
                        .value("params", "wgYCCAA%3D") // Show unavailable videos
                        .done())
                .getBytes(StandardCharsets.UTF_8);

        browseResponse = getJsonPostResponse("browse", body, localization);
        YoutubeParsingHelper.defaultAlertsCheck(browseResponse);
        isNewPlaylistInterface = checkIfResponseIsNewPlaylistInterface();
    }

    /**
     * Whether the playlist response is using only the new playlist design.
     *
     * <p>
     * This new response changes how metadata is returned, and does not provide author thumbnails.
     * </p>
     *
     * <p>
     * The new response can be detected by checking whether a header JSON object is returned in the
     * browse response (the old returns instead a sidebar one).
     * </p>
     *
     * @return Whether the playlist response is using only the new playlist design
     */
    private boolean checkIfResponseIsNewPlaylistInterface() {
        // The "old" playlist UI can be also returned with the new one
        return browseResponse.has("header") && !browseResponse.has(SIDEBAR);
    }

    @Nonnull
    private JsonObject getUploaderInfo() throws ParsingException {
        if (uploaderInfo == null) {
            uploaderInfo = browseResponse.getObject(SIDEBAR)
                    .getObject("playlistSidebarRenderer")
                    .getArray("items")
                    .stream()
                    .filter(JsonObject.class::isInstance)
                    .map(JsonObject.class::cast)
                    .filter(item -> item.getObject("playlistSidebarSecondaryInfoRenderer")
                            .getObject("videoOwner")
                            .has(VIDEO_OWNER_RENDERER))
                    .map(item -> item.getObject("playlistSidebarSecondaryInfoRenderer")
                            .getObject("videoOwner")
                            .getObject(VIDEO_OWNER_RENDERER))
                    .findFirst()
                    .orElseThrow(() -> new ParsingException("Could not get uploader info"));
        }

        return uploaderInfo;
    }

    @Nonnull
    private JsonObject getPlaylistInfo() throws ParsingException {
        if (playlistInfo == null) {
            playlistInfo = browseResponse.getObject(SIDEBAR)
                    .getObject("playlistSidebarRenderer")
                    .getArray("items")
                    .stream()
                    .filter(JsonObject.class::isInstance)
                    .map(JsonObject.class::cast)
                    .filter(item -> item.has("playlistSidebarPrimaryInfoRenderer"))
                    .map(item -> item.getObject("playlistSidebarPrimaryInfoRenderer"))
                    .findFirst()
                    .orElseThrow(() -> new ParsingException("Could not get playlist info"));
        }

        return playlistInfo;
    }

    @Nonnull
    private JsonObject getPlaylistHeader() {
        if (playlistHeader == null) {
            playlistHeader = browseResponse.getObject("header")
                    .getObject("playlistHeaderRenderer");
        }

        return playlistHeader;
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        final String name = getTextFromObject(getPlaylistInfo().getObject("title"));
        if (!isNullOrEmpty(name)) {
            return name;
        }

        return browseResponse.getObject("microformat")
                .getObject("microformatDataRenderer")
                .getString("title");
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() throws ParsingException {
        final JsonArray playlistMetadataThumbnailsArray;
        if (isNewPlaylistInterface) {
            playlistMetadataThumbnailsArray = getPlaylistHeader().getObject("playlistHeaderBanner")
                    .getObject("heroPlaylistThumbnailRenderer")
                    .getObject("thumbnail")
                    .getArray("thumbnails");
        } else {
            playlistMetadataThumbnailsArray = playlistInfo.getObject("thumbnailRenderer")
                    .getObject("playlistVideoThumbnailRenderer")
                    .getObject("thumbnail")
                    .getArray("thumbnails");
        }

        if (!isNullOrEmpty(playlistMetadataThumbnailsArray)) {
            return getImagesFromThumbnailsArray(playlistMetadataThumbnailsArray);
        }

        // This data structure is returned in both layouts
        final JsonArray microFormatThumbnailsArray = browseResponse.getObject("microformat")
                    .getObject("microformatDataRenderer")
                    .getObject("thumbnail")
                    .getArray("thumbnails");

        if (!isNullOrEmpty(microFormatThumbnailsArray)) {
            return getImagesFromThumbnailsArray(microFormatThumbnailsArray);
        }

        throw new ParsingException("Could not get playlist thumbnails");
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        try {
            return getUrlFromNavigationEndpoint(isNewPlaylistInterface
                    ? getPlaylistHeader().getObject("ownerText")
                    .getArray("runs")
                    .getObject(0)
                    .getObject("navigationEndpoint")
                    : getUploaderInfo().getObject("navigationEndpoint"));
        } catch (final Exception e) {
            throw new ParsingException("Could not get playlist uploader url", e);
        }
    }

    @Override
    public String getUploaderName() throws ParsingException {
        try {
            return getTextFromObject(isNewPlaylistInterface
                    ? getPlaylistHeader().getObject("ownerText")
                    : getUploaderInfo().getObject("title"));
        } catch (final Exception e) {
            throw new ParsingException("Could not get playlist uploader name", e);
        }
    }

    @Nonnull
    @Override
    public List<Image> getUploaderAvatars() throws ParsingException {
        if (isNewPlaylistInterface) {
            // The new playlist interface doesn't provide an uploader avatar
            return List.of();
        }

        try {
            return getImagesFromThumbnailsArray(getUploaderInfo().getObject("thumbnail")
                    .getArray("thumbnails"));
        } catch (final Exception e) {
            throw new ParsingException("Could not get playlist uploader avatars", e);
        }
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        // YouTube doesn't provide this information
        return false;
    }

    @Override
    public long getStreamCount() throws ParsingException {
        if (isNewPlaylistInterface) {
            final String numVideosText =
                    getTextFromObject(getPlaylistHeader().getObject("numVideosText"));
            if (numVideosText != null) {
                try {
                    return Long.parseLong(Utils.removeNonDigitCharacters(numVideosText));
                } catch (final NumberFormatException ignored) {
                }
            }

            final String firstByLineRendererText = getTextFromObject(
                    getPlaylistHeader().getArray("byline")
                            .getObject(0)
                            .getObject("text"));

            if (firstByLineRendererText != null) {
                try {
                    return Long.parseLong(Utils.removeNonDigitCharacters(firstByLineRendererText));
                } catch (final NumberFormatException ignored) {
                }
            }
        }

        // These data structures are returned in both layouts
        final JsonArray briefStats =
                (isNewPlaylistInterface ? getPlaylistHeader() : getPlaylistInfo())
                        .getArray("briefStats");
        if (!briefStats.isEmpty()) {
            final String briefsStatsText = getTextFromObject(briefStats.getObject(0));
            if (briefsStatsText != null) {
                return Long.parseLong(Utils.removeNonDigitCharacters(briefsStatsText));
            }
        }

        final JsonArray stats = (isNewPlaylistInterface ? getPlaylistHeader() : getPlaylistInfo())
                .getArray("stats");
        if (!stats.isEmpty()) {
            final String statsText = getTextFromObject(stats.getObject(0));
            if (statsText != null) {
                return Long.parseLong(Utils.removeNonDigitCharacters(statsText));
            }
        }

        return ITEM_COUNT_UNKNOWN;
    }

    @Nonnull
    @Override
    public Description getDescription() throws ParsingException {
        final String description = getTextFromObject(
                getPlaylistInfo().getObject("description"),
                true
        );

        return new Description(description, Description.HTML);
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws IOException, ExtractionException {
        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        Page nextPage = null;

        final JsonArray contents = browseResponse.getObject("contents")
                .getObject("twoColumnBrowseResultsRenderer")
                .getArray("tabs")
                .getObject(0)
                .getObject("tabRenderer")
                .getObject("content")
                .getObject("sectionListRenderer")
                .getArray("contents");

        final JsonObject videoPlaylistObject = contents.stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .map(content -> content.getObject("itemSectionRenderer")
                        .getArray("contents")
                        .getObject(0))
                .filter(content -> content.has(PLAYLIST_VIDEO_LIST_RENDERER)
                        || content.has(RICH_GRID_RENDERER))
                .findFirst()
                .orElse(null);

        if (videoPlaylistObject != null) {
            final JsonObject renderer;
            if (videoPlaylistObject.has(PLAYLIST_VIDEO_LIST_RENDERER)) {
                renderer = videoPlaylistObject.getObject(PLAYLIST_VIDEO_LIST_RENDERER);
            } else if (videoPlaylistObject.has(RICH_GRID_RENDERER)) {
                renderer = videoPlaylistObject.getObject(RICH_GRID_RENDERER);
            } else {
                return new InfoItemsPage<>(collector, null);
            }

            final JsonArray videosArray = renderer.getArray("contents");
            collectStreamsFrom(collector, videosArray);

            nextPage = getNextPageFrom(videosArray);
        }

        return new InfoItemsPage<>(collector, nextPage);
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(final Page page) throws IOException,
            ExtractionException {
        if (page == null || isNullOrEmpty(page.getUrl())) {
            throw new IllegalArgumentException("Page doesn't contain an URL");
        }

        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

        final JsonObject ajaxJson = getJsonPostResponse("browse", page.getBody(),
                getExtractorLocalization());

        final JsonArray continuation = ajaxJson.getArray("onResponseReceivedActions")
                .getObject(0)
                .getObject("appendContinuationItemsAction")
                .getArray("continuationItems");

        collectStreamsFrom(collector, continuation);

        return new InfoItemsPage<>(collector, getNextPageFrom(continuation));
    }

    @Nullable
    private Page getNextPageFrom(final JsonArray contents)
            throws IOException, ExtractionException {
        if (isNullOrEmpty(contents)) {
            return null;
        }

        final JsonObject lastElement = contents.getObject(contents.size() - 1);
        if (lastElement.has("continuationItemRenderer")) {
            final String continuation = lastElement
                    .getObject("continuationItemRenderer")
                    .getObject("continuationEndpoint")
                    .getObject("continuationCommand")
                    .getString("token");

            final byte[] body = JsonWriter.string(prepareDesktopJsonBuilder(
                            getExtractorLocalization(), getExtractorContentCountry())
                            .value("continuation", continuation)
                            .done())
                    .getBytes(StandardCharsets.UTF_8);

            return new Page(YOUTUBEI_V1_URL + "browse?key=" + getKey()
                    + DISABLE_PRETTY_PRINT_PARAMETER, body);
        } else {
            return null;
        }
    }

    private void collectStreamsFrom(@Nonnull final StreamInfoItemsCollector collector,
                                    @Nonnull final JsonArray videos) {
        final TimeAgoParser timeAgoParser = getTimeAgoParser();
        videos.stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .forEach(video -> {
                    if (video.has(PLAYLIST_VIDEO_RENDERER)) {
                        collector.commit(new YoutubeStreamInfoItemExtractor(
                                video.getObject(PLAYLIST_VIDEO_RENDERER), timeAgoParser));
                    } else if (video.has(RICH_ITEM_RENDERER)) {
                        final JsonObject richItemRenderer = video.getObject(RICH_ITEM_RENDERER);
                        if (richItemRenderer.has("content")) {
                            final JsonObject richItemRendererContent =
                                    richItemRenderer.getObject("content");
                            if (richItemRendererContent.has(REEL_ITEM_RENDERER)) {
                                collector.commit(new YoutubeReelInfoItemExtractor(
                                        richItemRendererContent.getObject(REEL_ITEM_RENDERER),
                                        timeAgoParser));
                            }
                        }
                    }
                });
    }

    @Nonnull
    @Override
    public PlaylistInfo.PlaylistType getPlaylistType() throws ParsingException {
        return extractPlaylistTypeFromPlaylistUrl(getUrl());
    }
}
