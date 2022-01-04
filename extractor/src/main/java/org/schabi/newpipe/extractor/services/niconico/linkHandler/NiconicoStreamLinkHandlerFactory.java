package org.schabi.newpipe.extractor.services.niconico.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Parser;

public class NiconicoStreamLinkHandlerFactory extends LinkHandlerFactory {
    private static final String SMILEVIDEO = "(nicovideo\\.jp|nico\\.ms)\\/(.*?)\\?(.*)";
    @Override
    public String getId(String url) throws ParsingException {
        return Parser.matchGroup(SMILEVIDEO, url, 1);
    }

    @Override
    public String getUrl(String id) throws ParsingException {
        return "https://www.nicovideo.jp/watch/" + id;
    }

    @Override
    public boolean onAcceptUrl(String url) throws ParsingException {
        try {
            return getId(url) != null;
        }
        catch (ParsingException e)
        {
            return false;
        }
    }
}
