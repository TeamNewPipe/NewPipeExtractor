package org.schabi.newpipe.extractor.linkhandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.search.filter.BaseSearchFilters;
import org.schabi.newpipe.extractor.search.filter.FilterContainer;
import org.schabi.newpipe.extractor.search.filter.FilterItem;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class SearchQueryHandlerFactory extends ListLinkHandlerFactory {
    protected final BaseSearchFilters searchFilters;

    protected SearchQueryHandlerFactory(final BaseSearchFilters searchFilters) {
        this.searchFilters = searchFilters;
    }

    ///////////////////////////////////
    // To Override
    ///////////////////////////////////

    @Override
    public abstract String getUrl(String query,
                                  @Nonnull List<FilterItem> selectedContentFilter,
                                  @Nullable List<FilterItem> selectedSortFilter)
            throws ParsingException, UnsupportedOperationException;

    @SuppressWarnings("unused")
    public String getSearchString(final String url) {
        return "";
    }

    ///////////////////////////////////
    // Logic
    ///////////////////////////////////

    @Override
    public String getId(final String url) throws ParsingException, UnsupportedOperationException {
        return getSearchString(url);
    }

    @Override
    public SearchQueryHandler fromQuery(final String query,
                                        final List<FilterItem> contentFilter,
                                        final List<FilterItem> sortFilter) throws ParsingException {
        return new SearchQueryHandler(super.fromQuery(query, contentFilter, sortFilter));
    }

    public SearchQueryHandler fromQuery(final String query) throws ParsingException {
        return fromQuery(query, List.of(), List.of());
    }

    /**
     * It's not mandatory for NewPipe to handle the Url
     */
    @Override
    public boolean onAcceptUrl(final String url) {
        return false;
    }

    /**
     * {@link BaseSearchFilters#getContentFilters()}
     */
    public FilterContainer getAvailableContentFilter() {
        return searchFilters.getContentFilters();
    }

    /**
     * {@link BaseSearchFilters#getContentFilterSortFilterVariant(int)}
     */
    public FilterContainer getContentFilterSortFilterVariant(final int contentFilterId) {
        return searchFilters.getContentFilterSortFilterVariant(contentFilterId);
    }

    /**
     * {@link BaseSearchFilters#getFilterItem(int)}
     */
    public FilterItem getFilterItem(final int filterId) {
        return searchFilters.getFilterItem(filterId);
    }
}
