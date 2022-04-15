package org.schabi.newpipe.extractor.services.youtube.invidious.extractors;

import static org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousParsingHelper.getUploadDateFromEpochTime;

import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousParsingHelper;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import javax.annotation.Nullable;


public class InvidiousStreamInfoItemExtractor implements StreamInfoItemExtractor {

    private final String baseUrl;
    private final JsonObject json;

    public InvidiousStreamInfoItemExtractor(
            final JsonObject json,
            final String baseUrl
    ) {
        this.json = json;
        this.baseUrl = baseUrl;
    }

    @Override
    public StreamType getStreamType() {
        if (json.getBoolean("liveNow")) {
            return StreamType.LIVE_STREAM;
        }
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public boolean isAd() throws ParsingException {
        return json.getBoolean("premium")
                || json.getBoolean("paid");
    }

    @Override
    public long getDuration() {
        final Number number = json.getNumber("lengthSeconds");
        return number == null ? -1 : number.longValue();
    }

    @Override
    public long getViewCount() {
        return json.getLong("viewCount", -1);
    }

    @Override
    public String getUploaderName() {
        return json.getString("author");
    }

    @Override
    public String getUploaderUrl() {
        final String authorUrl = json.getString("authorUrl");
        if (authorUrl != null) {
            return baseUrl + authorUrl;
        }
        return baseUrl + "/channel/" + json.getString("authorId");
    }

    @Nullable
    @Override
    public String getTextualUploadDate() {
        return json.getString("publishedText");
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() {
        final Number epochTime = json.getNumber("published");
        if (epochTime != null) {
            return getUploadDateFromEpochTime(epochTime.longValue());
        }

        return null;
    }

    @Override
    public String getName() {
        return json.getString("title");
    }

    @Override
    public String getUrl() throws ParsingException {
        return baseUrl + "/watch?v=" + json.getString("videoId");
    }

    @Override
    public String getThumbnailUrl() {
        return InvidiousParsingHelper.getThumbnailUrl(json.getArray("videoThumbnails"));
    }

}
