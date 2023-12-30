package org.schabi.newpipe.extractor.services.peertube.linkHandler;

import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.UnsupportedTabException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.search.filter.FilterItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;

public final class PeertubeChannelTabLinkHandlerFactory extends ListLinkHandlerFactory {
    private static final PeertubeChannelTabLinkHandlerFactory INSTANCE
            = new PeertubeChannelTabLinkHandlerFactory();

    private PeertubeChannelTabLinkHandlerFactory() {
    }

    public static PeertubeChannelTabLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Nonnull
    public static String getUrlSuffix(@Nonnull final FilterItem tab)
            throws UnsupportedTabException {
        if (tab.equals(ChannelTabs.VIDEOS)) {
            return "/videos";
        } else if (tab.equals(ChannelTabs.CHANNELS)) { // only available on accounts
            return "/video-channels";
        } else if (tab.equals(ChannelTabs.PLAYLISTS)) { // only available on channels
            return "/video-playlists";
        }
        throw new UnsupportedTabException(tab);
    }

    @Override
    public String getId(final String url) throws ParsingException, UnsupportedOperationException {
        return PeertubeChannelLinkHandlerFactory.getInstance().getId(url);
    }

    @Override
    public String getUrl(final String id,
                         @Nonnull final List<FilterItem> contentFilter,
                         @Nullable final List<FilterItem> sortFilter)
            throws ParsingException, UnsupportedOperationException {
        return PeertubeChannelLinkHandlerFactory.getInstance().getUrl(id)
                + getUrlSuffix(contentFilter.get(0));
    }

    @Override
    public String getUrl(final String id,
                         final List<FilterItem> contentFilter,
                         final List<FilterItem> sortFilter,
                         final String baseUrl)
            throws ParsingException, UnsupportedOperationException {
        return PeertubeChannelLinkHandlerFactory.getInstance()
                .getUrl(id, null, null, baseUrl)
                + getUrlSuffix(contentFilter.get(0));
    }

    @Override
    public boolean onAcceptUrl(final String url) throws ParsingException {
        return PeertubeChannelLinkHandlerFactory.getInstance().onAcceptUrl(url);
    }
}
