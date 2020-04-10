package org.schabi.newpipe.extractor.services.peertube.linkHandler;

import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Parser;

import java.util.List;

public class PeertubeChannelLinkHandlerFactory extends ListLinkHandlerFactory {

    private static final PeertubeChannelLinkHandlerFactory instance = new PeertubeChannelLinkHandlerFactory();
    private static final String ID_PATTERN = "(accounts|video-channels)/([^/?&#]*)";
    private static final String API_ENDPOINT = "/api/v1/";

    public static PeertubeChannelLinkHandlerFactory getInstance() {
        return instance;
    }

    @Override
    public String getId(String url) throws ParsingException {
        return Parser.matchGroup(ID_PATTERN, url, 0);
    }

    @Override
    public String getUrl(String id, List<String> contentFilters, String searchFilter) throws ParsingException {
        String baseUrl = ServiceList.PeerTube.getBaseUrl();
        return getUrl(id, contentFilters, searchFilter, baseUrl);
    }

    @Override
    public String getUrl(String id, List<String> contentFilter, String sortFilter, String baseUrl)
            throws ParsingException {

        if (id.matches(ID_PATTERN)) {
            return baseUrl + API_ENDPOINT + id;
        } else {
            // This is needed for compatibility with older versions were we didn't support video channels yet
            return baseUrl + API_ENDPOINT + "accounts/" + id;
        }
    }

    @Override
    public boolean onAcceptUrl(String url) {
        return url.contains("/accounts/") || url.contains("/video-channels/");
    }
}
