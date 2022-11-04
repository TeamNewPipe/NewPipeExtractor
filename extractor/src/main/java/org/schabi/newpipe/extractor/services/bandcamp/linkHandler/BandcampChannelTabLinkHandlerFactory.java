package org.schabi.newpipe.extractor.services.bandcamp.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ChannelTabs;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;

import java.util.List;

public final class BandcampChannelTabLinkHandlerFactory extends ListLinkHandlerFactory {
    private static final BandcampChannelTabLinkHandlerFactory INSTANCE
            = new BandcampChannelTabLinkHandlerFactory();

    private BandcampChannelTabLinkHandlerFactory() {
    }

    public static BandcampChannelTabLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public String getId(final String url) throws ParsingException {
        return BandcampChannelLinkHandlerFactory.getInstance().getId(url);
    }

    @Override
    public String getUrl(final String id, final List<String> contentFilter, final String sortFilter)
            throws ParsingException {
        final String tab = contentFilter.get(0);
        if (!tab.equals(ChannelTabs.ALBUMS)) {
            throw new ParsingException("tab " + tab + " not supported");
        }

        // This is not an actual page on the Bandcamp website, but it auto-redirects
        // to the main page and we need a unique URL for the album tab
        return BandcampChannelLinkHandlerFactory.getInstance().getUrl(id) + "/album";
    }

    @Override
    public boolean onAcceptUrl(final String url) throws ParsingException {
        return BandcampChannelLinkHandlerFactory.getInstance().onAcceptUrl(url);
    }

    @Override
    public String[] getAvailableContentFilter() {
        return new String[] {
                ChannelTabs.ALBUMS,
        };
    }
}
