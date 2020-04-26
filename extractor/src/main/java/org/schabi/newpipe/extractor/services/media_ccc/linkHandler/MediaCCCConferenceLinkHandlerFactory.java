package org.schabi.newpipe.extractor.services.media_ccc.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Parser;

import java.util.List;

public class MediaCCCConferenceLinkHandlerFactory extends ListLinkHandlerFactory {
    @Override
    public String getUrl(final String id, final List<String> contentFilter, final String sortFilter)
            throws ParsingException {
        return "https://media.ccc.de/public/conferences/" + id;
    }

    @Override
    public String getId(final String url) throws ParsingException {
        if (url.startsWith("https://media.ccc.de/public/conferences/")
                || url.startsWith("https://api.media.ccc.de/public/conferences/")) {
            return url.replaceFirst("https://(api\\.)?media\\.ccc\\.de/public/conferences/", "");
        } else if (url.startsWith("https://media.ccc.de/c/")) {
            return Parser.matchGroup1("https://media.ccc.de/c/([^?#]*)", url);
        } else if (url.startsWith("https://media.ccc.de/b/")) {
            return Parser.matchGroup1("https://media.ccc.de/b/([^?#]*)", url);
        }
        throw new ParsingException("Could not get id from url: " + url);
    }

    @Override
    public boolean onAcceptUrl(final String url) {
        try {
            getId(url);
            return true;
        } catch (ParsingException e) {
            return false;
        }
    }
}
