package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor;

import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getImageUrl;

public class BandcampPlaylistInfoItemFeaturedExtractor implements PlaylistInfoItemExtractor {

    private final JsonObject featuredStory;

    public BandcampPlaylistInfoItemFeaturedExtractor(JsonObject featuredStory) {
        this.featuredStory = featuredStory;
    }

    @Override
    public String getUploaderName() {
        return featuredStory.getString("band_name");
    }

    @Override
    public long getStreamCount() {
        return featuredStory.getInt("num_streamable_tracks");
    }

    @Override
    public String getName() {
        return featuredStory.getString("album_title");
    }

    @Override
    public String getUrl() {
        return featuredStory.getString("item_url");
    }

    @Override
    public String getThumbnailUrl() {
        return featuredStory.has("art_id") ? getImageUrl(featuredStory.getLong("art_id"), true) : "";
    }
}