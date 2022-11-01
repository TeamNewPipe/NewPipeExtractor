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
     * The name id of the filter group.
     *
     * The id has to be translated to an actual string that the user will see in the UI.
     */
    private final LibraryStringIds nameId;

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

    public FilterItem(final int identifier, final LibraryStringIds nameId) {
        this.identifier = identifier;
        this.nameId = nameId;
    }

    /**
     * @return {@link #identifier}
     */
    public int getIdentifier() {
        return this.identifier;
    }

    /**
     * @return {@link #nameId}
     */
    public LibraryStringIds getNameId() {
        return this.nameId;
    }

    /**
     * This class is used to have a sub title divider between regular {@link FilterItem}s.
     */
    public static class DividerItem extends FilterItem {

        public DividerItem(final LibraryStringIds nameId) {
            super(FilterContainer.ITEM_IDENTIFIER_UNKNOWN, nameId);
        }
    }

    @Override
    public boolean equals(final Object obj) {
        return (obj instanceof FilterItem) && ((FilterItem) obj).identifier == this.identifier;
    }

    @Override
    public int hashCode() {
        return identifier;
    }
}
