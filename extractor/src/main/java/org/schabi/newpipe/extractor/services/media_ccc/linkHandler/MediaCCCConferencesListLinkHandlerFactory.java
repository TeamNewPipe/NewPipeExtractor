package org.schabi.newpipe.extractor.services.media_ccc.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;

import java.util.List;

public class MediaCCCConferencesListLinkHandlerFactory extends ListLinkHandlerFactory {
    @Override
    public String getId(String url) throws ParsingException {
        return "conferences";
    }

    @Override
    public String getUrl(String id, List<String> contentFilter, String sortFilter) throws ParsingException {
        return "https://api.media.ccc.de/public/conferences";
    }

    @Override
    public boolean onAcceptUrl(String url) throws ParsingException {
        return url.equals("https://media.ccc.de/b/conferences")
                || url.equals("https://api.media.ccc.de/public/conferences");
    }
}
