package org.schabi.newpipe.extractor.services.youtube.invidious.linkHandler;

import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousService;
import org.schabi.newpipe.extractor.services.youtube.shared.linkHandler.YoutubeLikeCommentsLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.shared.linkHandler.YoutubeLikeStreamLinkHandlerFactory;

public class InvidiousCommentLinkHandlerFactory extends YoutubeLikeCommentsLinkHandlerFactory {

    public InvidiousCommentLinkHandlerFactory(final InvidiousService service) {
        super((YoutubeLikeStreamLinkHandlerFactory) service.getStreamLHFactory());
    }
}
