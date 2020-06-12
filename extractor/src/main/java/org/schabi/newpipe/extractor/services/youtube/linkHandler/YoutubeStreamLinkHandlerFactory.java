package org.schabi.newpipe.extractor.services.youtube.linkHandler;

import org.schabi.newpipe.extractor.Instance;
import org.schabi.newpipe.extractor.exceptions.FoundAdException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.annotation.Nullable;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.BASE_YOUTUBE_INTENT_URL;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isHooktubeURL;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isInvidiousURL;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isYoutubeURL;
import static org.schabi.newpipe.extractor.utils.Utils.getQueryValue;
import static org.schabi.newpipe.extractor.utils.Utils.isHTTP;
import static org.schabi.newpipe.extractor.utils.Utils.stringToURL;

/*
 * Created by Christian Schabesberger on 02.02.16.
 *
 * Copyright (C) Christian Schabesberger 2018 <chris.schabesberger@mailbox.org>
 * YoutubeStreamLinkHandlerFactory.java is part of NewPipe.
 *
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

public class YoutubeStreamLinkHandlerFactory extends LinkHandlerFactory {

    private static final YoutubeStreamLinkHandlerFactory instance = new YoutubeStreamLinkHandlerFactory();
    private static Instance invidiousInstance;

    private YoutubeStreamLinkHandlerFactory() {
    }

    public static YoutubeStreamLinkHandlerFactory getInstance() {
        return instance;
    }

    public static YoutubeStreamLinkHandlerFactory getInstance(final Instance invidiousInstance) {
        YoutubeStreamLinkHandlerFactory.invidiousInstance = invidiousInstance;
        return instance;
    }

    private static boolean isId(@Nullable String id) {
        return id != null && id.matches("[a-zA-Z0-9_-]{11}");
    }

    private static String assertIsId(@Nullable String id) throws ParsingException {
        if (isId(id)) {
            return id;
        } else {
            throw new ParsingException("The given string is not a Youtube-Video-ID");
        }
    }

    @Override
    public LinkHandler fromUrl(String url) throws ParsingException {
        if (url.startsWith(BASE_YOUTUBE_INTENT_URL)) {
            return super.fromUrl(url, BASE_YOUTUBE_INTENT_URL);
        } else {
            return super.fromUrl(url);
        }
    }

    @Override
    public String getUrl(String id) {
        if (useInvidiousBackend() && invidiousInstance.isValid()) {
            return invidiousInstance.getUrl();
        } else {
            return "https://www.youtube.com/watch?v=" + id;
        }
    }

    @Override
    public String getId(String urlString) throws ParsingException, IllegalArgumentException {
        try {
            URI uri = new URI(urlString);
            String scheme = uri.getScheme();

            if (scheme != null && (scheme.equals("vnd.youtube") || scheme.equals("vnd.youtube.launch"))) {
                String schemeSpecificPart = uri.getSchemeSpecificPart();
                if (schemeSpecificPart.startsWith("//")) {
                    final String possiblyId = schemeSpecificPart.substring(2);
                    if (isId(possiblyId)) {
                        return possiblyId;
                    }

                    urlString = "https:" + schemeSpecificPart;
                } else {
                    return assertIsId(schemeSpecificPart);
                }
            }
        } catch (URISyntaxException ignored) {
        }

        URL url;
        try {
            url = stringToURL(urlString);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("The given URL is not valid");
        }

        String host = url.getHost();
        String path = url.getPath();
        // remove leading "/" of URL-path if URL-path is given
        if (!path.isEmpty()) {
            path = path.substring(1);
        }

        final boolean isInvidiousUrl = isInvidiousURL(url); // save it to only call once it.

        if (!isHTTP(url) || !(isYoutubeURL(url) || isHooktubeURL(url) || isInvidiousUrl)) {
            if (host.equalsIgnoreCase("googleads.g.doubleclick.net")) {
                throw new FoundAdException("Error found ad: " + urlString);
            }

            throw new ParsingException("The url is not a Youtube-URL");
        }

        if (YoutubePlaylistLinkHandlerFactory.getInstance().acceptUrl(urlString)) {
            throw new ParsingException("Error no suitable url: " + urlString);
        }

        // using uppercase instead of lowercase, because toLowercase replaces some unicode characters
        // with their lowercase ASCII equivalent. Using toLowercase could result in faultily matching unicode urls.
        final String hostUpperCase = host.toUpperCase();

        if (hostUpperCase.equals("WWW.YOUTUBE-NOCOOKIE.COM") && path.startsWith("embed/")) {
            String id = path.split("/")[1];
            return assertIsId(id);
        } else if (hostUpperCase.equals("YOUTUBE.COM") || hostUpperCase.equals("WWW.YOUTUBE.COM")
                || hostUpperCase.equals("M.YOUTUBE.COM") || hostUpperCase.equals("MUSIC.YOUTUBE.COM")) {

            if (path.equals("attribution_link")) {
                String uQueryValue = getQueryValue(url, "u");

                URL decodedURL;
                try {
                    decodedURL = stringToURL("http://www.youtube.com" + uQueryValue);
                } catch (MalformedURLException e) {
                    throw new ParsingException("Error no suitable url: " + urlString);
                }

                String viewQueryValue = getQueryValue(decodedURL, "v");
                return assertIsId(viewQueryValue);
            }

            if (path.startsWith("embed/")) {
                String id = path.split("/")[1];

                return assertIsId(id);
            }

            String viewQueryValue = getQueryValue(url, "v");
            return assertIsId(viewQueryValue);
        } else if (hostUpperCase.equals("YOUTU.BE")) {

            String viewQueryValue = getQueryValue(url, "v");

            if (viewQueryValue != null) {
                return assertIsId(viewQueryValue);
            }

            return assertIsId(path);

        } else {
            if (hostUpperCase.equals("HOOKTUBE.COM")) {
                if (path.startsWith("v/")) {
                    String id = path.substring("v/".length());

                    return assertIsId(id);
                }
                if (path.startsWith("watch/")) {
                    String id = path.substring("watch/".length());

                    return assertIsId(id);
                }
            }

            if (hostUpperCase.equals("HOOKTUBE.COM") || isInvidiousUrl) {
                if (path.equals("watch")) {
                    String viewQueryValue = getQueryValue(url, "v");
                    if (viewQueryValue != null) {
                        return assertIsId(viewQueryValue);
                    }
                }
                if (path.startsWith("embed/")) {
                    String id = path.substring("embed/".length());

                    return assertIsId(id);
                }

                String viewQueryValue = getQueryValue(url, "v");
                if (viewQueryValue != null) {
                    return assertIsId(viewQueryValue);
                }

                return assertIsId(path);
            }

        }

        throw new ParsingException("Error no suitable url: " + urlString);

    }

    @Override
    public boolean onAcceptUrl(final String url) throws FoundAdException {
        try {
            getId(url);
            return true;
        } catch (FoundAdException fe) {
            throw fe;
        } catch (ParsingException e) {
            return false;
        }
    }

    private boolean useInvidiousBackend() {
        return invidiousInstance != null;
    }
}
