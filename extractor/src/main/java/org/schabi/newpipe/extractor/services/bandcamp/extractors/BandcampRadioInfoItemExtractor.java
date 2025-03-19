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
import java.time.Duration;
import java.util.List;

import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.BASE_URL;
import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getImagesFromImageId;
import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.parseDate;

public class BandcampRadioInfoItemExtractor implements StreamInfoItemExtractor {

    private final JsonObject show;

    public BandcampRadioInfoItemExtractor(final JsonObject radioShow) {
        show = radioShow;
    }

    @Nonnull
    @Override
    public Duration getDurationObject() {
        /* Duration is only present in the more detailed information that has to be queried
        separately. Therefore, over 300 queries would be needed every time the kiosk is opened if we
        were to display the real value. */
        //return Duration.ofSeconds(query(show.getInt("id")).getLong("audio_duration"));
        return Duration.ZERO;
    }

    @Nullable
    @Override
    public String getShortDescription() {
        return show.getString("desc");
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
        // The "title" field contains the title of the series, e.g. "Bandcamp Weekly".
        return show.getString("title");
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
