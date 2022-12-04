package org.schabi.newpipe.extractor.services.peertube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.channel.ChannelInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import javax.annotation.Nonnull;

public class PeertubeChannelInfoItemExtractor implements ChannelInfoItemExtractor {

    final JsonObject item;
    final JsonObject uploader;
    final String baseUrl;
    public PeertubeChannelInfoItemExtractor(@Nonnull final JsonObject item,
                                            @Nonnull final String baseUrl) {
        this.item = item;
        this.uploader = item.getObject("uploader");
        this.baseUrl = baseUrl;
    }

    @Override
    public String getName() throws ParsingException {
        return item.getString("displayName");
    }

    @Override
    public String getUrl() throws ParsingException {
        return item.getString("url");
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        final JsonArray avatars = item.getArray("avatars");
        if (avatars.isEmpty()) {
            return null;
        }
        int highestRes = -1;
        JsonObject avatar = null;
        for (final Object a: avatars) {
            if (((JsonObject) a).getInt("width") > highestRes) {
                avatar = (JsonObject) a;
                highestRes = avatar.getInt("width");
            }
        }
        return baseUrl + avatar.getString("path");
    }

    @Override
    public String getDescription() throws ParsingException {
        return item.getString("description");
    }

    @Override
    public long getSubscriberCount() throws ParsingException {
        return item.getInt("followersCount");
    }

    @Override
    public long getStreamCount() throws ParsingException {
        return ChannelExtractor.ITEM_COUNT_UNKNOWN;
    }

    @Override
    public boolean isVerified() throws ParsingException {
        return false;
    }
}
