package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nonnull;
import java.util.List;

import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getImagesFromImageId;

public class BandcampPlaylistInfoItemFeaturedExtractor implements PlaylistInfoItemExtractor {

    private final JsonObject featuredStory;

    public BandcampPlaylistInfoItemFeaturedExtractor(final JsonObject featuredStory) {
        this.featuredStory = featuredStory;
    }

    @Override
    public String getUploaderName() {
        return featuredStory.getString("band_name");
    }

    @Override
    public String getUploaderUrl() {
        return null;
    }

    @Override
    public boolean isUploaderVerified() {
        return false;
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
        return Utils.replaceHttpWithHttps(featuredStory.getString("item_url"));
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() {
        return featuredStory.has("art_id")
                ? getImagesFromImageId(featuredStory.getLong("art_id"), true)
                : getImagesFromImageId(featuredStory.getLong("item_art_id"), true);
    }
}
