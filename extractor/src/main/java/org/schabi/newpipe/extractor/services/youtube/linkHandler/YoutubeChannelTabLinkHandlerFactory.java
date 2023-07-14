package org.schabi.newpipe.extractor.services.youtube.linkHandler;

import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.UnsupportedTabException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;

import javax.annotation.Nonnull;
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
    public static String getUrlSuffix(@Nonnull final String tab)
            throws UnsupportedTabException {
        switch (tab) {
            case ChannelTabs.VIDEOS:
                return "/videos";
            case ChannelTabs.SHORTS:
                return "/shorts";
            case ChannelTabs.LIVESTREAMS:
                return "/streams";
            case ChannelTabs.PLAYLISTS:
                return "/playlists";
            case ChannelTabs.CHANNELS:
                return "/channels";
        }
        throw new UnsupportedTabException(tab);
    }

    @Override
    public String getUrl(final String id,
                         final List<String> contentFilter,
                         final String sortFilter)
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

    @Override
    public String[] getAvailableContentFilter() {
        return new String[] {
                ChannelTabs.VIDEOS,
                ChannelTabs.SHORTS,
                ChannelTabs.LIVESTREAMS,
                ChannelTabs.PLAYLISTS,
                ChannelTabs.CHANNELS
        };
    }
}
