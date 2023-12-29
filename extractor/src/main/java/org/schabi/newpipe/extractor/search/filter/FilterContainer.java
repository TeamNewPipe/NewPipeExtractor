// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.search.filter;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is a container that keeps either content filters or sort filters organized.
 *
 * Sort/content filters ({@link FilterItem}s) are organized within {@link FilterGroup}s.
 */
public final class FilterContainer {

    /**
     * Mark {@link FilterItem}'s and {@link FilterGroup}'s which identifier is not (yet) set.
     */
    public static final int ITEM_IDENTIFIER_UNKNOWN = -1;

    private final Map<Integer, FilterItem> idToFilterItem = new HashMap<>();
    private final FilterGroup[] filterGroups;

    public FilterContainer(final FilterGroup[] filterGroups) {
        this.filterGroups = filterGroups;
        for (final FilterGroup group : filterGroups) {
            for (final FilterItem item : group.getFilterItems()) {
                idToFilterItem.put(item.getIdentifier(), item);
            }
        }
    }

    /**
     * Quickly access a {@link FilterItem} that belongs to this {@link FilterContainer}.
     *
     * @param id the identifier of the {@link FilterItem}
     * @return
     */
    public FilterItem getFilterItem(final int id) {
        return idToFilterItem.get(id);
    }

    public FilterGroup[] getFilterGroups() {
        return filterGroups;
    }
}
