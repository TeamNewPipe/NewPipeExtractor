package org.schabi.newpipe.extractor.services.peertube.linkHandler;

import java.util.List;

import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Parser;

public class PeertubeChannelLinkHandlerFactory extends ListLinkHandlerFactory {

    private static final PeertubeChannelLinkHandlerFactory instance = new PeertubeChannelLinkHandlerFactory();
    private static final String ID_PATTERN = "/accounts/([^/?&#]*)";
    private static final String ACCOUNTS_ENDPOINT = "/api/v1/accounts/";

    public static PeertubeChannelLinkHandlerFactory getInstance() {
        return instance;
    }

    @Override
    public String getId(String url) throws ParsingException {
        return Parser.matchGroup1(ID_PATTERN, url);
    }

    @Override
    public String getUrl(String id, List<String> contentFilters, String searchFilter) throws ParsingException {
        String baseUrl = ServiceList.PeerTube.getBaseUrl();
        return getUrl(id, contentFilters, searchFilter, baseUrl);
    }
    
    @Override
    public String getUrl(String id, List<String> contentFilter, String sortFilter, String baseUrl)
            throws ParsingException {
        return baseUrl + ACCOUNTS_ENDPOINT + id;
    }

    @Override
    public boolean onAcceptUrl(String url) {
        return url.contains("/accounts/");
    }
}
