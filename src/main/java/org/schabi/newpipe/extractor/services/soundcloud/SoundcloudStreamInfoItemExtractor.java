package org.schabi.newpipe.extractor.services.soundcloud;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

public class SoundcloudStreamInfoItemExtractor implements StreamInfoItemExtractor {

    private final JsonObject searchResult;

    public SoundcloudStreamInfoItemExtractor(JsonObject searchResult) {
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
        return searchResult.getNumber("duration", 0).longValue() / 1000L;
    }

    @Override
    public String getUploaderName() {
        //return searchResult.getObject("user").getString("username");
        return searchResult.getObject("track").getObject("user").getString("username");
    }

    @Override
    public String getUploadDate() throws ParsingException {
        return SoundcloudParsingHelper.toDateString(searchResult.getObject("track").getString("created_at"));
    }

    @Override
    public long getViewCount() {
        return searchResult.getNumber("playback_count", 0).longValue();
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
