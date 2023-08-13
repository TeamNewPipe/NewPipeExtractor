// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getImagesFromImageId;
import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getImagesFromImageUrl;
import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.parseDate;
import static org.schabi.newpipe.extractor.utils.Utils.HTTPS;
import static org.schabi.newpipe.extractor.utils.Utils.replaceHttpWithHttps;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParserException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.PaidContentException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BandcampStreamExtractor extends StreamExtractor {
    private JsonObject albumJson;
    private JsonObject current;
    private Document document;

    public BandcampStreamExtractor(final StreamingService service, final LinkHandler linkHandler) {
        super(service, linkHandler);
    }


    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        final String html = downloader.get(getLinkHandler().getUrl()).responseBody();
        document = Jsoup.parse(html);
        albumJson = getAlbumInfoJson(html);
        current = albumJson.getObject("current");

        if (albumJson.getArray("trackinfo").size() > 1) {
            // In this case, we are actually viewing an album page!
            throw new ExtractionException("Page is actually an album, not a track");
        }

        if (albumJson.getArray("trackinfo").getObject(0).isNull("file")) {
            throw new PaidContentException("This track is not available without being purchased");
        }
    }

    /**
     * Get the JSON that contains album's metadata from page
     *
     * @param html Website
     * @return Album metadata JSON
     * @throws ParsingException In case of a faulty website
     */
    public static JsonObject getAlbumInfoJson(final String html) throws ParsingException {
        try {
            return JsonUtils.getJsonData(html, "data-tralbum");
        } catch (final JsonParserException e) {
            throw new ParsingException("Faulty JSON; page likely does not contain album data", e);
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new ParsingException("JSON does not exist", e);
        }
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return current.getString("title");
    }

    @Nonnull
    @Override
    public String getUploaderUrl() throws ParsingException {
        final String[] parts = getUrl().split("/");
        // https: (/) (/) * .bandcamp.com (/) and leave out the rest
        return HTTPS + parts[2] + "/";
    }

    @Nonnull
    @Override
    public String getUrl() throws ParsingException {
        return replaceHttpWithHttps(albumJson.getString("url"));
    }

    @Nonnull
    @Override
    public String getUploaderName() throws ParsingException {
        return albumJson.getString("artist");
    }

    @Nullable
    @Override
    public String getTextualUploadDate() {
        return current.getString("publish_date");
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return parseDate(getTextualUploadDate());
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() throws ParsingException {
        if (albumJson.isNull("art_id")) {
            return List.of();
        }

        return getImagesFromImageId(albumJson.getLong("art_id"), true);
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

    @Nonnull
    @Override
    public Description getDescription() {
        final String s = Utils.nonEmptyAndNullJoin("\n\n", current.getString("about"),
                current.getString("lyrics"), current.getString("credits"));
        return new Description(s, Description.PLAIN_TEXT);
    }

    @Override
    public List<AudioStream> getAudioStreams() {
        return Collections.singletonList(new AudioStream.Builder()
                .setId("mp3-128")
                .setContent(albumJson.getArray("trackinfo")
                        .getObject(0)
                        .getObject("file")
                        .getString("mp3-128"), true)
                .setMediaFormat(MediaFormat.MP3)
                .setAverageBitrate(128)
                .build());
    }

    @Override
    public long getLength() throws ParsingException {
        return (long) albumJson.getArray("trackinfo").getObject(0)
                .getDouble("duration");
    }

    @Override
    public List<VideoStream> getVideoStreams() {
        return Collections.emptyList();
    }

    @Override
    public List<VideoStream> getVideoOnlyStreams() {
        return Collections.emptyList();
    }

    @Override
    public StreamType getStreamType() {
        return StreamType.AUDIO_STREAM;
    }

    @Override
    public PlaylistInfoItemsCollector getRelatedItems() {
        final PlaylistInfoItemsCollector collector = new PlaylistInfoItemsCollector(getServiceId());
        document.getElementsByClass("recommended-album")
                .stream()
                .map(BandcampRelatedPlaylistInfoItemExtractor::new)
                .forEach(collector::commit);

        return collector;
    }

    @Nonnull
    @Override
    public String getCategory() {
        // Get first tag from html, which is the artist's Genre
        return document.getElementsByClass("tralbum-tags").stream()
                .flatMap(element -> element.getElementsByClass("tag").stream())
                .map(Element::text)
                .findFirst()
                .orElse("");
    }

    @Nonnull
    @Override
    public String getLicence() {
        /*
        Tests resulted in this mapping of ints to licence:
        https://cloud.disroot.org/s/ZTWBxbQ9fKRmRWJ/preview (screenshot from a Bandcamp artist's
        account)
        */

        switch (current.getInt("license_type")) {
            case 1:
                return "All rights reserved ©";
            case 2:
                return "CC BY-NC-ND 3.0";
            case 3:
                return "CC BY-NC-SA 3.0";
            case 4:
                return "CC BY-NC 3.0";
            case 5:
                return "CC BY-ND 3.0";
            case 6:
                return "CC BY 3.0";
            case 8:
                return "CC BY-SA 3.0";
            default:
                return "Unknown";
        }
    }

    @Nonnull
    @Override
    public List<String> getTags() {
        return document.getElementsByAttributeValue("itemprop", "keywords")
                .stream()
                .map(Element::text)
                .collect(Collectors.toList());
    }
}
