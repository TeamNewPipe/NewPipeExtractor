package org.schabi.newpipe.extractor.services.youtube.extractors;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.DISABLE_PRETTY_PRINT_PARAMETER;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.YOUTUBEI_V1_URL;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getJsonPostResponse;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getKey;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonWriter;

import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ContentNotSupportedException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
    private Optional<JsonObject> channelHeader;
    private boolean isCarouselHeader = false;
    private JsonObject videoTab;
    private JsonObject playlistsTab;

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
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        final String channelPath = super.getId();
        final String[] channelId = channelPath.split("/");
        String id = "";
        // If the url is an URL which is not a /channel URL, we need to use the
        // navigation/resolve_url endpoint of the InnerTube API to get the channel id. Otherwise,
        // we couldn't get information about the channel associated with this URL, if there is one.
        if (!channelId[0].equals("channel")) {
            final byte[] body = JsonWriter.string(prepareDesktopJsonBuilder(
                            getExtractorLocalization(), getExtractorContentCountry())
                            .value("url", "https://www.youtube.com/" + channelPath)
                            .done())
                    .getBytes(StandardCharsets.UTF_8);

            final JsonObject jsonResponse = getJsonPostResponse("navigation/resolve_url",
                    body, getExtractorLocalization());

            checkIfChannelResponseIsValid(jsonResponse);

            final JsonObject endpoint = jsonResponse.getObject("endpoint");

            final String webPageType = endpoint.getObject("commandMetadata")
                    .getObject("webCommandMetadata")
                    .getString("webPageType", "");

            final JsonObject browseEndpoint = endpoint.getObject("browseEndpoint");
            final String browseId = browseEndpoint.getString("browseId", "");

            if (webPageType.equalsIgnoreCase("WEB_PAGE_TYPE_BROWSE")
                    || webPageType.equalsIgnoreCase("WEB_PAGE_TYPE_CHANNEL")
                    && !browseId.isEmpty()) {
                if (!browseId.startsWith("UC")) {
                    throw new ExtractionException("Redirected id is not pointing to a channel");
                }

                id = browseId;
                redirectedChannelId = browseId;
            }
        } else {
            id = channelId[1];
        }
        JsonObject ajaxJson = null;

        int level = 0;
        while (level < 3) {
            final byte[] body = JsonWriter.string(prepareDesktopJsonBuilder(
                            getExtractorLocalization(), getExtractorContentCountry())
                            .value("browseId", id)
                            .value("params", "EgZ2aWRlb3M%3D") // Equal to videos
                            .done())
                    .getBytes(StandardCharsets.UTF_8);

            final JsonObject jsonResponse = getJsonPostResponse("browse", body,
                    getExtractorLocalization());

            checkIfChannelResponseIsValid(jsonResponse);

            final JsonObject endpoint = jsonResponse.getArray("onResponseReceivedActions")
                    .getObject(0)
                    .getObject("navigateAction")
                    .getObject("endpoint");

            final String webPageType = endpoint.getObject("commandMetadata")
                    .getObject("webCommandMetadata")
                    .getString("webPageType", "");

            final String browseId = endpoint.getObject("browseEndpoint").getString("browseId",
                    "");

            if (webPageType.equalsIgnoreCase("WEB_PAGE_TYPE_BROWSE")
                    || webPageType.equalsIgnoreCase("WEB_PAGE_TYPE_CHANNEL")
                    && !browseId.isEmpty()) {
                if (!browseId.startsWith("UC")) {
                    throw new ExtractionException("Redirected id is not pointing to a channel");
                }

                id = browseId;
                redirectedChannelId = browseId;
                level++;
            } else {
                ajaxJson = jsonResponse;
                break;
            }
        }

        if (ajaxJson == null) {
            throw new ExtractionException("Could not fetch initial JSON data");
        }

        initialData = ajaxJson;
        YoutubeParsingHelper.defaultAlertsCheck(initialData);
    }

    private void checkIfChannelResponseIsValid(@Nonnull final JsonObject jsonResponse)
            throws ContentNotAvailableException {
        if (!isNullOrEmpty(jsonResponse.getObject("error"))) {
            final JsonObject errorJsonObject = jsonResponse.getObject("error");
            final int errorCode = errorJsonObject.getInt("code");
            if (errorCode == 404) {
                throw new ContentNotAvailableException("This channel doesn't exist.");
            } else {
                throw new ContentNotAvailableException("Got error:\""
                        + errorJsonObject.getString("status") + "\": "
                        + errorJsonObject.getString("message"));
            }
        }
    }

    @Nonnull
    private Optional<JsonObject> getChannelHeader() {
        if (channelHeader == null) {
            final JsonObject h = initialData.getObject("header");

            if (h.has("c4TabbedHeaderRenderer")) {
                channelHeader = Optional.of(h.getObject("c4TabbedHeaderRenderer"));
            } else if (h.has("carouselHeaderRenderer")) {
                isCarouselHeader = true;
                channelHeader = h.getObject("carouselHeaderRenderer")
                        .getArray("contents")
                        .stream()
                        .filter(JsonObject.class::isInstance)
                        .map(JsonObject.class::cast)
                        .filter(itm -> itm.has("topicChannelDetailsRenderer"))
                        .findFirst()
                        .map(itm -> itm.getObject("topicChannelDetailsRenderer"));
            } else {
                channelHeader = Optional.empty();
            }
        }
        return channelHeader;
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
        return getChannelHeader()
                .flatMap(header -> Optional.ofNullable(header.getString("channelId")).or(
                        () -> Optional.ofNullable(header.getObject("navigationEndpoint")
                                .getObject("browseEndpoint")
                                .getString("browseId"))
                ))
                .or(() -> Optional.ofNullable(redirectedChannelId))
                .orElseThrow(() -> new ParsingException("Could not get channel id"));
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        final String mdName = initialData.getObject("metadata")
                .getObject("channelMetadataRenderer")
                .getString("title");
        if (!isNullOrEmpty(mdName)) {
            return mdName;
        }

        final Optional<JsonObject> header = getChannelHeader();
        if (header.isPresent()) {
            final Object title = header.get().get("title");
            if (title instanceof String) {
                return (String) title;
            } else if (title instanceof JsonObject) {
                final String headerName = getTextFromObject((JsonObject) title);
                if (!isNullOrEmpty(headerName)) {
                    return headerName;
                }
            }
        }

        throw new ParsingException("Could not get channel name");
    }

    @Override
    public String getAvatarUrl() throws ParsingException {
        return getChannelHeader().flatMap(header -> Optional.ofNullable(
                        header.getObject("avatar").getArray("thumbnails")
                                .getObject(0).getString("url")
                ))
                .map(YoutubeParsingHelper::fixThumbnailUrl)
                .orElseThrow(() -> new ParsingException("Could not get avatar"));
    }

    @Override
    public String getBannerUrl() throws ParsingException {
        return getChannelHeader().flatMap(header -> Optional.ofNullable(
                        header.getObject("banner").getArray("thumbnails")
                                .getObject(0).getString("url")
                ))
                .filter(url -> !url.contains("s.ytimg.com") && !url.contains("default_banner"))
                .map(YoutubeParsingHelper::fixThumbnailUrl)
                .orElseThrow(() -> new ParsingException("Could not get banner"));
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
        final Optional<JsonObject> header = getChannelHeader();
        if (header.isPresent()) {
            JsonObject textObject = null;

            if (header.get().has("subscriberCountText")) {
                textObject = header.get().getObject("subscriberCountText");
            } else if (header.get().has("subtitle")) {
                textObject = header.get().getObject("subtitle");
            }

            if (textObject != null) {
                try {
                    return Utils.mixedNumberWordToLong(getTextFromObject(textObject));
                } catch (final NumberFormatException e) {
                    throw new ParsingException("Could not get subscriber count", e);
                }
            }
        }
        return UNKNOWN_SUBSCRIBER_COUNT;
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
        // The CarouselHeaderRenderer does not contain any verification badges.
        // Since it is only shown on YT-internal channels or on channels of large organizations
        // broadcasting live events, we can assume the channel to be verified.
        if (isCarouselHeader) {
            return true;
        }

        return getChannelHeader()
                .map(header -> header.getArray("badges"))
                .map(YoutubeParsingHelper::isVerified)
                .orElse(false);
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
    public InfoItemsPage<StreamInfoItem> getPage(final Page page)
            throws IOException, ExtractionException {
        if (page == null || isNullOrEmpty(page.getUrl())) {
            throw new IllegalArgumentException("Page doesn't contain an URL");
        }

        final List<String> channelIds = page.getIds();

        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

        final JsonObject ajaxJson = getJsonPostResponse("browse", page.getBody(),
                getExtractorLocalization());

        final JsonObject sectionListContinuation = ajaxJson.getArray("onResponseReceivedActions")
                .getObject(0)
                .getObject("appendContinuationItemsAction");

        final JsonObject continuation = collectStreamsFrom(collector, sectionListContinuation
                .getArray("continuationItems"), channelIds);

        return new InfoItemsPage<>(collector, getNextPageFrom(continuation, channelIds));
    }

    @Nullable
    private Page getNextPageFrom(final JsonObject continuations,
                                 final List<String> channelIds)
            throws IOException, ExtractionException {
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
        if (videoTab != null) {
            return videoTab;
        }

        final JsonObject foundVideoTab = YoutubeParsingHelper.getTabByName(initialData, "Videos")
                .orElseThrow(
                        () -> new ContentNotSupportedException("This channel has no Videos tab"));

        final String messageRendererText = getTextFromObject(
                foundVideoTab.getObject("content")
                        .getObject("sectionListRenderer")
                        .getArray("contents")
                        .getObject(0)
                        .getObject("itemSectionRenderer")
                        .getArray("contents")
                        .getObject(0)
                        .getObject("messageRenderer")
                        .getObject("text"));
        if (messageRendererText != null
                && messageRendererText.equals("This channel has no videos.")) {
            return null;
        }

        videoTab = foundVideoTab;
        return foundVideoTab;
    }

    @Override
    public YoutubeChannelPlaylistExtractor getPlaylists() throws ParsingException {
        final JsonObject tab = getPlaylistsTab();
        if (tab != null) {
            return new YoutubeChannelPlaylistExtractor(getService(), getLinkHandler(),
                    tab.getObject("endpoint").getObject("browseEndpoint"));
        }
        return null;
    }

    @Nullable
    private JsonObject getPlaylistsTab() throws ParsingException {
        if (playlistsTab != null) {
            return playlistsTab;
        }

        this.playlistsTab =  YoutubeParsingHelper.getPlaylistsTab(initialData);

        return playlistsTab;
    }

}
