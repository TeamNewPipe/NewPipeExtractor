package org.schabi.newpipe.extractor.services.youtube.linkHandler;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import org.schabi.newpipe.extractor.exceptions.FoundAdException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Parser;

public class YoutubeCommentsLinkHandlerFactory extends ListLinkHandlerFactory {

    private static final YoutubeCommentsLinkHandlerFactory instance = new YoutubeCommentsLinkHandlerFactory();
    private static final String ID_PATTERN = "([\\-a-zA-Z0-9_]{11})";

    public static YoutubeCommentsLinkHandlerFactory getInstance() {
        return instance;
    }

    @Override
    public String getUrl(String id) {
        return "https://m.youtube.com/watch?v=" + id;
    }

    @Override
    public String getId(String url) throws ParsingException, IllegalArgumentException {
        if (url.isEmpty()) {
            throw new IllegalArgumentException("The url parameter should not be empty");
        }

        String id;
        String lowercaseUrl = url.toLowerCase();
        if (lowercaseUrl.contains("youtube")) {
            if (url.contains("attribution_link")) {
                try {
                    String escapedQuery = Parser.matchGroup1("u=(.[^&|$]*)", url);
                    String query = URLDecoder.decode(escapedQuery, "UTF-8");
                    id = Parser.matchGroup1("v=" + ID_PATTERN, query);
                } catch (UnsupportedEncodingException uee) {
                    throw new ParsingException("Could not parse attribution_link", uee);
                }
            } else if (url.contains("vnd.youtube")) {
                id = Parser.matchGroup1(ID_PATTERN, url);
            } else if (url.contains("embed")) {
                id = Parser.matchGroup1("embed/" + ID_PATTERN, url);
            } else if (url.contains("googleads")) {
                throw new FoundAdException("Error found add: " + url);
            } else {
                id = Parser.matchGroup1("[?&]v=" + ID_PATTERN, url);
            }
        } else if (lowercaseUrl.contains("youtu.be")) {
            if (url.contains("v=")) {
                id = Parser.matchGroup1("v=" + ID_PATTERN, url);
            } else {
                id = Parser.matchGroup1("[Yy][Oo][Uu][Tt][Uu]\\.[Bb][Ee]/" + ID_PATTERN, url);
            }
        } else if(lowercaseUrl.contains("hooktube")) {
            if(lowercaseUrl.contains("&v=")
                    || lowercaseUrl.contains("?v=")) {
                id = Parser.matchGroup1("[?&]v=" + ID_PATTERN, url);
            } else if (url.contains("/embed/")) {
                id = Parser.matchGroup1("embed/" + ID_PATTERN, url);
            } else if (url.contains("/v/")) {
                id = Parser.matchGroup1("v/" + ID_PATTERN, url);
            } else if (url.contains("/watch/")) {
                id = Parser.matchGroup1("watch/" + ID_PATTERN, url);
            } else {
                throw new ParsingException("Error no suitable url: " + url);
            }
        } else {
            throw new ParsingException("Error no suitable url: " + url);
        }


        if (!id.isEmpty()) {
            return id;
        } else {
            throw new ParsingException("Error could not parse url: " + url);
        }
    }

    @Override
    public boolean onAcceptUrl(final String url) throws FoundAdException {
        final String lowercaseUrl = url.toLowerCase();
        if (lowercaseUrl.contains("youtube")
                || lowercaseUrl.contains("youtu.be")
                || lowercaseUrl.contains("hooktube")) {
            // bad programming I know
            try {
                getId(url);
                return true;
            } catch (FoundAdException fe) {
                throw fe;
            } catch (ParsingException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public String getUrl(String id, List<String> contentFilter, String sortFilter) throws ParsingException {
        return getUrl(id);
    }
}
