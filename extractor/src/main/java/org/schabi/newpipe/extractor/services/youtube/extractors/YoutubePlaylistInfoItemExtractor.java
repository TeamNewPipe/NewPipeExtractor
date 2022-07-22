package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubePlaylistLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nonnull;
import java.util.List;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getImagesFromThumbnailsArray;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getUrlFromObject;

public class YoutubePlaylistInfoItemExtractor implements PlaylistInfoItemExtractor {
    private final JsonObject playlistInfoItem;

    public YoutubePlaylistInfoItemExtractor(final JsonObject playlistInfoItem) {
        this.playlistInfoItem = playlistInfoItem;
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() throws ParsingException {
        try {
            JsonArray thumbnails = playlistInfoItem.getArray("thumbnails")
                    .getObject(0)
                    .getArray("thumbnails");
            if (thumbnails.isEmpty()) {
                thumbnails = playlistInfoItem.getObject("thumbnail")
                        .getArray("thumbnails");
            }

            return getImagesFromThumbnailsArray(thumbnails);
        } catch (final Exception e) {
            throw new ParsingException("Could not get thumbnails", e);
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
        String videoCountText = playlistInfoItem.getString("videoCount");
        if (videoCountText == null) {
            videoCountText = getTextFromObject(playlistInfoItem.getObject("videoCountText"));
        }

        if (videoCountText == null) {
            videoCountText = getTextFromObject(playlistInfoItem.getObject("videoCountShortText"));
        }

        if (videoCountText == null) {
            throw new ParsingException("Could not get stream count");
        }

        try {
            return Long.parseLong(Utils.removeNonDigitCharacters(videoCountText));
        } catch (final Exception e) {
            throw new ParsingException("Could not get stream count", e);
        }
    }
}
