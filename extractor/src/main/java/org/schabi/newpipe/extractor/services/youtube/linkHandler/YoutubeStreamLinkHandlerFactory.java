package org.schabi.newpipe.extractor.services.youtube.linkHandler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.exceptions.FoundAdException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.utils.Parser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

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
    private static final String ID_PATTERN = "([\\-a-zA-Z0-9_]{11})";

    private YoutubeStreamLinkHandlerFactory() {
    }

    public static YoutubeStreamLinkHandlerFactory getInstance() {
        return instance;
    }

    @Override
    public String getUrl(String id) {
        return "https://www.youtube.com/watch?v=" + id;
    }

    @Override
    public String getId(String url) throws ParsingException, IllegalArgumentException {
        if (url.isEmpty()) {
            throw new IllegalArgumentException("The url parameter should not be empty");
        }

        String lowercaseUrl = url.toLowerCase();
        if (lowercaseUrl.contains("youtube")) {
            if (lowercaseUrl.contains("list=")) {
                throw new ParsingException("Error no suitable url: " + url);
            }
            if (url.contains("attribution_link")) {
                try {
                    String escapedQuery = Parser.matchGroup1("u=(.[^&|$]*)", url);
                    String query = URLDecoder.decode(escapedQuery, "UTF-8");
                    return Parser.matchGroup1("v=" + ID_PATTERN, query);
                } catch (UnsupportedEncodingException uee) {
                    throw new ParsingException("Could not parse attribution_link", uee);
                }
            }
            if (url.contains("vnd.youtube")) {
                return Parser.matchGroup1(ID_PATTERN, url);
            }
            if (url.contains("embed")) {
                return Parser.matchGroup1("embed/" + ID_PATTERN, url);
            }
            if (url.contains("googleads")) {
                throw new FoundAdException("Error found add: " + url);
            }
            return Parser.matchGroup1("[?&]v=" + ID_PATTERN, url);
        }
        if (lowercaseUrl.contains("youtu.be")) {
            if (lowercaseUrl.contains("list=")) {
                throw new ParsingException("Error no suitable url: " + url);
            }
            if (url.contains("v=")) {
                return Parser.matchGroup1("v=" + ID_PATTERN, url);
            }
            return Parser.matchGroup1("[Yy][Oo][Uu][Tt][Uu]\\.[Bb][Ee]/" + ID_PATTERN, url);
        }
        if (lowercaseUrl.contains("hooktube")) {
            if (lowercaseUrl.contains("&v=")
                    || lowercaseUrl.contains("?v=")) {
                return Parser.matchGroup1("[?&]v=" + ID_PATTERN, url);
            }
            if (url.contains("/embed/")) {
                return Parser.matchGroup1("embed/" + ID_PATTERN, url);
            }
            if (url.contains("/v/")) {
                return Parser.matchGroup1("v/" + ID_PATTERN, url);
            }
            if (url.contains("/watch/")) {
                return Parser.matchGroup1("watch/" + ID_PATTERN, url);
            }
        }
        throw new ParsingException("Error no suitable url: " + url);
    }

    @Override
    public boolean onAcceptUrl(final String url) throws FoundAdException {
        final String lowercaseUrl = url.toLowerCase();
        if (!lowercaseUrl.contains("youtube")  &&
            !lowercaseUrl.contains("youtu.be") &&
            !lowercaseUrl.contains("hooktube")) {
            return false;
            // bad programming I know <-- nice meme
        }
        try {
            getId(url);
            return true;
        } catch (FoundAdException fe) {
            throw fe;
        } catch (ParsingException e) {
            return false;
        }
    }
}
