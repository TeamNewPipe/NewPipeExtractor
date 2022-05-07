package org.schabi.newpipe.extractor.services.youtube.invidious.extractors;

import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.channel.ChannelInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousParsingHelper;

public class InvidiousChannelInfoItemExtractor implements ChannelInfoItemExtractor {

    private final String baseUrl;
    private final JsonObject json;

    public InvidiousChannelInfoItemExtractor(
            final JsonObject json,
            final String baseUrl
    ) {
        this.json = json;
        this.baseUrl = baseUrl;
    }

    @Override
    public String getName() {
        return json.getString("author");
    }

    @Override
    public String getUrl() {
        return baseUrl + json.getString("authorUrl");
    }

    @Override
    public String getThumbnailUrl() {
        return InvidiousParsingHelper.getThumbnailUrl(json.getArray("authorThumbnails"));
    }

    @Override
    public String getDescription() throws ParsingException {
        // Note: descriptionHtml is also available
        return json.getString("description");
    }

    @Override
    public long getSubscriberCount() throws ParsingException {
        return json.getLong("subCount");
    }

    @Override
    public long getStreamCount() throws ParsingException {
        return json.getLong("videoCount");
    }
}
