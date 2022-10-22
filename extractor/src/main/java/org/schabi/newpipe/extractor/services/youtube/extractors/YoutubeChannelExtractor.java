package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonWriter;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ContentNotSupportedException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ChannelTabHandler;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.ChannelResponseData;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.DISABLE_PRETTY_PRINT_PARAMETER;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.YOUTUBEI_V1_URL;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.addClientInfoHeaders;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.fixThumbnailUrl;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getChannelResponse;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getKey;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getValidJsonResponseBody;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.resolveChannelId;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

/*
 * Created by Christian Schabesberger on 25.07.16.
 *
 * Copyright (C) Christian Schabesberger 2018 <chris.schabesberger@mailbox.org>
 * YoutubeChannelExtractor.java is part of NewPipe.
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

public class YoutubeChannelExtractor extends ChannelExtractor {
    private JsonObject initialData;
    private JsonObject videoTab;
    private List<ChannelTabHandler> tabs;

    /**
     * Some channels have response redirects and the only way to reliably get the id is by saving it
     * <p>
     * "Movies & Shows":
     * <pre>
     * UCuJcl0Ju-gPDoksRjK1ya-w ┐
     * UChBfWrfBXL9wS6tQtgjt_OQ ├ UClgRkhTL3_hImCAmdLfDE4g
     * UCok7UTQQEP1Rsctxiv3gwSQ ┘
     * </pre>
     */
    private String redirectedChannelId;

    public YoutubeChannelExtractor(final StreamingService service,
                                   final ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) throws IOException,
            ExtractionException {
        final String channelPath = super.getId();
        final String id = resolveChannelId(channelPath);
        final ChannelResponseData data = getChannelResponse(id, "EgZ2aWRlb3M%3D",
                getExtractorLocalization(), getExtractorContentCountry());

        initialData = data.responseJson;
        redirectedChannelId = data.channelId;
    }

    @Nonnull
    @Override
    public String getUrl() throws ParsingException {
        try {
            return YoutubeChannelLinkHandlerFactory.getInstance().getUrl("channel/" + getId());
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

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        try {
            return initialData.getObject("header").getObject("c4TabbedHeaderRenderer")
                    .getString("title");
        } catch (final Exception e) {
            throw new ParsingException("Could not get channel name", e);
        }
    }

    @Override
    public String getAvatarUrl() throws ParsingException {
        try {
            final String url = initialData.getObject("header")
                    .getObject("c4TabbedHeaderRenderer").getObject("avatar").getArray("thumbnails")
                    .getObject(0).getString("url");

            return fixThumbnailUrl(url);
        } catch (final Exception e) {
            throw new ParsingException("Could not get avatar", e);
        }
    }

    @Override
    public String getBannerUrl() throws ParsingException {
        try {
            final String url = initialData.getObject("header")
                    .getObject("c4TabbedHeaderRenderer").getObject("banner").getArray("thumbnails")
                    .getObject(0).getString("url");

            if (url == null || url.contains("s.ytimg.com") || url.contains("default_banner")) {
                return null;
            }

            return fixThumbnailUrl(url);
        } catch (final Exception e) {
            throw new ParsingException("Could not get banner", e);
        }
    }

    @Override
    public String getFeedUrl() throws ParsingException {
        try {
            return YoutubeParsingHelper.getFeedUrlFrom(getId());
        } catch (final Exception e) {
            throw new ParsingException("Could not get feed url", e);
        }
    }

    @Override
    public long getSubscriberCount() throws ParsingException {
        final JsonObject c4TabbedHeaderRenderer = initialData.getObject("header")
                .getObject("c4TabbedHeaderRenderer");
        if (!c4TabbedHeaderRenderer.has("subscriberCountText")) {
            return UNKNOWN_SUBSCRIBER_COUNT;
        }
        try {
            return Utils.mixedNumberWordToLong(getTextFromObject(c4TabbedHeaderRenderer
                    .getObject("subscriberCountText")));
        } catch (final NumberFormatException e) {
            throw new ParsingException("Could not get subscriber count", e);
        }
    }

    @Override
    public String getDescription() throws ParsingException {
        try {
            return initialData.getObject("metadata").getObject("channelMetadataRenderer")
                    .getString("description");
        } catch (final Exception e) {
            throw new ParsingException("Could not get channel description", e);
        }
    }

    @Override
    public String getParentChannelName() {
        return "";
    }

    @Override
    public String getParentChannelUrl() {
        return "";
    }

    @Override
    public String getParentChannelAvatarUrl() {
        return "";
    }

    @Override
    public boolean isVerified() throws ParsingException {
        final JsonArray badges = initialData.getObject("header")
                .getObject("c4TabbedHeaderRenderer")
                .getArray("badges");

        return YoutubeParsingHelper.isVerified(badges);
    }

    @Nonnull
    @Override
    public List<ChannelTabHandler> getTabs() throws ParsingException {
        getVideoTab();
        return tabs;
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws IOException, ExtractionException {
        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

        Page nextPage = null;

        if (getVideoTab() != null) {
            final JsonObject tabContent = getVideoTab().getObject("content");
            JsonArray items = tabContent
                    .getObject("sectionListRenderer")
                    .getArray("contents").getObject(0).getObject("itemSectionRenderer")
                    .getArray("contents").getObject(0).getObject("gridRenderer").getArray("items");

            if (items.isEmpty()) {
                items = tabContent.getObject("richGridRenderer").getArray("contents");
            }

            final List<String> channelIds = new ArrayList<>();
            channelIds.add(getName());
            channelIds.add(getUrl());
            final JsonObject continuation = collectStreamsFrom(collector, items, channelIds);

            nextPage = getNextPageFrom(continuation, channelIds);
        }

        return new InfoItemsPage<>(collector, nextPage);
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(final Page page) throws IOException,
            ExtractionException {
        if (page == null || isNullOrEmpty(page.getUrl())) {
            throw new IllegalArgumentException("Page doesn't contain an URL");
        }

        final List<String> channelIds = page.getIds();

        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        final Map<String, List<String>> headers = new HashMap<>();
        addClientInfoHeaders(headers);

        final Response response = getDownloader().post(page.getUrl(), headers, page.getBody(),
                getExtractorLocalization());

        final JsonObject ajaxJson = JsonUtils.toJsonObject(getValidJsonResponseBody(response));

        final JsonObject sectionListContinuation = ajaxJson.getArray("onResponseReceivedActions")
                .getObject(0)
                .getObject("appendContinuationItemsAction");

        final JsonObject continuation = collectStreamsFrom(collector, sectionListContinuation
                .getArray("continuationItems"), channelIds);

        return new InfoItemsPage<>(collector, getNextPageFrom(continuation, channelIds));
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

        return new Page(YOUTUBEI_V1_URL + "browse?key=" + getKey()
                + DISABLE_PRETTY_PRINT_PARAMETER, null, channelIds, null, body);
    }

    /**
     * Collect streams from an array of items
     *
     * @param collector  the collector where videos will be committed
     * @param videos     the array to get videos from
     * @param channelIds the ids of the channel, which are its name and its URL
     * @return the continuation object
     */
    private JsonObject collectStreamsFrom(@Nonnull final StreamInfoItemsCollector collector,
                                          @Nonnull final JsonArray videos,
                                          @Nonnull final List<String> channelIds) {
        collector.reset();

        final String uploaderName = channelIds.get(0);
        final String uploaderUrl = channelIds.get(1);
        final TimeAgoParser timeAgoParser = getTimeAgoParser();

        JsonObject continuation = null;

        for (final Object object : videos) {
            final JsonObject video = (JsonObject) object;
            if (video.has("gridVideoRenderer")) {
                collector.commit(new YoutubeStreamInfoItemExtractor(
                        video.getObject("gridVideoRenderer"), timeAgoParser) {
                    @Override
                    public String getUploaderName() {
                        return uploaderName;
                    }

                    @Override
                    public String getUploaderUrl() {
                        return uploaderUrl;
                    }
                });
            } else if (video.has("richItemRenderer")) {
                collector.commit(new YoutubeStreamInfoItemExtractor(
                        video.getObject("richItemRenderer")
                                .getObject("content").getObject("videoRenderer"), timeAgoParser) {
                    @Override
                    public String getUploaderName() {
                        return uploaderName;
                    }

                    @Override
                    public String getUploaderUrl() {
                        return uploaderUrl;
                    }
                });

            } else if (video.has("continuationItemRenderer")) {
                continuation = video.getObject("continuationItemRenderer");
            }
        }

        return continuation;
    }

    @Nullable
    private JsonObject getVideoTab() throws ParsingException {
        if (this.videoTab != null) {
            return this.videoTab;
        }

        final JsonArray responseTabs = initialData.getObject("contents")
                .getObject("twoColumnBrowseResultsRenderer")
                .getArray("tabs");

        JsonObject foundVideoTab = null;
        tabs = new ArrayList<>();

        final Consumer<ChannelTabHandler.Tab> addTab = tab ->
                tabs.add(new ChannelTabHandler(getLinkHandler(), tab));

        for (final Object tab : responseTabs) {
            if (((JsonObject) tab).has("tabRenderer")) {
                final JsonObject tabRenderer = ((JsonObject) tab).getObject("tabRenderer");
                final String tabUrl = tabRenderer.getObject("endpoint")
                        .getObject("commandMetadata").getObject("webCommandMetadata")
                        .getString("url");
                if (tabUrl != null) {
                    final String[] urlParts = tabUrl.split("/");
                    final String urlSuffix = urlParts[urlParts.length - 1];

                    switch (urlSuffix) {
                        case "videos":
                            foundVideoTab = tabRenderer;
                            break;
                        case "playlists":
                            addTab.accept(ChannelTabHandler.Tab.Playlists);
                            break;
                        case "streams":
                            addTab.accept(ChannelTabHandler.Tab.Livestreams);
                            break;
                        case "shorts":
                            addTab.accept(ChannelTabHandler.Tab.Shorts);
                            break;
                        case "channels":
                            addTab.accept(ChannelTabHandler.Tab.Channels);
                            break;
                    }
                }
            }
        }

        if (foundVideoTab == null) {
            if (tabs.isEmpty()) {
                throw new ContentNotSupportedException("This channel has no supported tabs");
            }

            return null;
        }

        try {
            final String messageRendererText = getTextFromObject(foundVideoTab.getObject("content")
                    .getObject("sectionListRenderer").getArray("contents").getObject(0)
                    .getObject("itemSectionRenderer").getArray("contents").getObject(0)
                    .getObject("messageRenderer").getObject("text"));
            if (messageRendererText != null
                    && messageRendererText.equals("This channel has no videos.")) {
                return null;
            }
        } catch (final ParsingException ignored) {
        }

        this.videoTab = foundVideoTab;
        return foundVideoTab;
    }
}
