package org.schabi.newpipe.extractor.services.media_ccc.extractors.infoItems;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCParsingHelper;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import javax.annotation.Nullable;

public class MediaCCCStreamInfoItemExtractor implements StreamInfoItemExtractor {
    private JsonObject event;

    public MediaCCCStreamInfoItemExtractor(final JsonObject event) {
        this.event = event;
    }

    @Override
    public StreamType getStreamType() {
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public boolean isAd() {
        return false;
    }

    @Override
    public long getDuration() {
        return event.getInt("length");
    }

    @Override
    public long getViewCount() {
        return event.getInt("view_count");
    }

    @Override
    public String getUploaderName() {
        return event.getString("conference_url")
                .replaceFirst("https://(api\\.)?media\\.ccc\\.de/public/conferences/", "");
    }

    @Override
    public String getUploaderUrl() {
        return event.getString("conference_url");
    }

    @Nullable
    @Override
    public String getTextualUploadDate() {
        return event.getString("release_date");
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return new DateWrapper(MediaCCCParsingHelper.parseDateFrom(getTextualUploadDate()));
    }

    @Override
    public String getName() throws ParsingException {
        return event.getString("title");
    }

    @Override
    public String getUrl() throws ParsingException {
        return "https://media.ccc.de/public/events/"
                + event.getString("guid");
    }

    @Override
    public String getThumbnailUrl() {
        return event.getString("thumb_url");
    }
}
