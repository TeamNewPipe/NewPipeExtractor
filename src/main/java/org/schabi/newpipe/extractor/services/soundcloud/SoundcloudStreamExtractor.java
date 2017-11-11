package org.schabi.newpipe.extractor.services.soundcloud;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.*;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.stream.*;
import org.schabi.newpipe.extractor.utils.Parser;

import java.io.IOException;
import java.util.*;

public class SoundcloudStreamExtractor extends StreamExtractor {
    private JsonObject track;

    public SoundcloudStreamExtractor(StreamingService service, String url) throws IOException, ExtractionException {
        super(service, url);
    }

    @Override
    public void fetchPage() throws IOException, ExtractionException {
        track = SoundcloudParsingHelper.resolveFor(getOriginalUrl());

        String policy = track.getString("policy", "");
        if (!policy.equals("ALLOW") && !policy.equals("MONETIZE")) {
            throw new ContentNotAvailableException("Content not available: policy " + policy);
        }
    }

    @Override
    public String getCleanUrl() {
        return track.isString("permalink_url") ? track.getString("permalink_url") : getOriginalUrl();
    }

    @Override
    public String getId() {
        return track.getInt("id") + "";
    }

    @Override
    public String getName() {
        return track.getString("title");
    }

    @Override
    public String getUploadDate() throws ParsingException {
        return SoundcloudParsingHelper.toDateString(track.getString("created_at"));
    }

    @Override
    public String getThumbnailUrl() {
        return track.getString("artwork_url", "");
    }

    @Override
    public String getDescription() {
        return track.getString("description");
    }

    @Override
    public int getAgeLimit() {
        return 0;
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

    @Override
    public String getUploaderUrl() {
        return track.getObject("user").getString("permalink_url", "");
    }

    @Override
    public String getUploaderName() {
        return track.getObject("user").getString("username", "");
    }

    @Override
    public String getUploaderAvatarUrl() {
        return track.getObject("user", new JsonObject()).getString("avatar_url", "");
    }

    @Override
    public String getDashMpdUrl() {
        return null;
    }

    @Override
    public List<AudioStream> getAudioStreams() throws IOException, ExtractionException {
        List<AudioStream> audioStreams = new ArrayList<>();
        Downloader dl = NewPipe.getDownloader();

        String apiUrl = "https://api.soundcloud.com/i1/tracks/" + getId() + "/streams"
                + "?client_id=" + SoundcloudParsingHelper.clientId();

        String response = dl.download(apiUrl);
        JsonObject responseObject;
        try {
            responseObject = JsonParser.object().from(response);
        } catch (JsonParserException e) {
            throw new ParsingException("Could not parse json response", e);
        }

        String mp3Url = responseObject.getString("http_mp3_128_url");
        if (mp3Url != null && !mp3Url.isEmpty()) {
            audioStreams.add(new AudioStream(mp3Url, MediaFormat.MP3, 128));
        } else {
            throw new ExtractionException("Could not get SoundCloud's track audio url");
        }

        return audioStreams;
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
    public List<Subtitles> getSubtitlesDefault() throws IOException, ExtractionException {
        return null;
    }

    @Override
    public List<Subtitles> getSubtitles(SubtitlesFormat format) throws IOException, ExtractionException {
        return null;
    }

    @Override
    public StreamType getStreamType() {
        return StreamType.AUDIO_STREAM;
    }

    @Override
    public StreamInfoItem getNextVideo() throws IOException, ExtractionException {
        return null;
    }

    @Override
    public StreamInfoItemCollector getRelatedVideos() throws IOException, ExtractionException {
        StreamInfoItemCollector collector = new StreamInfoItemCollector(getServiceId());

        String apiUrl = "https://api-v2.soundcloud.com/tracks/" + getId() + "/related"
                + "?client_id=" + SoundcloudParsingHelper.clientId();

        SoundcloudParsingHelper.getStreamsFromApi(collector, apiUrl);
        return collector;
    }


    @Override
    public String getErrorMessage() {
        return null;
    }
}
