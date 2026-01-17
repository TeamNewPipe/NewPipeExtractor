package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import javax.annotation.Nonnull;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getUrlFromObject;

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
        return getTextFromObject(longBylineText)
                .or(() -> getTextFromObject(shortBylineText))
                .orElseThrow(() -> new ParsingException("Could not get uploader name"));
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        return getUrlFromObject(longBylineText)
                .or(() -> getUrlFromObject(shortBylineText))
                .orElseThrow(() -> new ParsingException("Could not get uploader URL"));
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        // We do not have this information in showRenderers
        return false;
    }
}
