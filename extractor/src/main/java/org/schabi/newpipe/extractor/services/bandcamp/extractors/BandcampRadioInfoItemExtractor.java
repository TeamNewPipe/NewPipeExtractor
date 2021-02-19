// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import javax.annotation.Nullable;

import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getImageUrl;

public class BandcampRadioInfoItemExtractor implements StreamInfoItemExtractor {

    private final JsonObject show;

    public BandcampRadioInfoItemExtractor(final JsonObject radioShow) {
        show = radioShow;
    }

    @Override
    public long getDuration() {
        /* Duration is only present in the more detailed information that has to be queried separately.
         * Because the servers would probably not like over 300 queries every time someone opens the kiosk,
         * we're just providing 0 here.
         */
        //return query(show.getInt("id")).getLong("audio_duration");
        return 0;
    }

    @Nullable
    @Override
    public String getTextualUploadDate() {
        return show.getString("date");
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return BandcampExtractorHelper.parseDate(getTextualUploadDate());
    }

    @Override
    public String getName() throws ParsingException {
        return show.getString("subtitle");
    }

    @Override
    public String getUrl() {
        return "https://bandcamp.com/?show=" + show.getInt("id");
    }

    @Override
    public String getThumbnailUrl() {
        return getImageUrl(show.getLong("image_id"), false);
    }

    @Override
    public StreamType getStreamType() {
        return StreamType.AUDIO_STREAM;
    }

    @Override
    public long getViewCount() {
        return -1;
    }

    @Override
    public String getUploaderName() {
        // JSON does not contain uploader name
        return "";
    }

    @Override
    public String getUploaderUrl() {
        return "";
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return false;
    }

    @Override
    public boolean isAd() {
        return false;
    }
}
