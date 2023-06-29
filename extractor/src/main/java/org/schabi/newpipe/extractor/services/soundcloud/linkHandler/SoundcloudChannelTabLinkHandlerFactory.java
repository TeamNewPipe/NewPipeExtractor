package org.schabi.newpipe.extractor.services.soundcloud.linkHandler;

import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.UnsupportedTabException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;

import javax.annotation.Nonnull;
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
    public static String getUrlSuffix(final String tab) throws UnsupportedOperationException {
        switch (tab) {
            case ChannelTabs.TRACKS:
                return "/tracks";
            case ChannelTabs.PLAYLISTS:
                return "/sets";
            case ChannelTabs.ALBUMS:
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
                         final List<String> contentFilter,
                         final String sortFilter) throws ParsingException {
        return SoundcloudChannelLinkHandlerFactory.getInstance().getUrl(id)
                + getUrlSuffix(contentFilter.get(0));
    }

    @Override
    public boolean onAcceptUrl(final String url) throws ParsingException {
        return SoundcloudChannelLinkHandlerFactory.getInstance().onAcceptUrl(url);
    }

    @Override
    public String[] getAvailableContentFilter() {
        return new String[] {
                ChannelTabs.TRACKS,
                ChannelTabs.PLAYLISTS,
                ChannelTabs.ALBUMS,
        };
    }
}
