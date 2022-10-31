package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubePlaylistLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Utils;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.fixThumbnailUrl;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getUrlFromObject;


public class YoutubePlaylistInfoItemExtractor implements PlaylistInfoItemExtractor {
    private final JsonObject playlistInfoItem;

    public YoutubePlaylistInfoItemExtractor(final JsonObject playlistInfoItem) {
        this.playlistInfoItem = playlistInfoItem;
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        try {
            final String url = playlistInfoItem.getArray("thumbnails").getObject(0)
                    .getArray("thumbnails").getObject(0).getString("url");

            return fixThumbnailUrl(url);
        } catch (final Exception e) {
            throw new ParsingException("Could not get thumbnail url", e);
        }
    }

    @Override
    public String getName() throws ParsingException {
        try {
            return getTextFromObject(playlistInfoItem.getObject("title"));
        } catch (final Exception e) {
            throw new ParsingException("Could not get name", e);
        }
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
    public String getUploaderName() throws ParsingException {
        try {
            return getTextFromObject(playlistInfoItem.getObject("longBylineText"));
        } catch (final Exception e) {
            throw new ParsingException("Could not get uploader name", e);
        }
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        try {
            return getUrlFromObject(playlistInfoItem.getObject("longBylineText"));
        } catch (final Exception e) {
            throw new ParsingException("Could not get uploader url", e);
        }
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        try {
            return YoutubeParsingHelper.isVerified(playlistInfoItem.getArray("ownerBadges"));
        } catch (final Exception e) {
            throw new ParsingException("Could not get uploader verification info", e);
        }
    }

    @Override
    public long getStreamCount() throws ParsingException {
        try {
            return Long.parseLong(Utils.removeNonDigitCharacters(
                    playlistInfoItem.getString("videoCount")));
        } catch (final Exception e) {
            throw new ParsingException("Could not get stream count", e);
        }
    }
}
