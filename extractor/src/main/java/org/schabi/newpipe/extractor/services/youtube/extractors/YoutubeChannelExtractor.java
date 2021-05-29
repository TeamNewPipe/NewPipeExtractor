package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonWriter;

import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ContentNotSupportedException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;

import javax.annotation.Nonnull;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.fixThumbnailUrl;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getJsonPostResponse;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getKey;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getValidJsonResponseBody;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareJsonBuilder;
import static org.schabi.newpipe.extractor.utils.Utils.EMPTY_STRING;
import static org.schabi.newpipe.extractor.utils.Utils.UTF_8;
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

@SuppressWarnings("WeakerAccess")
public class YoutubeChannelExtractor extends ChannelExtractor {
    private JsonObject initialData;
    private JsonObject videoTab;

    /**
     * Some channels have response redirects and the only way to reliably get the id is by saving it.
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
        final String channel_path = super.getId();
        final String[] channelInfo = channel_path.split("/");
        String id = "";
        // If the url is an URL which is not a /channel URL, we need to use the
        // navigation/resolve_url endpoint of the youtubei API to get the channel id. Otherwise, we
        // couldn't get information about the channel associated with this URL, if there is one.
        if (!channelInfo[0].equals("channel")) {
            final byte[] body = JsonWriter.string(prepareJsonBuilder(getExtractorLocalization(),
                    getExtractorContentCountry())
                    .value("url", "https://www.youtube.com/" + channel_path)
                    .done())
                    .getBytes(UTF_8);

            final JsonObject jsonResponse = getJsonPostResponse("navigation/resolve_url",
                    body, getExtractorLocalization());

            if (jsonResponse.has("error")) {
                if (jsonResponse.getInt("code") == 404) {
                    throw new ContentNotAvailableException("No channel associated with this user"
                            + "exists");
                } else {
                    throw new ContentNotAvailableException("Got error:\""
                            + jsonResponse.getString("status") + "\""
                            + jsonResponse.getString("message"));
                }
            }

            final JsonObject endpoint = jsonResponse.getObject("endpoint");

            final String webPageType = endpoint.getObject("commandMetadata")
                    .getObject("webCommandMetadata")
                    .getString("webPageType", EMPTY_STRING);

            final JsonObject browseEndpoint = endpoint.getObject("browseEndpoint");
            final String browseId = browseEndpoint.getString("browseId", EMPTY_STRING);

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
            id = channelInfo[1];
        }
        JsonObject ajaxJson = null;

        int level = 0;
        while (level < 3) {
            final byte[] body = JsonWriter.string(prepareJsonBuilder(getExtractorLocalization(),
                    getExtractorContentCountry())
                    .value("browseId", id)
                    .value("params", "EgZ2aWRlb3M%3D") // Equal to videos
                    .done())
                    .getBytes(UTF_8);

            final JsonObject jsonResponse = getJsonPostResponse("browse", body,
                    getExtractorLocalization());

            if (!isNullOrEmpty(jsonResponse.getObject("error"))) {
                final int errorCode = jsonResponse.getObject("error").getInt("code");
                if (errorCode == 400) {
                    throw new ContentNotAvailableException("This channel doesn't exists");
                } else {
                    throw new ContentNotAvailableException("Got error:\""
                            + jsonResponse.getString("status") + "\""
                            + jsonResponse.getString("message"));
                }
            }

            final JsonObject endpoint = jsonResponse.getArray("onResponseReceivedActions")
                    .getObject(0)
                    .getObject("navigateAction")
                    .getObject("endpoint");

            final String webPageType = endpoint.getObject("commandMetadata")
                    .getObject("webCommandMetadata")
                    .getString("webPageType", EMPTY_STRING);

            final String browseId = endpoint.getObject("browseEndpoint").getString("browseId",
                    EMPTY_STRING);

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
                .getString("channelId", EMPTY_STRING);

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
            String url = initialData.getObject("header")
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
            String url = initialData.getObject("header")
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
        if (c4TabbedHeaderRenderer.has("subscriberCountText")) {
            try {
                return Utils.mixedNumberWordToLong(getTextFromObject(c4TabbedHeaderRenderer
                        .getObject("subscriberCountText")));
            } catch (final NumberFormatException e) {
                throw new ParsingException("Could not get subscriber count", e);
            }
        } else {
            return ITEM_COUNT_UNKNOWN;
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
    public String getParentChannelName() throws ParsingException {
        return "";
    }

    @Override
    public String getParentChannelUrl() throws ParsingException {
        return "";
    }

    @Override
    public String getParentChannelAvatarUrl() throws ParsingException {
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
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws IOException, ExtractionException {
        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

        Page nextPage = null;

        if (getVideoTab() != null) {
            final JsonObject gridRenderer = getVideoTab().getObject("content")
                    .getObject("sectionListRenderer")
                    .getArray("contents").getObject(0).getObject("itemSectionRenderer")
                    .getArray("contents").getObject(0).getObject("gridRenderer");

            final JsonObject continuation = collectStreamsFrom(collector, gridRenderer
                    .getArray("items"));

            nextPage = getNextPageFrom(continuation);
        }

        return new InfoItemsPage<>(collector, nextPage);
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(final Page page) throws IOException,
            ExtractionException {
        if (page == null || isNullOrEmpty(page.getUrl())) {
            throw new IllegalArgumentException("Page doesn't contain an URL");
        }

        // Unfortunately, we have to fetch the page even if we are only getting next streams,
        // as they don't deliver enough information on their own (the channel name, for example).
        fetchPage();

        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        final Response response = getDownloader().post(page.getUrl(), null, page.getBody(),
                getExtractorLocalization());

        final JsonObject ajaxJson = JsonUtils.toJsonObject(getValidJsonResponseBody(response));

        JsonObject sectionListContinuation = ajaxJson.getArray("onResponseReceivedActions")
                .getObject(0)
                .getObject("appendContinuationItemsAction");

        final JsonObject continuation = collectStreamsFrom(collector, sectionListContinuation
                .getArray("continuationItems"));

        return new InfoItemsPage<>(collector, getNextPageFrom(continuation));
    }

    private Page getNextPageFrom(final JsonObject continuations) throws IOException,
            ExtractionException {
        if (isNullOrEmpty(continuations)) {
            return null;
        }

        final JsonObject continuationEndpoint = continuations.getObject("continuationEndpoint");
        final String continuation = continuationEndpoint.getObject("continuationCommand")
                .getString("token");

        final byte[] body = JsonWriter.string(prepareJsonBuilder(getExtractorLocalization(),
                getExtractorContentCountry())
                .value("continuation", continuation)
                .done())
                .getBytes(UTF_8);

        return new Page("https://www.youtube.com/youtubei/v1/browse?key=" + getKey(), body);
    }

    /**
     * Collect streams from an array of items
     *
     * @param collector the collector where videos will be commited
     * @param videos    the array to get videos from
     * @return the continuation object
     * @throws ParsingException if an error happened while extracting
     */
    private JsonObject collectStreamsFrom(final StreamInfoItemsCollector collector,
                                          final JsonArray videos) throws ParsingException {
        collector.reset();

        final String uploaderName = getName();
        final String uploaderUrl = getUrl();
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
            } else if (video.has("continuationItemRenderer")) {
                continuation = video.getObject("continuationItemRenderer");
            }
        }

        return continuation;
    }

    private JsonObject getVideoTab() throws ParsingException {
        if (this.videoTab != null) return this.videoTab;

        JsonArray tabs = initialData.getObject("contents")
                .getObject("twoColumnBrowseResultsRenderer")
                .getArray("tabs");
        JsonObject videoTab = null;

        for (final Object tab : tabs) {
            if (((JsonObject) tab).has("tabRenderer")) {
                if (((JsonObject) tab).getObject("tabRenderer").getString("title",
                        EMPTY_STRING).equals("Videos")) {
                    videoTab = ((JsonObject) tab).getObject("tabRenderer");
                    break;
                }
            }
        }

        if (videoTab == null) {
            throw new ContentNotSupportedException("This channel has no Videos tab");
        }

        final String messageRendererText = getTextFromObject(videoTab.getObject("content")
                .getObject("sectionListRenderer").getArray("contents").getObject(0)
                .getObject("itemSectionRenderer").getArray("contents").getObject(0)
                .getObject("messageRenderer").getObject("text"));
        if (messageRendererText != null
                && messageRendererText.equals("This channel has no videos.")) {
            return null;
        }

        this.videoTab = videoTab;
        return videoTab;
    }
}
