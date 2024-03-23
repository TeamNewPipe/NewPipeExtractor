package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import javax.annotation.Nonnull;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getUrlFromObject;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

/**
 * A {@link YoutubeBaseShowInfoItemExtractor} implementation for {@code showRenderer}s.
 */
class YoutubeShowRendererInfoItemExtractor extends YoutubeBaseShowInfoItemExtractor {

    @Nonnull
    private final JsonObject shortBylineText;
    @Nonnull
    private final JsonObject longBylineText;

    YoutubeShowRendererInfoItemExtractor(@Nonnull final JsonObject showRenderer) {
        super(showRenderer);
        this.shortBylineText = showRenderer.getObject("shortBylineText");
        this.longBylineText = showRenderer.getObject("longBylineText");
    }

    @Override
    public String getUploaderName() throws ParsingException {
        String name = getTextFromObject(longBylineText);
        if (isNullOrEmpty(name)) {
            name = getTextFromObject(shortBylineText);
            if (isNullOrEmpty(name)) {
                throw new ParsingException("Could not get uploader name");
            }
        }
        return name;
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        String uploaderUrl = getUrlFromObject(longBylineText);
        if (uploaderUrl == null) {
            uploaderUrl = getUrlFromObject(shortBylineText);
            if (uploaderUrl == null) {
                throw new ParsingException("Could not get uploader URL");
            }
        }
        return uploaderUrl;
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        // We do not have this information in showRenderers
        return false;
    }
}
