package org.schabi.newpipe.extractor.services.soundcloud.extractors;

import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL;
import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.clientId;
import static org.schabi.newpipe.extractor.stream.AudioStream.UNKNOWN_BITRATE;
import static org.schabi.newpipe.extractor.stream.Stream.ID_UNKNOWN;
import static org.schabi.newpipe.extractor.utils.Utils.EMPTY_STRING;
import static org.schabi.newpipe.extractor.utils.Utils.HTTPS;
import static org.schabi.newpipe.extractor.utils.Utils.UTF_8;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.GeographicRestrictionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.exceptions.SoundCloudGoPlusContentException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.DeliveryMethod;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.Stream;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.VideoStream;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SoundcloudStreamExtractor extends StreamExtractor {
    private JsonObject track;
    private boolean isAvailable = true;

    public SoundcloudStreamExtractor(final StreamingService service,
                                     final LinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) throws IOException,
            ExtractionException {
        track = SoundcloudParsingHelper.resolveFor(downloader, getUrl());

        final String policy = track.getString("policy", EMPTY_STRING);
        if (!policy.equals("ALLOW") && !policy.equals("MONETIZE")) {
            isAvailable = false;

            if (policy.equals("SNIP")) {
                throw new SoundCloudGoPlusContentException();
            }

            if (policy.equals("BLOCK")) {
                throw new GeographicRestrictionException(
                        "This track is not available in user's country");
            }

            throw new ContentNotAvailableException("Content not available: policy " + policy);
        }
    }

    @Nonnull
    @Override
    public String getId() {
        return String.valueOf(track.getInt("id"));
    }

    @Nonnull
    @Override
    public String getName() {
        return track.getString("title");
    }

    @Nonnull
    @Override
    public String getTextualUploadDate() {
        return track.getString("created_at")
                .replace("T", " ")
                .replace("Z", EMPTY_STRING);
    }

    @Nonnull
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return new DateWrapper(SoundcloudParsingHelper.parseDateFrom(track.getString(
                "created_at")));
    }

    @Nonnull
    @Override
    public String getThumbnailUrl() {
        String artworkUrl = track.getString("artwork_url", EMPTY_STRING);
        if (artworkUrl.isEmpty()) {
            artworkUrl = track.getObject("user").getString("avatar_url", EMPTY_STRING);
        }
        return artworkUrl.replace("large.jpg", "crop.jpg");
    }

    @Nonnull
    @Override
    public Description getDescription() {
        return new Description(track.getString("description"), Description.PLAIN_TEXT);
    }

    @Override
    public long getLength() {
        return track.getLong("duration") / 1000L;
    }

    @Override
    public long getTimeStamp() throws ParsingException {
        return getTimestampSeconds("(#t=\\d{0,3}h?\\d{0,3}m?\\d{1,3}s?)");
    }

    @Override
    public long getViewCount() {
        return track.getLong("playback_count");
    }

    @Override
    public long getLikeCount() {
        return track.getLong("favoritings_count", -1);
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

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return track.getObject("user").getBoolean("verified");
    }

    @Nonnull
    @Override
    public String getUploaderAvatarUrl() {
        return SoundcloudParsingHelper.getAvatarUrl(track);
    }

    @Override
    public List<AudioStream> getAudioStreams() throws ExtractionException {
        final List<AudioStream> audioStreams = new ArrayList<>();

        // Streams can be streamable and downloadable - or explicitly not.
        // For playing the track, it is only necessary to have a streamable track.
        // If this is not the case, this track might not be published yet.
        if (!track.getBoolean("streamable") || !isAvailable) {
            return audioStreams;
        }

        try {
            final JsonArray transcodings = track.getObject("media").getArray("transcodings");
            if (!isNullOrEmpty(transcodings)) {
                // Get information about what stream formats are available
                extractAudioStreams(transcodings, checkMp3ProgressivePresence(transcodings),
                        audioStreams);
            }

            extractDownloadableFileIfAvailable(audioStreams);
        } catch (final NullPointerException e) {
            throw new ExtractionException("Could not get audio streams", e);
        }

        return audioStreams;
    }

    private static boolean checkMp3ProgressivePresence(@Nonnull final JsonArray transcodings) {
        return transcodings.stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .anyMatch(transcodingJsonObject -> transcodingJsonObject.getString("preset")
                        .contains("mp3") && transcodingJsonObject.getObject("format")
                        .getString("protocol").equals("progressive"));
    }

    @Nonnull
    private String getTranscodingUrl(final String endpointUrl)
            throws IOException, ExtractionException {
        final String apiStreamUrl = endpointUrl + "?client_id=" + clientId();
        final String response = NewPipe.getDownloader().get(apiStreamUrl).responseBody();
        final JsonObject urlObject;
        try {
            urlObject = JsonParser.object().from(response);
        } catch (final JsonParserException e) {
            throw new ParsingException("Could not parse streamable URL", e);
        }

        return urlObject.getString("url");
    }

    @Nullable
    private String getDownloadUrl(@Nonnull final String trackId)
            throws IOException, ExtractionException {
        final String response = NewPipe.getDownloader().get(SOUNDCLOUD_API_V2_URL + "tracks/"
                + trackId + "/download" + "?client_id=" + clientId()).responseBody();

        final JsonObject downloadJsonObject;
        try {
            downloadJsonObject = JsonParser.object().from(response);
        } catch (final JsonParserException e) {
            throw new ParsingException("Could not parse download URL", e);
        }
        final String redirectUri = downloadJsonObject.getString("redirectUri");
        if (!isNullOrEmpty(redirectUri)) {
            return redirectUri;
        }
        return null;
    }

    private void extractAudioStreams(@Nonnull final JsonArray transcodings,
                                     final boolean mp3ProgressiveInStreams,
                                     final List<AudioStream> audioStreams) {
        transcodings.stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .forEachOrdered(transcoding -> {
                    final String url = transcoding.getString("url");
                    if (isNullOrEmpty(url)) {
                        return;
                    }

                    final String preset = transcoding.getString("preset", ID_UNKNOWN);
                    final String protocol = transcoding.getObject("format").getString("protocol");
                    final AudioStream.Builder builder = new AudioStream.Builder()
                            .setId(preset);

                    try {
                        // streamUrl can be either the MP3 progressive stream URL or the
                        // manifest URL of the HLS MP3 stream (if there is no MP3 progressive
                        // stream, see above)
                        final String streamUrl = getTranscodingUrl(url);

                        if (preset.contains("mp3")) {
                            // Don't add the MP3 HLS stream if there is a progressive stream
                            // present because the two have the same bitrate
                            final boolean isHls = protocol.equals("hls");
                            if (mp3ProgressiveInStreams && isHls) {
                                return;
                            }

                            builder.setMediaFormat(MediaFormat.MP3);
                            builder.setAverageBitrate(128);

                            if (isHls) {
                                builder.setDeliveryMethod(DeliveryMethod.HLS);
                                builder.setContent(streamUrl, true);

                                final AudioStream hlsStream = builder.build();
                                if (!Stream.containSimilarStream(hlsStream, audioStreams)) {
                                    audioStreams.add(hlsStream);
                                }

                                final String progressiveHlsUrl =
                                        getSingleUrlFromHlsManifest(streamUrl);
                                builder.setDeliveryMethod(DeliveryMethod.PROGRESSIVE_HTTP);
                                builder.setContent(progressiveHlsUrl, true);

                                final AudioStream progressiveHlsStream = builder.build();
                                if (!Stream.containSimilarStream(
                                        progressiveHlsStream, audioStreams)) {
                                    audioStreams.add(progressiveHlsStream);
                                }

                                // The MP3 HLS stream has been added in both versions (HLS and
                                // progressive with the manifest parsing trick), so we need to
                                // continue (otherwise the code would try to add again the stream,
                                // which would be not added because the containsSimilarStream
                                // method would return false and an audio stream object would be
                                // created for nothing)
                                return;
                            } else {
                                builder.setContent(streamUrl, true);
                            }
                        } else if (preset.contains("opus")) {
                            // The HLS manifest trick doesn't work for opus streams
                            builder.setContent(streamUrl, true);
                            builder.setMediaFormat(MediaFormat.OPUS);
                            builder.setAverageBitrate(64);
                            builder.setDeliveryMethod(DeliveryMethod.HLS);
                        } else {
                            // Unknown format, skip to the next audio stream
                            return;
                        }

                        final AudioStream audioStream = builder.build();
                        if (!Stream.containSimilarStream(audioStream, audioStreams)) {
                            audioStreams.add(audioStream);
                        }
                    } catch (final ExtractionException | IOException ignored) {
                        // Something went wrong when trying to get and add this audio stream,
                        // skip to the next one
                    }
                });
    }

    /**
     * Add the downloadable format if it is available.
     *
     * <p>
     * A track can have the {@code downloadable} boolean set to {@code true}, but it doesn't mean
     * we can download it.
     * </p>
     *
     * <p>
     * If the value of the {@code has_download_left} boolean is {@code true}, the track can be
     * downloaded, and not otherwise.
     * </p>
     *
     * @param audioStreams the audio streams to which the downloadable file is added
     */
    public void extractDownloadableFileIfAvailable(final List<AudioStream> audioStreams) {
        if (track.getBoolean("downloadable") && track.getBoolean("has_downloads_left")) {
            try {
                final String downloadUrl = getDownloadUrl(getId());
                if (!isNullOrEmpty(downloadUrl)) {
                    audioStreams.add(new AudioStream.Builder()
                            .setId("original-format")
                            .setContent(downloadUrl, true)
                            .setAverageBitrate(UNKNOWN_BITRATE)
                            .build());
                }
            } catch (final Exception ignored) {
                // If something went wrong when trying to get the download URL, ignore the
                // exception throw because this "stream" is not necessary to play the track
            }
        }
    }

    /**
     * Parses a SoundCloud HLS MP3 manifest to get a single URL of HLS streams.
     *
     * <p>
     * This method downloads the provided manifest URL, finds all web occurrences in the manifest,
     * gets the last segment URL, changes its segment range to {@code 0/track-length}, and return
     * this as a string.
     * </p>
     *
     * <p>
     * This was working before for Opus streams, but has been broken by SoundCloud.
     * </p>
     *
     * @param  hlsManifestUrl the URL of the manifest to be parsed
     * @return a single URL that contains a range equal to the length of the track
     */
    @Nonnull
    private static String getSingleUrlFromHlsManifest(@Nonnull final String hlsManifestUrl)
            throws ParsingException {
        final String hlsManifestResponse;

        try {
            hlsManifestResponse = NewPipe.getDownloader().get(hlsManifestUrl).responseBody();
        } catch (final IOException | ReCaptchaException e) {
            throw new ParsingException("Could not get SoundCloud HLS manifest");
        }

        final String[] lines = hlsManifestResponse.split("\\r?\\n");
        for (int l = lines.length - 1; l >= 0; l--) {
            final String line = lines[l];
            // Get the last URL from manifest, because it contains the range of the stream
            if (line.trim().length() != 0 && !line.startsWith("#") && line.startsWith(HTTPS)) {
                final String[] hlsLastRangeUrlArray = line.split("/");
                return HTTPS + hlsLastRangeUrlArray[2] + "/media/0/" + hlsLastRangeUrlArray[5]
                        + "/" + hlsLastRangeUrlArray[6];
            }
        }

        throw new ParsingException("Could not get any URL from HLS manifest");
    }

    private static String urlEncode(final String value) {
        try {
            return URLEncoder.encode(value, UTF_8);
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
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

    @Nullable
    @Override
    public StreamInfoItemsCollector getRelatedItems() throws IOException, ExtractionException {
        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

        final String apiUrl = SOUNDCLOUD_API_V2_URL + "tracks/" + urlEncode(getId())
                + "/related?client_id=" + urlEncode(clientId());

        SoundcloudParsingHelper.getStreamsFromApi(collector, apiUrl);
        return collector;
    }

    @Override
    public Privacy getPrivacy() {
        return track.getString("sharing").equals("public") ? Privacy.PUBLIC : Privacy.PRIVATE;
    }

    @Nonnull
    @Override
    public String getCategory() {
        return track.getString("genre");
    }

    @Nonnull
    @Override
    public String getLicence() {
        return track.getString("license");
    }

    @Nonnull
    @Override
    public List<String> getTags() {
        // Tags are separated by spaces, but they can be multiple words escaped by quotes "
        final String[] tagList = track.getString("tag_list").split(" ");
        final List<String> tags = new ArrayList<>();
        final StringBuilder escapedTag = new StringBuilder();
        boolean isEscaped = false;
        for (final String tag : tagList) {
            if (tag.startsWith("\"")) {
                escapedTag.append(tag.replace("\"", ""));
                isEscaped = true;
            } else if (isEscaped) {
                if (tag.endsWith("\"")) {
                    escapedTag.append(" ").append(tag.replace("\"", ""));
                    isEscaped = false;
                    tags.add(escapedTag.toString());
                } else {
                    escapedTag.append(" ").append(tag);
                }
            } else if (!tag.isEmpty()) {
                tags.add(tag);
            }
        }
        return tags;
    }
}
