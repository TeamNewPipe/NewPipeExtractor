// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.search.filter;

import org.schabi.newpipe.extractor.services.youtube.search.filter.YoutubeFilters;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The base class for every service describing their {@link FilterItem}s,
 * {@link FilterGroup}s, the relation between content filters and sort filters.
 */
public abstract class BaseSearchFilters {

    protected final Map<Integer, FilterContainer> sortFilterVariants = new HashMap<>();
    protected FilterGroup.Factory groupsFactory = new FilterGroup.Factory();
    protected List<FilterItem> selectedContentFilter = null;
    protected List<FilterItem> selectedSortFilter;
    protected FilterContainer contentFiltersVariant;
    protected List<FilterGroup> contentFilterGroups = new LinkedList<>();

    protected BaseSearchFilters() {
        init();
        build();
    }

    /**
     * Set the user selected sort filters which the user has selected in the UI.
     *
     * @param selectedSortFilter list with sort filters identifiers
     */
    public void setSelectedSortFilter(@Nullable final List<FilterItem> selectedSortFilter) {
        this.selectedSortFilter = selectedSortFilter;
    }

    /**
     * Set the selected content filter
     *
     * @param selectedContentFilter the name of the content filter
     */
    public void setSelectedContentFilter(@Nullable final List<FilterItem> selectedContentFilter) {
        this.selectedContentFilter = selectedContentFilter;
    }

    /**
     * Evaluate content and sort filters. This method should be run after:
     * {@link #setSelectedContentFilter(List)} and {@link #setSelectedSortFilter(List)}
     * <p>
     * Note: Whether you should implement this method or {@link #evaluateSelectedContentFilters()}
     * and/or {@link #evaluateSelectedSortFilters()} depends on your service needs and/or
     * how you want to implement.
     *
     * @return the query that should be appended to the searchUrl/whatever
     */
    public String evaluateSelectedFilters(@Nullable final String searchString) {
        // please implement method in derived class if you want to use it
        return null;
    }

    /**
     * Evaluate content filters. This method should be run after:
     * {@link #setSelectedContentFilter(List)}
     *
     * @return the sortQuery that should be appended to the searchUrl/whatever
     */
    public String evaluateSelectedContentFilters() {
        // please implement method in derived class if you want to use it
        return null;
    }

    /**
     * Evaluate sort filters. This method should be run after:
     * {@link #setSelectedSortFilter(List)}
     *
     * @return the contentQuery that should be appended to the searchUrl/whatever
     */
    public String evaluateSelectedSortFilters() {
        // please implement method in derived class if you want to use it
        return null;
    }

    /**
     * create all 'sort' and 'content filter' items and all 'sort filter variants' in this method.
     * See eg. {@link YoutubeFilters#init()}
     */
    protected abstract void init();

    /**
     * Transform the filter group list into an array and create the {@link FilterContainer}
     * with the content filters that are present for this service (eg. YouTube).
     */
    protected void build() {
        if (contentFilterGroups == null) {
            throw new RuntimeException("Never call method build() twice");
        }

        this.contentFiltersVariant = new FilterContainer(
                contentFilterGroups.toArray(new FilterGroup[0]));

        // building done
        contentFilterGroups.clear();
        contentFilterGroups = null;
    }

    /**
     * Add content Filter SortVariants.
     * <p>
     * Each content filter may have a corresponding sort filter variant.
     *
     * @param contentFilterId the content filter this sort variant applies to
     * @param variant         the corresponding sort filter variant
     */
    protected void addContentFilterSortVariant(
            final int contentFilterId,
            @Nonnull final FilterContainer variant) {
        this.sortFilterVariants.put(contentFilterId, variant);
    }

    /**
     * Get (if available) the sort filter variant for this content filter id.
     *
     * @param identifier the id of a content {@link FilterItem}
     * @return the sort filter variant for above stated content filter item. Null if there is none.
     */
    public FilterContainer getContentFilterSortFilterVariant(
            final int identifier) {
        return this.sortFilterVariants.get(identifier);
    }

    /**
     * Get all available content filters for this service.
     *
     * @return all available content filters
     */
    public FilterContainer getContentFilters() {
        return this.contentFiltersVariant;
    }

    /**
     * Get the {@link FilterItem} for the corresponding Id.
     *
     * @param filterId the filter id
     * @return the corresponding filter, null if none exists
     */
    public FilterItem getFilterItem(final int filterId) {
        return groupsFactory.getFilterForId(filterId);
    }

    /**
     * Add the content filter groups that should be available
     */
    protected void addContentFilterGroup(@Nonnull final FilterGroup filterGroup) {
        if (contentFilterGroups != null) {
            contentFilterGroups.add(filterGroup);
        } else {
            throw new RuntimeException("Never call this method after build()");
        }
    }
}
