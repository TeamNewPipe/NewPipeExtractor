package org.schabi.newpipe.extractor.services.soundcloud.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.channel.ChannelInfoItemExtractor;

import static org.schabi.newpipe.extractor.utils.JsonUtils.EMPTY_STRING;
import static org.schabi.newpipe.extractor.utils.Utils.replaceHttpWithHttps;

public class SoundcloudChannelInfoItemExtractor implements ChannelInfoItemExtractor {
    private final JsonObject itemObject;

    public SoundcloudChannelInfoItemExtractor(JsonObject itemObject) {
        this.itemObject = itemObject;
    }

    @Override
    public String getName() {
        return itemObject.getString("username");
    }

    @Override
    public String getUrl() {
        return replaceHttpWithHttps(itemObject.getString("permalink_url"));
    }

    @Override
    public String getThumbnailUrl() {
        String avatarUrl = itemObject.getString("avatar_url", EMPTY_STRING);
        String avatarUrlBetterResolution = avatarUrl.replace("large.jpg", "crop.jpg");
        return avatarUrlBetterResolution;
    }

    @Override
    public long getSubscriberCount() {
        return itemObject.getLong("followers_count");
    }

    @Override
    public long getStreamCount() {
        return itemObject.getLong("track_count");
    }

    @Override
    public String getDescription() {
        return itemObject.getString("description", EMPTY_STRING);
    }
}
