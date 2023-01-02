package org.schabi.newpipe.extractor.services.peertube.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.channel.ChannelInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import javax.annotation.Nonnull;
import java.util.Comparator;

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
        return item.getArray("avatars").stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .max(Comparator.comparingInt(avatar -> avatar.getInt("width")))
                .map(avatar -> baseUrl + avatar.getString("path"))
                .orElse(null);
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
