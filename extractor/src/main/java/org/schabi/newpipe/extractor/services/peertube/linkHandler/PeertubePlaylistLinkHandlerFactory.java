package org.schabi.newpipe.extractor.services.peertube.linkHandler;


import java.util.List;

import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Parser;

public class PeertubePlaylistLinkHandlerFactory extends ListLinkHandlerFactory {

    private static final PeertubePlaylistLinkHandlerFactory instance = new PeertubePlaylistLinkHandlerFactory();
    private static final String ID_PATTERN = "/video-channels/([^/?&#]*)";
    private static final String VIDEO_CHANNELS_ENDPOINT = "/api/v1/video-channels/";

    public static PeertubePlaylistLinkHandlerFactory getInstance() {
        return instance;
    }

    @Override
    public String getUrl(String id, List<String> contentFilters, String sortFilter) {
        String baseUrl = ServiceList.PeerTube.getBaseUrl();
        return getUrl(id, contentFilters, sortFilter, baseUrl);
    }
    
    @Override
    public String getUrl(String id, List<String> contentFilters, String sortFilter, String baseUrl) {
        return baseUrl + VIDEO_CHANNELS_ENDPOINT + id;
    }
    
    @Override
    public String getId(String url) throws ParsingException {
        return Parser.matchGroup1(ID_PATTERN, url);
    }


    @Override
    public boolean onAcceptUrl(final String url) {
        return url.contains("/video-channels/");
    }
}
