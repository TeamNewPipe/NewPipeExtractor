package org.schabi.newpipe.extractor.services.youtube.youtube.linkHandler;

import org.schabi.newpipe.extractor.services.youtube.shared.linkHandler.YoutubeLikePlaylistLinkHandlerFactory;

import java.util.List;

public final class YoutubePlaylistLinkHandlerFactory extends YoutubeLikePlaylistLinkHandlerFactory {

    private static final YoutubePlaylistLinkHandlerFactory INSTANCE =
            new YoutubePlaylistLinkHandlerFactory();

    private YoutubePlaylistLinkHandlerFactory() {
    }

    public static YoutubePlaylistLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public String getUrl(final String id, final List<String> contentFilters,
                         final String sortFilter) {
        return "https://www.youtube.com/playlist?list=" + id;
    }

    @Override
    protected String getMixUrl(final String videoID, final String listID) {
        return "https://www.youtube.com/watch?v=" + videoID + "&list=" + listID;
    }
}
