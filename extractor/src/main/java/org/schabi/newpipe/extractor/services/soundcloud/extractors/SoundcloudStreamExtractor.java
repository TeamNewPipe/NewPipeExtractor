package org.schabi.newpipe.extractor.services.soundcloud.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ContentNotSupportedException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.SubtitlesStream;
import org.schabi.newpipe.extractor.stream.VideoStream;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import static org.schabi.newpipe.extractor.utils.JsonUtils.EMPTY_STRING;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

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
        List<AudioStream> audioStreams = new ArrayList<>();
        Downloader dl = NewPipe.getDownloader();

        // Streams can be streamable and downloadable - or explicitly not.
        // For playing the track, it is only necessary to have a streamable track.
        // If this is not the case, this track might not be published yet.
        if (!track.getBoolean("streamable")) return audioStreams;

        try {
            JsonArray transcodings = track.getObject("media").getArray("transcodings");

            // get information about what stream formats are available
            for (Object transcoding : transcodings) {

                JsonObject t = (JsonObject) transcoding;
                String url = t.getString("url");

                if (!isNullOrEmpty(url)) {

                    // We can only play the mp3 format, but not handle m3u playlists / streams.
                    // what about Opus?
                    if (t.getString("preset").contains("mp3")
                            && t.getObject("format").getString("protocol").equals("progressive")) {
                        // This url points to the endpoint which generates a unique and short living url to the stream.
                        // TODO: move this to a separate method to generate valid urls when needed (e.g. resuming a paused stream)
                        url += "?client_id=" + SoundcloudParsingHelper.clientId();
                        String res = dl.get(url).responseBody();

                        try {
                            JsonObject mp3UrlObject = JsonParser.object().from(res);
                            // Links in this file are also only valid for a short period.
                            audioStreams.add(new AudioStream(mp3UrlObject.getString("url"),
                                    MediaFormat.MP3, 128));
                        } catch (JsonParserException e) {
                            throw new ParsingException("Could not parse streamable url", e);
                        }
                    }
                }
            }

        } catch (NullPointerException e) {
            throw new ExtractionException("Could not get SoundCloud's track audio url", e);
        }

        if (audioStreams.isEmpty()) {
            throw new ContentNotSupportedException("HLS audio streams are not yet supported");
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
