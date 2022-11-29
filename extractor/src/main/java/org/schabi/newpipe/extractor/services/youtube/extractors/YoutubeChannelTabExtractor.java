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
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ChannelTabs;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelTabLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.JsonUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.ChannelResponseData;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.DISABLE_PRETTY_PRINT_PARAMETER;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.YOUTUBEI_V1_URL;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.addClientInfoHeaders;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getChannelResponse;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getKey;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getValidJsonResponseBody;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.resolveChannelId;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public class YoutubeChannelTabExtractor extends ChannelTabExtractor {
    private JsonObject initialData;
    private JsonObject tabData;

    private String redirectedChannelId;
    @Nullable
    private String visitorData;

    public YoutubeChannelTabExtractor(final StreamingService service,
                                      final ListLinkHandler linkHandler) {
        super(service, linkHandler);
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
        final String params = getParams();
        final String id = resolveChannelId(super.getId());
        final ChannelResponseData data = getChannelResponse(id, params,
                getExtractorLocalization(), getExtractorContentCountry());

        initialData = data.responseJson;
        redirectedChannelId = data.channelId;
        visitorData =
                initialData.getObject("responseContext").getString("visitorData");
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
        final String channelId = initialData.getObject("header")
                .getObject("c4TabbedHeaderRenderer")
                .getString("channelId", "");

        if (!channelId.isEmpty()) {
            return channelId;
        } else if (!isNullOrEmpty(redirectedChannelId)) {
            return redirectedChannelId;
        } else {
            throw new ParsingException("Could not get channel id");
        }
    }

    private String getChannelName() throws ParsingException {
        try {
            return initialData.getObject("header").getObject("c4TabbedHeaderRenderer")
                    .getString("title");
        } catch (final Exception e) {
            throw new ParsingException("Could not get channel name", e);
        }
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws IOException, ExtractionException {
        final MultiInfoItemsCollector collector = new MultiInfoItemsCollector(getServiceId());

        Page nextPage = null;

        if (getTabData() != null) {
            final JsonObject tabContent = tabData.getObject("content");
            JsonArray items = tabContent
                    .getObject("sectionListRenderer")
                    .getArray("contents").getObject(0).getObject("itemSectionRenderer")
                    .getArray("contents").getObject(0).getObject("gridRenderer").getArray("items");

            if (items.isEmpty()) {
                items = tabContent.getObject("richGridRenderer").getArray("contents");

                if (items.isEmpty()) {
                    items = tabContent.getObject("sectionListRenderer").getArray("contents");
                }
            }

            final List<String> channelIds = new ArrayList<>();
            channelIds.add(getChannelName());
            channelIds.add(getUrl());
            final JsonObject continuation = collectItemsFrom(collector, items, channelIds);

            nextPage = getNextPageFrom(continuation, channelIds);
        }

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
        final Map<String, List<String>> headers = new HashMap<>();
        addClientInfoHeaders(headers);

        final Response response = getDownloader().post(page.getUrl(), headers, page.getBody(),
                getExtractorLocalization());

        final JsonObject ajaxJson = JsonUtils.toJsonObject(getValidJsonResponseBody(response));

        final JsonObject sectionListContinuation = ajaxJson.getArray("onResponseReceivedActions")
                .getObject(0)
                .getObject("appendContinuationItemsAction");

        final JsonObject continuation = collectItemsFrom(collector, sectionListContinuation
                .getArray("continuationItems"), channelIds);

        return new InfoItemsPage<>(collector,
                getNextPageFrom(continuation, channelIds));
    }

    @Nullable
    private JsonObject getTabData() throws ParsingException {
        if (this.tabData != null) {
            return this.tabData;
        }

        final String urlSuffix = YoutubeChannelTabLinkHandlerFactory.getUrlSuffix(getTab());

        final JsonArray tabs = initialData.getObject("contents")
                .getObject("twoColumnBrowseResultsRenderer")
                .getArray("tabs");

        JsonObject foundTab = null;
        for (final Object tab : tabs) {
            if (((JsonObject) tab).has("tabRenderer")) {
                if (((JsonObject) tab).getObject("tabRenderer").getObject("endpoint")
                        .getObject("commandMetadata").getObject("webCommandMetadata")
                        .getString("url").endsWith(urlSuffix)) {
                    foundTab = ((JsonObject) tab).getObject("tabRenderer");
                    break;
                }
            }
        }

        // No tab
        if (foundTab == null) {
            return null;
        }

        // No content
        final JsonArray tabContents = foundTab.getObject("content").getObject("sectionListRenderer")
                .getArray("contents").getObject(0)
                .getObject("itemSectionRenderer").getArray("contents");
        if (tabContents.size() == 1 && tabContents.getObject(0).has("messageRenderer")) {
            return null;
        }

        this.tabData = foundTab;
        return foundTab;
    }

    @Nullable
    private JsonObject collectItemsFrom(@Nonnull final MultiInfoItemsCollector collector,
                                        @Nonnull final JsonArray items,
                                        @Nonnull final List<String> channelIds) {
        JsonObject continuation = null;

        for (final Object object : items) {
            final JsonObject item = (JsonObject) object;
            final JsonObject optContinuation = collectItem(
                    collector, item, channelIds);
            if (optContinuation != null) {
                continuation = optContinuation;
            }
        }
        return continuation;
    }

    @Nullable
    private JsonObject collectItem(@Nonnull final MultiInfoItemsCollector collector,
                                   @Nonnull final JsonObject item,
                                   @Nonnull final List<String> channelIds) {
        final Consumer<JsonObject> commitVideo = videoRenderer -> collector.commit(
                new YoutubeStreamInfoItemExtractor(videoRenderer, getTimeAgoParser()) {
                    @Override
                    public String getUploaderName() {
                        return channelIds.get(0);
                    }

                    @Override
                    public String getUploaderUrl() {
                        return channelIds.get(1);
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
            return item.getObject("continuationItemRenderer");
        }
        return null;
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
}
