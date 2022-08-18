// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.linkHandler;

import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.BASE_URL;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.extractor.services.bandcamp.search.filter.BandcampFilters;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.util.List;

public final class BandcampSearchQueryHandlerFactory extends SearchQueryHandlerFactory {

    private static final BandcampSearchQueryHandlerFactory INSTANCE
            = new BandcampSearchQueryHandlerFactory();

    private BandcampSearchQueryHandlerFactory() {
        super(new BandcampFilters());
    }

    public static BandcampSearchQueryHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public String getUrl(final String query,
                         final List<FilterItem> selectedContentFilter,
                         final List<FilterItem> selectedSortFilter)
            throws ParsingException, UnsupportedOperationException {


        searchFilters.setSelectedSortFilter(selectedSortFilter);
        searchFilters.setSelectedContentFilter(selectedContentFilter);

        final String filterQuery = searchFilters.evaluateSelectedContentFilters();
        try {
            return BASE_URL + "/search?q=" + Utils.encodeUrlUtf8(query)
                    + filterQuery + "&page=1";
        } catch (final UnsupportedEncodingException e) {
            throw new ParsingException("query \"" + query + "\" could not be encoded", e);
        }
    }
}
