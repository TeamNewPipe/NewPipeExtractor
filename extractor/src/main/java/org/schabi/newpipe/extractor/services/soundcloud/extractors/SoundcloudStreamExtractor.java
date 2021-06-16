package org.schabi.newpipe.extractor.services.soundcloud.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.MetaInfo;
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
import org.schabi.newpipe.extractor.stream.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL;
import static org.schabi.newpipe.extractor.utils.Utils.*;

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
            if (policy.equals("BLOCK")) throw new GeographicRestrictionException(
                        "This track is not available in user's country");
            throw new ContentNotAvailableException("Content not available: policy " + policy);
        }
    }

    @Nonnull
    @Override
    public String getId() {
        return track.getInt("id") + EMPTY_STRING;
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
    public int getAgeLimit() {
        return NO_AGE_LIMIT;
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

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return track.getObject("user").getBoolean("verified");
    }

    @Nonnull
    @Override
    public String getUploaderAvatarUrl() {
        return SoundcloudParsingHelper.getAvatarUrl(track);
    }

    @Nonnull
    @Override
    public String getSubChannelUrl() {
        return "";
    }

    @Nonnull
    @Override
    public String getSubChannelName() {
        return "";
    }

    @Nonnull
    @Override
    public String getSubChannelAvatarUrl() {
        return "";
    }

    @Nonnull
    @Override
    public String getDashMpdUrl() {
        return "";
    }

    @Nonnull
    @Override
    public String getHlsUrl() {
        return "";
    }

    @Override
    public List<AudioStream> getAudioStreams() throws ExtractionException {
        final List<AudioStream> audioStreams = new ArrayList<>();

        // Streams can be streamable and downloadable - or explicitly not.
        // For playing the track, it is only necessary to have a streamable track.
        // If this is not the case, this track might not be published yet.
        if (!track.getBoolean("streamable") || !isAvailable) return audioStreams;

        try {
            final JsonArray transcodings = track.getObject("media").getArray("transcodings");
            if (transcodings != null) {
                // Get information about what stream formats are available
                extractAudioStreams(transcodings, checkMp3ProgressivePresence(transcodings),
                        audioStreams);
            }
        } catch (final NullPointerException e) {
            throw new ExtractionException("Could not get SoundCloud's tracks audio URL", e);
        }

        return audioStreams;
    }

    private static boolean checkMp3ProgressivePresence(@Nonnull final JsonArray transcodings) {
        boolean presence = false;
        for (final Object transcoding : transcodings) {
            final JsonObject transcodingJsonObject = (JsonObject) transcoding;
            if (transcodingJsonObject.getString("preset").contains("mp3") &&
                    transcodingJsonObject.getObject("format").getString("protocol")
                            .equals("progressive")) {
                presence = true;
                break;
            }
        }
        return presence;
    }

    @Nonnull
    private static String getTranscodingUrl(final String endpointUrl,
                                            final String protocol)
            throws IOException, ExtractionException {
        final Downloader downloader = NewPipe.getDownloader();
        final String apiStreamUrl = endpointUrl + "?client_id="
                + SoundcloudParsingHelper.clientId();
        final String response = downloader.get(apiStreamUrl).responseBody();
        final JsonObject urlObject;
        try {
            urlObject = JsonParser.object().from(response);
        } catch (final JsonParserException e) {
            throw new ParsingException("Could not parse streamable url", e);
        }
        final String urlString = urlObject.getString("url");

        if (protocol.equals("progressive")) {
            return urlString;
        } else if (protocol.equals("hls")) {
            try {
                return getSingleUrlFromHlsManifest(urlString);
            } catch (final ParsingException ignored) {
            }
        }
        // else, unknown protocol
        return "";
    }

    private static void extractAudioStreams(@Nonnull final JsonArray transcodings,
                                            final boolean mp3ProgressiveInStreams,
                                            final List<AudioStream> audioStreams) {
        for (final Object transcoding : transcodings) {
            final JsonObject transcodingJsonObject = (JsonObject) transcoding;
            final String url = transcodingJsonObject.getString("url");
            if (isNullOrEmpty(url)) {
                continue;
            }
            final String mediaUrl;
            final String preset = transcodingJsonObject.getString("preset");
            final String protocol = transcodingJsonObject.getObject("format")
                    .getString("protocol");
            MediaFormat mediaFormat = null;
            int bitrate = 0;
            if (preset.contains("mp3")) {
                // Don't add the MP3 HLS stream if there is a progressive stream present
                // because the two have the same bitrate
                if (mp3ProgressiveInStreams && protocol.equals("hls")) {
                    continue;
                }
                mediaFormat = MediaFormat.MP3;
                bitrate = 128;
            } else if (preset.contains("opus")) {
                mediaFormat = MediaFormat.OPUS;
                bitrate = 64;
            }

            if (mediaFormat != null) {
                try {
                    mediaUrl = getTranscodingUrl(url, protocol);
                    if (!mediaUrl.isEmpty()) {
                        audioStreams.add(new AudioStream(preset, mediaUrl,  mediaFormat, bitrate));
                    }
                } catch (final Exception ignored) {
                    // Something went wrong when parsing this transcoding, don't add it to the
                    // audioStreams
                }
            }
        }
    }

    /**
     * Parses a SoundCloud HLS manifest to get a single URL of HLS streams.
     * <p>
     * This method downloads the provided manifest URL, find all web occurrences in the manifest,
     * get the last segment URL, changes its segment range to {@code 0/track-length} and return
     * this string.
     * @param  hlsManifestUrl the URL of the manifest to be parsed
     * @return a single URL that contains a range equal to the length of the track
     */
    @Nonnull
    private static String getSingleUrlFromHlsManifest(final String hlsManifestUrl)
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
    @Nonnull
    public List<SubtitlesStream> getSubtitlesDefault() {
        return Collections.emptyList();
    }

    @Override
    @Nonnull
    public List<SubtitlesStream> getSubtitles(MediaFormat format) {
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
                + "/related?client_id=" + urlEncode(SoundcloudParsingHelper.clientId());

        SoundcloudParsingHelper.getStreamsFromApi(collector, apiUrl);
        return collector;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Nonnull
    @Override
    public String getHost() {
        return "";
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

    @Override
    public Locale getLanguageInfo() {
        return null;
    }

    @Nonnull
    @Override
    public List<String> getTags() {
        // Tags are separated by spaces, but they can be multiple words escaped by quotes "
        final String[] tagList = track.getString("tag_list").split(" ");
        final List<String> tags = new ArrayList<>();
        String escapedTag = "";
        boolean isEscaped = false;
        for (final String tag : tagList) {
            if (tag.startsWith("\"")) {
                escapedTag += tag.replace("\"", "");
                isEscaped = true;
            } else if (isEscaped) {
                if (tag.endsWith("\"")) {
                    escapedTag += " " + tag.replace("\"", "");
                    isEscaped = false;
                    tags.add(escapedTag);
                } else {
                    escapedTag += " " + tag;
                }
            } else if (!tag.isEmpty()) {
                tags.add(tag);
            }
        }
        return tags;
    }

    @Nonnull
    @Override
    public String getSupportInfo() {
        return "";
    }

    @Nonnull
    @Override
    public List<StreamSegment> getStreamSegments() {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public List<MetaInfo> getMetaInfo() {
        return Collections.emptyList();
    }
}
