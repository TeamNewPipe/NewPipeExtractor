package org.schabi.newpipe.extractor.services.peertube.extractors;

import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor;

import javax.annotation.Nonnull;

public class PeertubePlaylistInfoItemExtractor implements PlaylistInfoItemExtractor  {

    final JsonObject item;
    final JsonObject uploader;
    final String baseUrl;

    public PeertubePlaylistInfoItemExtractor(@Nonnull final JsonObject item,
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
        return baseUrl + item.getString("thumbnailPath");
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return uploader.getString("displayName");
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        return uploader.getString("url");
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return false;
    }

    @Override
    public long getStreamCount() throws ParsingException {
        return item.getInt("videosLength");
    }
}
