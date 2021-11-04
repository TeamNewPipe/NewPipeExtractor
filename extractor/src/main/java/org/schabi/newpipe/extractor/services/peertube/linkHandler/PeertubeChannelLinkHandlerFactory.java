package org.schabi.newpipe.extractor.services.peertube.linkHandler;

import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Parser;

import java.util.List;

public class PeertubeChannelLinkHandlerFactory extends ListLinkHandlerFactory {

    private static final PeertubeChannelLinkHandlerFactory instance = new PeertubeChannelLinkHandlerFactory();
    private static final String ID_PATTERN = "((accounts|a)|(video-channels|c))/([^/?&#]*)";
    public static final String API_ENDPOINT = "/api/v1/";

    public static PeertubeChannelLinkHandlerFactory getInstance() {
        return instance;
    }

    @Override
    public String getId(String url) throws ParsingException {
        return fixId(Parser.matchGroup(ID_PATTERN, url, 0));
    }

    @Override
    public String getUrl(String id, List<String> contentFilters, String searchFilter) throws ParsingException {
        return getUrl(id, contentFilters, searchFilter, ServiceList.PeerTube.getBaseUrl());
    }

    @Override
    public String getUrl(String id, List<String> contentFilter, String sortFilter, String baseUrl)
            throws ParsingException {
        if (id.matches(ID_PATTERN)) {
            return baseUrl + "/" + fixId(id);
        } else {
            // This is needed for compatibility with older versions were we didn't support video channels yet
            return baseUrl + "/accounts/" + id;
        }
    }

    @Override
    public boolean onAcceptUrl(String url) {
        return url.contains("/accounts/") || url.contains("/a/")
                || url.contains("/video-channels/") || url.contains("/c/");
    }

    /**
     * Fix id
     *
     * <p>
     * a/:accountName and c/:channelName ids are supported
     * by the PeerTube web client (>= v3.3.0)
     * but not by the API.
     * </p>
     *
     * @param id the id to fix
     * @return the fixed id
     */
    private String fixId(String id) {
        if (id.startsWith("a/")) {
            id = "accounts" + id.substring(1);
        } else if (id.startsWith("c/")) {
            id = "video-channels" + id.substring(1);
        }
        return id;
    }
}
