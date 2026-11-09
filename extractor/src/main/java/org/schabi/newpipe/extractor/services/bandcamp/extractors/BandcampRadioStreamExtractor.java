package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.BASE_API_URL;
import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.BASE_URL;
import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getImageUrl;
import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getImagesFromImageId;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.Image.ResolutionLevel;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ContentNotSupportedException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.AudioTrackType;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamSegment;
import org.schabi.newpipe.extractor.stream.StreamingProtocol;
import org.schabi.newpipe.extractor.stream.deliverysource.uri.HttpDeliverySource;
import org.schabi.newpipe.extractor.stream.deliverysource.uri.UriObject;
import org.schabi.newpipe.extractor.stream.impl.UriAudioStream;
import org.schabi.newpipe.extractor.stream.interfaces.Stream;
import org.schabi.newpipe.extractor.stream.mediaformat.AudioMediaFormat;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BandcampRadioStreamExtractor extends BandcampStreamExtractor {

    private static final String OPUS_LO = "opus-lo";
    private JsonObject showInfo;

    public BandcampRadioStreamExtractor(final StreamingService service,
                                        final LinkHandler linkHandler) {
        super(service, linkHandler);
    }

    static JsonObject query(final int id) throws ParsingException {
        try {
            return JsonParser.object().from(NewPipe.getDownloader()
                    .get(BASE_API_URL + "/bcweekly/1/get?id=" + id).responseBody());
        } catch (final IOException | ReCaptchaException | JsonParserException e) {
            throw new ParsingException("could not get show data", e);
        }
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        showInfo = query(Integer.parseInt(getId()));
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        /* Select "subtitle" and not "audio_title", as the latter would cause a lot of
         * items to show the same title, e.g. "Bandcamp Weekly".
         */
        return showInfo.getString("subtitle");
    }

    @Nonnull
    @Override
    public String getUploaderUrl() throws ContentNotSupportedException {
        throw new ContentNotSupportedException("Fan pages are not supported");
    }

    @Nonnull
    @Override
    public String getUrl() throws ParsingException {
        return getLinkHandler().getUrl();
    }

    @Nonnull
    @Override
    public String getUploaderName() throws ParsingException {
        return Jsoup.parse(showInfo.getString("image_caption")).getElementsByTag("a").stream()
                .map(Element::text)
                .findFirst()
                .orElseThrow(() -> new ParsingException("Could not get uploader name"));
    }

    @Nullable
    @Override
    public String getTextualUploadDate() {
        return showInfo.getString("published_date");
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() throws ParsingException {
        return getImagesFromImageId(showInfo.getLong("show_image_id"), false);
    }

    @Nonnull
    @Override
    public List<Image> getUploaderAvatars() {
        return Collections.singletonList(
                new Image(BASE_URL + "/img/buttons/bandcamp-button-circle-whitecolor-512.png",
                        512, 512, ResolutionLevel.MEDIUM));
    }

    @Nonnull
    @Override
    public Description getDescription() {
        return Description.of(showInfo.getString("desc"), Description.Type.PLAIN_TEXT);
    }

    @Override
    public long getLength() {
        return showInfo.getLong("audio_duration");
    }

    @Nonnull
    @Override
    public List<Stream> getStreams() throws IOException, ParsingException {
        final List<Stream> audioStreams = new ArrayList<>();
        final JsonObject streams = showInfo.getObject("audio_stream");

        final String mp3StreamUrl = streams.getString(MP3_128);
        if (!Utils.isNullOrEmpty(mp3StreamUrl)) {
            audioStreams.add(new UriAudioStream(
                    MP3_128,
                    null,
                    null,
                    Boolean.FALSE,
                    AudioMediaFormat.MP3,
                    128,
                    null,
                    2,
                    AudioTrackType.ORIGINAL,
                    Boolean.FALSE,
                    "mp3",
                    new HttpDeliverySource(new UriObject(mp3StreamUrl,
                            getExpirationTimestampFromUrl(mp3StreamUrl), null),
                            List.of(),
                            Map.of(),
                            HttpDeliverySource.HttpMethod.GET,
                            null),
                    StreamingProtocol.PROGRESSIVE));
        }

        final String opusStreamUrl = streams.getString(OPUS_LO);
        if (!Utils.isNullOrEmpty(opusStreamUrl)) {
            audioStreams.add(new UriAudioStream(
                    OPUS_LO,
                    null,
                    null,
                    Boolean.FALSE,
                    AudioMediaFormat.OPUS,
                    100,
                    null,
                    2,
                    AudioTrackType.ORIGINAL,
                    Boolean.FALSE,
                    "ogg",
                    new HttpDeliverySource(new UriObject(opusStreamUrl,
                            getExpirationTimestampFromUrl(opusStreamUrl),
                            null),
                            List.of(),
                            Map.of(),
                            HttpDeliverySource.HttpMethod.GET,
                            null),
                    StreamingProtocol.PROGRESSIVE));
        }

        return audioStreams;
    }

    @Nonnull
    @Override
    public List<StreamSegment> getStreamSegments() throws ParsingException {
        final JsonArray tracks = showInfo.getArray("tracks");
        final List<StreamSegment> segments = new ArrayList<>(tracks.size());
        for (final Object t : tracks) {
            final JsonObject track = (JsonObject) t;
            final StreamSegment segment = new StreamSegment(
                    track.getString("title"), track.getInt("timecode"));
            // "track art" is the track's album cover
            segment.setPreviewUrl(getImageUrl(track.getLong("track_art_id"), true));
            segment.setChannelName(track.getString("artist"));
            segments.add(segment);
        }
        return segments;
    }

    @Nonnull
    @Override
    public String getLicence() {
        // Contrary to other Bandcamp streams, radio streams don't have a license
        return "";
    }

    @Nonnull
    @Override
    public String getCategory() {
        // Contrary to other Bandcamp streams, radio streams don't have categories
        return "";
    }

    @Nonnull
    @Override
    public List<String> getTags() {
        // Contrary to other Bandcamp streams, radio streams don't have tags
        return Collections.emptyList();
    }

    @Override
    public PlaylistInfoItemsCollector getRelatedItems() {
        // Contrary to other Bandcamp streams, radio streams don't have related items
        return null;
    }
}
