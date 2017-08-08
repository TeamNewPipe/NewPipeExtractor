package org.schabi.newpipe.extractor.services.soundcloud;

import org.json.JSONArray;
import org.json.JSONObject;
import org.schabi.newpipe.extractor.Downloader;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.stream.*;
import org.schabi.newpipe.extractor.utils.Parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SoundcloudStreamExtractor extends StreamExtractor {
    private JSONObject track;

    public SoundcloudStreamExtractor(StreamingService service, String url) throws IOException, ExtractionException {
        super(service, url);
    }

    @Override
    public void fetchPage() throws IOException, ExtractionException {
        track = SoundcloudParsingHelper.resolveFor(getOriginalUrl());

        if (!track.getString("policy").equals("ALLOW") && !track.getString("policy").equals("MONETIZE")) {
            throw new ContentNotAvailableException("Content not available: policy " + track.getString("policy"));
        }
    }

    @Override
    public String getCleanUrl() {
        try {
            return track.getString("permalink_url");
        } catch (Exception e) {
            return getOriginalUrl();
        }
    }

    @Override
    public String getId() {
        return track.getInt("id") + "";
    }

    @Override
    public String getTitle() {
        return track.getString("title");
    }

    @Override
    public String getDescription() {
        return track.getString("description");
    }

    @Override
    public String getUploader() {
        return track.getJSONObject("user").getString("username");
    }

    @Override
    public int getLength() {
        return track.getInt("duration") / 1000;
    }

    @Override
    public long getViewCount() {
        return track.getLong("playback_count");
    }

    @Override
    public String getUploadDate() throws ParsingException {
        return SoundcloudParsingHelper.toDateString(track.getString("created_at"));
    }

    @Override
    public String getThumbnailUrl() {
        return track.optString("artwork_url");
    }

    @Override
    public String getUploaderThumbnailUrl() {
        return track.getJSONObject("user").getString("avatar_url");
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
        JSONObject responseObject = new JSONObject(response);

        AudioStream audioStream = new AudioStream(responseObject.getString("http_mp3_128_url"), MediaFormat.MP3.id, 128);
        audioStreams.add(audioStream);

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
    public int getTimeStamp() throws ParsingException {
        String timeStamp;
        try {
            timeStamp = Parser.matchGroup1("(#t=\\d{0,3}h?\\d{0,3}m?\\d{1,3}s?)", getOriginalUrl());
        } catch (Parser.RegexException e) {
            // catch this instantly since an url does not necessarily have to have a time stamp

            // -2 because well the testing system will then know its the regex that failed :/
            // not good i know
            return -2;
        }

        if (!timeStamp.isEmpty()) {
            try {
                String secondsString = "";
                String minutesString = "";
                String hoursString = "";
                try {
                    secondsString = Parser.matchGroup1("(\\d{1,3})s", timeStamp);
                    minutesString = Parser.matchGroup1("(\\d{1,3})m", timeStamp);
                    hoursString = Parser.matchGroup1("(\\d{1,3})h", timeStamp);
                } catch (Exception e) {
                    //it could be that time is given in another method
                    if (secondsString.isEmpty() //if nothing was got,
                            && minutesString.isEmpty()//treat as unlabelled seconds
                            && hoursString.isEmpty()) {
                        secondsString = Parser.matchGroup1("t=(\\d+)", timeStamp);
                    }
                }

                int seconds = secondsString.isEmpty() ? 0 : Integer.parseInt(secondsString);
                int minutes = minutesString.isEmpty() ? 0 : Integer.parseInt(minutesString);
                int hours = hoursString.isEmpty() ? 0 : Integer.parseInt(hoursString);

                //don't trust BODMAS!
                return seconds + (60 * minutes) + (3600 * hours);
                //Log.d(TAG, "derived timestamp value:"+ret);
                //the ordering varies internationally
            } catch (ParsingException e) {
                throw new ParsingException("Could not get timestamp.", e);
            }
        } else {
            return 0;
        }
    }

    @Override
    public int getAgeLimit() {
        return 0;
    }

    @Override
    public String getAverageRating() {
        return null;
    }

    @Override
    public int getLikeCount() {
        return track.getInt("likes_count");
    }

    @Override
    public int getDislikeCount() {
        return 0;
    }

    @Override
    public StreamInfoItemExtractor getNextVideo() throws IOException, ExtractionException {
        return null;
    }

    @Override
    public StreamInfoItemCollector getRelatedVideos() throws IOException, ExtractionException {
        StreamInfoItemCollector collector = new StreamInfoItemCollector(getServiceId());
        Downloader dl = NewPipe.getDownloader();

        String apiUrl = "https://api-v2.soundcloud.com/tracks/" + getId() + "/related"
                + "?client_id=" + SoundcloudParsingHelper.clientId();

        String response = dl.download(apiUrl);
        JSONObject responseObject = new JSONObject(response);
        JSONArray responseCollection = responseObject.getJSONArray("collection");

        for (int i = 0; i < responseCollection.length(); i++) {
            JSONObject relatedVideo = responseCollection.getJSONObject(i);
            collector.commit(new SoundcloudStreamInfoItemExtractor(relatedVideo));
        }
        return collector;
    }

    @Override
    public String getUserUrl() {
        return track.getJSONObject("user").getString("permalink_url");
    }

    @Override
    public StreamType getStreamType() {
        return StreamType.AUDIO_STREAM;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }
}
