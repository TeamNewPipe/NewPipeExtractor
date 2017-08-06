package org.schabi.newpipe.extractor.services.soundcloud;

import org.json.JSONObject;
import org.schabi.newpipe.extractor.channel.ChannelInfoItemExtractor;

public class SoundcloudChannelInfoItemExtractor implements ChannelInfoItemExtractor {
    private JSONObject searchResult;

    public SoundcloudChannelInfoItemExtractor(JSONObject searchResult) {
        this.searchResult = searchResult;
    }

    @Override
    public String getThumbnailUrl() {
        return searchResult.getString("avatar_url");
    }

    @Override
    public String getChannelName() {
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
        return searchResult.getString("description");
    }
}
