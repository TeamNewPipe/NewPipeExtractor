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

    /**
     * Get the tab's URL suffix
     * <p>
     * These URLs dont actually exist on the Bandcamp website as both albums and tracks
     * are listed on the main page, but they redirect to the main page and we need a
     * unique URL for each tab.
     *
     * @param tab Tab value
     * @return URL suffix
     * @throws ParsingException if the tab is not supported
     */
    public static String getUrlSuffix(final String tab) throws ParsingException {
        switch (tab) {
            case ChannelTabs.TRACKS:
                return "/track";
            case ChannelTabs.ALBUMS:
                return "/album";
        }
        throw new ParsingException("tab " + tab + " not supported");
    }

    @Override
    public String getId(final String url) throws ParsingException {
        return BandcampChannelLinkHandlerFactory.getInstance().getId(url);
    }

    @Override
    public String getUrl(final String id, final List<String> contentFilter, final String sortFilter)
            throws ParsingException {
        return BandcampChannelLinkHandlerFactory.getInstance().getUrl(id)
                + getUrlSuffix(contentFilter.get(0));
    }

    @Override
    public boolean onAcceptUrl(final String url) throws ParsingException {
        return BandcampChannelLinkHandlerFactory.getInstance().onAcceptUrl(url);
    }

    @Override
    public String[] getAvailableContentFilter() {
        return new String[]{
                ChannelTabs.TRACKS,
                ChannelTabs.ALBUMS,
        };
    }
}
