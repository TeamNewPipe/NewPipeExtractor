/*
 * Created by Christian Schabesberger on 25.07.16.
 *
 * Copyright (C) 2018 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * YoutubeChannelExtractor.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.schabi.newpipe.extractor.services.youtube.extractors;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeChannelHelper.getChannelResponse;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeChannelHelper.resolveChannelId;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.linkhandler.ReadyChannelTabListLinkHandler;
import org.schabi.newpipe.extractor.services.youtube.YoutubeChannelHelper;
import org.schabi.newpipe.extractor.services.youtube.YoutubeChannelHelper.ChannelHeader;
import org.schabi.newpipe.extractor.services.youtube.YoutubeChannelHelper.ChannelHeader.HeaderType;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeChannelTabExtractor.VideosTabExtractor;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelTabLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class YoutubeChannelExtractor extends ChannelExtractor {

    // Constants of objects used multiples from channel responses
    private static final String IMAGE = "image";
    private static final String CONTENTS = "contents";
    private static final String CONTENT_PREVIEW_IMAGE_VIEW_MODEL = "contentPreviewImageViewModel";
    private static final String PAGE_HEADER_VIEW_MODEL = "pageHeaderViewModel";
    private static final String TAB_RENDERER = "tabRenderer";
    private static final String CONTENT = "content";
    private static final String METADATA = "metadata";
    private static final String AVATAR = "avatar";
    private static final String THUMBNAILS = "thumbnails";
    private static final String SOURCES = "sources";
    private static final String BANNER = "banner";

    private JsonObject jsonResponse;

    @Nullable
    private ChannelHeader channelHeader;

    private String channelId;

    /**
     * If a channel is age-restricted, its pages are only accessible to logged-in and
     * age-verified users, we get an {@code channelAgeGateRenderer} in this case, containing only
     * the following metadata: channel name and channel avatar.
     *
     * <p>
     * This restriction doesn't seem to apply to all countries.
     * </p>
     */
    @Nullable
    private JsonObject channelAgeGateRenderer;

    public YoutubeChannelExtractor(final StreamingService service,
                                   final ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        final String channelPath = super.getId();
        final String id = resolveChannelId(channelPath);
        // Fetch Videos tab
        final YoutubeChannelHelper.ChannelResponseData data = getChannelResponse(id,
                "EgZ2aWRlb3PyBgQKAjoA", getExtractorLocalization(), getExtractorContentCountry());

        jsonResponse = data.jsonResponse;
        channelHeader = YoutubeChannelHelper.getChannelHeader(jsonResponse);
        channelId = data.channelId;
        channelAgeGateRenderer = YoutubeChannelHelper.getChannelAgeGateRenderer(jsonResponse);
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
        assertPageFetched();
        return YoutubeChannelHelper.getChannelId(channelHeader, jsonResponse, channelId);
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        assertPageFetched();
        return YoutubeChannelHelper.getChannelName(
                channelHeader, channelAgeGateRenderer, jsonResponse);
    }

    @Nonnull
    @Override
    public List<Image> getAvatars() throws ParsingException {
        assertPageFetched();
        if (channelAgeGateRenderer != null) {
            return Optional.ofNullable(channelAgeGateRenderer.getObject(AVATAR)
                    .getArray(THUMBNAILS))
                    .map(YoutubeParsingHelper::getImagesFromThumbnailsArray)
                    .orElseThrow(() -> new ParsingException("Could not get avatars"));
        }

        return Optional.ofNullable(channelHeader)
                .map(header -> {
                    switch (header.headerType) {
                        case PAGE:
                            final JsonObject imageObj = header.json.getObject(CONTENT)
                                    .getObject(PAGE_HEADER_VIEW_MODEL)
                                    .getObject(IMAGE);

                            if (imageObj.has(CONTENT_PREVIEW_IMAGE_VIEW_MODEL)) {
                                return imageObj.getObject(CONTENT_PREVIEW_IMAGE_VIEW_MODEL)
                                        .getObject(IMAGE)
                                        .getArray(SOURCES);
                            }

                            if (imageObj.has("decoratedAvatarViewModel")) {
                                return imageObj.getObject("decoratedAvatarViewModel")
                                        .getObject(AVATAR)
                                        .getObject("avatarViewModel")
                                        .getObject(IMAGE)
                                        .getArray(SOURCES);
                            }

                            // Return an empty avatar array as a fallback
                            return new JsonArray();
                        case INTERACTIVE_TABBED:
                            return header.json.getObject("boxArt")
                                    .getArray(THUMBNAILS);
                        case C4_TABBED:
                        case CAROUSEL:
                        default:
                            return header.json.getObject(AVATAR)
                                    .getArray(THUMBNAILS);
                    }
                })
                .map(YoutubeParsingHelper::getImagesFromThumbnailsArray)
                .orElseThrow(() -> new ParsingException("Could not get avatars"));
    }

    @Nonnull
    @Override
    public List<Image> getBanners() {
        assertPageFetched();
        if (channelAgeGateRenderer != null) {
            return List.of();
        }

        return Optional.ofNullable(channelHeader)
                .map(header -> {
                    if (header.headerType == HeaderType.PAGE) {
                        final JsonObject pageHeaderViewModel = header.json.getObject(CONTENT)
                                .getObject(PAGE_HEADER_VIEW_MODEL);

                        if (pageHeaderViewModel.has(BANNER)) {
                            return pageHeaderViewModel.getObject(BANNER)
                                    .getObject("imageBannerViewModel")
                                    .getObject(IMAGE)
                                    .getArray(SOURCES);
                        }

                        // No banner is available (this should happen on pageHeaderRenderers of
                        // system channels), use an empty JsonArray instead
                        return new JsonArray();
                    }

                    return header.json
                            .getObject(BANNER)
                            .getArray(THUMBNAILS);
                })
                .map(YoutubeParsingHelper::getImagesFromThumbnailsArray)
                .orElse(List.of());
    }

    @Override
    public String getFeedUrl() throws ParsingException {
        // RSS feeds are accessible for age-restricted channels, no need to check whether a channel
        // has a channelAgeGateRenderer
        try {
            return YoutubeParsingHelper.getFeedUrlFrom(getId());
        } catch (final Exception e) {
            throw new ParsingException("Could not get feed URL", e);
        }
    }

    @Override
    public long getSubscriberCount() throws ParsingException {
        assertPageFetched();
        if (channelAgeGateRenderer != null) {
            return UNKNOWN_SUBSCRIBER_COUNT;
        }

        if (channelHeader != null) {
            if (channelHeader.headerType == HeaderType.INTERACTIVE_TABBED) {
                // No subscriber count is available on interactiveTabbedHeaderRenderer header
                return UNKNOWN_SUBSCRIBER_COUNT;
            }

            final JsonObject headerJson = channelHeader.json;
            if (channelHeader.headerType == HeaderType.PAGE) {
                return getSubscriberCountFromPageChannelHeader(headerJson);
            }

            JsonObject textObject = null;

            if (headerJson.has("subscriberCountText")) {
                textObject = headerJson.getObject("subscriberCountText");
            } else if (headerJson.has("subtitle")) {
                textObject = headerJson.getObject("subtitle");
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

    private long getSubscriberCountFromPageChannelHeader(@Nonnull final JsonObject headerJson)
            throws ParsingException {
        final JsonObject metadataObject = headerJson.getObject(CONTENT)
                .getObject(PAGE_HEADER_VIEW_MODEL)
                .getObject(METADATA);
        if (metadataObject.has("contentMetadataViewModel")) {
            final JsonArray metadataPart = metadataObject.getObject("contentMetadataViewModel")
                    .getArray("metadataRows")
                    .stream()
                    .filter(JsonObject.class::isInstance)
                    .map(JsonObject.class::cast)
                    .map(metadataRow -> metadataRow.getArray("metadataParts"))
                    /*
                    Find metadata parts which have two elements: channel handle and subscriber
                    count.

                    On autogenerated music channels, the subscriber count is not shown with this
                    header.

                    Use the first metadata parts object found.
                     */
                    .filter(metadataParts -> metadataParts.size() == 2)
                    .findFirst()
                    .orElse(null);
            if (metadataPart == null) {
                // As the parsing of the metadata parts object needed to get the subscriber count
                // is fragile, return UNKNOWN_SUBSCRIBER_COUNT when it cannot be got
                return UNKNOWN_SUBSCRIBER_COUNT;
            }

            try {
                // The subscriber count is at the same position for all languages as of 02/03/2024
                return Utils.mixedNumberWordToLong(metadataPart.getObject(0)
                        .getObject("text")
                        .getString(CONTENT));
            } catch (final NumberFormatException e) {
                throw new ParsingException("Could not get subscriber count", e);
            }
        }

        // If the channel header has no contentMetadataViewModel (which is the case for system
        // channels using this header), return UNKNOWN_SUBSCRIBER_COUNT
        return UNKNOWN_SUBSCRIBER_COUNT;
    }

    @Override
    public String getDescription() throws ParsingException {
        assertPageFetched();
        if (channelAgeGateRenderer != null) {
            return null;
        }

        try {
            if (channelHeader != null
                    && channelHeader.headerType == HeaderType.INTERACTIVE_TABBED) {
                /*
                In an interactiveTabbedHeaderRenderer, the real description, is only available
                in its header
                The other one returned in non-About tabs accessible in the
                microformatDataRenderer object of the response may be completely different
                The description extracted is incomplete and the original one can be only
                accessed from the About tab
                 */
                return getTextFromObject(channelHeader.json.getObject("description"));
            }

            return jsonResponse.getObject(METADATA)
                    .getObject("channelMetadataRenderer")
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

    @Nonnull
    @Override
    public List<Image> getParentChannelAvatars() {
        return List.of();
    }

    @Override
    public boolean isVerified() throws ParsingException {
        assertPageFetched();
        if (channelAgeGateRenderer != null) {
            // Verified status is unknown with channelAgeGateRenderers, return false in this case
            return false;
        }

        if (channelHeader == null) {
            throw new ParsingException(
            "Could not get channel verified status, no channel header has been extracted");
        }

        return YoutubeChannelHelper.isChannelVerified(channelHeader);
    }

    @Nonnull
    @Override
    public List<ListLinkHandler> getTabs() throws ParsingException {
        assertPageFetched();
        if (channelAgeGateRenderer == null) {
            return getTabsForNonAgeRestrictedChannels();
        }

        return getTabsForAgeRestrictedChannels();
    }

    @Nonnull
    private List<ListLinkHandler> getTabsForNonAgeRestrictedChannels() throws ParsingException {
        final JsonArray responseTabs = jsonResponse.getObject(CONTENTS)
                .getObject("twoColumnBrowseResultsRenderer")
                .getArray("tabs");

        final List<ListLinkHandler> tabs = new ArrayList<>();
        final Consumer<String> addNonVideosTab = tabName -> {
            try {
                tabs.add(YoutubeChannelTabLinkHandlerFactory.getInstance().fromQuery(
                        channelId, List.of(tabName), ""));
            } catch (final ParsingException ignored) {
                // Do not add the tab if we couldn't create the LinkHandler
            }
        };

        final String name = getName();
        final String url = getUrl();
        final String id = getId();

        responseTabs.stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .filter(tab -> tab.has(TAB_RENDERER))
                .map(tab -> tab.getObject(TAB_RENDERER))
                .forEach(tabRenderer -> {
                    final String tabUrl = tabRenderer.getObject("endpoint")
                            .getObject("commandMetadata")
                            .getObject("webCommandMetadata")
                            .getString("url");
                    if (tabUrl != null) {
                        final String[] urlParts = tabUrl.split("/");
                        if (urlParts.length == 0) {
                            return;
                        }

                        final String urlSuffix = urlParts[urlParts.length - 1];

                        /*
                        Make a copy of the channelHeader member to avoid keeping a reference to
                        this YoutubeChannelExtractor instance which would prevent serialization of
                        the ReadyChannelTabListLinkHandler instance created above
                         */
                        final ChannelHeader channelHeaderCopy;
                        if (channelHeader == null) {
                            channelHeaderCopy = null;
                        } else {
                            channelHeaderCopy = new ChannelHeader(channelHeader.json,
                                    channelHeader.headerType);
                        }

                        switch (urlSuffix) {
                            case "videos":
                                // Since the Videos tab has already its contents fetched, make
                                // sure it is in the first position
                                // YoutubeChannelTabExtractor still supports fetching this tab
                                tabs.add(0, new ReadyChannelTabListLinkHandler(
                                        tabUrl,
                                        channelId,
                                        ChannelTabs.VIDEOS,
                                        (service, linkHandler) -> new VideosTabExtractor(
                                                service, linkHandler, tabRenderer,
                                                channelHeaderCopy, name, id, url)));
                                break;
                            case "shorts":
                                addNonVideosTab.accept(ChannelTabs.SHORTS);
                                break;
                            case "streams":
                                addNonVideosTab.accept(ChannelTabs.LIVESTREAMS);
                                break;
                            case "releases":
                                addNonVideosTab.accept(ChannelTabs.ALBUMS);
                                break;
                            case "playlists":
                                addNonVideosTab.accept(ChannelTabs.PLAYLISTS);
                                break;
                            default:
                                // Unsupported channel tab, ignore it
                                break;
                        }
                    }
                });

        return Collections.unmodifiableList(tabs);
    }

    @Nonnull
    private List<ListLinkHandler> getTabsForAgeRestrictedChannels() throws ParsingException {
        // As we don't have access to the channel tabs list, consider that the channel has videos,
        // shorts and livestreams, the data only accessible without login on YouTube's desktop
        // client using uploads system playlists
        // The playlists channel tab is still available on YouTube Music, but this is not
        // implemented in the extractor

        final List<ListLinkHandler> tabs = new ArrayList<>();
        final String channelUrl = getUrl();

        final Consumer<String> addTab = tabName ->
                tabs.add(new ReadyChannelTabListLinkHandler(channelUrl + "/" + tabName,
                        channelId, tabName, YoutubeChannelTabPlaylistExtractor::new));

        addTab.accept(ChannelTabs.VIDEOS);
        addTab.accept(ChannelTabs.SHORTS);
        addTab.accept(ChannelTabs.LIVESTREAMS);
        return Collections.unmodifiableList(tabs);
    }

    @Nonnull
    @Override
    public List<String> getTags() throws ParsingException {
        assertPageFetched();
        if (channelAgeGateRenderer != null) {
            return List.of();
        }

        return jsonResponse.getObject("microformat")
                .getObject("microformatDataRenderer")
                .getArray("tags")
                .stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .collect(Collectors.toUnmodifiableList());
    }
}
