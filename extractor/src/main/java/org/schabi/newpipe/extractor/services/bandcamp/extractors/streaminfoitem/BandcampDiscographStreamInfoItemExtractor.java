package org.schabi.newpipe.extractor.services.bandcamp.extractors.streaminfoitem;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper;

import javax.annotation.Nullable;

public class BandcampDiscographStreamInfoItemExtractor extends BandcampStreamInfoItemExtractor {

    private final JsonObject discograph;
    public BandcampDiscographStreamInfoItemExtractor(final JsonObject discograph,
                                                     final String uploaderUrl) {
        super(uploaderUrl);
        this.discograph = discograph;
    }

    @Override
    public String getUploaderName() {
        return discograph.getString("band_name");
    }

    @Nullable
    @Override
    public String getUploaderAvatarUrl() {
        return null;
    }

    @Override
    public String getName() {
        return discograph.getString("title");
    }

    @Override
    public String getUrl() throws ParsingException {
        return BandcampExtractorHelper.getStreamUrlFromIds(
                discograph.getLong("band_id"),
                discograph.getLong("item_id"),
                discograph.getString("item_type")
        );
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return BandcampExtractorHelper.getImageUrl(
                discograph.getLong("art_id"), true
        );
    }

    @Override
    public long getDuration() {
        return -1;
    }
}
