package org.schabi.newpipe.extractor.services.youtube.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.extractor.services.youtube.search.filter.YoutubeFilters;

import java.util.List;

import javax.annotation.Nonnull;

public final class YoutubeSearchQueryHandlerFactory extends SearchQueryHandlerFactory {

    private static final YoutubeSearchQueryHandlerFactory INSTANCE =
            new YoutubeSearchQueryHandlerFactory();

    private YoutubeSearchQueryHandlerFactory() {
        super(new YoutubeFilters());
    }

    @Nonnull
    public static YoutubeSearchQueryHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public String getUrl(final String searchString,
                         @Nonnull final List<FilterItem> selectedContentFilter,
                         final List<FilterItem> selectedSortFilter) throws ParsingException {
        searchFilters.setSelectedContentFilter(selectedContentFilter);
        searchFilters.setSelectedSortFilter(selectedSortFilter);
        return searchFilters.evaluateSelectedFilters(searchString);
    }
}
