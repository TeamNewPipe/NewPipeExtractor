package org.schabi.newpipe.extractor.services.youtube;


import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.UrlIdHandler;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.stream.AbstractStreamInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItemCollector;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.utils.Parser;

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

    private Document doc;
    /**
     * It's lazily initialized (when getNextStreams is called)
     */
    private Document nextStreamsAjax;

    /*//////////////////////////////////////////////////////////////////////////
    // Variables for cache purposes (not "select" the current document all over again)
    //////////////////////////////////////////////////////////////////////////*/
    private String channelId;
    private String channelName;
    private String avatarUrl;
    private String bannerUrl;
    private String feedUrl;
    private long subscriberCount = -1;

    public YoutubeChannelExtractor(UrlIdHandler urlIdHandler, String url, int serviceId) throws ExtractionException, IOException {
        super(urlIdHandler, urlIdHandler.cleanUrl(url), serviceId);
        fetchDocument();
    }

    @Override
    public String getChannelId() throws ParsingException {
        try {
            if (channelId == null) {
                channelId = getUrlIdHandler().getId(getUrl());
            }

            return channelId;
        } catch (Exception e) {
            throw new ParsingException("Could not get channel id");
        }
    }

    @Override
    public String getChannelName() throws ParsingException {
        try {
            if (channelName == null) {
                channelName = doc.select("span[class=\"qualified-channel-title-text\"]").first().select("a").first().text();
            }

            return channelName;
        } catch (Exception e) {
            throw new ParsingException("Could not get channel name");
        }
    }

    @Override
    public String getAvatarUrl() throws ParsingException {
        try {
            if (avatarUrl == null) {
                avatarUrl = doc.select("img[class=\"channel-header-profile-image\"]").first().attr("abs:src");
            }

            return avatarUrl;
        } catch (Exception e) {
            throw new ParsingException("Could not get avatar", e);
        }
    }

    @Override
    public String getBannerUrl() throws ParsingException {
        try {
            if (bannerUrl == null) {
                Element el = doc.select("div[id=\"gh-banner\"]").first().select("style").first();
                String cssContent = el.html();
                String url = "https:" + Parser.matchGroup1("url\\(([^)]+)\\)", cssContent);

                bannerUrl = url.contains("s.ytimg.com") || url.contains("default_banner") ? null : url;
            }

            return bannerUrl;
        } catch (Exception e) {
            throw new ParsingException("Could not get Banner", e);
        }
    }

    @Override
    public StreamInfoItemCollector getStreams() throws ParsingException {
        StreamInfoItemCollector collector = getStreamPreviewInfoCollector();
        Element ul = doc.select("ul[id=\"browse-items-primary\"]").first();
        collectStreamsFrom(collector, ul);
        return collector;
    }

    @Override
    public long getSubscriberCount() throws ParsingException {

        if (subscriberCount == -1) {
            Element el = doc.select("span[class*=\"yt-subscription-button-subscriber-count\"]").first();
            if (el != null) {
                subscriberCount = Long.parseLong(el.text().replaceAll("\\D+", ""));
            } else {
                throw new ParsingException("Could not get subscriber count");
            }
        }

        return subscriberCount;
    }

    @Override
    public String getFeedUrl() throws ParsingException {
        try {
            if (feedUrl == null) {
                String channelId = doc.getElementsByClass("yt-uix-subscription-button").first().attr("data-channel-external-id");
                feedUrl = channelId == null ? "" : CHANNEL_FEED_BASE + channelId;
            }

            return feedUrl;
        } catch (Exception e) {
            throw new ParsingException("Could not get feed url", e);
        }
    }

    @Override
    public StreamInfoItemCollector getNextStreams() throws ExtractionException, IOException {
        if (!hasMoreStreams()) {
            throw new ExtractionException("Channel doesn't have more streams");
        }

        StreamInfoItemCollector collector = new StreamInfoItemCollector(getUrlIdHandler(), getServiceId());
        setupNextStreamsAjax(NewPipe.getDownloader());
        collectStreamsFrom(collector, nextStreamsAjax.select("body").first());

        return collector;
    }

    private void setupNextStreamsAjax(Downloader downloader) throws IOException, ReCaptchaException, ParsingException {
        String ajaxDataRaw = downloader.download(nextStreamsUrl);
        try {
            JSONObject ajaxData = new JSONObject(ajaxDataRaw);

            String htmlDataRaw = ajaxData.getString("content_html");
            nextStreamsAjax = Jsoup.parse(htmlDataRaw, nextStreamsUrl);

            String nextStreamsHtmlDataRaw = ajaxData.getString("load_more_widget_html");
            if (!nextStreamsHtmlDataRaw.isEmpty()) {
                Document nextStreamsData = Jsoup.parse(nextStreamsHtmlDataRaw, nextStreamsUrl);
                nextStreamsUrl = getNextStreamsUrl(nextStreamsData);
            } else {
                nextStreamsUrl = "";
            }
        } catch (JSONException e) {
            throw new ParsingException("Could not parse json data for next streams", e);
        }
    }

    private String getNextStreamsUrl(Document d) throws ParsingException {
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

    private void fetchDocument() throws IOException, ReCaptchaException, ParsingException {
        Downloader downloader = NewPipe.getDownloader();

        String userUrl = getUrl() + "/videos?view=0&flow=list&sort=dd&live_view=10000";
        String pageContent = downloader.download(userUrl);
        doc = Jsoup.parse(pageContent, userUrl);

        nextStreamsUrl = getNextStreamsUrl(doc);
        nextStreamsAjax = null;
    }

    private void collectStreamsFrom(StreamInfoItemCollector collector, Element element) throws ParsingException {
        collector.getItemList().clear();

        for (final Element li : element.children()) {
            if (li.select("div[class=\"feed-item-dismissable\"]").first() != null) {
                collector.commit(new StreamInfoItemExtractor() {
                    @Override
                    public AbstractStreamInfo.StreamType getStreamType() throws ParsingException {
                        return AbstractStreamInfo.StreamType.VIDEO_STREAM;
                    }

                    @Override
                    public boolean isAd() throws ParsingException {
                        return !li.select("span[class*=\"icon-not-available\"]").isEmpty();
                    }

                    @Override
                    public String getWebPageUrl() throws ParsingException {
                        try {
                            Element el = li.select("div[class=\"feed-item-dismissable\"]").first();
                            Element dl = el.select("h3").first().select("a").first();
                            return dl.attr("abs:href");
                        } catch (Exception e) {
                            throw new ParsingException("Could not get web page url for the video", e);
                        }
                    }

                    @Override
                    public String getTitle() throws ParsingException {
                        try {
                            Element el = li.select("div[class=\"feed-item-dismissable\"]").first();
                            Element dl = el.select("h3").first().select("a").first();
                            return dl.text();
                        } catch (Exception e) {
                            throw new ParsingException("Could not get title", e);
                        }
                    }

                    @Override
                    public int getDuration() throws ParsingException {
                        try {
                            return YoutubeParsingHelper.parseDurationString(
                                    li.select("span[class=\"video-time\"]").first().text());
                        } catch (Exception e) {
                            if (isLiveStream(li)) {
                                // -1 for no duration
                                return -1;
                            } else {
                                throw new ParsingException("Could not get Duration: " + getTitle(), e);
                            }
                        }
                    }

                    @Override
                    public String getUploader() throws ParsingException {
                        return getChannelName();
                    }

                    @Override
                    public String getUploadDate() throws ParsingException {
                        try {
                            Element meta = li.select("div[class=\"yt-lockup-meta\"]").first();
                            Element li = meta.select("li").first();
                            if (li == null) {
                                //this means we have a youtube red video
                                return "";
                            } else {
                                return li.text();
                            }
                        } catch (Exception e) {
                            throw new ParsingException("Could not get upload date", e);
                        }
                    }

                    @Override
                    public long getViewCount() throws ParsingException {
                        String output;
                        String input;
                        try {
                            input = li.select("div[class=\"yt-lockup-meta\"]").first()
                                    .select("li").get(1)
                                    .text();
                        } catch (IndexOutOfBoundsException e) {
                            return -1;
                        }

                        output = input.replaceAll("\\D+", "");

                        try {
                            return Long.parseLong(output);
                        } catch (NumberFormatException e) {
                            // if this happens the video probably has no views
                            if (!input.isEmpty()) {
                                return 0;
                            } else {
                                throw new ParsingException("Could not handle input: " + input, e);
                            }
                        }
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

                    private boolean isLiveStream(Element item) {
                        Element bla = item.select("span[class*=\"yt-badge-live\"]").first();

                        if (bla == null) {
                            // sometimes livestreams dont have badges but sill are live streams
                            // if video time is not available we most likly have an offline livestream
                            if (item.select("span[class*=\"video-time\"]").first() == null) {
                                return true;
                            }
                        }
                        return bla != null;
                    }
                });
            }
        }
    }
}
