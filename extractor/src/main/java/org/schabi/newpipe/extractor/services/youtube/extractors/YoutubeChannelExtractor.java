package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import static org.schabi.newpipe.extractor.utils.Utils.HTTP;
import static org.schabi.newpipe.extractor.utils.Utils.HTTPS;

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
    /*package-private*/ static final String CHANNEL_URL_BASE = "https://www.youtube.com/channel/";
    private static final String CHANNEL_URL_PARAMETERS = "/videos?view=0&flow=list&sort=dd&live_view=10000";

    private Document doc;
    private JsonObject initialData;

    public YoutubeChannelExtractor(StreamingService service, ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        String channelUrl = super.getUrl() + CHANNEL_URL_PARAMETERS;
        final Response response = downloader.get(channelUrl, getExtractorLocalization());
        doc = YoutubeParsingHelper.parseAndCheckPage(channelUrl, response);
        initialData = YoutubeParsingHelper.getInitialData(response.responseBody());
    }


    @Override
    public String getNextPageUrl() throws ExtractionException {
        return getNextPageUrlFrom(getVideoTab().getObject("content").getObject("sectionListRenderer").getArray("continuations"));
    }

    @Nonnull
    @Override
    public String getUrl() throws ParsingException {
        try {
            return CHANNEL_URL_BASE + getId();
        } catch (ParsingException e) {
            return super.getUrl();
        }
    }

    @Nonnull
    @Override
    public String getId() throws ParsingException {
        try {
            return doc.select("meta[property=\"og:url\"]").first().attr("content").replace(CHANNEL_URL_BASE, "");
        } catch (Exception ignored) {}
        try {
            return initialData.getObject("header").getObject("c4TabbedHeaderRenderer").getObject("navigationEndpoint").getObject("browseEndpoint").getString("browseId");
        } catch (Exception ignored) {}

        // fallback method; does not work with channels that have no "Subscribe" button (e.g. EminemVEVO)
        try {
            Element element = doc.getElementsByClass("yt-uix-subscription-button").first();
            if (element == null) element = doc.getElementsByClass("yt-uix-subscription-preferences-button").first();

            return element.attr("data-channel-external-id");
        } catch (Exception e) {
            throw new ParsingException("Could not get channel id", e);
        }
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        try {
            return doc.select("meta[property=\"og:title\"]").first().attr("content");
        } catch (Exception e) {
            throw new ParsingException("Could not get channel name", e);
        }
    }

    @Override
    public String getAvatarUrl() throws ParsingException {
        try {
            return initialData.getObject("header").getObject("c4TabbedHeaderRenderer").getObject("avatar").getArray("thumbnails").getObject(0).getString("url");
        } catch (Exception e) {
            throw new ParsingException("Could not get avatar", e);
        }
    }

    @Override
    public String getBannerUrl() throws ParsingException {
        try {
            String url = null;
            try {
                url = initialData.getObject("header").getObject("c4TabbedHeaderRenderer").getObject("banner").getArray("thumbnails").getObject(0).getString("url");
            } catch (Exception ignored) {}
            if (url == null || url.contains("s.ytimg.com") || url.contains("default_banner")) {
                return null;
            }
            // the first characters of the banner URLs are different for each channel and some are not even valid URLs
            if (url.startsWith("//")) {
                url = url.substring(2);
            }
            if (url.startsWith(HTTP)) {
                url = Utils.replaceHttpWithHttps(url);
            } else if (!url.startsWith(HTTPS)) {
                url = HTTPS + url;
            }

            return url;
        } catch (Exception e) {
            throw new ParsingException("Could not get Banner", e);
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
                return Utils.mixedNumberWordToLong(subscriberInfo.getArray("runs").getObject(0).getString("text"));
            } catch (NumberFormatException e) {
                throw new ParsingException("Could not get subscriber count", e);
            }
        } else {
            // If the element is null, the channel have the subscriber count disabled
            return -1;
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

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws ExtractionException {
        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

        JsonArray videos = getVideoTab().getObject("content").getObject("sectionListRenderer").getArray("contents");
        collectStreamsFrom(collector, videos);

        return new InfoItemsPage<>(collector, getNextPageUrl());
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(String pageUrl) throws IOException, ExtractionException {
        if (pageUrl == null || pageUrl.isEmpty()) {
            throw new ExtractionException(new IllegalArgumentException("Page url is empty or null"));
        }

        // Unfortunately, we have to fetch the page even if we are only getting next streams,
        // as they don't deliver enough information on their own (the channel name, for example).
        fetchPage();

        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        JsonArray ajaxJson;

        Map<String, List<String>> headers = new HashMap<>();
        headers.put("X-YouTube-Client-Name", Collections.singletonList("1"));
        try {
            // Use the hardcoded client version first to get JSON with a structure we know
            headers.put("X-YouTube-Client-Version",
                    Collections.singletonList(YoutubeParsingHelper.HARDCODED_CLIENT_VERSION));
            final String response = getDownloader().get(pageUrl, headers, getExtractorLocalization()).responseBody();
            if (response.length() < 50) { // ensure to have a valid response
                throw new ParsingException("Could not parse json data for next streams");
            }
            ajaxJson = JsonParser.array().from(response);
        } catch (Exception e) {
            try {
                headers.put("X-YouTube-Client-Version",
                        Collections.singletonList(YoutubeParsingHelper.getClientVersion(initialData, doc.toString())));
                final String response = getDownloader().get(pageUrl, headers, getExtractorLocalization()).responseBody();
                if (response.length() < 50) { // ensure to have a valid response
                    throw new ParsingException("Could not parse json data for next streams");
                }
                ajaxJson = JsonParser.array().from(response);
            } catch (JsonParserException ignored) {
                throw new ParsingException("Could not parse json data for next streams", e);
            }
        }

        JsonObject sectionListContinuation = ajaxJson.getObject(1).getObject("response")
                .getObject("continuationContents").getObject("sectionListContinuation");

        collectStreamsFrom(collector, sectionListContinuation.getArray("contents"));

        return new InfoItemsPage<>(collector, getNextPageUrlFrom(sectionListContinuation.getArray("continuations")));
    }


    private String getNextPageUrlFrom(JsonArray continuations) {
        if (continuations == null) {
            return "";
        }

        JsonObject nextContinuationData = continuations.getObject(0).getObject("nextContinuationData");
        String continuation = nextContinuationData.getString("continuation");
        String clickTrackingParams = nextContinuationData.getString("clickTrackingParams");
        return "https://www.youtube.com/browse_ajax?ctoken=" + continuation + "&continuation=" + continuation
                + "&itct=" + clickTrackingParams;
    }

    private void collectStreamsFrom(StreamInfoItemsCollector collector, JsonArray videos) throws ParsingException {
        collector.reset();

        final String uploaderName = getName();
        final String uploaderUrl = getUrl();
        final TimeAgoParser timeAgoParser = getTimeAgoParser();

        for (Object video : videos) {
            JsonObject videoInfo = ((JsonObject) video).getObject("itemSectionRenderer")
                    .getArray("contents").getObject(0);
            if (videoInfo.getObject("videoRenderer") != null) {
                collector.commit(new YoutubeStreamInfoItemExtractor(videoInfo.getObject("videoRenderer"), timeAgoParser) {
                    @Override
                    public String getUploaderName() {
                        return uploaderName;
                    }

                    @Override
                    public String getUploaderUrl() {
                        return uploaderUrl;
                    }
                });
            }
        }
    }

    private JsonObject getVideoTab() throws ParsingException {
        JsonArray tabs = initialData.getObject("contents").getObject("twoColumnBrowseResultsRenderer")
                .getArray("tabs");
        JsonObject videoTab = null;

        for (Object tab : tabs) {
            if (((JsonObject) tab).getObject("tabRenderer") != null) {
                if (((JsonObject) tab).getObject("tabRenderer").getString("title").equals("Videos")) {
                    videoTab = ((JsonObject) tab).getObject("tabRenderer");
                    break;
                }
            }
        }

        if (videoTab == null) {
            throw new ParsingException("Could not find Videos tab");
        }

        return videoTab;
    }
}
