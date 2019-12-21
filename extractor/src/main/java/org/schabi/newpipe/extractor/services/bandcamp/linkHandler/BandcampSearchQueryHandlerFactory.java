// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class BandcampSearchQueryHandlerFactory extends SearchQueryHandlerFactory {

    private static final String SEARCH_URL = "https://bandcamp.com/search?q=";

    public static final String CHARSET_UTF_8 = "UTF-8";


    @Override
    public String getUrl(String query, List<String> contentFilter, String sortFilter) throws ParsingException {
        try {

            return SEARCH_URL +
                    URLEncoder.encode(query, CHARSET_UTF_8);

        } catch (UnsupportedEncodingException e) {
            throw new ParsingException("query \"" + query + "\" could not be encoded", e);
        }
    }
}
