package org.schabi.newpipe.extractor.services.youtube.shared.linkHandler;

import static org.schabi.newpipe.extractor.services.youtube.shared.YoutubeUrlHelper.isHooktubeURL;
import static org.schabi.newpipe.extractor.services.youtube.shared.YoutubeUrlHelper.isY2ubeURL;
import static org.schabi.newpipe.extractor.services.youtube.shared.YoutubeUrlHelper.isYoutubeServiceURL;
import static org.schabi.newpipe.extractor.services.youtube.shared.YoutubeUrlHelper.isYoutubeURL;

import org.schabi.newpipe.extractor.exceptions.FoundAdException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Utils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

public abstract class YoutubeLikeStreamLinkHandlerFactory extends LinkHandlerFactory
        implements YoutubeLikeLinkHandlerFactory {

    protected static final Pattern YOUTUBE_VIDEO_ID_REGEX_PATTERN
            = Pattern.compile("^([a-zA-Z0-9_-]{11})");
    protected static final List<String> SUBPATHS
            = Arrays.asList("embed/", "shorts/", "watch/", "v/", "w/");

    @Nullable
    private static String extractId(@Nullable final String id) {
        if (id == null) {
            return null;
        }

        final Matcher m = YOUTUBE_VIDEO_ID_REGEX_PATTERN.matcher(id);
        return m.find() ? m.group(1) : null;
    }

    private static String assertIsId(@Nullable final String id) throws ParsingException {
        final String extractedId = extractId(id);
        if (extractedId != null) {
            return extractedId;
        }
        throw new ParsingException("The given string is not a Youtube-Video-ID");
    }

    @Override
    public String getId(final String theUrlString)
            throws ParsingException, IllegalArgumentException {
        String urlString = theUrlString;
        try {
            final URI uri = new URI(urlString);
            final String scheme = uri.getScheme();

            if (scheme != null
                    && (scheme.equals("vnd.youtube") || scheme.equals("vnd.youtube.launch"))) {
                final String schemeSpecificPart = uri.getSchemeSpecificPart();
                if (!schemeSpecificPart.startsWith("//")) {
                    return assertIsId(schemeSpecificPart);
                }
                final String extractedId = extractId(schemeSpecificPart.substring(2));
                if (extractedId != null) {
                    return extractedId;
                }

                urlString = "https:" + schemeSpecificPart;
            }
        } catch (final URISyntaxException ignored) {
        }

        final URL url;
        try {
            url = Utils.stringToURL(urlString);
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException("The given URL is not valid");
        }

        final String host = url.getHost();
        String path = url.getPath();
        // remove leading "/" of URL-path if URL-path is given
        if (!path.isEmpty()) {
            path = path.substring(1);
        }

        if (!Utils.isHTTP(url) || !isSupportedYouTubeLikeHost(url)) {
            if ("googleads.g.doubleclick.net".equalsIgnoreCase(host)) {
                throw new FoundAdException("Error found ad: " + urlString);
            }

            throw new ParsingException("The url is not a Youtube-URL");
        }

        if (isPlaylistUrl(urlString)) {
            throw new ParsingException("This url is a playlist url: " + urlString);
        }

        if (isYoutubeURL(url) || isInvidiousUrl(url) || isHooktubeURL(url)) {
            if (isYoutubeURL(url)
                    && "attribution_link".equalsIgnoreCase(path)) {
                final String uQueryValue = Utils.getQueryValue(url, "u");

                final URL decodedURL;
                try {
                    decodedURL = Utils.stringToURL("https://www.youtube.com" + uQueryValue);
                } catch (final MalformedURLException e) {
                    throw new ParsingException("Url is invalid: " + urlString, e);
                }

                final String viewQueryValue = Utils.getQueryValue(decodedURL, "v");
                return assertIsId(viewQueryValue);

            } else if (isInvidiousUrl(url) || isHooktubeURL(url)) {
                // /watch?v=<ID>
                if ("watch".equalsIgnoreCase(path)) {
                    final String viewQueryValue = Utils.getQueryValue(url, "v");
                    if (viewQueryValue != null) {
                        return assertIsId(viewQueryValue);
                    }
                }

                // /<ID>
                final String maybeId = extractId(path);
                if (maybeId != null) {
                    return maybeId;
                }
            }

            final String maybeId = getIdFromSubpathsInPath(path);
            if (maybeId != null) {
                return maybeId;
            }

            final String viewQueryValue = Utils.getQueryValue(url, "v");
            return assertIsId(viewQueryValue);

        } else if (isYoutubeServiceURL(url) || isY2ubeURL(url)) {
            if ("youtube-nocookie.com".equalsIgnoreCase(
                    Utils.removeMAndWWWFromHost(url.getHost()))) {
                if (!path.startsWith("embed/")) {
                    throw new ParsingException("Invalid url: " + urlString);
                }
                return assertIsId(path.substring(6));
            }
            final String viewQueryValue = Utils.getQueryValue(url, "v");
            if (viewQueryValue != null) {
                return assertIsId(viewQueryValue);
            }

            return assertIsId(path);
        }

        throw new ParsingException("Error no suitable url: " + urlString);
    }

    protected abstract boolean isPlaylistUrl(String url);

    @Override
    public boolean onAcceptUrl(final String url) throws FoundAdException {
        try {
            getId(url);
            return true;
        } catch (final FoundAdException fe) {
            throw fe;
        } catch (final ParsingException e) {
            return false;
        }
    }

    private String getIdFromSubpathsInPath(final String path) throws ParsingException {
        for (final String subpath : SUBPATHS) {
            if (path.startsWith(subpath)) {
                return assertIsId(path.substring(subpath.length()));
            }
        }
        return null;
    }
}
