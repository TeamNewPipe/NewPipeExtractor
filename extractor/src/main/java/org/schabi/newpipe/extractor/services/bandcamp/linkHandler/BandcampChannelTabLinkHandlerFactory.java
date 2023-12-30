
package org.schabi.newpipe.extractor.services.bandcamp.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.exceptions.UnsupportedTabException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.search.filter.FilterItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
     * Get a tab's URL suffix.
     *
     * <p>
     * These URLs don't actually exist on the Bandcamp website, as both albums and tracks are
     * listed on the main page, but redirect to the main page, which is perfect for us as we need a
     * unique URL for each tab.
     * </p>
     *
     * @param tab the tab value, which must not be null
     * @return a URL suffix
     * @throws UnsupportedTabException if the tab is not supported
     */
    @Nonnull
    public static String getUrlSuffix(@Nonnull final FilterItem tab)
            throws UnsupportedTabException {
        if (tab.equals(ChannelTabs.TRACKS)) {
            return "/track";
        } else if (tab.equals(ChannelTabs.ALBUMS)) {
            return "/album";
        }
        throw new UnsupportedTabException(tab);
    }

    @Override
    public String getId(final String url) throws ParsingException, UnsupportedOperationException {
        return BandcampChannelLinkHandlerFactory.getInstance().getId(url);
    }

    @Override
    public String getUrl(final String id,
                         @Nonnull final List<FilterItem> contentFilter,
                         @Nullable final List<FilterItem> sortFilter)
            throws ParsingException, UnsupportedOperationException {
        return BandcampChannelLinkHandlerFactory.getInstance().getUrl(id)
                + getUrlSuffix(contentFilter.get(0));
    }

    @Override
    public boolean onAcceptUrl(final String url) throws ParsingException {
        return BandcampChannelLinkHandlerFactory.getInstance().onAcceptUrl(url);
    }
}
