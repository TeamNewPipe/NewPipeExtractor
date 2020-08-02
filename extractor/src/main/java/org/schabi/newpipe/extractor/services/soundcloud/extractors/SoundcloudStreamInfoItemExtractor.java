package org.schabi.newpipe.extractor.services.soundcloud.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import static org.schabi.newpipe.extractor.utils.JsonUtils.EMPTY_STRING;
import static org.schabi.newpipe.extractor.utils.Utils.replaceHttpWithHttps;

public class SoundcloudStreamInfoItemExtractor implements StreamInfoItemExtractor {

    protected final JsonObject itemObject;

    public SoundcloudStreamInfoItemExtractor(JsonObject itemObject) {
        this.itemObject = itemObject;
    }

    @Override
    public String getUrl() {
        return replaceHttpWithHttps(itemObject.getString("permalink_url"));
    }

    @Override
    public String getName() {
        return itemObject.getString("title");
    }

    @Override
    public long getDuration() {
        return itemObject.getLong("duration") / 1000L;
    }

    @Override
    public String getUploaderName() {
        return itemObject.getObject("user").getString("username");
    }

    @Override
    public String getUploaderUrl() {
        return replaceHttpWithHttps(itemObject.getObject("user").getString("permalink_url"));
    }

    @Override
    public String getTextualUploadDate() {
        return itemObject.getString("created_at");
    }

    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return new DateWrapper(SoundcloudParsingHelper.parseDateFrom(getTextualUploadDate()));
    }

    @Override
    public long getViewCount() {
        return itemObject.getLong("playback_count");
    }

    @Override
    public String getThumbnailUrl() {
        String artworkUrl = itemObject.getString("artwork_url", EMPTY_STRING);
        if (artworkUrl.isEmpty()) {
            artworkUrl = itemObject.getObject("user").getString("avatar_url");
        }
        String artworkUrlBetterResolution = artworkUrl.replace("large.jpg", "crop.jpg");
        return artworkUrlBetterResolution;
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
