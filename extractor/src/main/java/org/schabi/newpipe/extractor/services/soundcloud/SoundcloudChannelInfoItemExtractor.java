package org.schabi.newpipe.extractor.services.soundcloud;

import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.channel.ChannelInfoItemExtractor;

import java.util.ArrayList;
import java.util.List;

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
    public List<Image> getThumbnails() {
        List<Image> images = new ArrayList<>();

        String avatarUrl = itemObject.getString("avatar_url", "");

        images.add(new Image(avatarUrl, Image.LOW, Image.LOW));
        images.add(new Image(avatarUrl.replace("large.jpg", "crop.jpg"), Image.HIGH, Image.HIGH));

        return images;
    }

    @Override
    public long getSubscriberCount() {
        return itemObject.getNumber("followers_count", 0).longValue();
    }

    @Override
    public long getStreamCount() {
        return itemObject.getNumber("track_count", 0).longValue();
    }

    @Override
    public String getDescription() {
        return itemObject.getString("description", "");
    }
}
