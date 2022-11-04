package org.schabi.newpipe.extractor.services.youtube.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ChannelTabs;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;

import java.util.List;

public final class YoutubeChannelTabLinkHandlerFactory extends ListLinkHandlerFactory {
    private static final YoutubeChannelTabLinkHandlerFactory INSTANCE =
            new YoutubeChannelTabLinkHandlerFactory();

    private YoutubeChannelTabLinkHandlerFactory() {
    }

    public static YoutubeChannelTabLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    public static String getUrlSuffix(final String tab) throws ParsingException {
        switch (tab) {
            case ChannelTabs.PLAYLISTS:
                return "/playlists";
            case ChannelTabs.LIVESTREAMS:
                return "/streams";
            case ChannelTabs.SHORTS:
                return "/shorts";
            case ChannelTabs.CHANNELS:
                return "/channels";
        }
        throw new ParsingException("tab " + tab + " not supported");
    }

    @Override
    public String getUrl(final String id, final List<String> contentFilter, final String sortFilter)
            throws ParsingException {
        return "https://www.youtube.com/" + id + getUrlSuffix(contentFilter.get(0));
    }

    @Override
    public String getId(final String url) throws ParsingException {
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
                ChannelTabs.SHORTS,
                ChannelTabs.LIVESTREAMS,
                ChannelTabs.CHANNELS,
                ChannelTabs.PLAYLISTS,
        };
    }
}
