package org.schabi.newpipe.extractor.services.youtube.linkHandler;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.BASE_YOUTUBE_INTENT_URL;

import org.schabi.newpipe.extractor.exceptions.FoundAdException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;

import java.util.List;

public class YoutubeCommentsLinkHandlerFactory extends ListLinkHandlerFactory {

    private static final YoutubeCommentsLinkHandlerFactory instance = new YoutubeCommentsLinkHandlerFactory();

    public static YoutubeCommentsLinkHandlerFactory getInstance() {
        return instance;
    }

    @Override
    public ListLinkHandler fromUrl(String url) throws ParsingException {
        if (url.startsWith(BASE_YOUTUBE_INTENT_URL)){
            return super.fromUrl(url, BASE_YOUTUBE_INTENT_URL);
        } else {
            return super.fromUrl(url);
        }
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
