package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor;

public class BandcampAlbumInfoItemExtractor implements PlaylistInfoItemExtractor {
    private final JsonObject albumInfoItem;
    private final String uploaderUrl;

    public BandcampAlbumInfoItemExtractor(final JsonObject albumInfoItem,
                                          final String uploaderUrl) {
        this.albumInfoItem = albumInfoItem;
        this.uploaderUrl = uploaderUrl;
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
        return BandcampExtractorHelper.getImageUrl(albumInfoItem.getLong("art_id"), true);
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return albumInfoItem.getString("band_name");
    }

    @Override
    public String getUploaderUrl() {
        return uploaderUrl;
    }

    @Override
    public boolean isUploaderVerified() {
        return false;
    }

    @Override
    public long getStreamCount() {
        return -1;
    }
}
