// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;

/**
 * <p>Tracks don't have standalone ids, they are always in combination with the band id.
 * That's why id = url.</p>
 *
 * <p>Radio (bandcamp weekly) shows do have ids.</p>
 */
public class BandcampStreamLinkHandlerFactory extends LinkHandlerFactory {


    /**
     * @see BandcampStreamLinkHandlerFactory
     */
    @Override
    public String getId(final String url) throws ParsingException {
        if (url.matches("https?://bandcamp\\.com/\\?show=\\d+")) {
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
    public String getUrl(final String input) {
        if (input.matches("\\d+")) {
            return "https://bandcamp.com/?show=" + input;
        } else {
            return input;
        }
    }

    /**
     * Sometimes, the root page of an artist is also an album or track
     * page. In that case, it is assumed that one actually wants to open
     * the profile and not the track it has set as the default one.
     * <p>Urls are expected to be in this format to account for
     * custom domains:</p>
     * <code>https:// * . * /track/ *</code>
     */
    @Override
    public boolean onAcceptUrl(final String url) {
        return url.toLowerCase().matches("https?://.+\\..+/track/.+")
                || url.toLowerCase().matches("https?://bandcamp\\.com/\\?show=\\d+");
    }
}
