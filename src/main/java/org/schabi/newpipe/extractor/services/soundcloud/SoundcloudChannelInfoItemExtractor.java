package org.schabi.newpipe.extractor.services.soundcloud;

import com.github.openjson.JSONObject;
import org.schabi.newpipe.extractor.channel.ChannelInfoItemExtractor;

public class SoundcloudChannelInfoItemExtractor implements ChannelInfoItemExtractor {
    private JSONObject searchResult;

    public SoundcloudChannelInfoItemExtractor(JSONObject searchResult) {
        this.searchResult = searchResult;
    }

    @Override
    public String getName() {
        return searchResult.getString("username");
    }

    @Override
    public String getUrl() {
        return searchResult.getString("permalink_url");
    }

    @Override
    public String getThumbnailUrl() {
        return searchResult.optString("avatar_url");
    }

    @Override
    public long getSubscriberCount() {
        return searchResult.optLong("followers_count");
    }

    @Override
    public long getStreamCount() {
        return searchResult.optLong("track_count");
    }

    @Override
    public String getDescription() {
        return searchResult.optString("description");
    }
}
