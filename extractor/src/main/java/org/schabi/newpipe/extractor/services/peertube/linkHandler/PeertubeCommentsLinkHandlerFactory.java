package org.schabi.newpipe.extractor.services.peertube.linkHandler;

import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.exceptions.FoundAdException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Parser;

import java.util.List;

public class PeertubeCommentsLinkHandlerFactory extends ListLinkHandlerFactory {

    private static final PeertubeCommentsLinkHandlerFactory instance = new PeertubeCommentsLinkHandlerFactory();
    private static final String ID_PATTERN = "/videos/(watch/)?([^/?&#]*)";
    private static final String COMMENTS_ENDPOINT = "/api/v1/videos/%s/comment-threads";

    public static PeertubeCommentsLinkHandlerFactory getInstance() {
        return instance;
    }

    @Override
    public String getId(String url) throws ParsingException, IllegalArgumentException {
        return Parser.matchGroup(ID_PATTERN, url, 2);
    }

    @Override
    public boolean onAcceptUrl(final String url) throws FoundAdException {
        return url.contains("/videos/");
    }

    @Override
    public String getUrl(String id, List<String> contentFilter, String sortFilter) throws ParsingException {
        String baseUrl = ServiceList.PeerTube.getBaseUrl();
        return getUrl(id, contentFilter, sortFilter, baseUrl);
    }

    @Override
    public String getUrl(String id, List<String> contentFilter, String sortFilter, String baseUrl) throws ParsingException {
        return baseUrl + String.format(COMMENTS_ENDPOINT, id);
    }

}
