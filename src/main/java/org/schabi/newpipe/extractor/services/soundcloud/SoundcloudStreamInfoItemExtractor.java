package org.schabi.newpipe.extractor.services.soundcloud;

import org.json.JSONObject;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

public class SoundcloudStreamInfoItemExtractor implements StreamInfoItemExtractor {

    private final JSONObject searchResult;

    public SoundcloudStreamInfoItemExtractor(JSONObject searchResult) {
        this.searchResult = searchResult;
    }

    @Override
    public String getWebPageUrl() {
        return searchResult.getString("permalink_url");
    }

    @Override
    public String getTitle() {
        return searchResult.getString("title");
    }

    @Override
    public int getDuration() {
        return searchResult.getInt("duration") / 1000;
    }

    @Override
    public String getUploader() {
        return searchResult.getJSONObject("user").getString("username");
    }

    @Override
    public String getUploadDate() throws ParsingException {
        return SoundcloudParsingHelper.toDateString(searchResult.getString("created_at"));
    }

    @Override
    public long getViewCount() {
        return searchResult.getLong("playback_count");
    }

    @Override
    public String getThumbnailUrl() {
        return searchResult.getString("artwork_url");
    }

    @Override
    public StreamType getStreamType() {
        return StreamType.AUDIO_STREAM;
    }

    @Override
    public boolean isAd() {
        return false;
    }
}
