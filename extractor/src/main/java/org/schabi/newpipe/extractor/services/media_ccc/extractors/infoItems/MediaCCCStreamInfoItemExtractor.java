package org.schabi.newpipe.extractor.services.media_ccc.extractors.infoItems;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

public class MediaCCCStreamInfoItemExtractor implements StreamInfoItemExtractor {

    JsonObject event;

    public MediaCCCStreamInfoItemExtractor(JsonObject event) {
        this.event = event;
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public boolean isAd() throws ParsingException {
        return false;
    }

    @Override
    public long getDuration() throws ParsingException {
        return event.getInt("length");
    }

    @Override
    public long getViewCount() throws ParsingException {
        return event.getInt("view_count");
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return event.getString("conference_url")
                .replace("https://api.media.ccc.de/public/conferences/", "");
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        return event.getString("conference_url");
    }

    @Override
    public String getUploadDate() throws ParsingException {
        return event.getString("release_date");
    }

    @Override
    public String getName() throws ParsingException {
        return event.getString("title");
    }

    @Override
    public String getUrl() throws ParsingException {
        return "https://api.media.ccc.de/public/events/" +
                event.getString("guid");
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return event.getString("thumb_url");
    }
}
