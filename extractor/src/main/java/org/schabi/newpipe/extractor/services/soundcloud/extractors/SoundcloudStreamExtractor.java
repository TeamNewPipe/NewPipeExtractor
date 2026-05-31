package org.schabi.newpipe.extractor.services.soundcloud.extractors;

import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL;
import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.clientId;
import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.getAllImagesFromArtworkOrAvatarUrl;
import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.getAllImagesFromTrackObject;
import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.getAvatarUrl;
import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.parseDate;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.NewPipe;
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
import org.schabi.newpipe.extractor.stream.AudioTrackType;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.StreamingProtocol;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.schabi.newpipe.extractor.stream.deliverysource.uri.HttpDeliverySource;
import org.schabi.newpipe.extractor.stream.deliverysource.uri.UriDeliverySource;
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
        return String.valueOf(track.getLong("id"));
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
        return Description.of(track.getString("description"), Description.Type.PLAIN_TEXT);
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
        return List.of();
    }


    @Nonnull
    @Override
    public List<Stream> getStreams() throws IOException, ParsingException {
        // Streams can be streamable and downloadable - or explicitly not.
        // For playing the track, it is only necessary to have a streamable track.
        // If this is not the case, this track might not be published yet.
        if (!track.getBoolean("streamable") || !isAvailable) {
            return List.of();
        }

        final JsonArray transcodings = track.getObject("media")
                .getArray("transcodings");
        if (transcodings.isEmpty()) {
            return List.of();
        }

        final List<Stream> audioStreams = new ArrayList<>();
        transcodings.streamAsJsonObjects()
                .forEach(transcoding -> {
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

                    final String preset = transcoding.getString("preset");

                    final AudioMediaFormat audioMediaFormat;
                    final int averageBitrate;
                    final String codec;
                    if (preset.contains("mp3")) {
                        // Deprecated audio format
                        audioMediaFormat = AudioMediaFormat.MP3;
                        averageBitrate = 128;
                        codec = "mp3";
                    } else if (preset.contains("opus")) {
                        // Deprecated audio format
                        audioMediaFormat = AudioMediaFormat.OPUS;
                        averageBitrate = 64;
                        codec = "opus";
                    } else if (preset.contains("aac_160k")) {
                        audioMediaFormat = AudioMediaFormat.M4A;
                        averageBitrate = 160;
                        codec = "aac";
                    } else if (preset.contains("aac_96k")) {
                        audioMediaFormat = AudioMediaFormat.M4A;
                        averageBitrate = 96;
                        codec = "aac";
                    } else {
                        // Unknown format, skip to the next audio stream
                        return;
                    }

                    try {
                        audioStreams.add(new UriAudioStream(
                                preset,
                                null,
                                null,
                                Boolean.FALSE,
                                audioMediaFormat,
                                averageBitrate,
                                null,
                                2,
                                AudioTrackType.ORIGINAL,
                                Boolean.FALSE,
                                codec,
                                getDeliverySource(url),
                                protocol.equals("hls") ? StreamingProtocol.HLS
                                        : StreamingProtocol.PROGRESSIVE));
                    } catch (final ExtractionException | IOException ignored) {
                        // Something went wrong when trying to get this audio stream URL,
                        // skip to the next one
                    }
                });

        return audioStreams;
    }

    @Nonnull
    private UriDeliverySource getDeliverySource(@Nonnull final String url)
            throws IOException, ExtractionException {
        final String transcodingUrl = getTranscodingUrl(url);
        if (isNullOrEmpty(transcodingUrl)) {
            throw new ParsingException("Could not get transcoding URL");
        }

        final UriObject.Refresher refresher = new UriObject.Refresher() {
            @Override
            public UriObject refresh(@Nonnull final String uri) throws UriObject.RefreshException {
                final String newStreamUrl;
                try {
                    newStreamUrl = getTranscodingUrl(url);
                } catch (final Exception e) {
                    throw new UriObject.RefreshException(
                            "Could not get refreshed transcoding URL", e);
                }
                return new UriObject(newStreamUrl, UriObject.EXPIRATION_TIMESTAMP_UNKNOWN, this);
            }
        };

        return new HttpDeliverySource(new UriObject(transcodingUrl,
                UriObject.EXPIRATION_TIMESTAMP_UNKNOWN, refresher),
                List.of(),
                Map.of(),
                HttpDeliverySource.HttpMethod.GET,
                null);
    }

    @Nonnull
    private String getTranscodingUrl(final String endpointUrl)
            throws IOException, ExtractionException {
        String apiStreamUrl = endpointUrl + "?client_id=" + clientId();

        final String trackAuthorization = track.getString("track_authorization");
        if (!isNullOrEmpty(trackAuthorization)) {
            apiStreamUrl += "&track_authorization=" + trackAuthorization;
        }

        final String response = NewPipe.getDownloader().get(apiStreamUrl).responseBody();
        final JsonObject urlObject;
        try {
            urlObject = JsonParser.object().from(response);
        } catch (final JsonParserException e) {
            throw new ParsingException("Could not parse streamable URL", e);
        }

        return urlObject.getString("url");
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
