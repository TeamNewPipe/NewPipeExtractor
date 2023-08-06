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
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getKey;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

/**
 * A {@link ChannelTabExtractor} implementation for the YouTube service.
 *
 * <p>
 * It currently supports {@code Videos}, {@code Shorts}, {@code Live}, {@code Playlists} and
 * {@code Channels} tabs.
 * </p>
 */
public class YoutubeChannelTabExtractor extends ChannelTabExtractor {

    /**
     * Whether the visitor data extracted from the initial channel response is required to be used
     * for continuations.
     *
     * <p>
     * A valid {@code visitorData} is required to get continuations of shorts in channels.
     * </p>
     *
     * <p>
     * It should be not used when it is not needed, in order to reduce YouTube's tracking.
     * </p>
     */
    private final boolean useVisitorData;
    private JsonObject jsonResponse;
    private String channelId;
    @Nullable
    private String visitorData;

    public YoutubeChannelTabExtractor(final StreamingService service,
                                      final ListLinkHandler linkHandler) {
        super(service, linkHandler);
        useVisitorData = getName().equals(ChannelTabs.SHORTS);
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
            case ChannelTabs.PLAYLISTS:
                return "EglwbGF5bGlzdHPyBgQKAkIA";
            case ChannelTabs.CHANNELS:
                return "EghjaGFubmVsc_IGBAoCUgA%3D";
        }
        throw new ParsingException("Unsupported channel tab: " + name);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) throws IOException,
            ExtractionException {
        channelId = resolveChannelId(super.getId());

        final String params = getChannelTabsParameters();

        final YoutubeChannelHelper.ChannelResponseData data = getChannelResponse(channelId,
                params, getExtractorLocalization(), getExtractorContentCountry());

        jsonResponse = data.jsonResponse;
        channelId = data.channelId;
        if (useVisitorData) {
            visitorData = jsonResponse.getObject("responseContext").getString("visitorData");
        }
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
        final String id = jsonResponse.getObject("header")
                .getObject("c4TabbedHeaderRenderer")
                .getString("channelId", "");

        if (!id.isEmpty()) {
            return id;
        }

        final Optional<String> carouselHeaderId = jsonResponse.getObject("header")
                .getObject("carouselHeaderRenderer")
                .getArray("contents")
                .stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .filter(item -> item.has("topicChannelDetailsRenderer"))
                .findFirst()
                .flatMap(item ->
                        Optional.ofNullable(item.getObject("topicChannelDetailsRenderer")
                                .getObject("navigationEndpoint")
                                .getObject("browseEndpoint")
                                .getString("browseId")));
        if (carouselHeaderId.isPresent()) {
            return carouselHeaderId.get();
        }

        if (!isNullOrEmpty(channelId)) {
            return channelId;
        } else {
            throw new ParsingException("Could not get channel ID");
        }
    }

    protected String getChannelName() {
        final String metadataName = jsonResponse.getObject("metadata")
                .getObject("channelMetadataRenderer")
                .getString("title");
        if (!isNullOrEmpty(metadataName)) {
            return metadataName;
        }

        return YoutubeChannelHelper.getChannelHeader(jsonResponse)
                .map(header -> {
                    final Object title = header.json.get("title");
                    if (title instanceof String) {
                        return (String) title;
                    } else if (title instanceof JsonObject) {
                        final String headerName = getTextFromObject((JsonObject) title);
                        if (!isNullOrEmpty(headerName)) {
                            return headerName;
                        }
                    }
                    return "";
                })
                .orElse("");
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

        // If a channel tab is fetched, the next page requires channel ID and name, as channel
        // streams don't have their channel specified.
        // We also need to set the visitor data here when it should be enabled, as it is required
        // to get continuations on some channel tabs, and we need a way to pass it between pages
        final List<String> channelIds = useVisitorData && !isNullOrEmpty(visitorData)
                ? List.of(getChannelName(), getUrl(), visitorData)
                : List.of(getChannelName(), getUrl());

        final JsonObject continuation = collectItemsFrom(collector, items, channelIds)
                .orElse(null);

        final Page nextPage = getNextPageFrom(continuation, channelIds);

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
        return items.stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .map(item -> collectItem(collector, item, channelIds))
                .reduce(Optional.empty(), (c1, c2) -> c1.or(() -> c2));
    }

    private Optional<JsonObject> collectItem(@Nonnull final MultiInfoItemsCollector collector,
                                             @Nonnull final JsonObject item,
                                             @Nonnull final List<String> channelIds) {
        final TimeAgoParser timeAgoParser = getTimeAgoParser();

        if (item.has("richItemRenderer")) {
            final JsonObject richItem = item.getObject("richItemRenderer")
                    .getObject("content");

            if (richItem.has("videoRenderer")) {
                getCommitVideoConsumer(collector, timeAgoParser, channelIds,
                        richItem.getObject("videoRenderer"));
            } else if (richItem.has("reelItemRenderer")) {
                getCommitReelItemConsumer(collector, timeAgoParser, channelIds,
                        richItem.getObject("reelItemRenderer"));
            } else if (richItem.has("playlistRenderer")) {
                getCommitPlaylistConsumer(collector, channelIds,
                        item.getObject("playlistRenderer"));
            }
        } else if (item.has("gridVideoRenderer")) {
            getCommitVideoConsumer(collector, timeAgoParser, channelIds,
                    item.getObject("gridVideoRenderer"));
        } else if (item.has("gridPlaylistRenderer")) {
            getCommitPlaylistConsumer(collector, channelIds,
                    item.getObject("gridPlaylistRenderer"));
        } else if (item.has("gridChannelRenderer")) {
            collector.commit(new YoutubeChannelInfoItemExtractor(
                    item.getObject("gridChannelRenderer")));
        } else if (item.has("shelfRenderer")) {
            return collectItem(collector, item.getObject("shelfRenderer")
                    .getObject("content"), channelIds);
        } else if (item.has("itemSectionRenderer")) {
            return collectItemsFrom(collector, item.getObject("itemSectionRenderer")
                    .getArray("contents"), channelIds);
        } else if (item.has("horizontalListRenderer")) {
            return collectItemsFrom(collector, item.getObject("horizontalListRenderer")
                    .getArray("items"), channelIds);
        } else if (item.has("expandedShelfContentsRenderer")) {
            return collectItemsFrom(collector, item.getObject("expandedShelfContentsRenderer")
                    .getArray("items"), channelIds);
        } else if (item.has("continuationItemRenderer")) {
            return Optional.ofNullable(item.getObject("continuationItemRenderer"));
        }

        return Optional.empty();
    }

    private void getCommitVideoConsumer(@Nonnull final MultiInfoItemsCollector collector,
                                        @Nonnull final TimeAgoParser timeAgoParser,
                                        @Nonnull final List<String> channelIds,
                                        @Nonnull final JsonObject jsonObject) {
        collector.commit(
                new YoutubeStreamInfoItemExtractor(jsonObject, timeAgoParser) {
                    @Override
                    public String getUploaderName() throws ParsingException {
                        if (channelIds.size() >= 2) {
                            return channelIds.get(0);
                        }
                        return super.getUploaderName();
                    }

                    @Override
                    public String getUploaderUrl() throws ParsingException {
                        if (channelIds.size() >= 2) {
                            return channelIds.get(1);
                        }
                        return super.getUploaderUrl();
                    }
                });
    }

    private void getCommitReelItemConsumer(@Nonnull final MultiInfoItemsCollector collector,
                                           @Nonnull final TimeAgoParser timeAgoParser,
                                           @Nonnull final List<String> channelIds,
                                           @Nonnull final JsonObject jsonObject) {
        collector.commit(
                new YoutubeReelInfoItemExtractor(jsonObject, timeAgoParser) {
                    @Override
                    public String getUploaderName() throws ParsingException {
                        if (channelIds.size() >= 2) {
                            return channelIds.get(0);
                        }
                        return super.getUploaderName();
                    }

                    @Override
                    public String getUploaderUrl() throws ParsingException {
                        if (channelIds.size() >= 2) {
                            return channelIds.get(1);
                        }
                        return super.getUploaderUrl();
                    }
                });
    }

    private void getCommitPlaylistConsumer(@Nonnull final MultiInfoItemsCollector collector,
                                           @Nonnull final List<String> channelIds,
                                           @Nonnull final JsonObject jsonObject) {
        collector.commit(
                new YoutubePlaylistInfoItemExtractor(jsonObject) {
                    @Override
                    public String getUploaderName() throws ParsingException {
                        if (channelIds.size() >= 2) {
                            return channelIds.get(0);
                        }
                        return super.getUploaderName();
                    }

                    @Override
                    public String getUploaderUrl() throws ParsingException {
                        if (channelIds.size() >= 2) {
                            return channelIds.get(1);
                        }
                        return super.getUploaderUrl();
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
                        getExtractorContentCountry(),
                        useVisitorData && channelIds.size() >= 3 ? channelIds.get(2) : null)
                        .value("continuation", continuation)
                        .done())
                .getBytes(StandardCharsets.UTF_8);

        return new Page(YOUTUBEI_V1_URL + "browse?key=" + getKey()
                + DISABLE_PRETTY_PRINT_PARAMETER, null, channelIds, null, body);
    }

    /**
     * A {@link YoutubeChannelTabExtractor} for the {@code Videos} tab, if it has been already
     * fetched.
     */
    public static final class VideosTabExtractor extends YoutubeChannelTabExtractor {
        private final JsonObject tabRenderer;
        private final String channelName;
        private final String channelId;
        private final String channelUrl;

        VideosTabExtractor(final StreamingService service,
                           final ListLinkHandler linkHandler,
                           final JsonObject tabRenderer,
                           final String channelName,
                           final String channelId,
                           final String channelUrl) {
            super(service, linkHandler);
            this.tabRenderer = tabRenderer;
            this.channelName = channelName;
            this.channelId = channelId;
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
}
