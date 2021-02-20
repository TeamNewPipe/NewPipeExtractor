package org.schabi.newpipe.extractor.services.youtube.invidious.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousParsingHelper;

public class InvidiousPlaylistInfoItemExtractor implements PlaylistInfoItemExtractor {

    private final JsonObject json;
    private final String baseUrl;

    public InvidiousPlaylistInfoItemExtractor(final JsonObject json, final String baseUrl) {
        this.json = json;
        this.baseUrl = baseUrl;
    }

    @Override
    public String getName() {
        return json.getString("title");
    }

    @Override
    public String getUrl() {
        return baseUrl + "/playlist?list=" + json.getString("playlistId");
    }

    @Override
    public String getThumbnailUrl() {
        return InvidiousParsingHelper.getThumbnailUrl(json.getArray("videos").getObject(0).getArray("videoThumbnails"));
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return json.getString("author");
    }

    @Override
    public long getStreamCount() throws ParsingException {
        return json.getLong("videoCount");
    }
}
