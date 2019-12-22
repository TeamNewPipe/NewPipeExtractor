package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import javax.annotation.Nonnull;
import java.io.IOException;

import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getJSONFromJavaScriptVariables;
import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampStreamExtractor.getAlbumInfoJson;

public class BandcampPlaylistExtractor extends PlaylistExtractor {

    private Document document;
    private JSONObject albumJson;
    private JSONArray trackInfo;
    private String name;

    public BandcampPlaylistExtractor(StreamingService service, ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        String html = downloader.get(getLinkHandler().getUrl()).responseBody();
        document = Jsoup.parse(html);
        albumJson = getAlbumInfoJson(html);
        trackInfo = albumJson.getJSONArray("trackinfo");

        try {
            name = getJSONFromJavaScriptVariables(html, "EmbedData").getString("album_title");
        } catch (JSONException e) {
            throw new ParsingException("Faulty JSON; page likely does not contain album data", e);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ParsingException("JSON does not exist", e);
        }



        if (trackInfo.length() <= 1) {
            // In this case, we are actually viewing a track page!
            throw new ExtractionException("Page is actually a track, not an album");
        }
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        try {
            return document.getElementsByAttributeValue("property", "og:image").get(0).attr("content");
        } catch (NullPointerException e) {
            return "";
        }
    }

    @Override
    public String getBannerUrl() {
        return "";
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        String[] parts = getUrl().split("/");
        // https: (/) (/) * .bandcamp.com (/) and leave out the rest
        return "https://" + parts[2] + "/";
    }

    @Override
    public String getUploaderName() {
        return albumJson.getString("artist");
    }

    @Override
    public String getUploaderAvatarUrl() {
        try {
            return document.getElementsByClass("band-photo").first().attr("src");
        } catch (NullPointerException e) {
            return "";
        }
    }

    @Override
    public long getStreamCount() {
        return trackInfo.length();
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws ExtractionException {

        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

        for (int i = 0; i < trackInfo.length(); i++) {
            JSONObject track = trackInfo.getJSONObject(i);

            collector.commit(new BandcampStreamInfoItemExtractor(
                    track.getString("title"),
                    getUploaderUrl() + track.getString("title_link"),
                    "",
                    track.getLong("duration"),
                    getService()
            ));
        }

        return new InfoItemsPage<>(collector, null);
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return name;
    }

    @Override
    public String getNextPageUrl() {
        return null;
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(String pageUrl) {
        return null;
    }
}
