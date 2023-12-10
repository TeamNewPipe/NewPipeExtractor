package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.BASE_API_URL;
import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.BASE_URL;
import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getImageUrl;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ContentNotSupportedException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamSegment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BandcampRadioStreamExtractor extends BandcampStreamExtractor {

    private static final String OPUS_LO = "opus-lo";
    private static final String MP3_128 = "mp3-128";
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
    public String getThumbnailUrl() throws ParsingException {
        return getImageUrl(showInfo.getLong("show_image_id"), false);
    }

    @Nonnull
    @Override
    public String getUploaderAvatarUrl() {
        return BASE_URL + "/img/buttons/bandcamp-button-circle-whitecolor-512.png";
    }

    @Nonnull
    @Override
    public Description getDescription() {
        return new Description(showInfo.getString("desc"), Description.PLAIN_TEXT);
    }

    @Override
    public long getLength() {
        return showInfo.getLong("audio_duration");
    }

    @Override
    public List<AudioStream> getAudioStreams() {
        final List<AudioStream> audioStreams = new ArrayList<>();
        final JsonObject streams = showInfo.getObject("audio_stream");

        if (streams.has(MP3_128)) {
            audioStreams.add(new AudioStream.Builder()
                    .setId(MP3_128)
                    .setContent(streams.getString(MP3_128), true)
                    .setMediaFormat(MediaFormat.MP3)
                    .setAverageBitrate(128)
                    .build());
        }

        if (streams.has(OPUS_LO)) {
            audioStreams.add(new AudioStream.Builder()
                    .setId(OPUS_LO)
                    .setContent(streams.getString(OPUS_LO), true)
                    .setMediaFormat(MediaFormat.OPUS)
                    .setAverageBitrate(100).build());
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
