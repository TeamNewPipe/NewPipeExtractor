package org.schabi.newpipe.extractor.services.media_ccc.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Parser;

import java.util.List;

public class MediaCCCConferenceLinkHandlerFactory extends ListLinkHandlerFactory {

    @Override
    public String getUrl(String id, List<String> contentFilter, String sortFilter) throws ParsingException {
        return "https://api.media.ccc.de/public/conferences/" + id;
    }

    @Override
    public String getId(String url) throws ParsingException {
        if(url.startsWith("https://api.media.ccc.de/public/conferences/")) {
            return url.replace("https://api.media.ccc.de/public/conferences/", "");
        } else if(url.startsWith("https://media.ccc.de/c/")) {
            return Parser.matchGroup1("https://media.ccc.de/c/([^?#]*)", url);
        } else {
            throw new ParsingException("Could not get id from url: " + url);
        }
    }

    @Override
    public boolean onAcceptUrl(String url) throws ParsingException {
        return url.startsWith("https://api.media.ccc.de/public/conferences/")
                || url.startsWith("https://media.ccc.de/c/");
    }
}
