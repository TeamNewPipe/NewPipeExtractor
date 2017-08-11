package org.schabi.newpipe.extractor.services.soundcloud;

import com.github.openjson.JSONObject;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

public class SoundcloudStreamInfoItemExtractor implements StreamInfoItemExtractor {

    private final JSONObject searchResult;

    public SoundcloudStreamInfoItemExtractor(JSONObject searchResult) {
        this.searchResult = searchResult;
    }

    @Override
    public String getUrl() {
        return searchResult.getString("permalink_url");
    }

    @Override
    public String getName() {
        return searchResult.getString("title");
    }

    @Override
    public long getDuration() {
        return searchResult.getLong("duration") / 1000L;
    }

    @Override
    public String getUploaderName() {
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
        return searchResult.optString("artwork_url");
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
