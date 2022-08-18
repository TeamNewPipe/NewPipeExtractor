// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.search.filter;

/**
 * This class represents a single filter option.
 * <p>
 * <b>More in detail:</b>
 * For example youtube offers the filter group 'Sort order'. This group
 * consists of filter options like 'Relevance', 'Views', 'Rating' etc.
 * -> for each filter option a FilterItem has to be created.
 */
public class FilterItem {

    /**
     * The name of the filter option, that will be visible to the user.
     */
    private final String name;

    /**
     * A sequential unique number identifier.
     *
     * <b>Note:</b>
     * - the uniqueness applies only to each service.
     * - Never reuse a previously unique number for another filter option/group
     * (Otherwise implementation in the client that may implement to store some user
     * specified defaults could have an undefined behaviour while loading).
     */
    private final int identifier;

    public FilterItem(final int identifier, final String name) {
        this.identifier = identifier;
        this.name = name;
    }

    /**
     * @return {@link #identifier}
     */
    public int getIdentifier() {
        return this.identifier;
    }

    /**
     * @return {@link #name}
     */
    public String getName() {
        return this.name;
    }

    /**
     * This class is used to have a sub title divider between regular {@link FilterItem}s.
     */
    public static class DividerItem extends FilterItem {

        public DividerItem(final String name) {
            super(FilterContainer.ITEM_IDENTIFIER_UNKNOWN, name);
        }
    }
}
