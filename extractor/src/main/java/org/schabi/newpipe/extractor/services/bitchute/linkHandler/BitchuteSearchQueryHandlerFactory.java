package org.schabi.newpipe.extractor.services.bitchute.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class BitchuteSearchQueryHandlerFactory extends SearchQueryHandlerFactory {

    private static BitchuteSearchQueryHandlerFactory instance = new BitchuteSearchQueryHandlerFactory();

    public static BitchuteSearchQueryHandlerFactory getInstance() {
        return instance;
    }

    private static final String CHARSET_UTF_8 = "UTF-8";
    private static final String SEARCH_URL = "https://search.bitchute.com/renderer?query=";

    @Override
    public String getUrl(String query, List<String> contentFilter, String sortFilter) throws ParsingException {
        try {
            return SEARCH_URL + URLEncoder.encode(query, CHARSET_UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new ParsingException("Could not encode query", e);
        }
    }


}
