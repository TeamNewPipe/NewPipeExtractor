package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.channel.ChannelTabExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper.fixThumbnailUrl;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper.getJsonResponse;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper.getTextFromObject;

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

    public YoutubeChannelExtractor(StreamingService service, ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        final String url = super.getUrl() + "/videos?pbj=1&view=0&flow=grid";

        final JsonArray ajaxJson = getJsonResponse(url, getExtractorLocalization());
        initialData = ajaxJson.getObject(1).getObject("response");
        YoutubeParsingHelper.defaultAlertsCheck(initialData);
    }

    @Nonnull
    @Override
    public String getUrl() throws ParsingException {
        try {
            return YoutubeChannelLinkHandlerFactory.getInstance().getUrl("channel/" + getId());
        } catch (ParsingException e) {
            return super.getUrl();
        }
    }

    @Nonnull
    @Override
    public String getId() throws ParsingException {
        try {
            return initialData.getObject("header").getObject("c4TabbedHeaderRenderer").getString("channelId");
        } catch (Exception e) {
            throw new ParsingException("Could not get channel id", e);
        }
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        try {
            return initialData.getObject("header").getObject("c4TabbedHeaderRenderer").getString("title");
        } catch (Exception e) {
            throw new ParsingException("Could not get channel name", e);
        }
    }

    @Override
    public String getAvatarUrl() throws ParsingException {
        try {
            String url = initialData.getObject("header").getObject("c4TabbedHeaderRenderer").getObject("avatar")
                    .getArray("thumbnails").getObject(0).getString("url");

            return fixThumbnailUrl(url);
        } catch (Exception e) {
            throw new ParsingException("Could not get avatar", e);
        }
    }

    @Override
    public String getBannerUrl() throws ParsingException {
        try {
            String url = null;
            try {
                url = initialData.getObject("header").getObject("c4TabbedHeaderRenderer").getObject("banner")
                        .getArray("thumbnails").getObject(0).getString("url");
            } catch (Exception ignored) {}
            if (url == null || url.contains("s.ytimg.com") || url.contains("default_banner")) {
                return null;
            }

            return fixThumbnailUrl(url);
        } catch (Exception e) {
            throw new ParsingException("Could not get banner", e);
        }
    }

    @Override
    public String getFeedUrl() throws ParsingException {
        try {
            return YoutubeParsingHelper.getFeedUrlFrom(getId());
        } catch (Exception e) {
            throw new ParsingException("Could not get feed url", e);
        }
    }

    @Override
    public long getSubscriberCount() throws ParsingException {
        final JsonObject subscriberInfo = initialData.getObject("header").getObject("c4TabbedHeaderRenderer").getObject("subscriberCountText");
        if (subscriberInfo != null) {
            try {
                return Utils.mixedNumberWordToLong(getTextFromObject(subscriberInfo));
            } catch (NumberFormatException e) {
                throw new ParsingException("Could not get subscriber count", e);
            }
        } else {
            // If there's no subscribe button, the channel has the subscriber count disabled
            if (initialData.getObject("header").getObject("c4TabbedHeaderRenderer").getObject("subscribeButton") == null) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    @Override
    public String getDescription() throws ParsingException {
        try {
            return initialData.getObject("metadata").getObject("channelMetadataRenderer").getString("description");
        } catch (Exception e) {
            throw new ParsingException("Could not get channel description", e);
        }
    }

    @Override
    public List<ChannelTabExtractor> getTabs() throws ParsingException {
        List<ChannelTabExtractor> tabs = new ArrayList<>();

        if (getTab("videos") != null)
            tabs.add(new YoutubeChannelVideosExtractor(getService(), (ListLinkHandler) getLinkHandler(), getTab("videos")));

        if (getTab("playlists") != null)
            tabs.add(new YoutubeChannelPlaylistsExtractor(getService(), (ListLinkHandler) getLinkHandler(), getName()));

        return tabs;
    }

    private JsonObject getTab(String tabName) throws ParsingException {
        JsonArray tabs = initialData.getObject("contents").getObject("twoColumnBrowseResultsRenderer")
                .getArray("tabs");
        JsonObject videoTab = null;

        for (Object tab : tabs) {
            if (((JsonObject) tab).getObject("tabRenderer") != null) {
                if (((JsonObject) tab).getObject("tabRenderer").getObject("endpoint").getObject("commandMetadata")
                        .getObject("webCommandMetadata").getString("url").endsWith(tabName)) {
                    videoTab = ((JsonObject) tab).getObject("tabRenderer");
                    break;
                }
            }
        }

        try {
            if (getTextFromObject(videoTab.getObject("content").getObject("sectionListRenderer")
                    .getArray("contents").getObject(0).getObject("itemSectionRenderer")
                    .getArray("contents").getObject(0).getObject("messageRenderer")
                    .getObject("text")).startsWith("This channel has no "))
                return null;
        } catch (Exception ignored) {}

        return videoTab;
    }
}
