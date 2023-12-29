package org.schabi.newpipe.extractor.services.soundcloud.linkHandler;

import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.UnsupportedTabException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.search.filter.FilterItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;

public final class SoundcloudChannelTabLinkHandlerFactory extends ListLinkHandlerFactory {
    private static final SoundcloudChannelTabLinkHandlerFactory INSTANCE
            = new SoundcloudChannelTabLinkHandlerFactory();

    private SoundcloudChannelTabLinkHandlerFactory() {
    }

    public static SoundcloudChannelTabLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Nonnull
    public static String getUrlSuffix(final FilterItem tab) throws UnsupportedOperationException {
        if (tab.equals(ChannelTabs.TRACKS)) {
            return "/tracks";
        } else if (tab.equals(ChannelTabs.PLAYLISTS)) {
            return "/sets";
        } else if (tab.equals(ChannelTabs.ALBUMS)) {
            return "/albums";
        }
        throw new UnsupportedTabException(tab);
    }

    @Override
    public String getId(final String url) throws ParsingException {
        return SoundcloudChannelLinkHandlerFactory.getInstance().getId(url);
    }

    @Override
    public String getUrl(final String id,
                         @Nonnull final List<FilterItem> contentFilter,
                         @Nullable final List<FilterItem> sortFilter) throws ParsingException {
        return SoundcloudChannelLinkHandlerFactory.getInstance().getUrl(id)
                + getUrlSuffix(contentFilter.get(0));
    }

    @Override
    public boolean onAcceptUrl(final String url) throws ParsingException {
        return SoundcloudChannelLinkHandlerFactory.getInstance().onAcceptUrl(url);
    }
}
