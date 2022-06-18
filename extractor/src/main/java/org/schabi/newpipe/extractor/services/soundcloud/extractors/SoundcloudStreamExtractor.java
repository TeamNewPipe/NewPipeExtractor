package org.schabi.newpipe.extractor.services.soundcloud.extractors;

import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL;
import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.clientId;
import static org.schabi.newpipe.extractor.utils.Utils.HTTPS;
import static org.schabi.newpipe.extractor.utils.Utils.UTF_8;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.GeographicRestrictionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.exceptions.SoundCloudGoPlusContentException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.Privacy;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.streamdata.delivery.simpleimpl.SimpleHLSDeliveryDataImpl;
import org.schabi.newpipe.extractor.streamdata.delivery.simpleimpl.SimpleProgressiveHTTPDeliveryDataImpl;
import org.schabi.newpipe.extractor.streamdata.format.AudioMediaFormat;
import org.schabi.newpipe.extractor.streamdata.format.registry.AudioFormatRegistry;
import org.schabi.newpipe.extractor.streamdata.stream.AudioStream;
import org.schabi.newpipe.extractor.streamdata.stream.simpleimpl.SimpleAudioStreamImpl;
import org.schabi.newpipe.extractor.streamdata.stream.util.NewPipeStreamCollectors;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

        final String policy = track.getString("policy", "");
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
                .replace("Z", "");
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
        String artworkUrl = track.getString("artwork_url", "");
        if (artworkUrl.isEmpty()) {
            artworkUrl = track.getObject("user").getString("avatar_url", "");
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

        // Streams can be streamable and downloadable - or explicitly not.
        // For playing the track, it is only necessary to have a streamable track.
        // If this is not the case, this track might not be published yet.
        // If audio streams were calculated, return the calculated result
        if (!track.getBoolean("streamable") || !isAvailable) {
            return Collections.emptyList();
        }

        try {
            final List<AudioStream> audioStreams = new ArrayList<>(extractAudioStreams());
            extractDownloadableFileIfAvailable().ifPresent(audioStreams::add);
            return audioStreams;
        } catch (final NullPointerException e) {
            throw new ExtractionException("Could not get SoundCloud's tracks audio URL", e);
        }
    }

    private static boolean checkMp3ProgressivePresence(@Nonnull final JsonArray transcodings) {
        return transcodings.stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .anyMatch(transcoding -> transcoding.getString("preset").contains("mp3")
                        && transcoding.getObject("format").getString("protocol")
                        .equals("progressive"));
    }

    @Nonnull
    private String getTranscodingUrl(final String endpointUrl,
                                     final String protocol)
            throws IOException, ExtractionException {
        final Downloader downloader = NewPipe.getDownloader();
        final String apiStreamUrl = endpointUrl + "?client_id="
                + clientId();
        final String response = downloader.get(apiStreamUrl).responseBody();
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

    private List<AudioStream> extractAudioStreams() {
        final JsonArray transcodings = track.getObject("media").getArray("transcodings");
        if (isNullOrEmpty(transcodings)) {
            return Collections.emptyList();
        }

        final boolean mp3ProgressiveInStreams = checkMp3ProgressivePresence(transcodings);

        return transcodings.stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .filter(transcoding -> !isNullOrEmpty(transcoding.getString("url")))
                .map(transcoding -> {
                    final String protocol = transcoding
                            .getObject("format")
                            .getString("protocol");
                    final String mediaUrl;
                    try {
                        mediaUrl = getTranscodingUrl(transcoding.getString("url"), protocol);
                    } catch (final Exception e) {
                        return null; // Abort if something went wrong
                    }
                    if (isNullOrEmpty(mediaUrl)) {
                        return null; // Ignore invalid urls
                    }

                    final String preset = transcoding.getString("preset", "");

                    final AudioMediaFormat mediaFormat;
                    final int averageBitrate;
                    if (preset.contains("mp3")) {
                        // Don't add the MP3 HLS stream if there is a progressive stream present
                        // because the two have the same bitrate
                        if (mp3ProgressiveInStreams && protocol.equals("hls")) {
                            return null;
                        }
                        mediaFormat = AudioFormatRegistry.MP3;
                        averageBitrate = 128;
                    } else if (preset.contains("opus")) {
                        mediaFormat = AudioFormatRegistry.OPUS;
                        averageBitrate = 64;
                    } else {
                        return null;
                    }

                    return (AudioStream) new SimpleAudioStreamImpl(
                            mediaFormat,
                            protocol.equals("hls")
                                    ? new SimpleHLSDeliveryDataImpl(mediaUrl)
                                    : new SimpleProgressiveHTTPDeliveryDataImpl(mediaUrl),
                            averageBitrate
                    );
                })
                .filter(Objects::nonNull)
                .collect(NewPipeStreamCollectors.toDistinctList());
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
     * @return An {@link Optional} that may contain the extracted audio stream
     */
    @Nonnull
    private Optional<AudioStream> extractDownloadableFileIfAvailable() {
        if (!track.getBoolean("downloadable") || !track.getBoolean("has_downloads_left")) {
            return Optional.empty();
        }

        try {
            final String downloadUrl = getDownloadUrl(getId());
            if (isNullOrEmpty(downloadUrl)) {
                return Optional.empty();
            }

            // Find out what type of file is served
            final String fileType = determineFileTypeFromDownloadUrl(downloadUrl);

            // No fileType found -> ignore it
            if (isNullOrEmpty(fileType)) {
                return Optional.empty();
            }

            return Optional.of(new SimpleAudioStreamImpl(
                    new AudioFormatRegistry().getFromSuffixOrThrow(fileType),
                    new SimpleProgressiveHTTPDeliveryDataImpl(downloadUrl)
            ));
        } catch (final Exception ignored) {
            // If something went wrong when trying to get the download URL, ignore the
            // exception throw because this "stream" is not necessary to play the track
            return Optional.empty();
        }
    }

    /**
     * Determines the file type/extension of the download url.
     * <p>
     * Note: Uses HTTP FETCH for inspection.
     * </p>
     */
    @Nullable
    private String determineFileTypeFromDownloadUrl(final String downloadUrl)
            throws IOException, ReCaptchaException {

        final Response response = NewPipe.getDownloader().head(downloadUrl);

        // As of 2022-06 Soundcloud uses AWS S3
        // Use the AWS header to identify the filetype first because it's simpler
        final String amzMetaFileType = response.getHeader("x-amz-meta-file-type");
        if (!isNullOrEmpty(amzMetaFileType)) {
            return amzMetaFileType;
        }

        // If the AWS header was not present try extract the filetype
        // by inspecting the download file name
        // Example-Value:
        // attachment;filename="SoundCloud%20Download"; filename*=utf-8''song.mp3
        final String contentDisp = response.getHeader("Content-Disposition");
        if (!isNullOrEmpty(contentDisp) && contentDisp.contains(".")) {
            return contentDisp.substring(contentDisp.lastIndexOf(".") + 1);
        }

        return null;
    }

    /**
     * Parses a SoundCloud HLS manifest to get a single URL of HLS streams.
     *
     * <p>
     * This method downloads the provided manifest URL, finds all web occurrences in the manifest,
     * gets the last segment URL, changes its segment range to {@code 0/track-length}, and return
     * this as a string.
     * </p>
     *
     * @param  hlsManifestUrl the URL of the manifest to be parsed
     * @return a single URL that contains a range equal to the length of the track
     */
    @Nonnull
    private static String getSingleUrlFromHlsManifest(@Nonnull final String hlsManifestUrl)
            throws ParsingException {
        final Downloader dl = NewPipe.getDownloader();
        final String hlsManifestResponse;

        try {
            hlsManifestResponse = dl.get(hlsManifestUrl).responseBody();
        } catch (final IOException | ReCaptchaException e) {
            throw new ParsingException("Could not get SoundCloud HLS manifest");
        }

        final String[] lines = hlsManifestResponse.split("\\r?\\n");
        for (int l = lines.length - 1; l >= 0; l--) {
            final String line = lines[l];
            // Get the last URL from manifest, because it contains the range of the stream
            if (line.trim().length() != 0 && !line.startsWith("#") && line.startsWith("https")) {
                final String[] hlsLastRangeUrlArray = line.split("/");
                return HTTPS + hlsLastRangeUrlArray[2] + "/media/0/" + hlsLastRangeUrlArray[5]
                        + "/" + hlsLastRangeUrlArray[6];
            }
        }
        throw new ParsingException("Could not get any URL from HLS manifest");
    }

    @Override
    public boolean isAudioOnly() {
        return true;
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


    private static String urlEncode(final String value) {
        try {
            return URLEncoder.encode(value, UTF_8);
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
