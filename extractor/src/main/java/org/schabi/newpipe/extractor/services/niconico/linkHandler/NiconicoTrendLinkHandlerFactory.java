package org.schabi.newpipe.extractor.services.niconico.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.niconico.NiconicoService;

import java.util.List;

public class NiconicoTrendLinkHandlerFactory extends ListLinkHandlerFactory {
    @Override
    public String getId(final String url) throws ParsingException {
        return "Trending";
    }

    @Override
    public boolean onAcceptUrl(final String url) throws ParsingException {
        return NiconicoService.DAILY_TREND_URL.equals(url);
    }

    @Override
    public String getUrl(final String id, final List<String> contentFilter,
                         final String sortFilter) throws ParsingException {
        return NiconicoService.DAILY_TREND_URL;
    }
}
