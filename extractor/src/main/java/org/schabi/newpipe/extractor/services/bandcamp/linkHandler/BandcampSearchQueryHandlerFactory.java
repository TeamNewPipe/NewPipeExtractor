// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.linkHandler;

import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.BASE_URL;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class BandcampSearchQueryHandlerFactory extends SearchQueryHandlerFactory {
    @Override
    public String getUrl(final String query,
                         final List<String> contentFilter,
                         final String sortFilter) throws ParsingException {
        try {
            return BASE_URL + "/search?q=" + Utils.encodeUrlUtf8(query) + "&page=1";
        } catch (final UnsupportedEncodingException e) {
            throw new ParsingException("query \"" + query + "\" could not be encoded", e);
        }
    }
}
