package org.schabi.newpipe.extractor.services.youtube.invidious.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousService;
import org.schabi.newpipe.extractor.services.youtube.shared.linkHandler.YoutubeLikeStreamLinkHandlerFactory;

public class InvidiousStreamLinkHandlerFactory extends YoutubeLikeStreamLinkHandlerFactory
        implements InvidiousLinkHandlerFactory {

    protected final String baseUrl;

    public InvidiousStreamLinkHandlerFactory(final InvidiousService service) {
        this.baseUrl = service.getInstance().getUrl();
    }

    @Override
    public String getInvidiousBaseUrl() {
        return baseUrl;
    }

    @Override
    public String getUrl(final String id) throws ParsingException {
        return baseUrl + "/watch?v=" + id;
    }
}
