package org.schabi.newpipe.extractor.services.youtube.shared.linkHandler;

import org.schabi.newpipe.extractor.exceptions.FoundAdException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;

import java.util.List;

public abstract class YoutubeLikeCommentsLinkHandlerFactory extends ListLinkHandlerFactory {

    protected final YoutubeLikeStreamLinkHandlerFactory streamLinkHandlerFactory;

    protected YoutubeLikeCommentsLinkHandlerFactory(
            final YoutubeLikeStreamLinkHandlerFactory streamLinkHandlerFactory
    ) {
        this.streamLinkHandlerFactory = streamLinkHandlerFactory;
    }

    @Override
    public String getUrl(final String id) throws ParsingException {
        return streamLinkHandlerFactory.getUrl(id);
    }

    @Override
    public String getId(final String urlString) throws ParsingException, IllegalArgumentException {
        // we need the same id, avoids duplicate code
        return streamLinkHandlerFactory.getId(urlString);
    }

    @Override
    public boolean onAcceptUrl(final String url) throws FoundAdException {
        return streamLinkHandlerFactory.onAcceptUrl(url);
    }

    @Override
    public String getUrl(final String id,
                         final List<String> contentFilter,
                         final String sortFilter) throws ParsingException {
        return getUrl(id);
    }
}
