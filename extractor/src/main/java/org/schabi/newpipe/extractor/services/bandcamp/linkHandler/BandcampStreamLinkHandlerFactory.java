// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;

/**
 * Tracks do have IDs, but they are not really useful. That's why id = url.
 * Instead, URLs are cleaned up so that they always look the same.
 */
public class BandcampStreamLinkHandlerFactory extends LinkHandlerFactory {


    /**
     * @see BandcampStreamLinkHandlerFactory
     */
    @Override
    public String getId(String url) throws ParsingException {
        return getUrl(url);
    }

    /**
     * Clean up url
     * @see BandcampStreamLinkHandlerFactory
     */
    @Override
    public String getUrl(String url) {
        if (url.endsWith("/"))
            url = url.substring(0, url.length() - 1);
        url = url.replace("http://", "https://").toLowerCase();
        return url;
    }

    /**
     * Sometimes, the root page of an artist is also an album or track
     * page. In that case, it is assumed that one actually wants to open
     * the profile and not the track it has set as the default one.
     * <br/><br/>Urls are expected to be in this format to account for
     * custom domains:
     * <br/><code>https:// * . * /track/ *</code>
     */
    @Override
    public boolean onAcceptUrl(String url) {
        return getUrl(url).matches("https?://.+\\..+/track/.+");
    }
}
