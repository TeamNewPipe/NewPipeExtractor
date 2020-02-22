package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("WeakerAccess")
public class YoutubePlaylistExtractor extends PlaylistExtractor {

    private Document doc;
    private JsonObject initialData;
    private JsonObject uploaderInfo;
    private JsonObject playlistInfo;
    private JsonObject playlistVideos;

    public YoutubePlaylistExtractor(StreamingService service, ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        final String url = getUrl();
        final Response response = downloader.get(url, getExtractorLocalization());
        doc = YoutubeParsingHelper.parseAndCheckPage(url, response);
        initialData = getInitialData();
        uploaderInfo = getUploaderInfo();
        playlistInfo = getPlaylistInfo();
        playlistVideos = getPlaylistVideos();
    }

    private JsonObject getInitialData() throws ParsingException {
        try {
            String initialData = Parser.matchGroup1("window\\[\"ytInitialData\"\\]\\s*=\\s*(\\{.*?\\});", doc.toString());
            return JsonParser.object().from(initialData);
        } catch (JsonParserException | Parser.RegexException e) {
            throw new ParsingException("Could not get ytInitialData", e);
        }
    }

    private JsonObject getUploaderInfo() throws ParsingException {
        JsonArray items = initialData.getObject("sidebar").getObject("playlistSidebarRenderer").getArray("items");
        try {
            JsonObject uploaderInfo = items.getObject(1).getObject("playlistSidebarSecondaryInfoRenderer")
                    .getObject("videoOwner").getObject("videoOwnerRenderer");
            if (uploaderInfo != null) {
                return uploaderInfo;
            }
        } catch (Exception ignored) {}

        // we might want to create a loop here instead of using duplicated code
        try {
            JsonObject uploaderInfo = items.getObject(items.size()).getObject("playlistSidebarSecondaryInfoRenderer")
                    .getObject("videoOwner").getObject("videoOwnerRenderer");
            if (uploaderInfo != null) {
                return uploaderInfo;
            }
        } catch (Exception e) {
            throw new ParsingException("Could not get uploader info", e);
        }
        throw new ParsingException("Could not get uploader info");
    }

    private JsonObject getPlaylistInfo() throws ParsingException {
        try {
            return initialData.getObject("sidebar").getObject("playlistSidebarRenderer").getArray("items")
                    .getObject(0).getObject("playlistSidebarPrimaryInfoRenderer");
        } catch (Exception e) {
            throw new ParsingException("Could not get PlaylistInfo", e);
        }
    }

    private JsonObject getPlaylistVideos() throws ParsingException {
        try {
            return initialData.getObject("contents").getObject("twoColumnBrowseResultsRenderer")
                    .getArray("tabs").getObject(0).getObject("tabRenderer").getObject("content").getObject("sectionListRenderer")
                    .getArray("contents").getObject(0).getObject("itemSectionRenderer").getArray("contents")
                    .getObject(0).getObject("playlistVideoListRenderer");
        } catch (Exception e) {
            throw new ParsingException("Could not get playlist info", e);
        }
    }

    @Override
    public String getNextPageUrl() throws ExtractionException {
        return getNextPageUrlFrom(doc);
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        try {
            String name = playlistInfo.getObject("title").getArray("runs").getObject(0).getString("text");
            if (name != null) return name;
        } catch (Exception ignored) {}
        try {
            return initialData.getObject("microformat").getObject("microformatDataRenderer").getString("title");
        } catch (Exception e) {
            throw new ParsingException("Could not get playlist name", e);
        }
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        try {
            return playlistInfo.getObject("thumbnailRenderer").getObject("playlistVideoThumbnailRenderer")
                    .getObject("thumbnail").getArray("thumbnails").getObject(0).getString("url");
        } catch (Exception ignored) {}
        try {
            return initialData.getObject("microformat").getObject("microformatDataRenderer").getObject("thumbnail")
                    .getArray("thumbnails").getObject(0).getString("url");
        } catch (Exception e) {
            throw new ParsingException("Could not get playlist thumbnail", e);
        }
    }

    @Override
    public String getBannerUrl() {
        return "";      // Banner can't be handled by frontend right now.
        // Whoever is willing to implement this should also implement it in the frontend.
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        try {
            return YoutubeChannelExtractor.CHANNEL_URL_BASE +
                    uploaderInfo.getObject("navigationEndpoint").getObject("browseEndpoint").getString("browseId");
        } catch (Exception e) {
            throw new ParsingException("Could not get playlist uploader url", e);
        }
    }

    @Override
    public String getUploaderName() throws ParsingException {
        try {
            return uploaderInfo.getObject("title").getArray("runs").getObject(0).getString("text");
        } catch (Exception e) {
            throw new ParsingException("Could not get playlist uploader name", e);
        }
    }

    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        try {
            return uploaderInfo.getObject("thumbnail").getArray("thumbnails").getObject(0).getString("url");
        } catch (Exception e) {
            throw new ParsingException("Could not get playlist uploader avatar", e);
        }
    }

    @Override
    public long getStreamCount() throws ParsingException {
        try {
            String viewsText = getPlaylistInfo().getArray("stats").getObject(0).getArray("runs").getObject(0).getString("text");
            return Long.parseLong(Utils.removeNonDigitCharacters(viewsText));
        } catch (Exception e) {
            throw new ParsingException("Could not get video count from playlist", e);
        }
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws ExtractionException {
        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        Element tbody = doc.select("tbody[id=\"pl-load-more-destination\"]").first();
        collectStreamsFrom(collector, tbody);
        return new InfoItemsPage<>(collector, getNextPageUrl());
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(final String pageUrl) throws IOException, ExtractionException {
        if (pageUrl == null || pageUrl.isEmpty()) {
            throw new ExtractionException(new IllegalArgumentException("Page url is empty or null"));
        }

        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        JsonObject pageJson;
        try {
            final String responseBody = getDownloader().get(pageUrl, getExtractorLocalization()).responseBody();
            pageJson = JsonParser.object().from(responseBody);
        } catch (JsonParserException pe) {
            throw new ParsingException("Could not parse ajax json", pe);
        }

        final Document pageHtml = Jsoup.parse("<table><tbody id=\"pl-load-more-destination\">"
                + pageJson.getString("content_html")
                + "</tbody></table>", pageUrl);

        collectStreamsFrom(collector, pageHtml.select("tbody[id=\"pl-load-more-destination\"]").first());

        return new InfoItemsPage<>(collector, getNextPageUrlFromAjax(pageJson, pageUrl));
    }

    private String getNextPageUrlFromAjax(final JsonObject pageJson, final String pageUrl)
            throws ParsingException {
        String nextPageHtml = pageJson.getString("load_more_widget_html");
        if (!nextPageHtml.isEmpty()) {
            return getNextPageUrlFrom(Jsoup.parse(nextPageHtml, pageUrl));
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
                // Sometimes playlists are simply so small, they don't have a more streams/videos
                return "";
            }
        } catch (Exception e) {
            throw new ParsingException("could not get next streams' url", e);
        }
    }

    private void collectStreamsFrom(@Nonnull StreamInfoItemsCollector collector, @Nullable Element element) {
        collector.reset();

        if (element == null) {
            return;
        }

        final LinkHandlerFactory streamLinkHandlerFactory = getService().getStreamLHFactory();
        final TimeAgoParser timeAgoParser = getTimeAgoParser();

        for (final Element li : element.children()) {
            if (isDeletedItem(li)) {
                continue;
            }

            collector.commit(new YoutubeStreamInfoItemExtractor(li, timeAgoParser) {
                public Element uploaderLink;

                @Override
                public boolean isAd() {
                    return false;
                }

                @Override
                public String getUrl() throws ParsingException {
                    try {
                        return streamLinkHandlerFactory.fromId(li.attr("data-video-id")).getUrl();
                    } catch (Exception e) {
                        throw new ParsingException("Could not get web page url for the video", e);
                    }
                }

                @Override
                public String getName() throws ParsingException {
                    try {
                        return li.attr("data-title");
                    } catch (Exception e) {
                        throw new ParsingException("Could not get title", e);
                    }
                }

                @Override
                public long getDuration() throws ParsingException {
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
                        throw new ParsingException("Could not get duration" + getUrl(), e);
                    }
                }


                private Element getUploaderLink() {
                    // should always be present since we filter deleted items
                    if (uploaderLink == null) {
                        uploaderLink = li.select("div[class=pl-video-owner] a").first();
                    }
                    return uploaderLink;
                }

                @Override
                public String getUploaderName() throws ParsingException {
                    return getUploaderLink().text();
                }

                @Override
                public String getUploaderUrl() throws ParsingException {
                    // this url is not always in the form "/channel/..."
                    // sometimes Youtube provides urls in the from "/user/..."
                    return getUploaderLink().attr("abs:href");
                }

                @Override
                public String getTextualUploadDate() {
                    return "";
                }

                @Override
                public long getViewCount() throws ParsingException {
                    return -1;
                }

                @Override
                public String getThumbnailUrl() throws ParsingException {
                    try {
                        return "https://i.ytimg.com/vi/" + streamLinkHandlerFactory.fromUrl(getUrl()).getId() + "/hqdefault.jpg";
                    } catch (Exception e) {
                        throw new ParsingException("Could not get thumbnail url", e);
                    }
                }
            });
        }
    }

    /**
     * Check if the playlist item is deleted
     *
     * @param li the list item
     * @return true if the item is deleted
     */
    private boolean isDeletedItem(Element li) {
        return li.select("div[class=pl-video-owner] a").isEmpty();
    }
}
