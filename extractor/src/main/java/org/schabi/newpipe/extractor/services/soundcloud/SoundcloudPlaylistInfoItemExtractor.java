package org.schabi.newpipe.extractor.services.soundcloud;

import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor;

import java.util.ArrayList;
import java.util.List;

import static org.schabi.newpipe.extractor.utils.Utils.replaceHttpWithHttps;

public class SoundcloudPlaylistInfoItemExtractor implements PlaylistInfoItemExtractor {
    private static final String USER_KEY = "user";
    private static final String AVATAR_URL_KEY = "avatar_url";
    private static final String ARTWORK_URL_KEY = "artwork_url";

    private final JsonObject itemObject;

    public SoundcloudPlaylistInfoItemExtractor(JsonObject itemObject) {
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
    public List<Image> getThumbnails() throws ParsingException {
        List<Image> images = new ArrayList<>();

        // Over-engineering at its finest
        if (itemObject.isString(ARTWORK_URL_KEY)) {
            final String artworkUrl = itemObject.getString(ARTWORK_URL_KEY, "");
            if (!artworkUrl.isEmpty()) {
                String artworkUrlBetterResolution = artworkUrl.replace("large.jpg", "crop.jpg");
                images.add(new Image(artworkUrlBetterResolution, -1, -1));
                return images;
            }
        }

        try {
            // Look for artwork url inside the track list
            for (Object track : itemObject.getArray("tracks")) {
                final JsonObject trackObject = (JsonObject) track;

                // First look for track artwork url
                if (trackObject.isString(ARTWORK_URL_KEY)) {
                    String artworkUrl = trackObject.getString(ARTWORK_URL_KEY, "");
                    if (!artworkUrl.isEmpty()) {
                        String artworkUrlBetterResolution = artworkUrl.replace("large.jpg", "crop.jpg");
                        images.add(new Image(artworkUrlBetterResolution, -1, -1));
                        return images;
                    }
                }

                // Then look for track creator avatar url
                final JsonObject creator = trackObject.getObject(USER_KEY, new JsonObject());
                final String creatorAvatar = creator.getString(AVATAR_URL_KEY, "");
                if (!creatorAvatar.isEmpty()) {
                    images.add(new Image(creatorAvatar, -1, -1));
                    return images;
                }
            }
        } catch (Exception ignored) {
            // Try other method
        }

        try {
            // Last resort, use user avatar url. If still not found, then throw exception.
            images.add(new Image(itemObject.getObject(USER_KEY).getString(AVATAR_URL_KEY, ""), -1, -1));
            return images;
        } catch (Exception e) {
            throw new ParsingException("Failed to extract playlist thumbnail url", e);
        }
    }

    @Override
    public String getUploaderName() throws ParsingException {
        try {
            return itemObject.getObject(USER_KEY).getString("username");
        } catch (Exception e) {
            throw new ParsingException("Failed to extract playlist uploader", e);
        }
    }

    @Override
    public long getStreamCount() {
        return itemObject.getNumber("track_count", 0).longValue();
    }
}
