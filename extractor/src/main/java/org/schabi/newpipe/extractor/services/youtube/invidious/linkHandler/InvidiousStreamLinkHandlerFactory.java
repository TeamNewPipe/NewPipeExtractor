package org.schabi.newpipe.extractor.services.youtube.invidious.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousService;
import org.schabi.newpipe.extractor.services.youtube.shared.linkHandler.YoutubeLikeStreamLinkHandlerFactory;

public class InvidiousStreamLinkHandlerFactory extends YoutubeLikeStreamLinkHandlerFactory
        implements InvidiousLinkHandlerFactory {

    protected final String baseUrl;
    protected final InvidiousPlaylistLinkHandlerFactory playlistLinkHandlerFactory;

    public InvidiousStreamLinkHandlerFactory(final InvidiousService service) {
        baseUrl = service.getInstance().getUrl();
        playlistLinkHandlerFactory =
                (InvidiousPlaylistLinkHandlerFactory) service.getPlaylistLHFactory();
    }

    @Override
    public String getInvidiousBaseUrl() {
        return baseUrl;
    }

    @Override
    protected boolean isPlaylistUrl(final String url) {
        return playlistLinkHandlerFactory.onAcceptUrl(url);
    }

    @Override
    public String getUrl(final String id) throws ParsingException {
        return baseUrl + "/watch?v=" + id;
    }
}
