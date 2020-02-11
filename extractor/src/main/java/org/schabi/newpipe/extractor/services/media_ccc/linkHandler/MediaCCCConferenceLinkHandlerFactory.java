package org.schabi.newpipe.extractor.services.media_ccc.linkHandler;

import org.schabi.newpipe.extractor.exceptions.FoundAdException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.Utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class MediaCCCConferenceLinkHandlerFactory extends ListLinkHandlerFactory {

    @Override
    public String getUrl(String id, List<String> contentFilter, String sortFilter) throws ParsingException {
        return "https://api.media.ccc.de/public/conferences/" + id;
    }

    @Override
    public String getId(String urlString) throws ParsingException {
        if (urlString.startsWith("https://api.media.ccc.de/public/conferences/")) {
            return urlString.replace("https://api.media.ccc.de/public/conferences/", "");
        } else if (urlString.startsWith("https://media.ccc.de/c/")) {
            return Parser.matchGroup1("https://media.ccc.de/c/([^?#]*)", urlString);
        }

        URL url;
        try {
            url = Utils.stringToURL(urlString);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("The given URL is not valid");
        }

        String path = url.getPath();
        // remove leading "/" of URL-path if URL-path is given
        if (!path.isEmpty()) {
            path = path.substring(1);
        }

        if (path.contains("b/")) {
            return path.substring(2);
        }

        throw new ParsingException("Could not get id from url: " + url);

    }

    @Override
    public boolean onAcceptUrl(String url) throws ParsingException {
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
