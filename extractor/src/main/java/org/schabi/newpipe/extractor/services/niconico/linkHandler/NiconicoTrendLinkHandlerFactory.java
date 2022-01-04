package org.schabi.newpipe.extractor.services.niconico.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.niconico.NiconicoService;

import java.util.List;

public class NiconicoTrendLinkHandlerFactory extends ListLinkHandlerFactory {
    @Override
    public String getId(String url) throws ParsingException {
        return "Trending";
    }

    @Override
    public boolean onAcceptUrl(String url) throws ParsingException {
        return NiconicoService.DAILY_TREND_URL.equals(url);
    }

    @Override
    public String getUrl(String id, List<String> contentFilter, String sortFilter) throws ParsingException {
        return NiconicoService.DAILY_TREND_URL;
    }
}
