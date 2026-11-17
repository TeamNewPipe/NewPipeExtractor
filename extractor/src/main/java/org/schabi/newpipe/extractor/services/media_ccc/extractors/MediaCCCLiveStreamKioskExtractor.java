package org.schabi.newpipe.extractor.services.media_ccc.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.List;

import static org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCParsingHelper.getThumbnailsFromLiveStreamItem;

public class MediaCCCLiveStreamKioskExtractor implements StreamInfoItemExtractor {

    private final JsonObject conferenceInfo;
    private final String group;
    private final JsonObject roomInfo;

    @Nonnull
    private final JsonObject currentTalk;

    public MediaCCCLiveStreamKioskExtractor(final JsonObject conferenceInfo,
                                            final String group,
                                            final JsonObject roomInfo) {
        this.conferenceInfo = conferenceInfo;
        this.group = group;
        this.roomInfo = roomInfo;
        this.currentTalk = roomInfo.getObject("talks").getObject("current");
    }

    @Override
    public String getName() throws ParsingException {
        if (isBreak()) {
            return roomInfo.getString("display") + " - Pause";
        } else {
            return currentTalk.getString("title");
        }
    }

    @Override
    public String getUrl() throws ParsingException {
        return roomInfo.getString("link");
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() throws ParsingException {
        return getThumbnailsFromLiveStreamItem(roomInfo);
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        boolean isVideo = false;
        for (final Object stream : roomInfo.getArray("streams")) {
            if ("video".equals(((JsonObject) stream).getString("type"))) {
                isVideo = true;
                break;
            }
        }
        return isVideo ? StreamType.LIVE_STREAM :  StreamType.AUDIO_LIVE_STREAM;
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
        if (isBreak()) {
            return new DateWrapper(OffsetDateTime.parse(currentTalk.getString("fstart")));
        } else {
            return new DateWrapper(OffsetDateTime.parse(conferenceInfo.getString("startsAt")));
        }
    }

    /**
     * Whether the current "talk" is a talk or a pause.
     */
    private boolean isBreak() {
        return OffsetDateTime.parse(currentTalk.getString("fstart")).isBefore(OffsetDateTime.now())
                || "gap".equals(currentTalk.getString("special"));
    }
}
