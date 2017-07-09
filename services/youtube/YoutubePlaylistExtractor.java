package org.schabi.newpipe.extractor.services.youtube;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.UrlIdHandler;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.stream.AbstractStreamInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItemCollector;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.utils.Parser;

import java.io.IOException;

@SuppressWarnings("WeakerAccess")
public class YoutubePlaylistExtractor extends PlaylistExtractor {

    private Document doc = null;
    /**
     * It's lazily initialized (when getNextStreams is called)
     */
    private Document nextStreamsAjax = null;

    /*//////////////////////////////////////////////////////////////////////////
    // Variables for cache purposes (not "select" the current document all over again)
    //////////////////////////////////////////////////////////////////////////*/
    private String playlistId;
    private String playlistName;
    private String avatarUrl;
    private String bannerUrl;

    private long streamsCount;

    private String uploaderUrl;
    private String uploaderName;
    private String uploaderAvatarUrl;

    public YoutubePlaylistExtractor(UrlIdHandler urlIdHandler, String url, int serviceId) throws IOException, ExtractionException {
        super(urlIdHandler, urlIdHandler.cleanUrl(url), serviceId);
        fetchDocument();
    }

    @Override
    public String getPlaylistId() throws ParsingException {
        try {
            if (playlistId == null) {
                playlistId = getUrlIdHandler().getId(getUrl());
            }

            return playlistId;
        } catch (Exception e) {
            throw new ParsingException("Could not get playlist id");
        }
    }

    @Override
    public String getPlaylistName() throws ParsingException {
        try {
            if (playlistName == null) {
                playlistName = doc.select("div[id=pl-header] h1[class=pl-header-title]").first().text();
            }

            return playlistName;
        } catch (Exception e) {
            throw new ParsingException("Could not get playlist name");
        }
    }

    @Override
    public String getAvatarUrl() throws ParsingException {
        try {
            if (avatarUrl == null) {
                avatarUrl = doc.select("div[id=pl-header] div[class=pl-header-thumb] img").first().attr("abs:src");
            }

            return avatarUrl;
        } catch (Exception e) {
            throw new ParsingException("Could not get playlist avatar");
        }
    }

    @Override
    public String getBannerUrl() throws ParsingException {
        try {
            if (bannerUrl == null) {
                Element el = doc.select("div[id=\"gh-banner\"] style").first();
                String cssContent = el.html();
                String url = "https:" + Parser.matchGroup1("url\\((.*)\\)", cssContent);
                if (url.contains("s.ytimg.com")) {
                    bannerUrl = null;
                } else {
                    bannerUrl = url.substring(0, url.indexOf(");"));
                }
            }

            return bannerUrl;
        } catch (Exception e) {
            throw new ParsingException("Could not get playlist Banner");
        }
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        try {
            if (uploaderUrl == null) {
                uploaderUrl = doc.select("ul[class=\"pl-header-details\"] li").first().select("a").first().attr("abs:href");
            }

            return uploaderUrl;
        } catch (Exception e) {
            throw new ParsingException("Could not get playlist uploader name");
        }
    }

    @Override
    public String getUploaderName() throws ParsingException {
        try {
            if (uploaderName == null) {
                uploaderName = doc.select("span[class=\"qualified-channel-title-text\"]").first().select("a").first().text();
            }

            return uploaderName;
        } catch (Exception e) {
            throw new ParsingException("Could not get playlist uploader name");
        }
    }

    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        try {
            if (uploaderAvatarUrl == null) {
                uploaderAvatarUrl = doc.select("div[id=gh-banner] img[class=channel-header-profile-image]").first().attr("abs:src");
            }

            return uploaderAvatarUrl;
        } catch (Exception e) {
            throw new ParsingException("Could not get playlist uploader avatar");
        }
    }

    @Override
    public long getStreamsCount() throws ParsingException {
        if (streamsCount <= 0) {
            String input;

            try {
                input = doc.select("ul[class=\"pl-header-details\"] li").get(1).text();
            } catch (IndexOutOfBoundsException e) {
                throw new ParsingException("Could not get video count from playlist", e);
            }

            try {
                streamsCount = Long.parseLong(input.replaceAll("\\D+", ""));
            } catch (NumberFormatException e) {
                // When there's no videos in a playlist, there's no number in the "innerHtml",
                // all characters that is not a number is removed, so we try to parse a empty string
                if (!input.isEmpty()) {
                    streamsCount = 0;
                } else {
                    throw new ParsingException("Could not handle input: " + input, e);
                }
            }
        }

        return streamsCount;
    }

    @Override
    public StreamInfoItemCollector getStreams() throws ParsingException {
        StreamInfoItemCollector collector = getStreamPreviewInfoCollector();
        Element tbody = doc.select("tbody[id=\"pl-load-more-destination\"]").first();
        collectStreamsFrom(collector, tbody);
        return collector;
    }

    @Override
    public StreamInfoItemCollector getNextStreams() throws ExtractionException, IOException {
        if (!hasMoreStreams()){
            throw new ExtractionException("Playlist doesn't have more streams");
        }

        StreamInfoItemCollector collector = new StreamInfoItemCollector(getUrlIdHandler(), getServiceId());
        setupNextStreamsAjax(NewPipe.getDownloader());
        collectStreamsFrom(collector, nextStreamsAjax.select("tbody[id=\"pl-load-more-destination\"]").first());

        return collector;
    }

    private void setupNextStreamsAjax(Downloader downloader) throws IOException, ReCaptchaException, ParsingException {
        String ajaxDataRaw = downloader.download(nextStreamsUrl);
        try {
            JSONObject ajaxData = new JSONObject(ajaxDataRaw);

            String htmlDataRaw = "<table><tbody id=\"pl-load-more-destination\">" + ajaxData.getString("content_html") + "</tbody></table>";
            nextStreamsAjax = Jsoup.parse(htmlDataRaw, nextStreamsUrl);

            String nextStreamsHtmlDataRaw = ajaxData.getString("load_more_widget_html");
            if (!nextStreamsHtmlDataRaw.isEmpty()) {
                final Document nextStreamsData = Jsoup.parse(nextStreamsHtmlDataRaw, nextStreamsUrl);
                nextStreamsUrl = getNextStreamsUrl(nextStreamsData);
            } else {
                nextStreamsUrl = "";
            }
        } catch (JSONException e) {
            throw new ParsingException("Could not parse json data for next streams", e);
        }
    }

    private void fetchDocument() throws IOException, ReCaptchaException, ParsingException {
        Downloader downloader = NewPipe.getDownloader();

        String pageContent = downloader.download(getUrl());
        doc = Jsoup.parse(pageContent, getUrl());

        nextStreamsUrl = getNextStreamsUrl(doc);
        nextStreamsAjax = null;
    }

    private String getNextStreamsUrl(Document d) throws ParsingException {
        try {
            Element button = d.select("button[class*=\"yt-uix-load-more\"]").first();
            if (button != null) {
                return button.attr("abs:data-uix-load-more-href");
            } else {
                // Sometimes playlists are simply so small, they don't have a more streams/videos
                return "";
            }
        } catch (Exception e) {
            throw new ParsingException("could not get next streams' url", e);
        }
    }

    private void collectStreamsFrom(StreamInfoItemCollector collector, Element element) throws ParsingException {
        collector.getItemList().clear();

        final YoutubeStreamUrlIdHandler youtubeStreamUrlIdHandler = YoutubeStreamUrlIdHandler.getInstance();
        for (final Element li : element.children()) {
            collector.commit(new StreamInfoItemExtractor() {
                @Override
                public AbstractStreamInfo.StreamType getStreamType() throws ParsingException {
                    return AbstractStreamInfo.StreamType.VIDEO_STREAM;
                }

                @Override
                public String getWebPageUrl() throws ParsingException {
                    try {
                        return youtubeStreamUrlIdHandler.getUrl(li.attr("data-video-id"));
                    } catch (Exception e) {
                        throw new ParsingException("Could not get web page url for the video", e);
                    }
                }

                @Override
                public String getTitle() throws ParsingException {
                    try {
                        return li.attr("data-title");
                    } catch (Exception e) {
                        throw new ParsingException("Could not get title", e);
                    }
                }

                @Override
                public int getDuration() throws ParsingException {
                    try {
                        return YoutubeParsingHelper.parseDurationString(
                                li.select("div[class=\"timestamp\"] span").first().text().trim());
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
                    return li.select("div[class=pl-video-owner] a").text();
                }

                @Override
                public String getUploadDate() throws ParsingException {
                    return "";
                }

                @Override
                public long getViewCount() throws ParsingException {
                    return -1;
                }

                @Override
                public String getThumbnailUrl() throws ParsingException {
                    try {
                        return "https://i.ytimg.com/vi/" + youtubeStreamUrlIdHandler.getId(getWebPageUrl()) + "/hqdefault.jpg";
                    } catch (Exception e) {
                        throw new ParsingException("Could not get thumbnail url", e);
                    }
                }

                @Override
                public boolean isAd() throws ParsingException {
                    return false;
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
