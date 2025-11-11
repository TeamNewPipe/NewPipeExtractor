package org.schabi.newpipe.extractor.services.soundcloud.extractors;

import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL;
import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.clientId;
import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.getAllImagesFromArtworkOrAvatarUrl;
import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.getAllImagesFromTrackObject;
import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.getAvatarUrl;
import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.parseDate;
import static org.schabi.newpipe.extractor.stream.Stream.ID_UNKNOWN;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.GeographicRestrictionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.SoundCloudGoPlusContentException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.DeliveryMethod;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.HlsAudioStream;
import org.schabi.newpipe.extractor.stream.SoundcloudHlsUtils;
import org.schabi.newpipe.extractor.stream.Stream;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.schabi.newpipe.extractor.utils.ExtractorLogger;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SoundcloudStreamExtractor extends StreamExtractor {
    public static final String TAG = SoundcloudStreamExtractor.class.getSimpleName();
    private JsonObject track;
    private boolean isAvailable = true;

    public SoundcloudStreamExtractor(final StreamingService service,
                                     final LinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) throws IOException,
            ExtractionException {
        final var url = getUrl();
        ExtractorLogger.d(TAG, "onFetchPage(" + url + ")");
        track = SoundcloudParsingHelper.resolveFor(downloader, url);

        final String policy = track.getString("policy", "");
        ExtractorLogger.d(TAG, "policy is: " + policy);
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

    @Nullable
    @Override
    public String getTextualUploadDate() {
        return track.getString("created_at");
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return parseDate(getTextualUploadDate());
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() throws ParsingException {
        return getAllImagesFromTrackObject(track);
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
        final var timestamp = getTimestampSeconds("(#t=\\d{0,3}h?\\d{0,3}m?\\d{1,3}s?)");
        return timestamp == -2 ? 0 : timestamp;
    }

    @Override
    public long getViewCount() {
        return track.getLong("playback_count");
    }

    @Override
    public long getLikeCount() {
        return track.getLong("likes_count", -1);
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
    public List<Image> getUploaderAvatars() {
        return getAllImagesFromArtworkOrAvatarUrl(getAvatarUrl(track));
    }

    @Override
    public List<AudioStream> getAudioStreams() throws ExtractionException {
        final List<AudioStream> audioStreams = new ArrayList<>();

        // Streams can be streamable and downloadable - or explicitly not.
        // For playing the track, it is only necessary to have a streamable track.
        // If this is not the case, this track might not be published yet.
        if (!track.getBoolean("streamable") || !isAvailable) {
            ExtractorLogger.d(TAG, "Not streamable track: " + getUrl());
            return audioStreams;
        }

        try {
            final JsonArray transcodings = track.getObject("media")
                                                .getArray("transcodings");
            if (!isNullOrEmpty(transcodings)) {
                // Get information about what stream formats are available
                ExtractorLogger.d(TAG, "Extracting audio streams for " + getName());
                extractAudioStreams(transcodings, audioStreams);
            }
        } catch (final NullPointerException e) {
            throw new ExtractionException("Could not get audio streams", e);
        }

        return audioStreams;
    }

    // TODO: put this somewhere better
    /**
     * Constructs the API endpoint url for this track that will be called to get the url
     * for retrieving the actual byte data for playback
     * (e.g. the actual url could be an m3u8 playlist)
     * @param baseUrl The baseUrl needed to construct the full url
     * @return The full API endpoint url to call to get the actual playback url
     * @throws IOException If there is a problem getting clientId
     * @throws ExtractionException For the same reason
     */
    private String getApiStreamUrl(final String baseUrl) throws ExtractionException, IOException {
        String apiStreamUrl = baseUrl + "?client_id=" + clientId();

        final String trackAuthorization = track.getString("track_authorization");
        if (!isNullOrEmpty(trackAuthorization)) {
            apiStreamUrl += "&track_authorization=" + trackAuthorization;
        }
        return apiStreamUrl;
    }

    public static final class StreamBuildResult {
        public final String contentUrl;
        public final MediaFormat mediaFormat;

        public StreamBuildResult(final String contentUrl, final MediaFormat mediaFormat) {
            this.contentUrl = contentUrl;
            this.mediaFormat = mediaFormat;
        }

        @Override
        public String toString() {
            return "StreamBuildResult{"
                   + "contentUrl='" + contentUrl + '\''
                   + ", mediaFormat=" + mediaFormat
                   + '}';
        }
    }

    /**
     * Builds the common audio stream components for all SoundCloud audio streams<p>
     * Returns the stream content url if we support this type of transcoding, {@code null} otherwise
     * @param transcoding The SoundCloud JSON transcoding object for this stream
     * @param builder AudioStream builder to set the common values
     * @return the stream content url if this transcoding is supported and common values were built
     * {@code null} otherwise
     */
    @Nullable
    private StreamBuildResult buildBaseAudioStream(final JsonObject transcoding,
                                                   final AudioStream.Builder builder)
        throws ExtractionException, IOException {
        ExtractorLogger.d(TAG,  getName() + " Building base audio stream info");
        final var preset = transcoding.getString("preset", ID_UNKNOWN);
        final MediaFormat mediaFormat;
        if (preset.contains("mp3")) {
            mediaFormat = MediaFormat.MP3;
            builder.setAverageBitrate(128);
        } else if (preset.contains("opus")) {
            mediaFormat = MediaFormat.OPUS;
            builder.setAverageBitrate(64);
            builder.setDeliveryMethod(DeliveryMethod.HLS);
        } else if (preset.contains("aac_160k")) {
            mediaFormat = MediaFormat.M4A;
            builder.setAverageBitrate(160);
        } else {
            // Unknown format, return null to skip to the next audio stream
            return null;
        }

        builder.setMediaFormat(mediaFormat);

        builder.setId(preset);
        final var url = transcoding.getString("url");
        final var hlsPlaylistUrl = SoundcloudHlsUtils.getStreamContentUrl(getApiStreamUrl(url));
        builder.setContent(hlsPlaylistUrl, true);
        return new StreamBuildResult(hlsPlaylistUrl, mediaFormat);
    }

    private HlsAudioStream buildHlsAudioStream(final JsonObject transcoding)
            throws ExtractionException, IOException {
        ExtractorLogger.d(TAG, getName() + "Extracting hls audio stream");
        final var builder = new HlsAudioStream.Builder();
        final StreamBuildResult buildResult = buildBaseAudioStream(transcoding, builder);
        if (buildResult == null) {
            return null;
        }

        builder.setApiStreamUrl(getApiStreamUrl(transcoding.getString("url")));
        builder.setPlaylistId(SoundcloudHlsUtils.extractHlsPlaylistId(buildResult.contentUrl,
                                                            buildResult.mediaFormat));

        return builder.build();
    }

    private AudioStream buildProgressiveAudioStream(final JsonObject transcoding)
            throws ExtractionException, IOException {
        ExtractorLogger.d(TAG, getName() + "Extracting progressive audio stream");
        final var builder = new AudioStream.Builder();
        final StreamBuildResult buildResult = buildBaseAudioStream(transcoding, builder);
        return buildResult == null ? null : builder.build();
        // TODO: anything else?
    }

    private void extractAudioStreams(@Nonnull final JsonArray transcodings,
                                     final List<AudioStream> audioStreams) {
        transcodings.stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .forEachOrdered(transcoding -> {
                    final String url = transcoding.getString("url");
                    if (isNullOrEmpty(url)) {
                        return;
                    }

                    final String protocol = transcoding.getObject("format")
                            .getString("protocol");

                    if (protocol.contains("encrypted")) {
                        // Skip DRM-protected streams, which have encrypted in their protocol
                        // name
                        return;
                    }

                    final AudioStream audioStream;
                    try {
                        // SoundCloud only has one progressive stream, the rest are HLS
                        audioStream = protocol.equals("hls")
                                ? buildHlsAudioStream(transcoding)
                                : buildProgressiveAudioStream(transcoding);
                        if (audioStream != null
                            && !Stream.containSimilarStream(audioStream, audioStreams)) {
                            ExtractorLogger.d(TAG, audioStream.getFormat().getName() + " "
                                                + getName() + " " + audioStream.getContent());
                            audioStreams.add(audioStream);
                        }
                    } catch (final ExtractionException | IOException e) {
                        // Something went wrong when trying to get and add this audio stream,
                        // skip to the next one
                        final var preset = transcoding.getString("preset", "unknown");
                        ExtractorLogger.e(TAG,
                                     getName() + " Failed to extract audio stream for transcoding "
                                          + '[' + protocol + '/' + preset + "] " + url,
                                          e);
                    }
                });
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
        final String apiUrl = SOUNDCLOUD_API_V2_URL + "tracks/" + Utils.encodeUrlUtf8(getId())
                + "/related?client_id=" + Utils.encodeUrlUtf8(clientId());

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
