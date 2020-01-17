package org.schabi.newpipe.extractor.services.youtube.linkHandler;

import org.schabi.newpipe.extractor.exceptions.FoundAdException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;

import java.util.List;

public class YoutubeCommentsLinkHandlerFactory extends ListLinkHandlerFactory {

    private static final YoutubeCommentsLinkHandlerFactory instance = new YoutubeCommentsLinkHandlerFactory();

    public static YoutubeCommentsLinkHandlerFactory getInstance() {
        return instance;
    }

    @Override
    public String getUrl(String id) {
        return "https://m.youtube.com/watch?v=" + id;
    }

    @Override
    public String getId(String urlString) throws ParsingException, IllegalArgumentException {
        return YoutubeStreamLinkHandlerFactory.getInstance().getId(urlString); //we need the same id, avoids duplicate code
    }

    @Override
    public boolean onAcceptUrl(final String url) throws FoundAdException {
        try {
            getId(url);
            return true;
        } catch (FoundAdException fe) {
            throw fe;
        } catch (ParsingException e) {
            return false;
        }
    }

    @Override
    public String getUrl(String id, List<String> contentFilter, String sortFilter) throws ParsingException {
        return getUrl(id);
    }
}
