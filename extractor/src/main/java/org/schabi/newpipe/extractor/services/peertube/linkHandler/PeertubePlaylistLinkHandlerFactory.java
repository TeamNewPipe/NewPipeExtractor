package org.schabi.newpipe.extractor.services.peertube.linkHandler;


import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Parser;

import java.util.List;

public class PeertubePlaylistLinkHandlerFactory extends ListLinkHandlerFactory {
    private static final PeertubePlaylistLinkHandlerFactory instance = new PeertubePlaylistLinkHandlerFactory();
    private static final String ID_PATTERN = "/videos/watch/playlist/([^/?&#]*)";
    private static StreamingService service;

    public static PeertubePlaylistLinkHandlerFactory getInstance(StreamingService service) {
        PeertubePlaylistLinkHandlerFactory.service = service;
        return instance;
    }

    @Override
    public String getUrl(String id, List<String> contentFilters, String sortFilter) {
        String baseUrl = service.getBaseUrl();
        return getUrl(id, contentFilters, sortFilter, baseUrl);
    }

    @Override
    public String getUrl(String id, List<String> contentFilters, String sortFilter, String baseUrl) {
        return baseUrl + "/api/v1/video-playlists/" + id;
    }

    @Override
    public String getId(String url) throws ParsingException {
        return Parser.matchGroup1(ID_PATTERN, url);
    }

    @Override
    public boolean onAcceptUrl(final String url) {
        try {
            getId(url);
            return true;
        } catch (ParsingException e) {
            return false;
        }
    }
}
