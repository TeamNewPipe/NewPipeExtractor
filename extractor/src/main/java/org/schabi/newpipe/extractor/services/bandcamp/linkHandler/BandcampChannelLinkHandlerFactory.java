// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.linkHandler;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper;

import java.io.IOException;
import java.util.List;

/**
 * Artist do have IDs that are useful
 */
public class BandcampChannelLinkHandlerFactory extends ListLinkHandlerFactory {


    @Override
    public String getId(final String url) throws ParsingException {
        try {
            final String response = NewPipe.getDownloader().get(url).responseBody();

            // Use band data embedded in website to extract ID
            final JsonObject bandData = BandcampExtractorHelper.getJsonData(response, "data-band");

            return String.valueOf(bandData.getLong("id"));

        } catch (final IOException | ReCaptchaException | ArrayIndexOutOfBoundsException | JsonParserException e) {
            throw new ParsingException("Download failed", e);
        }
    }

    /**
     * Uses the mobile endpoint as a "translator" from id to url
     */
    @Override
    public String getUrl(final String id, final List<String> contentFilter, final String sortFilter)
            throws ParsingException {
        try {
            return BandcampExtractorHelper.getArtistDetails(id)
                    .getString("bandcamp_url")
                    .replace("http://", "https://");
        } catch (final NullPointerException e) {
            throw new ParsingException("JSON does not contain URL (invalid id?) or is otherwise invalid", e);
        }

    }

    /**
     * Accepts only pages that do not lead to an album or track. Supports external pages.
     */
    @Override
    public boolean onAcceptUrl(final String url) throws ParsingException {

        // Exclude URLs that lead to a track or album
        if (url.matches(".*/(album|track)/.*")) return false;

        // Test whether domain is supported
        return BandcampExtractorHelper.isSupportedDomain(url);
    }
}
