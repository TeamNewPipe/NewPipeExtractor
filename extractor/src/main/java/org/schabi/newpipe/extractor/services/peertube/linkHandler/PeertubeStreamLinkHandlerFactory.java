package org.schabi.newpipe.extractor.services.peertube.linkHandler;

import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.exceptions.FoundAdException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Parser;

public class PeertubeStreamLinkHandlerFactory extends LinkHandlerFactory {

    private static final PeertubeStreamLinkHandlerFactory instance = new PeertubeStreamLinkHandlerFactory();
    private static final String ID_PATTERN = "(/w/|(/videos/(watch/|embed/)?))(?!p/)([^/?&#]*)";
    // we exclude p/ because /w/p/ is playlist, not video
    public static final String VIDEO_API_ENDPOINT = "/api/v1/videos/";

    // From PeerTube 3.3.0, the default path is /w/.
    // We still use /videos/watch/ for compatibility reasons:
    // /videos/watch/ is still accepted by >=3.3.0 but /w/ isn't by <3.3.0
    private static final String VIDEO_PATH = "/videos/watch/";

    private PeertubeStreamLinkHandlerFactory() {
    }

    public static PeertubeStreamLinkHandlerFactory getInstance() {
        return instance;
    }

    @Override
    public String getUrl(String id) {
        return getUrl(id, ServiceList.PeerTube.getBaseUrl());
    }

    @Override
    public String getUrl(String id, String baseUrl) {
        return baseUrl + VIDEO_PATH + id;
    }

    @Override
    public String getId(String url) throws ParsingException, IllegalArgumentException {
        return Parser.matchGroup(ID_PATTERN, url, 4);
    }

    @Override
    public boolean onAcceptUrl(final String url) throws FoundAdException {
        if (url.contains("/playlist/")) return false;
        try {
            getId(url);
            return true;
        } catch (ParsingException e) {
            return false;
        }
    }
}
