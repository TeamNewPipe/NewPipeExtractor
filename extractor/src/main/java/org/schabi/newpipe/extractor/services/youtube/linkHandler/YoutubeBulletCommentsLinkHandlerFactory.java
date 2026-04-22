package org.schabi.newpipe.extractor.services.youtube.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;

import java.util.List;

public class YoutubeBulletCommentsLinkHandlerFactory extends ListLinkHandlerFactory {
    private static final YoutubeBulletCommentsLinkHandlerFactory INSTANCE
            = new YoutubeBulletCommentsLinkHandlerFactory();

    public static YoutubeBulletCommentsLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public String getId(final String url) throws ParsingException {
        return YoutubeStreamLinkHandlerFactory.getInstance().getId(url);
    }

    @Override
    public boolean onAcceptUrl(final String url) throws ParsingException {
        return YoutubeStreamLinkHandlerFactory.getInstance().onAcceptUrl(url);
    }

    @Override
    public String getUrl(final String id, final List<String> contentFilter,
                         final String sortFilter) throws ParsingException {
        return YoutubeStreamLinkHandlerFactory.getInstance().getUrl(id);
    }
}
