package org.schabi.newpipe.extractor.services.youtube.linkHandler;

import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.UnsupportedTabException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.search.filter.FilterItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;

public final class YoutubeChannelTabLinkHandlerFactory extends ListLinkHandlerFactory {
    private static final YoutubeChannelTabLinkHandlerFactory INSTANCE =
            new YoutubeChannelTabLinkHandlerFactory();

    private YoutubeChannelTabLinkHandlerFactory() {
    }

    public static YoutubeChannelTabLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Nonnull
    public static String getUrlSuffix(@Nonnull final FilterItem tab)
            throws UnsupportedTabException {
        if (tab.equals(ChannelTabs.VIDEOS)) {
            return "/videos";
        } else if (tab.equals(ChannelTabs.SHORTS)) {
            return "/shorts";
        } else if (tab.equals(ChannelTabs.LIVESTREAMS)) {
            return "/streams";
        } else if (tab.equals(ChannelTabs.PLAYLISTS)) {
            return "/playlists";
        }
        throw new UnsupportedTabException(tab);
    }

    @Override
    public String getUrl(final String id,
                         @Nonnull final List<FilterItem> contentFilter,
                         @Nullable final List<FilterItem> sortFilter)
            throws ParsingException, UnsupportedOperationException {
        return "https://www.youtube.com/" + id + getUrlSuffix(contentFilter.get(0));
    }

    @Override
    public String getId(final String url) throws ParsingException, UnsupportedOperationException {
        return YoutubeChannelLinkHandlerFactory.getInstance().getId(url);
    }

    @Override
    public boolean onAcceptUrl(final String url) throws ParsingException {
        try {
            getId(url);
        } catch (final ParsingException e) {
            return false;
        }
        return true;
    }
}
