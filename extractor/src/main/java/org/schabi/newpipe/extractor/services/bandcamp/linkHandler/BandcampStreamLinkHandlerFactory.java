// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper;

import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.BASE_URL;

/**
 * <p>Tracks don't have standalone ids, they are always in combination with the band id.
 * That's why id = url.</p>
 *
 * <p>Radio (bandcamp weekly) shows do have ids.</p>
 */
public final class BandcampStreamLinkHandlerFactory extends LinkHandlerFactory {

    private static final BandcampStreamLinkHandlerFactory INSTANCE
            = new BandcampStreamLinkHandlerFactory();

    private BandcampStreamLinkHandlerFactory() {
    }

    public static BandcampStreamLinkHandlerFactory getInstance() {
        return INSTANCE;
    }


    /**
     * @see BandcampStreamLinkHandlerFactory
     */
    @Override
    public String getId(final String url) throws ParsingException, UnsupportedOperationException {
        if (BandcampExtractorHelper.isRadioUrl(url)) {
            return url.split("bandcamp.com/\\?show=")[1];
        } else {
            return getUrl(url);
        }
    }

    /**
     * Clean up url
     * @see BandcampStreamLinkHandlerFactory
     */
    @Override
    public String getUrl(final String input)
            throws ParsingException, UnsupportedOperationException {
        if (input.matches("\\d+")) {
            return BASE_URL + "/?show=" + input;
        } else {
            return input;
        }
    }

    /**
     * Accepts URLs that point to a bandcamp radio show or that are a bandcamp
     * domain and point to a track.
     */
    @Override
    public boolean onAcceptUrl(final String url) throws ParsingException {

        // Accept Bandcamp radio
        if (BandcampExtractorHelper.isRadioUrl(url)) {
            return true;
        }

        // Don't accept URLs that don't point to a track
        if (!url.toLowerCase().matches("https?://.+\\..+/track/.+")) {
            return false;
        }

        // Test whether domain is supported
        return BandcampExtractorHelper.isSupportedDomain(url);
    }
}
