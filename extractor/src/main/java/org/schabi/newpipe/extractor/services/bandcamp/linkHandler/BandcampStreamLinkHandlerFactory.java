// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;

/**
 * <p>Tracks don't have standalone ids, they are always in combination with the band id.
 * That's why id = url. Instead, URLs are cleaned up so that they always look the same.</p>
 *
 * <p>Radio (bandcamp weekly) shows do have ids.</p>
 */
public class BandcampStreamLinkHandlerFactory extends LinkHandlerFactory {


    /**
     * @see BandcampStreamLinkHandlerFactory
     */
    @Override
    public String getId(String url) throws ParsingException {
        if (url.matches("https?://bandcamp\\.com/\\?show=\\d+")) {
            return url.split("bandcamp.com/\\?show=")[1];
        } else
            return getUrl(url);
    }

    /**
     * Clean up url
     * @see BandcampStreamLinkHandlerFactory
     */
    @Override
    public String getUrl(String input) {
        if (input.matches("\\d+"))
            return "https://bandcamp.com/?show=" + input;
        if (input.endsWith("/"))
            input = input.substring(0, input.length() - 1);
        input = input.replace("http://", "https://").toLowerCase();
        return input;
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
    public boolean onAcceptUrl(String url) {
        return getUrl(url).matches("https?://.+\\..+/track/.+") || getUrl(url).matches("https?://bandcamp\\.com/\\?show=\\d+");
    }
}
