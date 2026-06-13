package org.schabi.newpipe.extractor.services.youtube.extractors;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.DISABLE_PRETTY_PRINT_PARAMETER;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.YOUTUBEI_V1_URL;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.extractPlaylistTypeFromPlaylistUrl;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getJsonPostResponse;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getImagesFromThumbnailsArray;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getUrlFromNavigationEndpoint;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder;
import static org.schabi.newpipe.extractor.services.youtube.protos.playlist.PlaylistProtobufContinuation.ContinuationParams;
import static org.schabi.newpipe.extractor.services.youtube.protos.playlist.PlaylistProtobufContinuation.PlaylistContinuation;
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
import java.util.Base64;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class YoutubePlaylistExtractor extends PlaylistExtractor {
    // Names of some objects in JSON response frequently used in this class
    private static final String PLAYLIST_VIDEO_RENDERER = "playlistVideoRenderer";
    private static final String RICH_ITEM_RENDERER = "richItemRenderer";
    private static final String REEL_ITEM_RENDERER = "reelItemRenderer";
    private static final String LOCKUP_VIEW_MODEL = "lockupViewModel";
    private static final String SIDEBAR = "sidebar";
    private static final String HEADER = "header";
    private static final String VIDEO_OWNER_RENDERER = "videoOwnerRenderer";
    private static final String MICROFORMAT = "microformat";
    private static final String COMMAND_EXECUTOR_COMMAND = "commandExecutorCommand";
    private static final String THUMBNAIL = "thumbnail";
    private static final String THUMBNAILS = "thumbnails";
    private static final String ON_RESPONSE_RECEIVED_ACTIONS = "onResponseReceivedActions";
    private static final String CONTINUATION_ITEMS = "continuationItems";
    private static final String APPEND_CONTINUATION_ITEMS_ACTION = "appendContinuationItemsAction";
    private static final String CONTINUATION_COMMAND = "continuationCommand";
    private static final String TITLE = "title";

    private static final String BROWSE_ENDPOINT = "browse";

    // Continuation properties requesting first page and showing unavailable videos
    private static final String PLAYLIST_CONTINUATION_PROPERTIES_BASE64 = "CADCBgIIAA%3D%3D";

    private JsonObject browseMetadataResponse;
    private JsonObject initialBrowseContinuationResponse;

    private JsonObject playlistInfo;
    private JsonObject uploaderInfo;
    private JsonObject playlistHeader;

    private boolean isNewPlaylistInterface;
    private Boolean isCoursePlaylist = null;

    public YoutubePlaylistExtractor(final StreamingService service,
                                    final ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) throws IOException,
            ExtractionException {
        final String playlistId = getId();

        final Localization localization = getExtractorLocalization();
        final byte[] body = JsonWriter.string(prepareDesktopJsonBuilder(localization,
                        getExtractorContentCountry())
                        .value("browseId", "VL" + playlistId)
                        .value("params", "wgYCCAA%3D") // Show unavailable videos
                        .done())
                .getBytes(StandardCharsets.UTF_8);

        browseMetadataResponse = getJsonPostResponse(BROWSE_ENDPOINT,
                List.of("$fields=" + SIDEBAR + "," + HEADER + "," + MICROFORMAT + ",alerts"),
                body,
                localization);

        YoutubeParsingHelper.defaultAlertsCheck(browseMetadataResponse);
        isNewPlaylistInterface = checkIfResponseIsNewPlaylistInterface();

        final PlaylistContinuation playlistContinuation = PlaylistContinuation.newBuilder()
                .setParameters(ContinuationParams.newBuilder()
                        .setBrowseId("VL" + playlistId)
                        .setPlaylistId(playlistId)
                        .setContinuationProperties(PLAYLIST_CONTINUATION_PROPERTIES_BASE64)
                        .build())
                .build();

        initialBrowseContinuationResponse = getJsonPostResponse(BROWSE_ENDPOINT,
                JsonWriter.string(prepareDesktopJsonBuilder(localization,
                        getExtractorContentCountry())
                        .value("continuation", Utils.encodeUrlUtf8(Base64.getUrlEncoder()
                                .encodeToString(playlistContinuation.toByteArray())))
                        .done())
                        .getBytes(StandardCharsets.UTF_8),
                localization);
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
        return browseMetadataResponse.has(HEADER) && !browseMetadataResponse.has(SIDEBAR);
    }

    @Nonnull
    private JsonObject getUploaderInfo() throws ParsingException {
        if (uploaderInfo == null) {
            uploaderInfo = browseMetadataResponse.getObject(SIDEBAR)
                    .getObject("playlistSidebarRenderer")
                    .getArray("items")
                    .streamAsJsonObjects()
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
            playlistInfo = browseMetadataResponse.getObject(SIDEBAR)
                    .getObject("playlistSidebarRenderer")
                    .getArray("items")
                    .streamAsJsonObjects()
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
            if (browseMetadataResponse == null) {
                return new JsonObject();
            }
            final JsonObject headerRenderer = browseMetadataResponse.getObject(HEADER)
                    .getObject("playlistHeaderRenderer");
            playlistHeader = headerRenderer != null ? headerRenderer : new JsonObject();
        }
        return playlistHeader;
    }

    private boolean isCoursePlaylist() {
        if (isCoursePlaylist == null) {
            isCoursePlaylist = getPlaylistHeader().getObject("onDescriptionTap")
                    .getObject(COMMAND_EXECUTOR_COMMAND)
                    .getArray("commands")
                    .streamAsJsonObjects()
                    .anyMatch(object -> "engagement-panel-course-metadata".equals(
                            object.getObject("showEngagementPanelEndpoint")
                                    .getObject("identifier")
                                    .getString("tag")));
        }

        return isCoursePlaylist;
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        final String name = getTextFromObject(getPlaylistInfo().getObject(TITLE));
        if (!isNullOrEmpty(name)) {
            return name;
        }

        return browseMetadataResponse.getObject(MICROFORMAT)
                .getObject("microformatDataRenderer")
                .getString(TITLE);
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() throws ParsingException {
        final JsonArray playlistMetadataThumbnailsArray;
        if (isNewPlaylistInterface) {
            playlistMetadataThumbnailsArray = getPlaylistHeader().getObject("playlistHeaderBanner")
                    .getObject("heroPlaylistThumbnailRenderer")
                    .getObject(THUMBNAIL)
                    .getArray(THUMBNAILS);
        } else {
            playlistMetadataThumbnailsArray = playlistInfo.getObject("thumbnailRenderer")
                    .getObject("playlistVideoThumbnailRenderer")
                    .getObject(THUMBNAIL)
                    .getArray(THUMBNAILS);
        }

        if (!isNullOrEmpty(playlistMetadataThumbnailsArray)) {
            return getImagesFromThumbnailsArray(playlistMetadataThumbnailsArray);
        }

        // This data structure is returned in both layouts
        final JsonArray microFormatThumbnailsArray = browseMetadataResponse.getObject(MICROFORMAT)
                    .getObject("microformatDataRenderer")
                    .getObject(THUMBNAIL)
                    .getArray(THUMBNAILS);

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
                    : getUploaderInfo().getObject(TITLE));
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
            return getImagesFromThumbnailsArray(getUploaderInfo().getObject(THUMBNAIL)
                    .getArray(THUMBNAILS));
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

        JsonArray initialItems = initialBrowseContinuationResponse
                .getArray(ON_RESPONSE_RECEIVED_ACTIONS)
                .getObject(0)
                .getObject("reloadContinuationItemsCommand")
                .getArray(CONTINUATION_ITEMS);

        if (initialItems.isEmpty()) {
            // New structure with lockup view models uses appendContinuationItemsAction for the
            // initial continuation too
            initialItems = initialBrowseContinuationResponse.getArray(ON_RESPONSE_RECEIVED_ACTIONS)
                    .getObject(0)
                    .getObject(APPEND_CONTINUATION_ITEMS_ACTION)
                    .getArray(CONTINUATION_ITEMS);
        }

        collectStreamsFrom(collector, initialItems);

        return new InfoItemsPage<>(collector, getNextPageFrom(initialItems));
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(final Page page) throws IOException,
            ExtractionException {
        if (page == null || isNullOrEmpty(page.getUrl())) {
            throw new IllegalArgumentException("Page doesn't contain an URL");
        }

        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

        final JsonObject ajaxJson = getJsonPostResponse(BROWSE_ENDPOINT, page.getBody(),
                getExtractorLocalization());

        final JsonArray continuation = ajaxJson.getArray(ON_RESPONSE_RECEIVED_ACTIONS)
                .getObject(0)
                .getObject(APPEND_CONTINUATION_ITEMS_ACTION)
                .getArray(CONTINUATION_ITEMS);

        collectStreamsFrom(collector, continuation);

        return new InfoItemsPage<>(collector, getNextPageFrom(continuation));
    }

    @Nullable
    private Page getNextPageFrom(final JsonArray contents)
            throws IOException, ExtractionException {
        if (isNullOrEmpty(contents)) {
            return null;
        }

        final String continuation;

        final JsonObject lastElement = contents.getObject(contents.size() - 1);
        if (lastElement.has("continuationItemRenderer")) {
            final JsonObject continuationEndpoint = lastElement
                    .getObject("continuationItemRenderer")
                    .getObject("continuationEndpoint");

            final JsonObject continuationObject;
            if (continuationEndpoint.has(COMMAND_EXECUTOR_COMMAND)) {
                // This structure is only used at the time this code is written in initial playlist
                // responses. continuationItemRenderer objects return multiple commands: one
                // containing the continuation we need and one a playlistVotingRefreshPopupCommand
                continuationObject = continuationEndpoint.getObject(COMMAND_EXECUTOR_COMMAND)
                        .getArray("commands")
                        .streamAsJsonObjects()
                        .filter(command -> command.has(CONTINUATION_COMMAND))
                        .findFirst()
                        .orElse(new JsonObject());
            } else {
                // At the time this code is written, this "classic" continuation structure is only
                // returned in browse responses of continuation requests
                continuationObject = continuationEndpoint;
            }

            continuation = continuationObject.getObject(CONTINUATION_COMMAND)
                    .getString("token");
        } else if (lastElement.has("continuationItemViewModel")) {
            final JsonObject continuationItemViewModel =
                    lastElement.getObject("continuationItemViewModel");

            continuation = continuationItemViewModel.getObject(CONTINUATION_COMMAND)
                    .getObject("innertubeCommand")
                    .getObject(CONTINUATION_COMMAND)
                    .getString("token");
        } else {
            return null;
        }

        if (isNullOrEmpty(continuation)) {
            // Invalid continuation or no continuation found
            return null;
        }

        final byte[] body = JsonWriter.string(prepareDesktopJsonBuilder(
                        getExtractorLocalization(), getExtractorContentCountry())
                        .value("continuation", continuation)
                        .done())
                .getBytes(StandardCharsets.UTF_8);

        return new Page(YOUTUBEI_V1_URL + "browse?" + DISABLE_PRETTY_PRINT_PARAMETER, body);
    }

    private void collectStreamsFrom(@Nonnull final StreamInfoItemsCollector collector,
                                    @Nonnull final JsonArray videos) {
        final TimeAgoParser timeAgoParser = getTimeAgoParser();
        final PlaylistExtractor playlistExtractor = this;
        final boolean isCoursePlaylistResult = isCoursePlaylist();

        videos.streamAsJsonObjects()
                .forEach(video -> {
                    if (video.has(PLAYLIST_VIDEO_RENDERER)) {
                        collector.commit(new YoutubeStreamInfoItemExtractor(
                            video.getObject(PLAYLIST_VIDEO_RENDERER), timeAgoParser) {
                                @Override
                                public String getUploaderName() throws ParsingException {
                                    if (isCoursePlaylistResult) {
                                        return playlistExtractor.getUploaderName();
                                    }
                                    return super.getUploaderName();
                                }

                                @Override
                                public String getUploaderUrl() throws ParsingException {
                                    if (isCoursePlaylistResult) {
                                        return playlistExtractor.getUploaderUrl();
                                    }
                                    return super.getUploaderUrl();
                                }
                            });
                    } else if (video.has(RICH_ITEM_RENDERER)) {
                        final JsonObject richItemRenderer = video.getObject(RICH_ITEM_RENDERER);
                        if (richItemRenderer.has("content")) {
                            final JsonObject richItemRendererContent =
                                    richItemRenderer.getObject("content");
                            if (richItemRendererContent.has(REEL_ITEM_RENDERER)) {
                                collector.commit(new YoutubeReelInfoItemExtractor(
                                        richItemRendererContent.getObject(REEL_ITEM_RENDERER)));
                            }
                        }
                    } else if (video.has(LOCKUP_VIEW_MODEL)) {
                        collector.commit(new YoutubeStreamInfoItemLockupExtractor(
                                video.getObject(LOCKUP_VIEW_MODEL), timeAgoParser) {
                            @Override
                            public boolean isChannelOrCoursePlaylistLockupItem() {
                                return isCoursePlaylistResult;
                            }

                            @Override
                            public String getUploaderName() throws ParsingException {
                                if (isCoursePlaylistResult) {
                                    return playlistExtractor.getUploaderName();
                                }
                                return super.getUploaderName();
                            }

                            @Override
                            public String getUploaderUrl() throws ParsingException {
                                if (isCoursePlaylistResult) {
                                    return playlistExtractor.getUploaderUrl();
                                }
                                return super.getUploaderUrl();
                            }
                        });
                    }
                });
    }

    @Nonnull
    @Override
    public PlaylistInfo.PlaylistType getPlaylistType() throws ParsingException {
        return extractPlaylistTypeFromPlaylistUrl(getUrl());
    }
}
