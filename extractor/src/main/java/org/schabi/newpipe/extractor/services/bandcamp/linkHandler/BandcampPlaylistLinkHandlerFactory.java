// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;

import java.util.List;

/**
 * Just as with streams, the album ids are essentially useless for us.
 */
public class BandcampPlaylistLinkHandlerFactory extends ListLinkHandlerFactory {
    @Override
    public String getId(String url) throws ParsingException {
        return getUrl(url);
    }

    @Override
    public String getUrl(String url, List<String> contentFilter, String sortFilter) throws ParsingException {
        return url;
    }

    @Override
    public boolean onAcceptUrl(String url) {
        return url.toLowerCase().matches("https?://.+\\..+/album/.+");
    }
}
