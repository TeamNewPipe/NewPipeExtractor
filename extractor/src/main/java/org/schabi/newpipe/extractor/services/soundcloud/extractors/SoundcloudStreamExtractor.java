package org.schabi.newpipe.extractor.services.soundcloud.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.DeliveryFormat;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.SubtitlesStream;
import org.schabi.newpipe.extractor.stream.VideoStream;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.schabi.newpipe.extractor.utils.JsonUtils.EMPTY_STRING;

public class SoundcloudStreamExtractor extends StreamExtractor {
    private JsonObject track;

    public SoundcloudStreamExtractor(StreamingService service, LinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        track = SoundcloudParsingHelper.resolveFor(downloader, getOriginalUrl());

        String policy = track.getString("policy", EMPTY_STRING);
        if (!policy.equals("ALLOW") && !policy.equals("MONETIZE")) {
            throw new ContentNotAvailableException("Content not available: policy " + policy);
        }
    }

    @Nonnull
    @Override
    public String getId() {
        return track.getInt("id") + "";
    }

    @Nonnull
    @Override
    public String getName() {
        return track.getString("title");
    }

    @Nonnull
    @Override
    public String getTextualUploadDate() throws ParsingException {
        return track.getString("created_at").replace("T"," ").replace("Z", "");
    }

    @Nonnull
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return new DateWrapper(SoundcloudParsingHelper.parseDateFrom(track.getString("created_at")));
    }

    @Nonnull
    @Override
    public String getThumbnailUrl() {
        String artworkUrl = track.getString("artwork_url", EMPTY_STRING);
        if (artworkUrl.isEmpty()) {
            artworkUrl = track.getObject("user").getString("avatar_url", EMPTY_STRING);
        }
        String artworkUrlBetterResolution = artworkUrl.replace("large.jpg", "crop.jpg");
        return artworkUrlBetterResolution;
    }

    @Override
    public Description getDescription() {
        return new Description(track.getString("description"), Description.PLAIN_TEXT);
    }

    @Override
    public int getAgeLimit() {
        return NO_AGE_LIMIT;
    }

    @Override
    public long getLength() {
        return track.getNumber("duration", 0).longValue() / 1000L;
    }

    @Override
    public long getTimeStamp() throws ParsingException {
        return getTimestampSeconds("(#t=\\d{0,3}h?\\d{0,3}m?\\d{1,3}s?)");
    }

    @Override
    public long getViewCount() {
        return track.getNumber("playback_count", 0).longValue();
    }

    @Override
    public long getLikeCount() {
        return track.getNumber("favoritings_count", -1).longValue();
    }

    @Override
    public long getDislikeCount() {
        return -1;
    }

    @Nonnull
    @Override
    public String getUploaderUrl() {
        return SoundcloudParsingHelper.getUploaderUrl(track);
    }

    @Nonnull
    @Override
    public String getUploaderName() {
        return SoundcloudParsingHelper.getUploaderName(track);
    }

    @Nonnull
    @Override
    public String getUploaderAvatarUrl() {
        return SoundcloudParsingHelper.getAvatarUrl(track);
    }

    @Nonnull
    @Override
    public String getSubChannelUrl() throws ParsingException {
        return "";
    }

    @Nonnull
    @Override
    public String getSubChannelName() throws ParsingException {
        return "";
    }

    @Nonnull
    @Override
    public String getSubChannelAvatarUrl() throws ParsingException {
        return "";
    }

    @Nonnull
    @Override
    public String getDashMpdUrl() {
        return "";
    }

    @Nonnull
    @Override
    public String getHlsUrl() throws ParsingException {
        return "";
    }

    @Override
    public List<AudioStream> getAudioStreams() throws IOException, ExtractionException {
        final List<AudioStream> audioStreams = new ArrayList<>();

        // Streams can be streamable and downloadable - or explicitly not.
        // For playing the track, it is only necessary to have a streamable track.
        // If this is not the case, this track might not be published yet.
        if (!track.getBoolean("streamable")) return audioStreams;

        try {
            final JsonArray transcodings = track.getObject("media").getArray("transcodings");

            // Get information about what stream formats are available
            for (Object transcoding : transcodings) {
                final JsonObject transcodingObject = (JsonObject) transcoding;
                String url = transcodingObject.getString("url");
                final String preset = transcodingObject.getString("preset");
                final JsonObject format = transcodingObject.getObject("format");
                final String protocol = format.getString("protocol");

                final MediaFormat mediaFormat;
                final int mediaQuality;

                if (preset.startsWith("mp3")) {
                    mediaFormat = MediaFormat.MP3;
                } else if (preset.startsWith("opus")) {
                    mediaFormat = MediaFormat.OPUS;
                } else {
                    throw new ParsingException("Unsupported media format: " + preset);
                }

                if (preset.equals("mp3_0_0") || preset.equals("mp3_0_1")
                        || preset.equals("mp3_standard")) {
                    mediaQuality = 128;
                } else if (preset.equals("opus_0_0")) {
                    mediaQuality = 64;
                } else {
                    throw new ParsingException("Unsupported media quality: " + preset);
                }

                url += "?client_id=" + SoundcloudParsingHelper.clientId();
                final String urlResponse = getDownloader().get(url).responseBody();

                final JsonObject urlObject;
                try {
                    urlObject = JsonParser.object().from(urlResponse);
                } catch (JsonParserException e) {
                    throw new ParsingException("Could not parse url response", e);
                }

                final DeliveryFormat deliveryFormat;
                final String mediaUrl = urlObject.getString("url");

                if (protocol.equalsIgnoreCase("progressive")) {
                    deliveryFormat = DeliveryFormat.direct(mediaUrl);
                } else if (protocol.equalsIgnoreCase("hls")) {
                    deliveryFormat = DeliveryFormat.hls(mediaUrl);
                } else {
                    throw new ParsingException("Unsupported protocol: " + protocol);
                }

                audioStreams.add(new AudioStream(deliveryFormat, mediaFormat, mediaQuality));
            }

        } catch (NullPointerException e) {
            throw new ExtractionException("Could not get SoundCloud's track audio url", e);
        }

        return audioStreams;
    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public List<VideoStream> getVideoStreams() throws IOException, ExtractionException {
        return null;
    }

    @Override
    public List<VideoStream> getVideoOnlyStreams() throws IOException, ExtractionException {
        return null;
    }

    @Override
    @Nonnull
    public List<SubtitlesStream> getSubtitlesDefault() throws IOException, ExtractionException {
        return Collections.emptyList();
    }

    @Override
    @Nonnull
    public List<SubtitlesStream> getSubtitles(MediaFormat format) throws IOException, ExtractionException {
        return Collections.emptyList();
    }

    @Override
    public StreamType getStreamType() {
        return StreamType.AUDIO_STREAM;
    }

    @Override
    public StreamInfoItem getNextStream() throws IOException, ExtractionException {
        return null;
    }

    @Override
    public StreamInfoItemsCollector getRelatedStreams() throws IOException, ExtractionException {
        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

        String apiUrl = "https://api-v2.soundcloud.com/tracks/" + urlEncode(getId()) + "/related"
                + "?client_id=" + urlEncode(SoundcloudParsingHelper.clientId());

        SoundcloudParsingHelper.getStreamsFromApi(collector, apiUrl);
        return collector;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public String getHost() throws ParsingException {
        return "";
    }

    @Override
    public String getPrivacy() throws ParsingException {
        return "";
    }

    @Override
    public String getCategory() throws ParsingException {
        return "";
    }

    @Override
    public String getLicence() throws ParsingException {
        return "";
    }

    @Override
    public Locale getLanguageInfo() throws ParsingException {
        return null;
    }

    @Nonnull
    @Override
    public List<String> getTags() throws ParsingException {
        return new ArrayList<>();
    }

    @Nonnull
    @Override
    public String getSupportInfo() throws ParsingException {
        return "";
    }
}
