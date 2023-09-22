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
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

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

    private JsonObject jsonResponse;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<ChannelHeader> channelHeader;

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
        channelAgeGateRenderer = getChannelAgeGateRenderer();
    }

    @Nullable
    private JsonObject getChannelAgeGateRenderer() {
        return jsonResponse.getObject("contents")
                .getObject("twoColumnBrowseResultsRenderer")
                .getArray("tabs")
                .stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .flatMap(tab -> tab.getObject("tabRenderer")
                        .getObject("content")
                        .getObject("sectionListRenderer")
                        .getArray("contents")
                        .stream()
                        .filter(JsonObject.class::isInstance)
                        .map(JsonObject.class::cast))
                .filter(content -> content.has("channelAgeGateRenderer"))
                .map(content -> content.getObject("channelAgeGateRenderer"))
                .findFirst()
                .orElse(null);
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
        return channelHeader.map(header -> header.json)
                .flatMap(header -> Optional.ofNullable(header.getString("channelId"))
                        .or(() -> Optional.ofNullable(header.getObject("navigationEndpoint")
                                .getObject("browseEndpoint")
                                .getString("browseId"))
                ))
                .or(() -> Optional.ofNullable(channelId))
                .orElseThrow(() -> new ParsingException("Could not get channel ID"));
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        assertPageFetched();
        if (channelAgeGateRenderer != null) {
            final String title = channelAgeGateRenderer.getString("channelTitle");
            if (isNullOrEmpty(title)) {
                throw new ParsingException("Could not get channel name");
            }
            return title;
        }

        final String metadataRendererTitle = jsonResponse.getObject("metadata")
                .getObject("channelMetadataRenderer")
                .getString("title");
        if (!isNullOrEmpty(metadataRendererTitle)) {
            return metadataRendererTitle;
        }

        return channelHeader.map(header -> {
            final JsonObject channelJson = header.json;
            switch (header.headerType) {
                case PAGE:
                    return channelJson.getObject("content")
                            .getObject("pageHeaderViewModel")
                            .getObject("title")
                            .getObject("dynamicTextViewModel")
                            .getObject("text")
                            .getString("content", channelJson.getString("pageTitle"));

                case CAROUSEL:
                case INTERACTIVE_TABBED:
                    return getTextFromObject(channelJson.getObject("title"));

                case C4_TABBED:
                default:
                    return channelJson.getString("title");
            }
        })
        // The channel name from a microformatDataRenderer may be different from the one displayed,
        // especially for auto-generated channels, depending on the language requested for the
        // interface (hl parameter of InnerTube requests' payload)
        .or(() -> Optional.ofNullable(jsonResponse.getObject("microformat")
                .getObject("microformatDataRenderer")
                .getString("title")))
        .orElseThrow(() -> new ParsingException("Could not get channel name"));
    }

    @Nonnull
    @Override
    public List<Image> getAvatars() throws ParsingException {
        assertPageFetched();
        if (channelAgeGateRenderer != null) {
            return Optional.ofNullable(channelAgeGateRenderer.getObject("avatar")
                    .getArray("thumbnails"))
                    .map(YoutubeParsingHelper::getImagesFromThumbnailsArray)
                    .orElseThrow(() -> new ParsingException("Could not get avatars"));
        }

        return channelHeader.map(header -> {
            switch (header.headerType) {
                case PAGE:
                    return header.json.getObject("content")
                            .getObject("pageHeaderViewModel")
                            .getObject("image")
                            .getObject("contentPreviewImageViewModel")
                            .getObject("image")
                            .getArray("sources");

                case INTERACTIVE_TABBED:
                    return header.json.getObject("boxArt")
                            .getArray("thumbnails");

                case C4_TABBED:
                case CAROUSEL:
                default:
                    return header.json.getObject("avatar")
                            .getArray("thumbnails");
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

        // No banner is available on pageHeaderRenderer headers
        return channelHeader.filter(header -> header.headerType != HeaderType.PAGE)
                .map(header -> header.json.getObject("banner")
                        .getArray("thumbnails"))
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

        if (channelHeader.isPresent()) {
            final ChannelHeader header = channelHeader.get();

            if (header.headerType == HeaderType.INTERACTIVE_TABBED
                    || header.headerType == HeaderType.PAGE) {
                // No subscriber count is available on interactiveTabbedHeaderRenderer and
                // pageHeaderRenderer headers
                return UNKNOWN_SUBSCRIBER_COUNT;
            }

            final JsonObject headerJson = header.json;
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

    @Override
    public String getDescription() throws ParsingException {
        assertPageFetched();
        if (channelAgeGateRenderer != null) {
            return null;
        }

        try {
            if (channelHeader.isPresent()) {
                final ChannelHeader header = channelHeader.get();

                if (header.headerType == HeaderType.PAGE) {
                    // A pageHeaderRenderer doesn't contain a description
                    return null;
                }

                if (header.headerType == HeaderType.INTERACTIVE_TABBED) {
                    /*
                    In an interactiveTabbedHeaderRenderer, the real description, is only available
                    in its header
                    The other one returned in non-About tabs accessible in the
                    microformatDataRenderer object of the response may be completely different
                    The description extracted is incomplete and the original one can be only
                    accessed from the About tab
                     */
                    return getTextFromObject(header.json.getObject("description"));
                }
            }

            // The description is cut and the original one can be only accessed from the About tab
            return jsonResponse.getObject("metadata")
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
            return false;
        }

        if (channelHeader.isPresent()) {
            final ChannelHeader header = channelHeader.get();

            // carouselHeaderRenderer and pageHeaderRenderer does not contain any verification
            // badges
            // Since they are only shown on YouTube internal channels or on channels of large
            // organizations broadcasting live events, we can assume the channel to be verified
            if (header.headerType == HeaderType.CAROUSEL || header.headerType == HeaderType.PAGE) {
                return true;
            }

            if (header.headerType == HeaderType.INTERACTIVE_TABBED) {
                // If the header has an autoGenerated property, it should mean that the channel has
                // been auto generated by YouTube: we can assume the channel to be verified in this
                // case
                return header.json.has("autoGenerated");
            }

            return YoutubeParsingHelper.isVerified(header.json.getArray("badges"));
        }

        return false;
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
        final JsonArray responseTabs = jsonResponse.getObject("contents")
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
                .filter(tab -> tab.has("tabRenderer"))
                .map(tab -> tab.getObject("tabRenderer"))
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
                                                service, linkHandler, tabRenderer, name, id, url)));

                                break;
                            case "shorts":
                                addNonVideosTab.accept(ChannelTabs.SHORTS);
                                break;
                            case "streams":
                                addNonVideosTab.accept(ChannelTabs.LIVESTREAMS);
                                break;
                            case "playlists":
                                addNonVideosTab.accept(ChannelTabs.PLAYLISTS);
                                break;
                            case "channels":
                                addNonVideosTab.accept(ChannelTabs.CHANNELS);
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
