package org.schabi.newpipe.extractor.services.peertube.extractors;

import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.Description;

import javax.annotation.Nonnull;

import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

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

    @Nonnull
    @Override
    public Description getDescription() throws ParsingException {
        final String description = item.getString("description");
        if (isNullOrEmpty(description)) {
            return Description.EMPTY_DESCRIPTION;
        }
        return new Description(description, Description.PLAIN_TEXT);
    }
}
