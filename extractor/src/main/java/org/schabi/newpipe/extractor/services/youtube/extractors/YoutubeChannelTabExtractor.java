package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonWriter;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.MultiInfoItemsCollector;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelTabExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ChannelTabs;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelTabLinkHandlerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.schabi.newpipe.extractor.services.youtube.YouTubeChannelHelper.ChannelResponseData;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.DISABLE_PRETTY_PRINT_PARAMETER;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.YOUTUBEI_V1_URL;
import static org.schabi.newpipe.extractor.services.youtube.YouTubeChannelHelper.getChannelResponse;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getJsonPostResponse;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getKey;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.defaultAlertsCheck;
import static org.schabi.newpipe.extractor.services.youtube.YouTubeChannelHelper.resolveChannelId;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public class YoutubeChannelTabExtractor extends ChannelTabExtractor {
    private final boolean usePlaylist;
    private JsonObject initialData;

    private String channelId;
    @Nullable
    private String visitorData;

    public YoutubeChannelTabExtractor(final StreamingService service,
                                      final ListLinkHandler linkHandler) {
        super(service, linkHandler);
        usePlaylist = getTab().equals(ChannelTabs.SHORTS);
    }

    private String getParams() throws ParsingException {
        switch (getTab()) {
            case ChannelTabs.PLAYLISTS:
                return "EglwbGF5bGlzdHPyBgQKAkIA";
            case ChannelTabs.LIVESTREAMS:
                return "EgdzdHJlYW1z8gYECgJ6AA%3D%3D";
            case ChannelTabs.SHORTS:
                return "EgZzaG9ydHPyBgUKA5oBAA%3D%3D";
            case ChannelTabs.CHANNELS:
                return "EghjaGFubmVsc_IGBAoCUgA%3D";
        }
        throw new ParsingException("tab " + getTab() + " not supported");
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) throws IOException,
            ExtractionException {
        channelId = resolveChannelId(super.getId());

        if (usePlaylist) {
            // Get shorts from YouTube's internal playlist (ID: UUSH + channel id without UC prefix)
            if (!channelId.startsWith("UC")) {
                throw new ParsingException("channel ID does not start with 'UC'");
            }
            final String browseId = "VLUUSH" + channelId.substring(2);

            final Localization localization = getExtractorLocalization();
            final byte[] body = JsonWriter.string(prepareDesktopJsonBuilder(localization,
                            getExtractorContentCountry())
                            .value("browseId", browseId)
                            .done())
                    .getBytes(StandardCharsets.UTF_8);

            initialData = getJsonPostResponse("browse", body, localization);
            defaultAlertsCheck(initialData);
        } else {
            final String params = getParams();

            final ChannelResponseData data = getChannelResponse(channelId, params,
                    getExtractorLocalization(), getExtractorContentCountry());

            initialData = data.responseJson;
            channelId = data.channelId;
            visitorData = initialData.getObject("responseContext").getString("visitorData");
        }
    }

    @Nonnull
    @Override
    public String getUrl() throws ParsingException {
        try {
            return YoutubeChannelTabLinkHandlerFactory.getInstance().getUrl("channel/" + getId(),
                    Collections.singletonList(getTab()), "");
        } catch (final ParsingException e) {
            return super.getUrl();
        }
    }

    @Nonnull
    @Override
    public String getId() throws ParsingException {
        if (!usePlaylist) {
            final String id = initialData.getObject("header")
                    .getObject("c4TabbedHeaderRenderer")
                    .getString("channelId", "");

            if (!id.isEmpty()) {
                return id;
            }
        }

        if (!isNullOrEmpty(channelId)) {
            return channelId;
        } else {
            throw new ParsingException("Could not get channel id");
        }
    }

    protected String getChannelName() {
        final String mdName = initialData
                .getObject("metadata")
                .getObject("channelMetadataRenderer")
                .getString("title");
        if (!isNullOrEmpty(mdName)) {
            return mdName;
        }

        return initialData.getObject("header").getObject("c4TabbedHeaderRenderer")
                .getString("title", "");
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws IOException, ExtractionException {
        final MultiInfoItemsCollector collector = new MultiInfoItemsCollector(getServiceId());

        JsonArray items = new JsonArray();
        if (usePlaylist) {
            final JsonArray contents = initialData.getObject("contents")
                    .getObject("twoColumnBrowseResultsRenderer")
                    .getArray("tabs")
                    .getObject(0)
                    .getObject("tabRenderer")
                    .getObject("content")
                    .getObject("sectionListRenderer")
                    .getArray("contents");

            final Optional<JsonObject> videoPlaylistObject = contents.stream()
                    .filter(JsonObject.class::isInstance)
                    .map(JsonObject.class::cast)
                    .map(content -> content.getObject("itemSectionRenderer")
                            .getArray("contents")
                            .getObject(0))
                    .filter(contentItemSectionRendererContents ->
                            contentItemSectionRendererContents.has("playlistVideoListRenderer"))
                    .findFirst();

            if (videoPlaylistObject.isPresent()) {
                items = videoPlaylistObject.get()
                        .getObject("playlistVideoListRenderer").getArray("contents");
            }
        } else {
            final Optional<JsonObject> tab = getTabData();

            if (tab.isPresent()) {
                final JsonObject tabContent = tab.get().getObject("content");
                items = tabContent
                        .getObject("sectionListRenderer")
                        .getArray("contents").getObject(0).getObject("itemSectionRenderer")
                        .getArray("contents").getObject(0).getObject("gridRenderer")
                        .getArray("items");

                if (items.isEmpty()) {
                    items = tabContent.getObject("richGridRenderer").getArray("contents");

                    if (items.isEmpty()) {
                        items = tabContent.getObject("sectionListRenderer").getArray("contents");
                    }
                }
            }
        }

        // If a channel tab is fetched, the next page requires channel ID and name,
        // since channel videos dont have their channel specified.
        final List<String> channelIds;
        if (usePlaylist) {
            channelIds = Collections.emptyList();
        } else {
            channelIds = List.of(getChannelName(), getUrl());
        }

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
                .getObject(0)
                .getObject("appendContinuationItemsAction");

        final JsonObject continuation = collectItemsFrom(collector, sectionListContinuation
                .getArray("continuationItems"), channelIds).orElse(null);

        return new InfoItemsPage<>(collector,
                getNextPageFrom(continuation, channelIds));
    }

    Optional<JsonObject> getTabData() throws ParsingException {
        final String urlSuffix = YoutubeChannelTabLinkHandlerFactory.getUrlSuffix(getTab());

        final JsonArray tabs = initialData.getObject("contents")
                .getObject("twoColumnBrowseResultsRenderer")
                .getArray("tabs");

        return tabs.stream()
                .filter(tab -> tab instanceof JsonObject && ((JsonObject) tab).has("tabRenderer"))
                .map(tab -> ((JsonObject) tab).getObject("tabRenderer"))
                .filter(tabRenderer -> tabRenderer.getObject("endpoint")
                        .getObject("commandMetadata").getObject("webCommandMetadata")
                        .getString("url").endsWith(urlSuffix))
                .findFirst()
                // Check if tab has no content
                .filter(tabRenderer -> {
                    final JsonArray tabContents = tabRenderer.getObject("content")
                            .getObject("sectionListRenderer")
                            .getArray("contents").getObject(0)
                            .getObject("itemSectionRenderer").getArray("contents");
                    return tabContents.size() != 1
                            || !tabContents.getObject(0).has("messageRenderer");
                });
    }

    private Optional<JsonObject> collectItemsFrom(@Nonnull final MultiInfoItemsCollector collector,
                                                  @Nonnull final JsonArray items,
                                                  @Nonnull final List<String> channelIds) {
        return items.stream()
                .filter(JsonObject.class::isInstance)
                .map(item -> collectItem(collector, (JsonObject) item, channelIds))
                .reduce(Optional.empty(), (c1, c2) -> c1.or(() -> c2));
    }

    private Optional<JsonObject> collectItem(@Nonnull final MultiInfoItemsCollector collector,
                                             @Nonnull final JsonObject item,
                                             @Nonnull final List<String> channelIds) {
        final Consumer<JsonObject> commitVideo = videoRenderer -> collector.commit(
                new YoutubeStreamInfoItemExtractor(videoRenderer, getTimeAgoParser()) {
                    @Override
                    public String getUploaderName() throws ParsingException {
                        if (channelIds.size() == 2) {
                            return channelIds.get(0);
                        }
                        return super.getUploaderName();
                    }

                    @Override
                    public String getUploaderUrl() throws ParsingException {
                        if (channelIds.size() == 2) {
                            return channelIds.get(1);
                        }
                        return super.getUploaderUrl();
                    }
                });

        if (item.has("gridVideoRenderer")) {
            commitVideo.accept(item.getObject("gridVideoRenderer"));
        } else if (item.has("richItemRenderer")) {
            final JsonObject richItem = item.getObject("richItemRenderer").getObject("content");

            if (richItem.has("videoRenderer")) {
                commitVideo.accept(richItem.getObject("videoRenderer"));

            } else if (richItem.has("reelItemRenderer")) {
                commitVideo.accept(richItem.getObject("reelItemRenderer"));
            }
        } else if (item.has("gridPlaylistRenderer")) {
            collector.commit(new YoutubePlaylistInfoItemExtractor(
                    item.getObject("gridPlaylistRenderer")) {
                @Override
                public String getUploaderName() {
                    return channelIds.get(0);
                }
            });
        } else if (item.has("gridChannelRenderer")) {
            collector.commit(new YoutubeChannelInfoItemExtractor(
                    item.getObject("gridChannelRenderer")));
        } else if (item.has("playlistVideoRenderer")) {
            commitVideo.accept(item.getObject("playlistVideoRenderer"));
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
                        getExtractorContentCountry(), visitorData)
                        .value("continuation", continuation)
                        .done())
                .getBytes(StandardCharsets.UTF_8);

        return new Page(YOUTUBEI_V1_URL + "browse?key=" + getKey()
                + DISABLE_PRETTY_PRINT_PARAMETER, null, channelIds, null, body);
    }

    public static class VideoTabExtractor extends YoutubeChannelTabExtractor {
        private final JsonObject tabRenderer;
        private final String channelName;
        private final String channelUrl;

        VideoTabExtractor(final StreamingService service,
                          final ListLinkHandler linkHandler,
                          final JsonObject tabRenderer,
                          final String channelName,
                          final String channelUrl) {
            super(service, linkHandler);
            this.tabRenderer = tabRenderer;
            this.channelName = channelName;
            this.channelUrl = channelUrl;
        }

        @Override
        public void onFetchPage(@Nonnull final Downloader downloader) {
            // nothing to do, all data was already fetched and is stored in the link handler
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
