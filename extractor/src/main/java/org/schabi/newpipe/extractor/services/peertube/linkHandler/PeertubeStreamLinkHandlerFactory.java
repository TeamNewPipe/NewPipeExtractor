package org.schabi.newpipe.extractor.services.peertube.linkHandler;

import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.exceptions.FoundAdException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Parser;

public class PeertubeStreamLinkHandlerFactory extends LinkHandlerFactory {

    private static final PeertubeStreamLinkHandlerFactory instance = new PeertubeStreamLinkHandlerFactory();
    private static final String ID_PATTERN = "/videos/(watch/|embed/)?([^/?&#]*)";
    public static final String VIDEO_API_ENDPOINT = "/api/v1/videos/";
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
        return Parser.matchGroup(ID_PATTERN, url, 2);
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
