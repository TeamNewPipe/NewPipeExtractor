package org.schabi.newpipe.extractor.services.peertube.linkHandler;

import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Parser;

import javax.annotation.Nonnull;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public final class PeertubeChannelLinkHandlerFactory extends ListLinkHandlerFactory {

    private static final PeertubeChannelLinkHandlerFactory INSTANCE
            = new PeertubeChannelLinkHandlerFactory();
    private static final String ID_PATTERN = "((accounts|a)|(video-channels|c))/([^/?&#]*)";
    private static final String ID_URL_PATTERN = "/((accounts|a)|(video-channels|c))/([^/?&#]*)";
    public static final String API_ENDPOINT = "/api/v1/";

    private PeertubeChannelLinkHandlerFactory() {
    }

    public static PeertubeChannelLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public String getId(final String url) throws ParsingException, UnsupportedOperationException {
        return fixId(Parser.matchGroup(ID_URL_PATTERN, url, 0));
    }

    @Override
    public String getUrl(final String id,
                         final List<String> contentFilters,
                         final String searchFilter)
            throws ParsingException, UnsupportedOperationException {
        return getUrl(id, contentFilters, searchFilter, ServiceList.PeerTube.getBaseUrl());
    }

    @Override
    public String getUrl(final String id,
                         final List<String> contentFilter,
                         final String sortFilter,
                         final String baseUrl)
            throws ParsingException, UnsupportedOperationException {
        if (id.matches(ID_PATTERN)) {
            return baseUrl + "/" + fixId(id);
        } else {
            // This is needed for compatibility with older versions were we didn't support
            // video channels yet
            return baseUrl + "/accounts/" + id;
        }
    }

    @Override
    public boolean onAcceptUrl(final String url) {
        // Only the first path segment counts, otherwise e.g. https://1drv.ms/w/c/... matches
        final String path;
        try {
            path = new URL(url).getPath();
        } catch (final IllegalArgumentException | MalformedURLException e) {
            return false;
        }
        if (path == null) {
            return false;
        }
        final String p = path.startsWith(API_ENDPOINT)
                ? path.substring(API_ENDPOINT.length() - 1) : path;
        return p.startsWith("/accounts/") || p.startsWith("/a/")
                || p.startsWith("/video-channels/") || p.startsWith("/c/");
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
    private String fixId(@Nonnull final String id) {
        final String cleanedId = id.startsWith("/") ? id.substring(1) : id;
        if (cleanedId.startsWith("a/")) {
            return "accounts" + cleanedId.substring(1);
        } else if (cleanedId.startsWith("c/")) {
            return "video-channels" + cleanedId.substring(1);
        }
        return cleanedId;
    }
}
