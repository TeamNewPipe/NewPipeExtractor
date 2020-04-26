package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParserException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import javax.annotation.Nonnull;
import java.io.IOException;

import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getImageUrl;
import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getJSONFromJavaScriptVariables;
import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampStreamExtractor.getAlbumInfoJson;

public class BandcampPlaylistExtractor extends PlaylistExtractor {

    /**
     * An arbitrarily chosen number above which cover arts won't be fetched individually for each track;
     * instead, it will be assumed that every track has the same cover art as the album, which is not
     * always the case.
     */
    private static final int MAXIMUM_INDIVIDUAL_COVER_ARTS = 10;

    private Document document;
    private JsonObject albumJson;
    private JsonArray trackInfo;
    private String name;

    public BandcampPlaylistExtractor(StreamingService service, ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        String html = downloader.get(getLinkHandler().getUrl()).responseBody();
        document = Jsoup.parse(html);
        albumJson = getAlbumInfoJson(html);
        trackInfo = albumJson.getArray("trackinfo");

        try {
            name = getJSONFromJavaScriptVariables(html, "EmbedData").getString("album_title");
        } catch (JsonParserException e) {
            throw new ParsingException("Faulty JSON; page likely does not contain album data", e);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ParsingException("JSON does not exist", e);
        }



        if (trackInfo.size() <= 0) {
            // Albums without trackInfo need to be purchased before they can be played
            throw new ContentNotAvailableException("Album needs to be purchased");
        }
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        if (albumJson.isNull("art_id")) return "";
        else return getImageUrl(albumJson.getLong("art_id"), true);
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
        return trackInfo.size();
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws ExtractionException {

        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

        for (int i = 0; i < trackInfo.size(); i++) {
            JsonObject track = trackInfo.getObject(i);

            if (trackInfo.size() < MAXIMUM_INDIVIDUAL_COVER_ARTS) {
                // Load cover art of every track individually
                collector.commit(new BandcampStreamInfoItemExtractor(
                        track.getString("title"),
                        getUploaderUrl() + track.getString("title_link"),
                        "",
                        track.getLong("duration"),
                        getService()
                ));
            } else {
                // Pretend every track has the same cover art as the album
                collector.commit(new BandcampStreamInfoItemExtractor(
                        track.getString("title"),
                        getUploaderUrl() + track.getString("title_link"),
                        getThumbnailUrl(),
                        "",
                        track.getLong("duration")
                ));
            }

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
