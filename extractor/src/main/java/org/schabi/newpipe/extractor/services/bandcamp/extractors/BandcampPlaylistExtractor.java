package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getImagesFromImageId;
import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getImagesFromImageUrl;
import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampStreamExtractor.getAlbumInfoJson;
import static org.schabi.newpipe.extractor.utils.JsonUtils.getJsonData;
import static org.schabi.newpipe.extractor.utils.Utils.HTTPS;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParserException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.PaidContentException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.streaminfoitem.BandcampPlaylistStreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import java.io.IOException;
import java.util.Objects;
import java.util.List;

import javax.annotation.Nonnull;

public class BandcampPlaylistExtractor extends PlaylistExtractor {

    /**
     * An arbitrarily chosen number above which cover arts won't be fetched individually for each
     * track; instead, it will be assumed that every track has the same cover art as the album,
     * which is not always the case.
     */
    private static final int MAXIMUM_INDIVIDUAL_COVER_ARTS = 10;

    private Document document;
    private JsonObject albumJson;
    private JsonArray trackInfo;
    private String name;

    public BandcampPlaylistExtractor(final StreamingService service,
                                     final ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        final String html = downloader.get(getLinkHandler().getUrl()).responseBody();
        document = Jsoup.parse(html);
        albumJson = getAlbumInfoJson(html);
        trackInfo = albumJson.getArray("trackinfo");

        try {
            name = getJsonData(html, "data-embed").getString("album_title");
        } catch (final JsonParserException e) {
            throw new ParsingException("Faulty JSON; page likely does not contain album data", e);
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new ParsingException("JSON does not exist", e);
        }

        if (trackInfo.isEmpty()) {
            // Albums without trackInfo need to be purchased before they can be played
            throw new PaidContentException("Album needs to be purchased");
        }
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() throws ParsingException {
        if (albumJson.isNull("art_id")) {
            return List.of();
        } else {
            return getImagesFromImageId(albumJson.getLong("art_id"), true);
        }
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        final String[] parts = getUrl().split("/");
        // https: (/) (/) * .bandcamp.com (/) and leave out the rest
        return HTTPS + parts[2] + "/";
    }

    @Override
    public String getUploaderName() {
        return albumJson.getString("artist");
    }

    @Nonnull
    @Override
    public List<Image> getUploaderAvatars() {
        return getImagesFromImageUrl(document.getElementsByClass("band-photo")
                .stream()
                .map(element -> element.attr("src"))
                .findFirst()
                .orElse(""));
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return false;
    }

    @Override
    public long getStreamCount() {
        return trackInfo.size();
    }

    @Nonnull
    @Override
    public Description getDescription() throws ParsingException {
        final Element tInfo = document.getElementById("trackInfo");
        if (tInfo == null) {
            throw new ParsingException("Could not find trackInfo in document");
        }
        final Elements about = tInfo.getElementsByClass("tralbum-about");
        final Elements credits = tInfo.getElementsByClass("tralbum-credits");
        final Element license = document.getElementById("license");
        if (about.isEmpty() && credits.isEmpty() && license == null) {
            return Description.EMPTY_DESCRIPTION;
        }
        final StringBuilder sb = new StringBuilder();
        if (!about.isEmpty()) {
            sb.append(Objects.requireNonNull(about.first()).html());
        }
        if (!credits.isEmpty()) {
            sb.append(Objects.requireNonNull(credits.first()).html());
        }
        if (license != null) {
            sb.append(license.html());
        }
        return new Description(sb.toString(), Description.HTML);
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws ExtractionException {

        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

        for (int i = 0; i < trackInfo.size(); i++) {
            final JsonObject track = trackInfo.getObject(i);

            if (trackInfo.size() < MAXIMUM_INDIVIDUAL_COVER_ARTS) {
                // Load cover art of every track individually
                collector.commit(new BandcampPlaylistStreamInfoItemExtractor(
                        track, getUploaderUrl(), getService()));
            } else {
                // Pretend every track has the same cover art as the album
                collector.commit(new BandcampPlaylistStreamInfoItemExtractor(
                        track, getUploaderUrl(), getThumbnails()));
            }
        }

        return new InfoItemsPage<>(collector, null);
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(final Page page) {
        return null;
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return name;
    }
}
