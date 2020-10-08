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
    public String getId(String url) throws ParsingException {
        try {
            String response = NewPipe.getDownloader().get(url).responseBody();

            // This variable contains band data!
            JsonObject bandData = BandcampExtractorHelper.getJsonData(response, "data-band");

            return String.valueOf(bandData.getLong("id"));

        } catch (IOException | ReCaptchaException | ArrayIndexOutOfBoundsException | JsonParserException e) {
            throw new ParsingException("Download failed", e);
        }
    }

    /**
     * Uses the mobile endpoint as a "translator" from id to url
     */
    @Override
    public String getUrl(String id, List<String> contentFilter, String sortFilter) throws ParsingException {
        try {
            return BandcampExtractorHelper.getArtistDetails(id)
                    .getString("bandcamp_url")
                    .replace("http://", "https://");
        } catch (NullPointerException e) {
            throw new ParsingException("JSON does not contain URL (invalid id?) or is otherwise invalid", e);
        }

    }

    /**
     * Matches <code>* .bandcamp.com</code> as well as custom domains
     * where the profile is at <code>* . * /releases</code>
     */
    @Override
    public boolean onAcceptUrl(String url) {

        // Is a subdomain of bandcamp.com?
        boolean isBandcampComArtistPage = url.matches("https?://.+\\.bandcamp\\.com/?");

        boolean isCustomDomainReleases = url.matches("https?://.+\\..+/releases/?(?!.)");

        return isBandcampComArtistPage || isCustomDomainReleases;
    }
}
