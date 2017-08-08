package org.schabi.newpipe.extractor.services.soundcloud;

import org.json.JSONObject;
import org.schabi.newpipe.extractor.user.UserInfoItemExtractor;

public class SoundcloudUserInfoItemExtractor implements UserInfoItemExtractor {
    private JSONObject searchResult;

    public SoundcloudUserInfoItemExtractor(JSONObject searchResult) {
        this.searchResult = searchResult;
    }

    @Override
    public String getThumbnailUrl() {
        return searchResult.optString("avatar_url");
    }

    @Override
    public String getUserName() {
        return searchResult.getString("username");
    }

    @Override
    public String getWebPageUrl() {
        return searchResult.getString("permalink_url");
    }

    @Override
    public long getSubscriberCount() {
        return searchResult.getLong("followers_count");
    }

    @Override
    public long getStreamCount() {
        return searchResult.getLong("track_count");
    }

    @Override
    public String getDescription() {
        return searchResult.optString("description");
    }
}
