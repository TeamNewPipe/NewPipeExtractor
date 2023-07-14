package org.schabi.newpipe.extractor.services.youtube.linkHandler;

import org.schabi.newpipe.extractor.exceptions.FoundAdException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;

import java.util.List;

public final class YoutubeCommentsLinkHandlerFactory extends ListLinkHandlerFactory {

    private static final YoutubeCommentsLinkHandlerFactory INSTANCE
            = new YoutubeCommentsLinkHandlerFactory();

    private YoutubeCommentsLinkHandlerFactory() {
    }

    public static YoutubeCommentsLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public String getUrl(final String id) throws ParsingException, UnsupportedOperationException {
        return "https://www.youtube.com/watch?v=" + id;
    }

    @Override
    public String getId(final String urlString)
            throws ParsingException, UnsupportedOperationException {
        // We need the same id, avoids duplicate code
        return YoutubeStreamLinkHandlerFactory.getInstance().getId(urlString);
    }

    @Override
    public boolean onAcceptUrl(final String url) throws FoundAdException {
        try {
            getId(url);
            return true;
        } catch (final FoundAdException fe) {
            throw fe;
        } catch (final ParsingException e) {
            return false;
        }
    }

    @Override
    public String getUrl(final String id,
                         final List<String> contentFilter,
                         final String sortFilter)
            throws ParsingException, UnsupportedOperationException {
        return getUrl(id);
    }
}
