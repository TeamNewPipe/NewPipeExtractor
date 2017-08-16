package org.schabi.newpipe.extractor.services.soundcloud;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.channel.ChannelInfoItemExtractor;

public class SoundcloudChannelInfoItemExtractor implements ChannelInfoItemExtractor {
    private JsonObject searchResult;

    public SoundcloudChannelInfoItemExtractor(JsonObject searchResult) {
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
        return searchResult.getString("avatar_url", "");
    }

    @Override
    public long getSubscriberCount() {
        return searchResult.getNumber("followers_count", 0).longValue();
    }

    @Override
    public long getStreamCount() {
        return searchResult.getNumber("track_count", 0).longValue();
    }

    @Override
    public String getDescription() {
        return searchResult.getString("description", "");
    }
}
