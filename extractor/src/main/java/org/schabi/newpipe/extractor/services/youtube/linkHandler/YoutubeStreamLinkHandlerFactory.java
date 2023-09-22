/*
 * Created by Christian Schabesberger on 02.02.16.
 *
 * Copyright (C) 2018 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * YoutubeStreamLinkHandlerFactory.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.schabi.newpipe.extractor.services.youtube.linkHandler;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isHooktubeURL;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isInvidiousURL;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isY2ubeURL;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isYoutubeServiceURL;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isYoutubeURL;

import org.schabi.newpipe.extractor.exceptions.FoundAdException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Utils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class YoutubeStreamLinkHandlerFactory extends LinkHandlerFactory {

    private static final Pattern YOUTUBE_VIDEO_ID_REGEX_PATTERN
            = Pattern.compile("^([a-zA-Z0-9_-]{11})");
    private static final YoutubeStreamLinkHandlerFactory INSTANCE
            = new YoutubeStreamLinkHandlerFactory();
    private static final List<String> SUBPATHS
            = List.of("embed/", "live/", "shorts/", "watch/", "v/", "w/");

    private YoutubeStreamLinkHandlerFactory() {
    }

    public static YoutubeStreamLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Nullable
    private static String extractId(@Nullable final String id) {
        if (id != null) {
            final Matcher m = YOUTUBE_VIDEO_ID_REGEX_PATTERN.matcher(id);
            return m.find() ? m.group(1) : null;
        }
        return null;
    }

    @Nonnull
    private static String assertIsId(@Nullable final String id) throws ParsingException {
        final String extractedId = extractId(id);
        if (extractedId != null) {
            return extractedId;
        } else {
            throw new ParsingException("The given string is not a YouTube video ID");
        }
    }

    @Nonnull
    @Override
    public String getUrl(final String id) throws ParsingException, UnsupportedOperationException {
        return "https://www.youtube.com/watch?v=" + id;
    }

    @SuppressWarnings("AvoidNestedBlocks")
    @Nonnull
    @Override
    public String getId(final String theUrlString)
            throws ParsingException, UnsupportedOperationException {
        String urlString = theUrlString;
        try {
            final URI uri = new URI(urlString);
            final String scheme = uri.getScheme();

            if (scheme != null
                    && (scheme.equals("vnd.youtube") || scheme.equals("vnd.youtube.launch"))) {
                final String schemeSpecificPart = uri.getSchemeSpecificPart();
                if (schemeSpecificPart.startsWith("//")) {
                    final String extractedId = extractId(schemeSpecificPart.substring(2));
                    if (extractedId != null) {
                        return extractedId;
                    }

                    urlString = "https:" + schemeSpecificPart;
                } else {
                    return assertIsId(schemeSpecificPart);
                }
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

        if (!Utils.isHTTP(url) || !(isYoutubeURL(url) || isYoutubeServiceURL(url)
                || isHooktubeURL(url) || isInvidiousURL(url) || isY2ubeURL(url))) {
            if (host.equalsIgnoreCase("googleads.g.doubleclick.net")) {
                throw new FoundAdException("Error: found ad: " + urlString);
            }

            throw new ParsingException("The URL is not a YouTube URL");
        }

        if (YoutubePlaylistLinkHandlerFactory.getInstance().acceptUrl(urlString)) {
            throw new ParsingException("Error: no suitable URL: " + urlString);
        }

        // Using uppercase instead of lowercase, because toLowercase replaces some unicode
        // characters with their lowercase ASCII equivalent. Using toLowercase could result in
        // faultily matching unicode urls.
        switch (host.toUpperCase()) {
            case "WWW.YOUTUBE-NOCOOKIE.COM": {
                if (path.startsWith("embed/")) {
                    return assertIsId(path.substring(6));
                }
                break;
            }

            case "YOUTUBE.COM":
            case "WWW.YOUTUBE.COM":
            case "M.YOUTUBE.COM":
            case "MUSIC.YOUTUBE.COM": {
                if (path.equals("attribution_link")) {
                    final String uQueryValue = Utils.getQueryValue(url, "u");

                    final URL decodedURL;
                    try {
                        decodedURL = Utils.stringToURL("https://www.youtube.com" + uQueryValue);
                    } catch (final MalformedURLException e) {
                        throw new ParsingException("Error: no suitable URL: " + urlString);
                    }

                    final String viewQueryValue = Utils.getQueryValue(decodedURL, "v");
                    return assertIsId(viewQueryValue);
                }

                final String maybeId = getIdFromSubpathsInPath(path);
                if (maybeId != null) {
                    return maybeId;
                }

                final String viewQueryValue = Utils.getQueryValue(url, "v");
                return assertIsId(viewQueryValue);
            }

            case "Y2U.BE":
            case "YOUTU.BE": {
                final String viewQueryValue = Utils.getQueryValue(url, "v");
                if (viewQueryValue != null) {
                    return assertIsId(viewQueryValue);
                }

                return assertIsId(path);
            }

            case "HOOKTUBE.COM":
            case "INVIDIO.US":
            case "DEV.INVIDIO.US":
            case "WWW.INVIDIO.US":
            case "REDIRECT.INVIDIOUS.IO":
            case "INVIDIOUS.SNOPYTA.ORG":
            case "YEWTU.BE":
            case "TUBE.CONNECT.CAFE":
            case "TUBUS.EDUVID.ORG":
            case "INVIDIOUS.KAVIN.ROCKS":
            case "INVIDIOUS-US.KAVIN.ROCKS":
            case "PIPED.KAVIN.ROCKS":
            case "INVIDIOUS.SITE":
            case "VID.MINT.LGBT":
            case "INVIDIOU.SITE":
            case "INVIDIOUS.FDN.FR":
            case "INVIDIOUS.048596.XYZ":
            case "INVIDIOUS.ZEE.LI":
            case "VID.PUFFYAN.US":
            case "YTPRIVATE.COM":
            case "INVIDIOUS.NAMAZSO.EU":
            case "INVIDIOUS.SILKKY.CLOUD":
            case "INVIDIOUS.EXONIP.DE":
            case "INV.RIVERSIDE.ROCKS":
            case "INVIDIOUS.BLAMEFRAN.NET":
            case "INVIDIOUS.MOOMOO.ME":
            case "YTB.TROM.TF":
            case "YT.CYBERHOST.UK":
            case "Y.COM.CM": { // code-block for hooktube.com and Invidious instances
                if (path.equals("watch")) {
                    final String viewQueryValue = Utils.getQueryValue(url, "v");
                    if (viewQueryValue != null) {
                        return assertIsId(viewQueryValue);
                    }
                }
                final String maybeId = getIdFromSubpathsInPath(path);
                if (maybeId != null) {
                    return maybeId;
                }

                final String viewQueryValue = Utils.getQueryValue(url, "v");
                if (viewQueryValue != null) {
                    return assertIsId(viewQueryValue);
                }

                return assertIsId(path);
            }
        }

        throw new ParsingException("Error: no suitable URL: " + urlString);
    }

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

    @Nullable
    private String getIdFromSubpathsInPath(@Nonnull final String path) throws ParsingException {
        for (final String subpath : SUBPATHS) {
            if (path.startsWith(subpath)) {
                final String id = path.substring(subpath.length());
                return assertIsId(id);
            }
        }
        return null;
    }
}
