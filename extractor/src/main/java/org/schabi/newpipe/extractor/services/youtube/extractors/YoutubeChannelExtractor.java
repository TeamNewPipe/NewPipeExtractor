package org.schabi.newpipe.extractor.services.youtube.extractors;

import static org.schabi.newpipe.extractor.services.youtube.YouTubeChannelHelper.ChannelResponseData;
import static org.schabi.newpipe.extractor.services.youtube.YouTubeChannelHelper.getChannelResponse;
import static org.schabi.newpipe.extractor.services.youtube.YouTubeChannelHelper.resolveChannelId;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.channel.ChannelTabExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ChannelTabs;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.linkhandler.ReadyChannelTabListLinkHandler;
import org.schabi.newpipe.extractor.services.youtube.YouTubeChannelHelper;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
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
    private Optional<YouTubeChannelHelper.ChannelHeader> channelHeader;
    private JsonObject videoTab;

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
        final String id = resolveChannelId(channelPath);
        // Fetch video tab
        final ChannelResponseData data = getChannelResponse(id, "EgZ2aWRlb3M%3D",
                getExtractorLocalization(), getExtractorContentCountry());

        initialData = data.responseJson;
        redirectedChannelId = data.channelId;
    }

    @Nonnull
    private Optional<YouTubeChannelHelper.ChannelHeader> getChannelHeader() {
        if (channelHeader == null) {
            channelHeader = YouTubeChannelHelper.getChannelHeader(initialData);
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
                .flatMap(header -> Optional.ofNullable(header.json.getString("channelId")).or(
                        () -> Optional.ofNullable(header.json.getObject("navigationEndpoint")
                                .getObject("browseEndpoint")
                                .getString("browseId"))
                ))
                .or(() -> Optional.ofNullable(redirectedChannelId))
                .orElseThrow(() -> new ParsingException("Could not get channel id"));
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        final String metadataName = initialData.getObject("metadata")
                .getObject("channelMetadataRenderer")
                .getString("title");
        if (!isNullOrEmpty(metadataName)) {
            return metadataName;
        }

        return getChannelHeader().flatMap(header -> {
            final Object title = header.json.get("title");
            if (title instanceof String) {
                return Optional.of((String) title);
            } else if (title instanceof JsonObject) {
                final String headerName = getTextFromObject((JsonObject) title);
                if (!isNullOrEmpty(headerName)) {
                    return Optional.of(headerName);
                }
            }
            return Optional.empty();
        }).orElseThrow(() -> new ParsingException("Could not get channel name"));
    }

    @Override
    public String getAvatarUrl() throws ParsingException {
        return getChannelHeader().flatMap(header -> Optional.ofNullable(
                        header.json.getObject("avatar").getArray("thumbnails")
                                .getObject(0).getString("url")
                ))
                .map(YoutubeParsingHelper::fixThumbnailUrl)
                .orElseThrow(() -> new ParsingException("Could not get avatar"));
    }

    @Override
    public String getBannerUrl() throws ParsingException {
        return getChannelHeader().flatMap(header -> Optional.ofNullable(
                        header.json.getObject("banner").getArray("thumbnails")
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
        final Optional<YouTubeChannelHelper.ChannelHeader> headerOpt = getChannelHeader();
        if (headerOpt.isPresent()) {
            final JsonObject header = headerOpt.get().json;
            JsonObject textObject = null;

            if (header.has("subscriberCountText")) {
                textObject = header.getObject("subscriberCountText");
            } else if (header.has("subtitle")) {
                textObject = header.getObject("subtitle");
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
        final Optional<YouTubeChannelHelper.ChannelHeader> headerOpt = getChannelHeader();
        if (headerOpt.isPresent()) {
            final YouTubeChannelHelper.ChannelHeader header = headerOpt.get();

            // The CarouselHeaderRenderer does not contain any verification badges.
            // Since it is only shown on YT-internal channels or on channels of large organizations
            // broadcasting live events, we can assume the channel to be verified.
            if (header.isCarouselHeader) {
                return true;
            }
            return YoutubeParsingHelper.isVerified(header.json.getArray("badges"));
        }
        return false;
    }

    @Nonnull
    @Override
    public List<ListLinkHandler> getTabs() throws ParsingException {
        final JsonArray responseTabs = initialData.getObject("contents")
                .getObject("twoColumnBrowseResultsRenderer")
                .getArray("tabs");

        final List<ListLinkHandler> tabs = new ArrayList<>();
        final Consumer<String> addTab = tab -> {
            try {
                tabs.add(YoutubeChannelTabLinkHandlerFactory.getInstance().fromQuery(
                        redirectedChannelId, Collections.singletonList(tab), ""));
            } catch (final ParsingException ignored) {
            }
        };

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
                            // since the videos tab already has its contents fetched, make sure
                            // it is in the first position
                            String name = "";
                            try {
                                name = getName();
                            } catch (final ParsingException ignored) {
                            }
                            tabs.add(0, new ReadyChannelTabListLinkHandler(tabUrl,
                                    redirectedChannelId, ChannelTabs.VIDEOS,
                                    new VideoTabExtractorBuilder(name, getUrl(), getId(),
                                            tabRenderer)));
                            break;
                        case "playlists":
                            addTab.accept(ChannelTabs.PLAYLISTS);
                            break;
                        case "streams":
                            addTab.accept(ChannelTabs.LIVESTREAMS);
                            break;
                        case "shorts":
                            addTab.accept(ChannelTabs.SHORTS);
                            break;
                        case "channels":
                            addTab.accept(ChannelTabs.CHANNELS);
                            break;
                    }
                }
            }
        }

        return tabs;
    }

    @Nonnull
    @Override
    public List<String> getTags() throws ParsingException {
        final JsonArray tags = initialData.getObject("microformat")
                .getObject("microformatDataRenderer").getArray("tags");

        return tags.stream().map(Object::toString).collect(Collectors.toList());
    }

    private static class VideoTabExtractorBuilder
            implements ReadyChannelTabListLinkHandler.ChannelTabExtractorBuilder {
        private final String channelName;
        private final String channelUrl;
        private final String channelId;
        private final JsonObject tabRenderer;

        VideoTabExtractorBuilder(final String channelName, final String channelUrl,
                                 final String channelId, final JsonObject tabRenderer) {
            this.channelName = channelName;
            this.channelUrl = channelUrl;
            this.channelId = channelId;
            this.tabRenderer = tabRenderer;
        }

        @Override
        public ChannelTabExtractor build(final StreamingService service,
                                         final ListLinkHandler linkHandler) {
            return new YoutubeChannelTabExtractor.VideoTabExtractor(
                    service, linkHandler, tabRenderer, channelName, channelId, channelUrl);
        }
    }
}
