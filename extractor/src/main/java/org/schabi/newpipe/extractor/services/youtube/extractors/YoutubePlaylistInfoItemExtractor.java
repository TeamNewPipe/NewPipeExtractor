package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubePlaylistLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Utils;

public class YoutubePlaylistInfoItemExtractor implements PlaylistInfoItemExtractor {
    private JsonObject playlistInfoItem;

    public YoutubePlaylistInfoItemExtractor(JsonObject playlistInfoItem) {
        this.playlistInfoItem = playlistInfoItem;
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        try {
            return playlistInfoItem.getArray("thumbnails").getObject(0).getArray("thumbnails")
                    .getObject(0).getString("url");
        } catch (Exception e) {
            throw new ParsingException("Could not get thumbnail url", e);
        }
    }

    @Override
    public String getName() throws ParsingException {
        try {
            return playlistInfoItem.getObject("title").getString("simpleText");
        } catch (Exception e) {
            throw new ParsingException("Could not get name", e);
        }
    }

    @Override
    public String getUrl() throws ParsingException {
        try {
            String id = playlistInfoItem.getString("playlistId");
            return YoutubePlaylistLinkHandlerFactory.getInstance().getUrl(id);
        } catch (Exception e) {
            throw new ParsingException("Could not get url", e);
        }
    }

    @Override
    public String getUploaderName() throws ParsingException {
        try {
            return playlistInfoItem.getObject("longBylineText").getArray("runs").getObject(0).getString("text");
        } catch (Exception e) {
            throw new ParsingException("Could not get uploader name", e);
        }
    }

    @Override
    public long getStreamCount() throws ParsingException {
        try {
            return Long.parseLong(Utils.removeNonDigitCharacters(playlistInfoItem.getString("videoCount")));
        } catch (Exception e) {
            throw new ParsingException("Could not get stream count", e);
        }
    }
}
