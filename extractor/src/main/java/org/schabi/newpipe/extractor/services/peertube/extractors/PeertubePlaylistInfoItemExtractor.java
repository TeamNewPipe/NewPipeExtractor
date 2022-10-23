package org.schabi.newpipe.extractor.services.peertube.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistInfo;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor;
import org.schabi.newpipe.extractor.utils.JsonUtils;

import javax.annotation.Nonnull;

public class PeertubePlaylistInfoItemExtractor implements PlaylistInfoItemExtractor {

    protected final JsonObject item;
    private final String baseUrl;

    public PeertubePlaylistInfoItemExtractor(final JsonObject item, final String baseUrl) {
        this.item = item;
        this.baseUrl = baseUrl;
    }

    @Override
    public String getUrl() throws ParsingException {
        final String uuid = JsonUtils.getString(item, "shortUUID");
        return baseUrl + "/w/p/" + uuid;
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return baseUrl + JsonUtils.getString(item, "thumbnailPath");
    }

    @Override
    public String getName() throws ParsingException {
        return JsonUtils.getString(item, "displayName");
    }

    @Override
    public String getUploaderName() throws ParsingException {
        final JsonObject owner = JsonUtils.getObject(item, "ownerAccount");
        return JsonUtils.getString(owner, "displayName");
    }

    @Override
    public long getStreamCount() throws ParsingException {
        return JsonUtils.getNumber(item, "videosLength").longValue();
    }

    @Nonnull
    @Override
    public PlaylistInfo.PlaylistType getPlaylistType() throws ParsingException {
        return PlaylistInfoItemExtractor.super.getPlaylistType();
    }
}
