package org.schabi.newpipe.extractor.services.peertube.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.channel.ChannelInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.JsonUtils;

public class PeertubeChannelInfoItemExtractor implements ChannelInfoItemExtractor {

    protected final JsonObject item;
    private final String baseUrl;

    public PeertubeChannelInfoItemExtractor(final JsonObject item, final String baseUrl) {
        this.item = item;
        this.baseUrl = baseUrl;
    }

    @Override
    public String getUrl() throws ParsingException {
        return JsonUtils.getString(item, "url");
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        final JsonObject avatar = JsonUtils.getObject(item, "avatar");
        return baseUrl + JsonUtils.getString(avatar, "path");
    }

    @Override
    public String getName() throws ParsingException {
        return JsonUtils.getString(item, "displayName");
    }

    @Override
    public String getDescription() throws ParsingException {
        return item.getString("description", "");
    }

    @Override
    public long getSubscriberCount() throws ParsingException {
        return JsonUtils.getNumber(item, "followersCount").longValue();
    }

    @Override
    public long getStreamCount() throws ParsingException {
        return ListExtractor.ITEM_COUNT_UNKNOWN;
    }

    @Override
    public boolean isVerified() throws ParsingException {
        return false;
    }
}
