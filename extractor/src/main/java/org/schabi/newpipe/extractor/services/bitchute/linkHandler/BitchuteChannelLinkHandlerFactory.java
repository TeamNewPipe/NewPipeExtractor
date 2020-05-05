package org.schabi.newpipe.extractor.services.bitchute.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class BitchuteChannelLinkHandlerFactory extends ListLinkHandlerFactory {

    private static final BitchuteChannelLinkHandlerFactory instance = new BitchuteChannelLinkHandlerFactory();

    public static BitchuteChannelLinkHandlerFactory getInstance() {
        return instance;
    }

    private static String assertsID(String id) throws ParsingException {
        if (id == null || !id.matches("[a-zA-Z0-9_-]{11,}")) {
            throw new ParsingException("Given string is not a Bitchute Channel ID");
        }
        return id;
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
            if (splitPath[0].equalsIgnoreCase("channel"))
                return assertsID(splitPath[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ParsingException("Error getting ID");
        }
        throw new ParsingException("Error url not suitable: " + urlString);
    }

    @Override
    public String getUrl(String id, List<String> contentFilter, String sortFilter) throws ParsingException {
        return "https://www.bitchute.com/channel/" + id;
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
