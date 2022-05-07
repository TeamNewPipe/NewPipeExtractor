package org.schabi.newpipe.extractor.services.youtube.youtube.linkHandler;

import org.schabi.newpipe.extractor.services.youtube.shared.linkHandler.YoutubeLikeCommentsLinkHandlerFactory;

public final class YoutubeCommentsLinkHandlerFactory extends YoutubeLikeCommentsLinkHandlerFactory {

    private static final YoutubeCommentsLinkHandlerFactory INSTANCE
            = new YoutubeCommentsLinkHandlerFactory();

    private YoutubeCommentsLinkHandlerFactory() {
        super(YoutubeStreamLinkHandlerFactory.getInstance());
    }

    public static YoutubeCommentsLinkHandlerFactory getInstance() {
        return INSTANCE;
    }
}
