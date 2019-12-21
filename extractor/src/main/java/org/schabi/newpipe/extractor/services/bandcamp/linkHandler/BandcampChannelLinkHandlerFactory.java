// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.linkHandler;

import org.json.JSONObject;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampStreamExtractor;
import org.schabi.newpipe.extractor.utils.ExtractorHelper;

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
            JSONObject bandData = BandcampExtractorHelper.getJSONFromJavaScriptVariables(response, "BandData");

            return String.valueOf(bandData.getLong("id"));

        } catch (IOException | ReCaptchaException e) {
            throw new ParsingException("Download failed", e);
        }
    }

    /**
     * Fetch artist details from mobile endpoint, thereby receiving their URL.
     * <a href=https://notabug.org/fynngodau/bandcampDirect/wiki/rewindBandcamp+%E2%80%93+Fetching+artist+details>
     * I once took a moment to note down how it works.</a>
     *
     * @throws ParsingException
     */
    @Override
    public String getUrl(String id, List<String> contentFilter, String sortFilter) throws ParsingException {
        try {
            String data = NewPipe.getDownloader().post(
                    "https://bandcamp.com/api/mobile/22/band_details",
                    null,
                    ("{\"band_id\":\"" + id + "\"}").getBytes()
            ).responseBody();

            return new JSONObject(data)
                    .getString("bandcamp_url")
                    .replace("http://", "https://");


        } catch (IOException | ReCaptchaException e) {
            throw new ParsingException("Download failed", e);
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
