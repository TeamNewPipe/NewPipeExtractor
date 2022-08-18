// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.search.filter;

import org.schabi.newpipe.extractor.search.filter.BaseSearchFilters;
import org.schabi.newpipe.extractor.search.filter.FilterItem;

public final class BandcampFilters extends BaseSearchFilters {

    public static final int ID_CF_MAIN_GRP = 0;
    public static final int ID_CF_MAIN_ALL = 1;
    public static final int ID_CF_MAIN_ARTISTS = 2;
    public static final int ID_CF_MAIN_ALBUMS = 3;
    public static final int ID_CF_MAIN_TRACKS = 4;
    // public static final int ID_CF_MAIN_FANS = 5;

    private static final String ALL = "all";
    private static final String ARTISTS = "artists & labels";
    private static final String ALBUMS = "albums";
    private static final String TRACKS = "tracks";
    // private static final String FANS = "fans";

    @Override
    public String evaluateSelectedContentFilters() {
        if (selectedSortFilter != null) {
            String sortQuery = "";

            if (selectedContentFilter != null && !selectedContentFilter.isEmpty()) {
                final BandcampContentFilterItem contentItem =
                        // we assume that there is just one content filter
                        (BandcampContentFilterItem) selectedContentFilter.get(0);
                if (contentItem != null && !contentItem.query.isEmpty()) {
                    sortQuery = "&" + contentItem.query;
                }
            }
            return sortQuery;
        }
        return "";
    }

    @Override
    protected void init() {
        /* content filters */
        groupsFactory.addFilterItem(new BandcampContentFilterItem(
                ID_CF_MAIN_ALL, ALL, ""));
        groupsFactory.addFilterItem(new BandcampContentFilterItem(
                ID_CF_MAIN_ARTISTS, ARTISTS, "item_type=b"));
        groupsFactory.addFilterItem(new BandcampContentFilterItem(
                ID_CF_MAIN_ALBUMS, ALBUMS, "item_type=a"));
        groupsFactory.addFilterItem(new BandcampContentFilterItem(
                ID_CF_MAIN_TRACKS, TRACKS, "item_type=t"));
        // FIXME no FANS extractor in BandcampSearchExtractor -> no content filter here
        // groupsFactory.addFilterItem(new BandcampContentFilterItem(
        //         ID_CF_MAIN_FANS, FANS, "item_type=f"));

        addContentFilterGroup(groupsFactory.createFilterGroup(ID_CF_MAIN_GRP, null, true,
                ID_CF_MAIN_ALL, new FilterItem[]{
                        groupsFactory.getFilterForId(ID_CF_MAIN_ALL),
                        groupsFactory.getFilterForId(ID_CF_MAIN_ARTISTS),
                        groupsFactory.getFilterForId(ID_CF_MAIN_ALBUMS),
                        groupsFactory.getFilterForId(ID_CF_MAIN_TRACKS),
                        // groupsFactory.getFilterForId(ID_CF_MAIN_FANS),
                }, null));
    }

    public static class BandcampContentFilterItem extends FilterItem {
        private final String query;

        public BandcampContentFilterItem(final int identifier, final String name,
                                         final String query) {
            super(identifier, name);
            this.query = query;
        }
    }
}
