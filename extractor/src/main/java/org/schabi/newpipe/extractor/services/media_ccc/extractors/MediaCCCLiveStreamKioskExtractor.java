package org.schabi.newpipe.extractor.services.media_ccc.extractors;

import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;

import javax.annotation.Nullable;

public class MediaCCCLiveStreamKioskExtractor implements StreamInfoItemExtractor {

    private final JsonObject conferenceInfo;
    private final String group;
    private final JsonObject roomInfo;

    public MediaCCCLiveStreamKioskExtractor(final JsonObject conferenceInfo,
                                            final String group,
                                            final JsonObject roomInfo) {
        this.conferenceInfo = conferenceInfo;
        this.group = group;
        this.roomInfo = roomInfo;
    }

    @Override
    public String getName() throws ParsingException {
        return roomInfo.getObject("talks").getObject("current").getString("title");
    }

    @Override
    public String getUrl() throws ParsingException {
        return roomInfo.getString("link");
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return roomInfo.getString("thumb");
    }

    @Override
    public boolean isLive() {
        return true;
    }

    @Override
    public boolean isAudioOnly() {
        return roomInfo.getArray("streams").stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .noneMatch(s -> "video".equalsIgnoreCase(s.getString("type")));
    }

    @Override
    public boolean isAd() throws ParsingException {
        return false;
    }

    @Override
    public long getDuration() throws ParsingException {
        return 0;
    }

    @Override
    public long getViewCount() throws ParsingException {
        return -1;
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return conferenceInfo.getString("conference") + " - " + group
                + " - " + roomInfo.getString("display");
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        return "https://media.ccc.de/c/" + conferenceInfo.getString("slug");
    }

    @Nullable
    @Override
    public String getUploaderAvatarUrl() {
        return null;
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return false;
    }

    @Nullable
    @Override
    public String getTextualUploadDate() throws ParsingException {
        return null;
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return null;
    }
}
