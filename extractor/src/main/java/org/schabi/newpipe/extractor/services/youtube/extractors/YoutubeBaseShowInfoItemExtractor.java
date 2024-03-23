package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nonnull;
import java.util.List;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getThumbnailsFromInfoItem;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getUrlFromNavigationEndpoint;

/**
 * The base {@link PlaylistInfoItemExtractor} for shows playlists UI elements.
 */
abstract class YoutubeBaseShowInfoItemExtractor implements PlaylistInfoItemExtractor {

    @Nonnull
    protected final JsonObject showRenderer;

    YoutubeBaseShowInfoItemExtractor(@Nonnull final JsonObject showRenderer) {
        this.showRenderer = showRenderer;
    }

    @Override
    public String getName() throws ParsingException {
        return showRenderer.getString("title");
    }

    @Override
    public String getUrl() throws ParsingException {
        return getUrlFromNavigationEndpoint(showRenderer.getObject("navigationEndpoint"));
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() throws ParsingException {
        return getThumbnailsFromInfoItem(showRenderer.getObject("thumbnailRenderer")
                .getObject("showCustomThumbnailRenderer"));
    }

    @Override
    public long getStreamCount() throws ParsingException {
        // The stream count should be always returned in the first text object for English
        // localizations, but the complete text is parsed for reliability purposes
        final String streamCountText = getTextFromObject(
                showRenderer.getObject("thumbnailOverlays")
                        .getObject("thumbnailOverlayBottomPanelRenderer")
                        .getObject("text"));
        if (streamCountText == null) {
            throw new ParsingException("Could not get stream count");
        }

        try {
            // The data returned could be a human/shortened number, but no show with more than 1000
            // videos has been found at the time this code was written
            return Long.parseLong(Utils.removeNonDigitCharacters(streamCountText));
        } catch (final NumberFormatException e) {
            throw new ParsingException("Could not convert stream count to a long", e);
        }
    }
}
