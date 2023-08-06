// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.linkHandler;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.util.List;

/**
 * Artist do have IDs that are useful
 */
public final class BandcampChannelLinkHandlerFactory extends ListLinkHandlerFactory {

    private static final BandcampChannelLinkHandlerFactory INSTANCE
            = new BandcampChannelLinkHandlerFactory();

    private BandcampChannelLinkHandlerFactory() {
    }

    public static BandcampChannelLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public String getId(final String url) throws ParsingException, UnsupportedOperationException {
        try {
            final String response = NewPipe.getDownloader().get(url).responseBody();

            // Use band data embedded in website to extract ID
            final JsonObject bandData = JsonUtils.getJsonData(response, "data-band");

            return String.valueOf(bandData.getLong("id"));

        } catch (final IOException | ReCaptchaException | ArrayIndexOutOfBoundsException
                | JsonParserException e) {
            throw new ParsingException("Download failed", e);
        }
    }

    /**
     * Uses the mobile endpoint as a "translator" from id to url
     */
    @Override
    public String getUrl(final String id, final List<String> contentFilter, final String sortFilter)
            throws ParsingException, UnsupportedOperationException {
        final JsonObject artistDetails = BandcampExtractorHelper.getArtistDetails(id);
        if (artistDetails.getBoolean("error")) {
            throw new ParsingException(
                    "JSON does not contain a channel URL (invalid id?) or is otherwise invalid");
        }
        return Utils.replaceHttpWithHttps(artistDetails.getString("bandcamp_url"));
    }

    /**
     * Accepts only pages that lead to the root of an artist profile. Supports external pages.
     */
    @Override
    public boolean onAcceptUrl(final String url) throws ParsingException {

        final String lowercaseUrl = url.toLowerCase();

        // https: | | artist.bandcamp.com | releases - music - album - track ( | name)
        //  0      1           2                           3                    (4)
        final String[] splitUrl = lowercaseUrl.split("/");

        // URL is too short
        if (splitUrl.length != 3 && splitUrl.length != 4) {
            return false;
        }

        // Must have "releases", "music", "album" or "track" as segment after URL or none at all
        if (splitUrl.length == 4 && !(splitUrl[3].equals("releases")
                || splitUrl[3].equals("music")
                || splitUrl[3].equals("album")
                || splitUrl[3].equals("track"))) {
            return false;
        } else {
            if (splitUrl[2].equals("daily.bandcamp.com")) {
                // Refuse links to daily.bandcamp.com as that is not an artist
                return false;
            }

            // Test whether domain is supported
            return BandcampExtractorHelper.isSupportedDomain(lowercaseUrl);
        }
    }
}
