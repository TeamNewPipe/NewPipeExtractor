package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonWriter;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.MultiInfoItemsCollector;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.services.youtube.YoutubeChannelHelper;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelTabLinkHandlerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeChannelHelper.getChannelResponse;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeChannelHelper.resolveChannelId;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.DISABLE_PRETTY_PRINT_PARAMETER;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.YOUTUBEI_V1_URL;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getJsonPostResponse;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

/**
 * A {@link ChannelTabExtractor} implementation for the YouTube service.
 *
 * <p>
 * It currently supports {@code Videos}, {@code Shorts}, {@code Live}, {@code Playlists},
 * {@code Albums} and {@code Channels} tabs.
 * </p>
 */
public class YoutubeChannelTabExtractor extends ChannelTabExtractor {

    @Nullable
    protected YoutubeChannelHelper.ChannelHeader channelHeader;

    private JsonObject jsonResponse;
    private String channelId;

    public YoutubeChannelTabExtractor(final StreamingService service,
                                      final ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Nonnull
    private String getChannelTabsParameters() throws ParsingException {
        final String name = getName();
        switch (name) {
            case ChannelTabs.VIDEOS:
                return "EgZ2aWRlb3PyBgQKAjoA";
            case ChannelTabs.SHORTS:
                return "EgZzaG9ydHPyBgUKA5oBAA%3D%3D";
            case ChannelTabs.LIVESTREAMS:
                return "EgdzdHJlYW1z8gYECgJ6AA%3D%3D";
            case ChannelTabs.ALBUMS:
                return "EghyZWxlYXNlc_IGBQoDsgEA";
            case ChannelTabs.PLAYLISTS:
                return "EglwbGF5bGlzdHPyBgQKAkIA";
            default:
                throw new ParsingException("Unsupported channel tab: " + name);
        }
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) throws IOException,
            ExtractionException {
        final String channelIdFromId = resolveChannelId(super.getId());

        final String params = getChannelTabsParameters();

        final YoutubeChannelHelper.ChannelResponseData data = getChannelResponse(channelIdFromId,
                params, getExtractorLocalization(), getExtractorContentCountry());

        jsonResponse = data.jsonResponse;
        channelHeader = YoutubeChannelHelper.getChannelHeader(jsonResponse);
        channelId = data.channelId;
    }

    @Nonnull
    @Override
    public String getUrl() throws ParsingException {
        try {
            return YoutubeChannelTabLinkHandlerFactory.getInstance()
                    .getUrl("channel/" + getId(), List.of(getName()), "");
        } catch (final ParsingException e) {
            return super.getUrl();
        }
    }

    @Nonnull
    @Override
    public String getId() throws ParsingException {
       return YoutubeChannelHelper.getChannelId(channelHeader, jsonResponse, channelId);
    }

    protected String getChannelName() throws ParsingException {
        return YoutubeChannelHelper.getChannelName(channelHeader,
                YoutubeChannelHelper.getChannelAgeGateRenderer(jsonResponse),
                jsonResponse);
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws IOException, ExtractionException {
        final MultiInfoItemsCollector collector = new MultiInfoItemsCollector(getServiceId());

        JsonArray items = new JsonArray();
        final Optional<JsonObject> tab = getTabData();

        if (tab.isPresent()) {
            final JsonObject tabContent = tab.get().getObject("content");

            items = tabContent.getObject("sectionListRenderer")
                    .getArray("contents")
                    .getObject(0)
                    .getObject("itemSectionRenderer")
                    .getArray("contents")
                    .getObject(0)
                    .getObject("gridRenderer")
                    .getArray("items");

            if (items.isEmpty()) {
                items = tabContent.getObject("richGridRenderer")
                        .getArray("contents");

                if (items.isEmpty()) {
                    items = tabContent.getObject("sectionListRenderer")
                            .getArray("contents");
                }
            }
        }

        final VerifiedStatus verifiedStatus;
        if (channelHeader == null) {
            verifiedStatus = VerifiedStatus.UNKNOWN;
        } else {
            verifiedStatus = YoutubeChannelHelper.isChannelVerified(channelHeader)
                    ? VerifiedStatus.VERIFIED
                    : VerifiedStatus.UNVERIFIED;
        }

        // If a channel tab is fetched, the next page requires channel ID and name, as channel
        // streams don't have their channel specified.
        // We also need to set the visitor data here when it should be enabled, as it is required
        // to get continuations on some channel tabs, and we need a way to pass it between pages
        final String channelName = getChannelName();
        final String channelUrl = getUrl();

        final JsonObject continuation = collectItemsFrom(collector, items, verifiedStatus,
                channelName, channelUrl)
                .orElse(null);

        final Page nextPage = getNextPageFrom(
                continuation, List.of(channelName, channelUrl, verifiedStatus.toString()));

        return new InfoItemsPage<>(collector, nextPage);
    }

    @Override
    public InfoItemsPage<InfoItem> getPage(final Page page)
            throws IOException, ExtractionException {
        if (page == null || isNullOrEmpty(page.getUrl())) {
            throw new IllegalArgumentException("Page doesn't contain an URL");
        }

        final List<String> channelIds = page.getIds();

        final MultiInfoItemsCollector collector = new MultiInfoItemsCollector(getServiceId());

        final JsonObject ajaxJson = getJsonPostResponse("browse", page.getBody(),
                getExtractorLocalization());

        final JsonObject sectionListContinuation = ajaxJson.getArray("onResponseReceivedActions")
                .stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .filter(jsonObject -> jsonObject.has("appendContinuationItemsAction"))
                .map(jsonObject -> jsonObject.getObject("appendContinuationItemsAction"))
                .findFirst()
                .orElse(new JsonObject());

        final JsonObject continuation = collectItemsFrom(collector,
                sectionListContinuation.getArray("continuationItems"), channelIds)
                .orElse(null);

        return new InfoItemsPage<>(collector, getNextPageFrom(continuation, channelIds));
    }

    Optional<JsonObject> getTabData() {
        final String urlSuffix = YoutubeChannelTabLinkHandlerFactory.getUrlSuffix(getName());

        return jsonResponse.getObject("contents")
                .getObject("twoColumnBrowseResultsRenderer")
                .getArray("tabs")
                .stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .filter(tab -> tab.has("tabRenderer"))
                .map(tab -> tab.getObject("tabRenderer"))
                .filter(tabRenderer -> tabRenderer.getObject("endpoint")
                        .getObject("commandMetadata").getObject("webCommandMetadata")
                        .getString("url", "").endsWith(urlSuffix))
                .findFirst()
                // Check if tab has no content
                .filter(tabRenderer -> {
                    final JsonArray tabContents = tabRenderer.getObject("content")
                            .getObject("sectionListRenderer")
                            .getArray("contents")
                            .getObject(0)
                            .getObject("itemSectionRenderer")
                            .getArray("contents");
                    return tabContents.size() != 1
                            || !tabContents.getObject(0).has("messageRenderer");
                });
    }

    private Optional<JsonObject> collectItemsFrom(@Nonnull final MultiInfoItemsCollector collector,
                                                  @Nonnull final JsonArray items,
                                                  @Nonnull final List<String> channelIds) {
        final String channelName;
        final String channelUrl;
        VerifiedStatus verifiedStatus;

        if (channelIds.size() >= 3) {
            channelName = channelIds.get(0);
            channelUrl = channelIds.get(1);
            try {
                verifiedStatus = VerifiedStatus.valueOf(channelIds.get(2));
            } catch (final IllegalArgumentException e) {
                // An IllegalArgumentException can be thrown if someone passes a third channel ID
                // which is not of the enum type in the getPage method, use the UNKNOWN
                // VerifiedStatus enum value in this case
                verifiedStatus = VerifiedStatus.UNKNOWN;
            }
        } else {
            channelName = null;
            channelUrl = null;
            verifiedStatus = VerifiedStatus.UNKNOWN;
        }

        return collectItemsFrom(collector, items, verifiedStatus, channelName, channelUrl);
    }

    private Optional<JsonObject> collectItemsFrom(@Nonnull final MultiInfoItemsCollector collector,
                                                  @Nonnull final JsonArray items,
                                                  @Nonnull final VerifiedStatus verifiedStatus,
                                                  @Nullable final String channelName,
                                                  @Nullable final String channelUrl) {
        return items.stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .map(item -> collectItem(
                        collector, item, verifiedStatus, channelName, channelUrl))
                .reduce(Optional.empty(), (c1, c2) -> c1.or(() -> c2));
    }

    private Optional<JsonObject> collectItem(@Nonnull final MultiInfoItemsCollector collector,
                                             @Nonnull final JsonObject item,
                                             @Nonnull final VerifiedStatus channelVerifiedStatus,
                                             @Nullable final String channelName,
                                             @Nullable final String channelUrl) {
        final TimeAgoParser timeAgoParser = getTimeAgoParser();

        if (item.has("richItemRenderer")) {
            final JsonObject richItem = item.getObject("richItemRenderer")
                    .getObject("content");

            if (richItem.has("videoRenderer")) {
                commitVideo(collector, timeAgoParser, richItem.getObject("videoRenderer"),
                        channelVerifiedStatus, channelName, channelUrl);
            } else if (richItem.has("reelItemRenderer")) {
                commitReel(collector, richItem.getObject("reelItemRenderer"),
                        channelVerifiedStatus, channelName, channelUrl);
            } else if (richItem.has("shortsLockupViewModel")) {
                commitShortsLockup(collector, richItem.getObject("shortsLockupViewModel"),
                        channelVerifiedStatus, channelName, channelUrl);
            } else if (richItem.has("playlistRenderer")) {
                commitPlaylist(collector, richItem.getObject("playlistRenderer"),
                        channelVerifiedStatus, channelName, channelUrl);
            }
        } else if (item.has("gridVideoRenderer")) {
            commitVideo(collector, timeAgoParser, item.getObject("gridVideoRenderer"),
                    channelVerifiedStatus, channelName, channelUrl);
        } else if (item.has("gridPlaylistRenderer")) {
            commitPlaylist(collector, item.getObject("gridPlaylistRenderer"),
                    channelVerifiedStatus, channelName, channelUrl);
        } else if (item.has("gridShowRenderer")) {
            collector.commit(new YoutubeGridShowRendererChannelInfoItemExtractor(
                    item.getObject("gridShowRenderer"), channelVerifiedStatus, channelName,
                    channelUrl));
        } else if (item.has("shelfRenderer")) {
            return collectItem(collector, item.getObject("shelfRenderer")
                    .getObject("content"), channelVerifiedStatus, channelName, channelUrl);
        } else if (item.has("itemSectionRenderer")) {
            return collectItemsFrom(collector, item.getObject("itemSectionRenderer")
                    .getArray("contents"), channelVerifiedStatus, channelName, channelUrl);
        } else if (item.has("horizontalListRenderer")) {
            return collectItemsFrom(collector, item.getObject("horizontalListRenderer")
                    .getArray("items"), channelVerifiedStatus, channelName, channelUrl);
        } else if (item.has("expandedShelfContentsRenderer")) {
            return collectItemsFrom(collector, item.getObject("expandedShelfContentsRenderer")
                    .getArray("items"), channelVerifiedStatus, channelName, channelUrl);
        } else if (item.has("lockupViewModel")) {
            final JsonObject lockupViewModel = item.getObject("lockupViewModel");
            if ("LOCKUP_CONTENT_TYPE_PLAYLIST".equals(lockupViewModel.getString("contentType"))) {
                commitPlaylistLockup(collector, lockupViewModel, channelVerifiedStatus,
                        channelName, channelUrl);
            }
        } else if (item.has("continuationItemRenderer")) {
            return Optional.ofNullable(item.getObject("continuationItemRenderer"));
        }

        return Optional.empty();
    }

    private static void commitReel(@Nonnull final MultiInfoItemsCollector collector,
                                   @Nonnull final JsonObject reelItemRenderer,
                                   @Nonnull final VerifiedStatus channelVerifiedStatus,
                                   @Nullable final String channelName,
                                   @Nullable final String channelUrl) {
        collector.commit(
                new YoutubeReelInfoItemExtractor(reelItemRenderer) {
                    @Override
                    public String getUploaderName() throws ParsingException {
                        return isNullOrEmpty(channelName) ? super.getUploaderName() : channelName;
                    }

                    @Override
                    public String getUploaderUrl() throws ParsingException {
                        return isNullOrEmpty(channelUrl) ? super.getUploaderName() : channelUrl;
                    }

                    @Override
                    public boolean isUploaderVerified() {
                        return channelVerifiedStatus == VerifiedStatus.VERIFIED;
                    }
                });
    }

    private static void commitShortsLockup(@Nonnull final MultiInfoItemsCollector collector,
                                           @Nonnull final JsonObject shortsLockupViewModel,
                                           @Nonnull final VerifiedStatus channelVerifiedStatus,
                                           @Nullable final String channelName,
                                           @Nullable final String channelUrl) {
        collector.commit(
                new YoutubeShortsLockupInfoItemExtractor(shortsLockupViewModel) {
                    @Override
                    public String getUploaderName() throws ParsingException {
                        return isNullOrEmpty(channelName) ? super.getUploaderName() : channelName;
                    }

                    @Override
                    public String getUploaderUrl() throws ParsingException {
                        return isNullOrEmpty(channelUrl) ? super.getUploaderName() : channelUrl;
                    }

                    @Override
                    public boolean isUploaderVerified() {
                        return channelVerifiedStatus == VerifiedStatus.VERIFIED;
                    }
                });
    }

    private void commitPlaylistLockup(@Nonnull final MultiInfoItemsCollector collector,
                                      @Nonnull final JsonObject playlistLockupViewModel,
                                      @Nonnull final VerifiedStatus channelVerifiedStatus,
                                      @Nullable final String channelName,
                                      @Nullable final String channelUrl) {
        collector.commit(
                new YoutubeMixOrPlaylistLockupInfoItemExtractor(playlistLockupViewModel) {
                    @Override
                    public String getUploaderName() throws ParsingException {
                        return isNullOrEmpty(channelName) ? super.getUploaderName() : channelName;
                    }

                    @Override
                    public String getUploaderUrl() throws ParsingException {
                        return isNullOrEmpty(channelUrl) ? super.getUploaderName() : channelUrl;
                    }

                    @Override
                    public boolean isUploaderVerified() throws ParsingException {
                        switch (channelVerifiedStatus) {
                            case VERIFIED:
                                return true;
                            case UNVERIFIED:
                                return false;
                            default:
                                return super.isUploaderVerified();
                        }
                    }
                });
    }

    private void commitVideo(@Nonnull final MultiInfoItemsCollector collector,
                             @Nonnull final TimeAgoParser timeAgoParser,
                             @Nonnull final JsonObject jsonObject,
                             @Nonnull final VerifiedStatus channelVerifiedStatus,
                             @Nullable final String channelName,
                             @Nullable final String channelUrl) {
        collector.commit(
                new YoutubeStreamInfoItemExtractor(jsonObject, timeAgoParser) {
                    @Override
                    public String getUploaderName() throws ParsingException {
                        return isNullOrEmpty(channelName) ? super.getUploaderName() : channelName;
                    }

                    @Override
                    public String getUploaderUrl() throws ParsingException {
                        return isNullOrEmpty(channelUrl) ? super.getUploaderName() : channelUrl;
                    }

                    @SuppressWarnings("DuplicatedCode")
                    @Override
                    public boolean isUploaderVerified() throws ParsingException {
                        switch (channelVerifiedStatus) {
                            case VERIFIED:
                                return true;
                            case UNVERIFIED:
                                return false;
                            default:
                                return super.isUploaderVerified();
                        }
                    }
                });
    }

    private void commitPlaylist(@Nonnull final MultiInfoItemsCollector collector,
                                @Nonnull final JsonObject jsonObject,
                                @Nonnull final VerifiedStatus channelVerifiedStatus,
                                @Nullable final String channelName,
                                @Nullable final String channelUrl) {
        collector.commit(
                new YoutubePlaylistInfoItemExtractor(jsonObject) {
                    @Override
                    public String getUploaderName() throws ParsingException {
                        return isNullOrEmpty(channelName) ? super.getUploaderName() : channelName;
                    }

                    @Override
                    public String getUploaderUrl() throws ParsingException {
                        return isNullOrEmpty(channelUrl) ? super.getUploaderName() : channelUrl;
                    }

                    @SuppressWarnings("DuplicatedCode")
                    @Override
                    public boolean isUploaderVerified() throws ParsingException {
                        switch (channelVerifiedStatus) {
                            case VERIFIED:
                                return true;
                            case UNVERIFIED:
                                return false;
                            default:
                                return super.isUploaderVerified();
                        }
                    }
                });
    }

    @Nullable
    private Page getNextPageFrom(final JsonObject continuations,
                                 final List<String> channelIds) throws IOException,
            ExtractionException {
        if (isNullOrEmpty(continuations)) {
            return null;
        }

        final JsonObject continuationEndpoint = continuations.getObject("continuationEndpoint");
        final String continuation = continuationEndpoint.getObject("continuationCommand")
                .getString("token");

        final byte[] body = JsonWriter.string(prepareDesktopJsonBuilder(getExtractorLocalization(),
                        getExtractorContentCountry())
                        .value("continuation", continuation)
                        .done())
                .getBytes(StandardCharsets.UTF_8);

        return new Page(YOUTUBEI_V1_URL + "browse?" + DISABLE_PRETTY_PRINT_PARAMETER, null,
                channelIds, null, body);
    }

    /**
     * A {@link YoutubeChannelTabExtractor} for the {@code Videos} tab, if it has been already
     * fetched.
     */
    public static final class VideosTabExtractor extends YoutubeChannelTabExtractor {
        private final JsonObject tabRenderer;
        private final String channelId;
        private final String channelName;
        private final String channelUrl;

        VideosTabExtractor(final StreamingService service,
                           final ListLinkHandler linkHandler,
                           final JsonObject tabRenderer,
                           @Nullable final YoutubeChannelHelper.ChannelHeader channelHeader,
                           final String channelName,
                           final String channelId,
                           final String channelUrl) {
            super(service, linkHandler);
            this.channelHeader = channelHeader;

            this.tabRenderer = tabRenderer;
            this.channelId = channelId;
            this.channelName = channelName;
            this.channelUrl = channelUrl;
        }

        @Override
        public void onFetchPage(@Nonnull final Downloader downloader) {
            // Nothing to do, the initial data was already fetched and is stored in the link handler
        }

        @Nonnull
        @Override
        public String getId() throws ParsingException {
            return channelId;
        }

        @Nonnull
        @Override
        public String getUrl() throws ParsingException {
            return channelUrl;
        }

        @Override
        protected String getChannelName() {
            return channelName;
        }

        @Override
        Optional<JsonObject> getTabData() {
            return Optional.of(tabRenderer);
        }
    }

    /**
     * Enum representing the verified state of a channel
     */
    private enum VerifiedStatus {
        VERIFIED,
        UNVERIFIED,
        UNKNOWN
    }

    private static final class YoutubeGridShowRendererChannelInfoItemExtractor
            extends YoutubeBaseShowInfoItemExtractor {

        @Nonnull
        private final VerifiedStatus verifiedStatus;

        @Nullable
        private final String channelName;

        @Nullable
        private final String channelUrl;

        private YoutubeGridShowRendererChannelInfoItemExtractor(
                @Nonnull final JsonObject gridShowRenderer,
                @Nonnull final VerifiedStatus verifiedStatus,
                @Nullable final String channelName,
                @Nullable final String channelUrl) {
            super(gridShowRenderer);
            this.verifiedStatus = verifiedStatus;
            this.channelName = channelName;
            this.channelUrl = channelUrl;
        }

        @Override
        public String getUploaderName() {
            return channelName;
        }

        @Override
        public String getUploaderUrl() {
            return channelUrl;
        }

        @Override
        public boolean isUploaderVerified() throws ParsingException {
            switch (verifiedStatus) {
                case VERIFIED:
                    return true;
                case UNVERIFIED:
                    return false;
                default:
                    throw new ParsingException("Could not get uploader verification status");
            }
        }
    }
}
