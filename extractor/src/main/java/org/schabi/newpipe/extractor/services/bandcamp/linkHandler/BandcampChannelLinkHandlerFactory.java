// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.linkHandler;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampStreamExtractor;

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
            return BandcampStreamExtractor.getAlbumInfoJson(response)
                    .getString("band_id");
        } catch (IOException | ReCaptchaException e) {
            throw new ParsingException("Download failed", e);
        }
    }

    @Override
    public String getUrl(String id, List<String> contentFilter, String sortFilter) throws ParsingException {
        return null; // TODO
    }

    /**
     * Matches <code>* .bandcamp.com</code> as well as custom domains
     * where the profile is at <code>* . * /releases</code>
     */
    @Override
    public boolean onAcceptUrl(String url) throws ParsingException {

        // Ends with "bandcamp.com" or "bandcamp.com/"?
        boolean endsWithBandcampCom =  url.endsWith("bandcamp.com")
                || url.endsWith("bandcamp.com/");

        // Is a subdomain of bandcamp.com?
        boolean isBandcampComSubdomain = url.matches("https?://.+\\.bandcamp\\.com");

        // Is root of bandcamp.com subdomain?
        boolean isBandcampComArtistPage = endsWithBandcampCom && isBandcampComSubdomain;

        boolean isCustomDomainReleases = url.matches("https?://.+\\..+/releases/?(?!.)");

        return isBandcampComArtistPage || isCustomDomainReleases;
    }
}
