package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor;

public class BandcampAlbumInfoItemExtractor implements PlaylistInfoItemExtractor {
    private final JsonObject albumInfoItem;

    public BandcampAlbumInfoItemExtractor(final JsonObject albumInfoItem) {
        this.albumInfoItem = albumInfoItem;
    }

    @Override
    public String getName() throws ParsingException {
        return albumInfoItem.getString("title");
    }

    @Override
    public String getUrl() throws ParsingException {
        return BandcampExtractorHelper.getStreamUrlFromIds(
                albumInfoItem.getLong("band_id"),
                albumInfoItem.getLong("item_id"),
                albumInfoItem.getString("item_type")
        );
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return BandcampExtractorHelper.getImageUrl(albumInfoItem.getLong("art_id"), true
        );
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return albumInfoItem.getString("band_name");
    }

    @Override
    public long getStreamCount() throws ParsingException {
        return -1;
    }
}
