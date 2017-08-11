package org.schabi.newpipe.extractor.services.youtube;

import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.UrlIdHandler;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemCollector;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;

@SuppressWarnings("WeakerAccess")
public class YoutubePlaylistExtractor extends PlaylistExtractor {

    private Document doc;
    /**
     * It's lazily initialized (when getNextStreams is called)
     */
    private Document nextStreamsAjax;

    public YoutubePlaylistExtractor(StreamingService service, String url, String nextStreamsUrl) throws IOException, ExtractionException {
        super(service, url, nextStreamsUrl);
    }

    @Override
    public void fetchPage() throws IOException, ExtractionException {
        Downloader downloader = NewPipe.getDownloader();

        String pageContent = downloader.download(getCleanUrl());
        doc = Jsoup.parse(pageContent, getCleanUrl());

        nextStreamsUrl = getNextStreamsUrlFrom(doc);
        nextStreamsAjax = null;
    }

    @Override
    public String getId() throws ParsingException {
        try {
            return getUrlIdHandler().getId(getCleanUrl());
        } catch (Exception e) {
            throw new ParsingException("Could not get playlist id");
        }
    }

    @Override
    public String getName() throws ParsingException {
        try {
            return doc.select("div[id=pl-header] h1[class=pl-header-title]").first().text();
        } catch (Exception e) {
            throw new ParsingException("Could not get playlist name");
        }
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        try {
            return doc.select("div[id=pl-header] div[class=pl-header-thumb] img").first().attr("abs:src");
        } catch (Exception e) {
            throw new ParsingException("Could not get playlist thumbnail");
        }
    }

    @Override
    public String getBannerUrl() throws ParsingException {
        try {
            Element el = doc.select("div[id=\"gh-banner\"] style").first();
            String cssContent = el.html();
            String url = "https:" + Parser.matchGroup1("url\\((.*)\\)", cssContent);
            if (url.contains("s.ytimg.com")) {
                return null;
            } else {
                return url.substring(0, url.indexOf(");"));
            }


        } catch (Exception e) {
            throw new ParsingException("Could not get playlist Banner");
        }
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        try {
            return doc.select("ul[class=\"pl-header-details\"] li").first().select("a").first().attr("abs:href");
        } catch (Exception e) {
            throw new ParsingException("Could not get playlist uploader name");
        }
    }

    @Override
    public String getUploaderName() throws ParsingException {
        try {
            return doc.select("span[class=\"qualified-channel-title-text\"]").first().select("a").first().text();
        } catch (Exception e) {
            throw new ParsingException("Could not get playlist uploader name");
        }
    }

    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        try {
            return doc.select("div[id=gh-banner] img[class=channel-header-profile-image]").first().attr("abs:src");
        } catch (Exception e) {
            throw new ParsingException("Could not get playlist uploader avatar");
        }
    }

    @Override
    public long getStreamCount() throws ParsingException {
        String input;

        try {
            input = doc.select("ul[class=\"pl-header-details\"] li").get(1).text();
        } catch (IndexOutOfBoundsException e) {
            throw new ParsingException("Could not get video count from playlist", e);
        }

        try {
            return Long.parseLong(Utils.removeNonDigitCharacters(input));
        } catch (NumberFormatException e) {
            // When there's no videos in a playlist, there's no number in the "innerHtml",
            // all characters that is not a number is removed, so we try to parse a empty string
            if (!input.isEmpty()) {
                return 0;
            } else {
                throw new ParsingException("Could not handle input: " + input, e);
            }
        }
    }

    @Override
    public StreamInfoItemCollector getStreams() throws IOException, ExtractionException {
        StreamInfoItemCollector collector = new StreamInfoItemCollector(getServiceId());
        Element tbody = doc.select("tbody[id=\"pl-load-more-destination\"]").first();
        collectStreamsFrom(collector, tbody);
        return collector;
    }

    @Override
    public NextItemsResult getNextStreams() throws IOException, ExtractionException {
        if (!hasMoreStreams()) {
            throw new ExtractionException("Playlist doesn't have more streams");
        }

        StreamInfoItemCollector collector = new StreamInfoItemCollector(getServiceId());
        setupNextStreamsAjax(NewPipe.getDownloader());
        collectStreamsFrom(collector, nextStreamsAjax.select("tbody[id=\"pl-load-more-destination\"]").first());

        return new NextItemsResult(collector.getItemList(), nextStreamsUrl);
    }

    private void setupNextStreamsAjax(Downloader downloader) throws IOException, ReCaptchaException, ParsingException {
        String ajaxDataRaw = downloader.download(nextStreamsUrl);
        try {
            JSONObject ajaxData = new JSONObject(ajaxDataRaw);

            String htmlDataRaw = "<table><tbody id=\"pl-load-more-destination\">" + ajaxData.getString("content_html") + "</tbody></table>";
            nextStreamsAjax = Jsoup.parse(htmlDataRaw, nextStreamsUrl);

            String nextStreamsHtmlDataRaw = ajaxData.getString("load_more_widget_html");
            if (!nextStreamsHtmlDataRaw.isEmpty()) {
                nextStreamsUrl = getNextStreamsUrlFrom(Jsoup.parse(nextStreamsHtmlDataRaw, nextStreamsUrl));
            } else {
                nextStreamsUrl = "";
            }
        } catch (JSONException e) {
            throw new ParsingException("Could not parse json data for next streams", e);
        }
    }

    private String getNextStreamsUrlFrom(Document d) throws ParsingException {
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

        final UrlIdHandler streamUrlIdHandler = getService().getStreamUrlIdHandler();
        for (final Element li : element.children()) {
            collector.commit(new YoutubeStreamInfoItemExtractor(li) {
                @Override
                public boolean isAd() throws ParsingException {
                    return false;
                }

                @Override
                public String getWebPageUrl() throws ParsingException {
                    try {
                        return streamUrlIdHandler.getUrl(li.attr("data-video-id"));
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
                        if (getStreamType() == StreamType.LIVE_STREAM) return -1;

                        Element first = li.select("div[class=\"timestamp\"] span").first();
                        if (first == null) {
                            // Video unavailable (private, deleted, etc.), this is a thing that happens specifically with playlists,
                            // because in other cases, those videos don't even show up
                            return -1;
                        }

                        return YoutubeParsingHelper.parseDurationString(first.text());
                    } catch (Exception e) {
                        throw new ParsingException("Could not get Duration: " + getTitle(), e);
                    }
                }

                @Override
                public String getUploaderName() throws ParsingException {
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
                        return "https://i.ytimg.com/vi/" + streamUrlIdHandler.getId(getWebPageUrl()) + "/hqdefault.jpg";
                    } catch (Exception e) {
                        throw new ParsingException("Could not get thumbnail url", e);
                    }
                }
            });
        }
    }
}
