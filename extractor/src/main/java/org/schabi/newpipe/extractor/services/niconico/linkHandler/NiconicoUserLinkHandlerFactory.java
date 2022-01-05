package org.schabi.newpipe.extractor.services.niconico.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.niconico.NiconicoService;
import org.schabi.newpipe.extractor.utils.Parser;

import java.util.List;

public class NiconicoUserLinkHandlerFactory extends ListLinkHandlerFactory {
    @Override
    public String getId(final String url) throws ParsingException {
        return Parser.matchGroup1(NiconicoService.USER_UPLOAD_LIST, url);
    }

    @Override
    public boolean onAcceptUrl(final String url) throws ParsingException {
        return Parser.isMatch(NiconicoService.USER_UPLOAD_LIST, url);
    }

    @Override
    public String getUrl(final String id, final List<String> contentFilter,
                         final String sortFilter) throws ParsingException {
        return NiconicoService.USER_URL + id;
    }
}
