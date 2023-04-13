package org.schabi.newpipe.extractor.services.peertube.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ChannelTabs;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;

import java.util.List;

public final class PeertubeChannelTabLinkHandlerFactory extends ListLinkHandlerFactory {
    private static final PeertubeChannelTabLinkHandlerFactory INSTANCE
            = new PeertubeChannelTabLinkHandlerFactory();

    private PeertubeChannelTabLinkHandlerFactory() {
    }

    public static PeertubeChannelTabLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    public static String getUrlSuffix(final String tab) throws ParsingException {
        switch (tab) {
            case ChannelTabs.VIDEOS:
                return "/videos";
            case ChannelTabs.CHANNELS: // only available on accounts
                return "/video-channels";
            case ChannelTabs.PLAYLISTS: // only available on channels
                return "/video-playlists";
        }
        throw new ParsingException("tab " + tab + " not supported");
    }

    @Override
    public String getId(final String url) throws ParsingException {
        return PeertubeChannelLinkHandlerFactory.getInstance().getId(url);
    }

    @Override
    public String getUrl(final String id, final List<String> contentFilter, final String sortFilter)
            throws ParsingException {
        return PeertubeChannelLinkHandlerFactory.getInstance().getUrl(id)
                + getUrlSuffix(contentFilter.get(0));
    }

    @Override
    public String getUrl(final String id, final List<String> contentFilter, final String sortFilter,
                         final String baseUrl) throws ParsingException {
        return PeertubeChannelLinkHandlerFactory.getInstance().getUrl(id, null, null, baseUrl)
                + getUrlSuffix(contentFilter.get(0));
    }

    @Override
    public boolean onAcceptUrl(final String url) throws ParsingException {
        return PeertubeChannelLinkHandlerFactory.getInstance().onAcceptUrl(url);
    }

    @Override
    public String[] getAvailableContentFilter() {
        return new String[] {
                ChannelTabs.PLAYLISTS,
                ChannelTabs.CHANNELS,
        };
    }
}
