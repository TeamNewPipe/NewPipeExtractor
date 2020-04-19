package org.schabi.newpipe.extractor.services.media_ccc.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Utils;

import java.net.MalformedURLException;
import java.net.URL;

public class MediaCCCStreamLinkHandlerFactory extends LinkHandlerFactory {
    @Override
    public String getId(final String urlString) throws ParsingException {
        if (urlString.startsWith("https://media.ccc.de/public/events/")
                && !urlString.contains("?q=")) {
            return urlString.substring(35); //remove …/public/events part
        }

        if (urlString.startsWith("https://api.media.ccc.de/public/events/")
                && !urlString.contains("?q=")) {
            return urlString.substring(39); //remove api…/public/events part
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

        if (path.startsWith("v/")) {
            return path.substring(2);
        }

        throw new ParsingException("Could not get id from url: " + url);
    }

    @Override
    public String getUrl(final String id) throws ParsingException {
        return "https://media.ccc.de/public/events/" + id;
    }

    @Override
    public boolean onAcceptUrl(final String url) {
        try {
            getId(url);
            return true;
        } catch (ParsingException e) {
            return false;
        }
    }
}
