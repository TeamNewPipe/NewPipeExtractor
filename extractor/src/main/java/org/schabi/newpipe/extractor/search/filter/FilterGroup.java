// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.search.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class represents a filter category/group. For example 'Sort order'.
 * <p>
 * Its main purpose is to host a bunch of {@link FilterItem}s that belong to that
 * group. Eg. 'Relevance', 'Views', 'Rating'
 */
public final class FilterGroup {

    /**
     * {@link #getIdentifier()}
     */
    private final int identifier;

    /**
     * The name id of the filter group.
     * <p>
     * The id has to be translated to an actual string that the user will see in the UI.
     */
    private final LibraryStringIds groupNameId;

    /**
     * Specify whether only one item can be selected in this group at a time.
     */
    private final boolean onlyOneCheckable;

    /**
     * Each group may have a default value that should be selected.
     * <p>
     * It should be set to the the {@link FilterItem}'s id. If there is no default option
     * it should be set to {@link FilterContainer#ITEM_IDENTIFIER_UNKNOWN}
     */
    private final int defaultSelectedFilterId;

    /**
     * The filter items that belong to this {@link FilterGroup}.
     */
    private final List<FilterItem> filterItems;

    /**
     * {@link #getAllSortFilters()}.
     */
    private final FilterContainer allSortFilters;

    private FilterGroup(final int identifier,
                        @Nullable final LibraryStringIds groupNameId,
                        final boolean onlyOneCheckable,
                        final int defaultSelectedFilterId,
                        @Nonnull final List<FilterItem> filterItems,
                        @Nullable final FilterContainer allSortFilters) {
        this.identifier = identifier;
        this.groupNameId = groupNameId;
        this.onlyOneCheckable = onlyOneCheckable;
        this.defaultSelectedFilterId = defaultSelectedFilterId;
        this.filterItems = filterItems;
        this.allSortFilters = allSortFilters;
    }


    /**
     * If this group is a content filter and has corresponding sort filters, this
     * {@link FilterContainer} contains all available sort filters for this group.
     *
     * @return may be null as not all {@link FilterGroup}s have sort filters.
     */
    public FilterContainer getAllSortFilters() {
        return allSortFilters;
    }

    /**
     * {@link FilterItem#getIdentifier()}
     */
    public int getIdentifier() {
        return this.identifier;
    }

    /**
     * {@link #groupNameId}
     */
    public LibraryStringIds getNameId() {
        return groupNameId;
    }

    /**
     * {@link #defaultSelectedFilterId}
     */
    public int getDefaultSelectedFilterId() {
        return defaultSelectedFilterId;
    }

    /**
     * {@link #filterItems}
     * @return
     */
    public List<FilterItem> getFilterItems() {
        return filterItems;
    }

    /**
     * {@link #onlyOneCheckable}
     */
    public boolean isOnlyOneCheckable() {
        return onlyOneCheckable;
    }

    /**
     * Factory for building {@link FilterGroup}s.
     * <p>
     * Each service should only have one instance.
     * This is implemented in {@link BaseSearchFilters}
     */
    public static class Factory {

        /**
         * A map that has all {@link FilterItem}s that are relevant for one service. Eg. Youtube
         */
        public final Map<Integer, FilterItem> filtersMap = new HashMap<>();

        /**
         * Check if a {@link FilterItem} has a unique id.
         *
         * @param filterItems a map with the previously added {@link FilterItem}'s to compare with.
         * @param item        the new {@link FilterItem} that should be added.
         */
        void uniqueIdChecker(final Map<Integer, FilterItem> filterItems,
                             final FilterItem item) {

            if (filterItems.containsKey(item.getIdentifier())) {
                throw new InvalidFilterIdException("Filter ID "
                        + item.getIdentifier() + " for \""
                        + item.getClass().getCanonicalName()
                        + "\" is used multiple times."
                        + " Filter ID's have to be unique per service");
            }
        }

        /**
         * Add a new {@link FilterItem} that is relevant to this service.
         * <p>
         * The {@link FilterItem}s are accessible by their id via {@link #getFilterForId(int)}
         *
         * @param filter the new {@link FilterItem} to be added to the factory.
         * @return the identifier of the {@link FilterItem}
         */
        public int addFilterItem(@Nonnull final FilterItem filter) {
            uniqueIdChecker(filtersMap, filter);
            filtersMap.put(filter.getIdentifier(), filter);
            return filter.getIdentifier();
        }

        public FilterGroup createFilterGroup(final int identifier,
                                             @Nullable final LibraryStringIds groupNameId,
                                             final boolean onlyOneCheckable,
                                             final int defaultSelectedFilterId,
                                             @Nonnull final FilterItem[] filterItems,
                                             @Nullable final FilterContainer allSortFilters) {
            return new FilterGroup(identifier, groupNameId, onlyOneCheckable,
                    defaultSelectedFilterId,
                    Arrays.stream(filterItems).collect(Collectors.toCollection(ArrayList::new)),
                    allSortFilters);
        }

        /**
         * Get previously via {@link #addFilterItem(FilterItem)} added {@link FilterItem}.
         *
         * @param identifier the id of the desired {@link FilterItem}
         * @return the desired {@link FilterItem}
         */
        public FilterItem getFilterForId(final int identifier) {
            return filtersMap.get(identifier);
        }

        private static class InvalidFilterIdException extends RuntimeException {
            InvalidFilterIdException(final String message) {
                super(message);
            }
        }
    }
}
