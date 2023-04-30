package org.schabi.newpipe.extractor.services.youtube.extractors;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubePlaylistLinkHandlerFactory;

import com.grack.nanojson.JsonObject;

public class GridPlaylistRendererExtractor implements PlaylistInfoItemExtractor {

    private final JsonObject playlistInfoItem;

    GridPlaylistRendererExtractor(final JsonObject playlistInfoItem) {
        this.playlistInfoItem = playlistInfoItem;
    }

    @Override
    public String getName() throws ParsingException {
        return playlistInfoItem.getObject("title").getArray("runs").getObject(0).getString("text");
    }

    @Override
    public String getUrl() throws ParsingException {
        try {
            final String id = playlistInfoItem.getString("playlistId");
            return YoutubePlaylistLinkHandlerFactory.getInstance().getUrl(id);
        } catch (final Exception e) {
            throw new ParsingException("Could not get url", e);
        }
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return playlistInfoItem.getObject("thumbnailRenderer")
                .getObject("playlistVideoThumbnailRenderer").getObject("thumbnail")
                .getArray("thumbnails").getObject(0).getString("url");
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return null;
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        return null;
    }

    public boolean isUploaderVerified() throws ParsingException {
        try {
            return YoutubeParsingHelper.isVerified(playlistInfoItem.getArray("ownerBadges"));
        } catch (final Exception e) {
            throw new ParsingException("Could not get uploader verification info", e);
        }
    }

    @Override
    public long getStreamCount() throws ParsingException {
        return Long.parseLong(
                playlistInfoItem.getObject("videoCountShortText").getString("simpleText"));
    }

}
