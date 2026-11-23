package org.schabi.newpipe.extractor.services.peertube.linkHandler;

import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.exceptions.FoundAdException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.Utils;

import java.net.URL;

public final class PeertubeStreamLinkHandlerFactory extends LinkHandlerFactory {

    private static final PeertubeStreamLinkHandlerFactory INSTANCE
            = new PeertubeStreamLinkHandlerFactory();
    private static final String ID_PATTERN = "(/w/|(/videos/(watch/|embed/)?))(?!p/)([^/?&#]*)";
    // we exclude p/ because /w/p/ is playlist, not video
    public static final String VIDEO_API_ENDPOINT = "/api/v1/videos/";
    public static final String API_ENDPOINT = "/api/v1/";

    // From PeerTube 3.3.0, the default path is /w/.
    // We still use /videos/watch/ for compatibility reasons:
    // /videos/watch/ is still accepted by >=3.3.0 but /w/ isn't by <3.3.0
    private static final String VIDEO_PATH = "/videos/watch/";

    private PeertubeStreamLinkHandlerFactory() {
    }

    public static PeertubeStreamLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public String getUrl(final String id) throws ParsingException, UnsupportedOperationException {
        return getUrl(id, ServiceList.PeerTube.getBaseUrl());
    }

    @Override
    public String getUrl(final String id, final String baseUrl) {
        return baseUrl + VIDEO_PATH + id;
    }

    @Override
    public String getId(final String url) throws ParsingException, UnsupportedOperationException {
        return Parser.matchGroup(ID_PATTERN, url, 4);
    }

    @Override
    public boolean onAcceptUrl(final String url) throws FoundAdException {
        if (url.contains("/playlist/")) {
            return false;
        }
        try {
            final URL urlObj = Utils.stringToURL(url);
            if (!Utils.isHTTP(urlObj)) {
                return false;
            }
            String path = urlObj.getPath();
            if (path.startsWith(API_ENDPOINT)) {
                // -1 because we want to keep the leading slash
                path = path.substring(API_ENDPOINT.length() - 1);
            }
            // Ensure the URL path starts with a PeerTube-specific prefix.
            // Without this check, any URL containing /w/ (e.g. https://1drv.ms/w/s!...)
            // would be falsely accepted as a PeerTube stream.
            if (!path.startsWith("/w/") && !path.startsWith("/videos/")
                    && !path.startsWith(VIDEO_API_ENDPOINT)) {
                return false;
            }
            // The id must be the last path segment: https://1drv.ms/w/c/<a>/<b> must not
            // be read as id "c". PeerTube ids are UUIDs, short UUIDs or numbers.
            final String id = getId(url);
            return id.matches("[A-Za-z0-9_-]+")
                    && path.matches("/(w|videos/watch|videos/embed|videos|api/v1/videos)/"
                    + java.util.regex.Pattern.quote(id) + "/?");
        } catch (final ParsingException | IllegalArgumentException
                | java.net.MalformedURLException e) {
            return false;
        }
    }
}
