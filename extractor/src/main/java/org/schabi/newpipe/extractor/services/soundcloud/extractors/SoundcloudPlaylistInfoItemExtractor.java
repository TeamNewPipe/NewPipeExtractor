package org.schabi.newpipe.extractor.services.soundcloud.extractors;

import static org.schabi.newpipe.extractor.utils.Utils.replaceHttpWithHttps;

import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor;

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

    @Override
    public String getThumbnailUrl() throws ParsingException {
        // Over-engineering at its finest
        if (itemObject.isString(ARTWORK_URL_KEY)) {
            final String artworkUrl = itemObject.getString(ARTWORK_URL_KEY, "");
            if (!artworkUrl.isEmpty()) {
                // An artwork URL with a better resolution
                return artworkUrl.replace("large.jpg", "crop.jpg");
            }
        }

        try {
            // Look for artwork url inside the track list
            for (final Object track : itemObject.getArray("tracks")) {
                final JsonObject trackObject = (JsonObject) track;

                // First look for track artwork url
                if (trackObject.isString(ARTWORK_URL_KEY)) {
                    final String artworkUrl = trackObject.getString(ARTWORK_URL_KEY, "");
                    if (!artworkUrl.isEmpty()) {
                        // An artwork URL with a better resolution
                        return artworkUrl.replace("large.jpg", "crop.jpg");
                    }
                }

                // Then look for track creator avatar url
                final JsonObject creator = trackObject.getObject(USER_KEY);
                final String creatorAvatar = creator.getString(AVATAR_URL_KEY, "");
                if (!creatorAvatar.isEmpty()) {
                    return creatorAvatar;
                }
            }
        } catch (final Exception ignored) {
            // Try other method
        }

        try {
            // Last resort, use user avatar url. If still not found, then throw exception.
            return itemObject.getObject(USER_KEY).getString(AVATAR_URL_KEY, "");
        } catch (final Exception e) {
            throw new ParsingException("Failed to extract playlist thumbnail url", e);
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
