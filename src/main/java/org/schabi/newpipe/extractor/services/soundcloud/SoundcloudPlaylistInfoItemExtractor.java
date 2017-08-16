package org.schabi.newpipe.extractor.services.soundcloud;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor;

public class SoundcloudPlaylistInfoItemExtractor implements PlaylistInfoItemExtractor {
    private static final String USER_KEY = "user";
    private static final String AVATAR_URL_KEY = "avatar_url";
    private static final String ARTWORK_URL_KEY = "artwork_url";
    private static final String NULL_VALUE = "null";

    private JSONObject searchResult;

    public SoundcloudPlaylistInfoItemExtractor(JSONObject searchResult) {
        this.searchResult = searchResult;
    }

    @Override
    public String getName() throws ParsingException {
        try {
            return searchResult.getString("title");
        } catch (Exception e) {
            throw new ParsingException("Failed to extract playlist name", e);
        }
    }

    @Override
    public String getUrl() throws ParsingException {
        try {
            return searchResult.getString("permalink_url");
        } catch (Exception e) {
            throw new ParsingException("Failed to extract playlist name", e);
        }
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        // Over-engineering at its finest
        try {
            final String artworkUrl = searchResult.optString(ARTWORK_URL_KEY);
            if (!artworkUrl.isEmpty() && !artworkUrl.equals(NULL_VALUE)) return artworkUrl;

            // Look for artwork url inside the track list
            final JSONArray tracks = searchResult.optJSONArray("tracks");
            if (tracks == null) return null;
            for (int i = 0; i < tracks.length(); i++) {
                if (tracks.isNull(i)) continue;
                final JSONObject track = tracks.optJSONObject(i);
                if (track == null) continue;

                // First look for track artwork url
                final String url = track.optString(ARTWORK_URL_KEY);
                if (!url.isEmpty() && !url.equals(NULL_VALUE)) return url;

                // Then look for track creator avatar url
                final JSONObject creator = track.getJSONObject(USER_KEY);
                final String creatorAvatar = creator.optString(AVATAR_URL_KEY);
                if (!creatorAvatar.isEmpty() && !creatorAvatar.equals(NULL_VALUE)) return creatorAvatar;
            }

            // Last resort, use user avatar url. If still not found, then throw exception.
            final JSONObject user = searchResult.getJSONObject(USER_KEY);
            return user.getString(AVATAR_URL_KEY);
        } catch (Exception e) {
            throw new ParsingException("Failed to extract playlist thumbnail url", e);
        }
    }

    @Override
    public String getUploaderName() throws ParsingException {
        try {
            final JSONObject user = searchResult.getJSONObject(USER_KEY);
            return user.optString("username");
        } catch (Exception e) {
            throw new ParsingException("Failed to extract playlist uploader", e);
        }
    }

    @Override
    public long getStreamCount() throws ParsingException {
        try {
            return Long.parseLong(searchResult.optString("track_count"));
        } catch (Exception e) {
            throw new ParsingException("Failed to extract playlist stream count", e);
        }
    }
}
