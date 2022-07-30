// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;

import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.BASE_URL;
import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getImagesFromImageId;
import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.parseDate;

public class BandcampRadioInfoItemExtractor implements StreamInfoItemExtractor {

    private final JsonObject show;

    public BandcampRadioInfoItemExtractor(final JsonObject radioShow) {
        show = radioShow;
    }

    @Override
    public long getDuration() {
        /* Duration is only present in the more detailed information that has to be queried
        separately. Therefore, over 300 queries would be needed every time the kiosk is opened if we
        were to display the real value. */
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
        return parseDate(getTextualUploadDate());
    }

    @Override
    public String getName() throws ParsingException {
        return show.getString("subtitle");
    }

    @Override
    public String getUrl() {
        return BASE_URL + "/?show=" + show.getInt("id");
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() {
        return getImagesFromImageId(show.getLong("image_id"), false);
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
