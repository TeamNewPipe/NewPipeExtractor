package org.schabi.newpipe.extractor.services.bitchute.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Utils;

import java.net.MalformedURLException;
import java.net.URL;

public class BitchuteStreamLinkHandlerFactory extends LinkHandlerFactory {

    private static final BitchuteStreamLinkHandlerFactory instance = new BitchuteStreamLinkHandlerFactory();

    public static BitchuteStreamLinkHandlerFactory getInstance() {
        return instance;
    }

    private static String assertsID(String id) throws ParsingException {
        if (id == null || !id.matches("[a-zA-Z0-9_-]")) {
            throw new ParsingException("Given string is not a Bitchute Video ID");
        }
        return id;
    }

    @Override
    public String getUrl(String id) {
        return "https://www.bitchute.com/video/" + id;
    }

    @Override
    public String getId(String urlString) throws ParsingException, IllegalArgumentException {
        URL url;
        try {
            url = Utils.stringToURL(urlString);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("The given URL is not valid");
        }

        String path = url.getPath();

        if (!path.isEmpty()) {
            //remove leading "/"
            path = path.substring(1);
        }

        try {
            String[] splitPath = path.split("/", 0);
            if (splitPath[0].equalsIgnoreCase("video"))
                return assertsID(splitPath[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ParsingException("Error getting ID");
        }
        throw new ParsingException("Error url not suitable: " + urlString);
    }

    @Override
    public boolean onAcceptUrl(String url) throws ParsingException {
        try {
            getId(url);
            return true;
        } catch (ParsingException e) {
            return false;
        }
    }
}