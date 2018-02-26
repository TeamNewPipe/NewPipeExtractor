package org.schabi.newpipe.extractor.services.youtube;


import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nonnull;
import java.io.IOException;

/*
 * Created by Christian Schabesberger on 25.07.16.
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
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
    private static final String CHANNEL_FEED_BASE = "https://www.youtube.com/feeds/videos.xml?channel_id=";
    private static final String CHANNEL_URL_PARAMETERS = "/videos?view=0&flow=list&sort=dd&live_view=10000";

    private Document doc;

    public YoutubeChannelExtractor(StreamingService service, String url) {
        super(service, url);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        String channelUrl = super.getCleanUrl() + CHANNEL_URL_PARAMETERS;
        String pageContent = downloader.download(channelUrl);
        doc = Jsoup.parse(pageContent, channelUrl);

    }

    @Override
    public String getNextPageUrl() throws ExtractionException {
        return getNextPageUrlFrom(doc);
    }

    @Nonnull
    @Override
    public String getCleanUrl() {
        try {
            return "https://www.youtube.com/channel/" + getId();
        } catch (ParsingException e) {
            return super.getCleanUrl();
        }
    }

    @Nonnull
    @Override
    public String getId() throws ParsingException {
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
            return doc.select("img[class=\"channel-header-profile-image\"]").first().attr("abs:src");
        } catch (Exception e) {
            throw new ParsingException("Could not get avatar", e);
        }
    }

    @Override
    public String getBannerUrl() throws ParsingException {
        try {
            Element el = doc.select("div[id=\"gh-banner\"]").first().select("style").first();
            String cssContent = el.html();
            String url = "https:" + Parser.matchGroup1("url\\(([^)]+)\\)", cssContent);

            return url.contains("s.ytimg.com") || url.contains("default_banner") ? null : url;
        } catch (Exception e) {
            throw new ParsingException("Could not get Banner", e);
        }
    }

    @Override
    public String getFeedUrl() throws ParsingException {
        try {
            return CHANNEL_FEED_BASE + getId();
        } catch (Exception e) {
            throw new ParsingException("Could not get feed url", e);
        }
    }

    @Override
    public long getSubscriberCount() throws ParsingException {
        Element el = doc.select("span[class*=\"yt-subscription-button-subscriber-count\"]").first();
        if (el != null) {
            return Long.parseLong(Utils.removeNonDigitCharacters(el.text()));
        } else {
            throw new ParsingException("Could not get subscriber count");
        }
    }

    @Override
    public String getDescription() throws ParsingException {
        try {
            return doc.select("meta[name=\"description\"]").first().attr("content");
        } catch (Exception e) {
            throw new ParsingException("Could not get channel description", e);
        }
    }

    @Nonnull
    @Override
    public StreamInfoItemsCollector getStreams() throws ExtractionException {
        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        Element ul = doc.select("ul[id=\"browse-items-primary\"]").first();
        collectStreamsFrom(collector, ul);
        return collector;
    }

    @Override
    public InfoItemPage getPage(String pageUrl) throws IOException, ExtractionException {
        try {

            if (!hasNextPage()) {
                throw new ExtractionException("Channel doesn't have more streams");
            }

            fetchPage();

            StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

            final JsonObject ajaxJson = JsonParser.object().from(
                    NewPipe.getDownloader()
                    .download(pageUrl));

            final Document ajaxHtml = Jsoup.parse(ajaxJson.getString("content_html"));

            collectStreamsFrom(collector, ajaxHtml.select("body").first());

            return new InfoItemPage(collector, getNextPageUrlFromAjaxPage(ajaxJson, pageUrl));
        } catch (JsonParserException pe) {
            throw new ParsingException("Could not parse json data for next streams", pe);
        }
    }

    private String getNextPageUrlFromAjaxPage(final JsonObject ajaxJson, final String pageUrl)
        throws ParsingException {
        String loadMoreHtmlDataRaw = ajaxJson.getString("load_more_widget_html");
        if (!loadMoreHtmlDataRaw.isEmpty()) {
            return getNextPageUrlFrom(Jsoup.parse(loadMoreHtmlDataRaw, pageUrl));
        } else {
            return "";
        }
    }

    private String getNextPageUrlFrom(Document d) throws ParsingException {
        try {
            Element button = d.select("button[class*=\"yt-uix-load-more\"]").first();
            if (button != null) {
                return button.attr("abs:data-uix-load-more-href");
            } else {
                // Sometimes channels are simply so small, they don't have a more streams/videos
                return "";
            }
        } catch (Exception e) {
            throw new ParsingException("could not get next streams' url", e);
        }
    }

    private void collectStreamsFrom(StreamInfoItemsCollector collector, Element element) throws ParsingException {
        collector.reset();

        final String uploaderName = getName();
        final String uploaderUrl = getCleanUrl();
        for (final Element li : element.children()) {
            if (li.select("div[class=\"feed-item-dismissable\"]").first() != null) {
                collector.commit(new YoutubeStreamInfoItemExtractor(li) {
                    @Override
                    public String getUrl() throws ParsingException {
                        try {
                            Element el = li.select("div[class=\"feed-item-dismissable\"]").first();
                            Element dl = el.select("h3").first().select("a").first();
                            return dl.attr("abs:href");
                        } catch (Exception e) {
                            throw new ParsingException("Could not get web page url for the video", e);
                        }
                    }

                    @Override
                    public String getName() throws ParsingException {
                        try {
                            Element el = li.select("div[class=\"feed-item-dismissable\"]").first();
                            Element dl = el.select("h3").first().select("a").first();
                            return dl.text();
                        } catch (Exception e) {
                            throw new ParsingException("Could not get title", e);
                        }
                    }

                    @Override
                    public String getUploaderName() throws ParsingException {
                        return uploaderName;
                    }

                    @Override
                    public String getUploaderUrl() throws ParsingException {
                        return uploaderUrl;
                    }

                    @Override
                    public String getThumbnailUrl() throws ParsingException {
                        try {
                            String url;
                            Element te = li.select("span[class=\"yt-thumb-clip\"]").first()
                                    .select("img").first();
                            url = te.attr("abs:src");
                            // Sometimes youtube sends links to gif files which somehow seem to not exist
                            // anymore. Items with such gif also offer a secondary image source. So we are going
                            // to use that if we've caught such an item.
                            if (url.contains(".gif")) {
                                url = te.attr("abs:data-thumb");
                            }
                            return url;
                        } catch (Exception e) {
                            throw new ParsingException("Could not get thumbnail url", e);
                        }
                    }
                });
            }
        }
    }
}
