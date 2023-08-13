package org.schabi.newpipe.extractor.services.soundcloud.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor;

import javax.annotation.Nonnull;
import java.util.List;

import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.getAllImagesFromArtworkOrAvatarUrl;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;
import static org.schabi.newpipe.extractor.utils.Utils.replaceHttpWithHttps;

public class SoundcloudPlaylistInfoItemExtractor implements PlaylistInfoItemExtractor {
    private static final String USER_KEY = "user";
    private static final String AVATAR_URL_KEY = "avatar_url";
    private static final String ARTWORK_URL_KEY = "artwork_url";

    private final JsonObject itemObject;

    public SoundcloudPlaylistInfoItemExtractor(final JsonObject itemObject) {
        this.itemObject = itemObject;
    }

    @Override
    public String getName() {
        return itemObject.getString("title");
    }

    @Override
    public String getUrl() {
        return replaceHttpWithHttps(itemObject.getString("permalink_url"));
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() throws ParsingException {
        // Over-engineering at its finest
        if (itemObject.isString(ARTWORK_URL_KEY)) {
            final String artworkUrl = itemObject.getString(ARTWORK_URL_KEY);
            if (!isNullOrEmpty(artworkUrl)) {
                return getAllImagesFromArtworkOrAvatarUrl(artworkUrl);
            }
        }

        try {
            // Look for artwork URL inside the track list
            for (final Object track : itemObject.getArray("tracks")) {
                final JsonObject trackObject = (JsonObject) track;

                // First look for track artwork URL
                if (trackObject.isString(ARTWORK_URL_KEY)) {
                    final String artworkUrl = trackObject.getString(ARTWORK_URL_KEY);
                    if (!isNullOrEmpty(artworkUrl)) {
                        return getAllImagesFromArtworkOrAvatarUrl(artworkUrl);
                    }
                }

                // Then look for track creator avatar URL
                final JsonObject creator = trackObject.getObject(USER_KEY);
                final String creatorAvatar = creator.getString(AVATAR_URL_KEY);
                if (!isNullOrEmpty(creatorAvatar)) {
                    return getAllImagesFromArtworkOrAvatarUrl(creatorAvatar);
                }
            }
        } catch (final Exception ignored) {
            // Try other method
        }

        try {
            // Last resort, use user avatar URL. If still not found, then throw an exception.
            return getAllImagesFromArtworkOrAvatarUrl(
                    itemObject.getObject(USER_KEY).getString(AVATAR_URL_KEY));
        } catch (final Exception e) {
            throw new ParsingException("Failed to extract playlist thumbnails", e);
        }
    }

    @Override
    public String getUploaderName() throws ParsingException {
        try {
            return itemObject.getObject(USER_KEY).getString("username");
        } catch (final Exception e) {
            throw new ParsingException("Failed to extract playlist uploader", e);
        }
    }

    @Override
    public String getUploaderUrl() {
        return itemObject.getObject(USER_KEY).getString("permalink_url");
    }

    @Override
    public boolean isUploaderVerified() {
        return itemObject.getObject(USER_KEY).getBoolean("verified");
    }

    @Override
    public long getStreamCount() {
        return itemObject.getLong("track_count");
    }
}
