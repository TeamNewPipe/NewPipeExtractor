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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.schabi.newpipe.extractor.utils.Utils.HTTPS;
import static org.schabi.newpipe.extractor.utils.Utils.*;

public class SoundcloudStreamExtractor extends StreamExtractor {
    private JsonObject track;

    public SoundcloudStreamExtractor(StreamingService service, LinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        track = SoundcloudParsingHelper.resolveFor(downloader, getUrl());

        String policy = track.getString("policy", EMPTY_STRING);
        if (!policy.equals("ALLOW") && !policy.equals("MONETIZE")) {
            if (policy.equals("SNIP")) {
                throw new SoundCloudGoPlusContentException();
            }
            if (policy.equals("BLOCK")) {
                throw new GeographicRestrictionException("This track is not available in user's country");
            }
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
        return new DateWrapper(SoundcloudParsingHelper.parseDateFrom(track.getString("created_at")));
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
    public List<AudioStream> getAudioStreams() throws IOException, ExtractionException {
        final List<AudioStream> audioStreams = new ArrayList<>();
        final Downloader dl = NewPipe.getDownloader();

        // Streams can be streamable and downloadable - or explicitly not.
        // For playing the track, it is only necessary to have a streamable track.
        // If this is not the case, this track might not be published yet.
        if (!track.getBoolean("streamable")) return audioStreams;

        try {
            final JsonArray transcodings = track.getObject("media").getArray("transcodings");

            // Get information about what stream formats are available
            for (final Object transcoding : transcodings) {
                final JsonObject t = (JsonObject) transcoding;
                String url = t.getString("url");
                final String mediaUrl;
                final MediaFormat mediaFormat;
                final int bitrate;

                if (!isNullOrEmpty(url)) {
                    if (t.getString("preset").contains("mp3")) {
                        mediaFormat = MediaFormat.MP3;
                        bitrate = 128;
                    } else if (t.getString("preset").contains("opus")) {
                        mediaFormat = MediaFormat.OPUS;
                        bitrate = 64;
                    } else {
                        continue;
                    }

                    // TODO: move this to a separate method to generate valid urls when needed (e.g. resuming a paused stream)

                    if (t.getObject("format").getString("protocol").equals("progressive")) {
                        // This url points to the endpoint which generates a unique and short living url to the stream.
                        url += "?client_id=" + SoundcloudParsingHelper.clientId();
                        final String res = dl.get(url).responseBody();

                        try {
                            JsonObject mp3UrlObject = JsonParser.object().from(res);
                            // Links in this file are also only valid for a short period.
                            mediaUrl = mp3UrlObject.getString("url");
                        } catch (final JsonParserException e) {
                            throw new ParsingException("Could not parse streamable url", e);
                        }
                    } else if (t.getObject("format").getString("protocol").equals("hls")) {
                        // This url points to the endpoint which generates a unique and short living url to the stream.
                        url += "?client_id=" + SoundcloudParsingHelper.clientId();
                        final String res = dl.get(url).responseBody();

                        try {
                            final JsonObject mp3HlsUrlObject = JsonParser.object().from(res);
                            // Links in this file are also only valid for a short period.

                            // Parsing the HLS manifest to get a single file by requesting a range equal to 0-track_length
                            final String hlsManifestResponse = dl.get(mp3HlsUrlObject.getString("url")).responseBody();
                            final List<String> hlsRangesList = new ArrayList<>();
                            final Matcher regex = Pattern.compile("((https?):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?+-=\\\\.&]*)")
                                    .matcher(hlsManifestResponse);

                            while (regex.find()) {
                                hlsRangesList.add(hlsManifestResponse.substring(regex.start(0), regex.end(0)));
                            }

                            final String hlsLastRangeUrl = hlsRangesList.get(hlsRangesList.size() - 1);
                            final String[] hlsLastRangeUrlArray = hlsLastRangeUrl.split("/");

                            mediaUrl = HTTPS + hlsLastRangeUrlArray[2] + "/media/0/" + hlsLastRangeUrlArray[5] + "/" + hlsLastRangeUrlArray[6];
                        } catch (final JsonParserException e) {
                            throw new ParsingException("Could not parse streamable url", e);
                        }
                    } else {
                        continue;
                    }

                    audioStreams.add(new AudioStream(mediaUrl, mediaFormat, bitrate));
                }
            }

        } catch (final NullPointerException e) {
            throw new ExtractionException("Could not get SoundCloud's track audio url", e);
        }

        return audioStreams;
    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, UTF_8);
        } catch (UnsupportedEncodingException e) {
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
    public StreamInfoItemsCollector getRelatedStreams() throws IOException, ExtractionException {
        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

        final String apiUrl = "https://api-v2.soundcloud.com/tracks/" + urlEncode(getId())
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

    @Nonnull
    @Override
    public String getPrivacy() {
        return "";
    }

    @Nonnull
    @Override
    public String getCategory() {
        return "";
    }

    @Nonnull
    @Override
    public String getLicence() {
        return "";
    }

    @Override
    public Locale getLanguageInfo() {
        return null;
    }

    @Nonnull
    @Override
    public List<String> getTags() {
        return Collections.emptyList();
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
