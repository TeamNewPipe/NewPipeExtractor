package org.schabi.newpipe.extractor.services.niconico.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.services.niconico.NiconicoService;
import org.schabi.newpipe.extractor.utils.Parser;

public class NiconicoStreamLinkHandlerFactory extends LinkHandlerFactory {
    @Override
    public String getId(String url) throws ParsingException {
        return Parser.matchGroup(NiconicoService.SMILEVIDEO, url, 2);
    }

    @Override
    public String getUrl(String id) throws ParsingException {
        return "https://www.nicovideo.jp/watch/" + id;
    }

    @Override
    public boolean onAcceptUrl(String url) throws ParsingException {
        try {
            getId(url);
            return true;
        }
        catch (ParsingException e)
        {
            throw new ParsingException(e.getMessage());
        }
    }
}
