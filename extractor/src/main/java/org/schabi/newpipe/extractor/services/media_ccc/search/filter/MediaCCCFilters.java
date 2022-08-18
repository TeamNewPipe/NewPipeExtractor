// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.media_ccc.search.filter;

import org.schabi.newpipe.extractor.search.filter.BaseSearchFilters;
import org.schabi.newpipe.extractor.search.filter.FilterItem;

public final class MediaCCCFilters extends BaseSearchFilters {

    public static final int ID_CF_MAIN_GRP = 0;
    public static final int ID_CF_MAIN_ALL = 1;
    public static final int ID_CF_MAIN_CONFERENCES = 2;
    public static final int ID_CF_MAIN_EVENTS = 3;

    public static final String ALL = "all";
    public static final String CONFERENCES = "conferences";
    public static final String EVENTS = "events";

    @Override
    protected void init() {
        /* content filters */
        groupsFactory.addFilterItem(new FilterItem(
                ID_CF_MAIN_ALL, ALL));
        groupsFactory.addFilterItem(new FilterItem(
                ID_CF_MAIN_CONFERENCES, CONFERENCES));
        groupsFactory.addFilterItem(new FilterItem(
                ID_CF_MAIN_EVENTS, EVENTS));

        addContentFilterGroup(groupsFactory.createFilterGroup(ID_CF_MAIN_GRP, null, true,
                ID_CF_MAIN_ALL, new FilterItem[]{
                        groupsFactory.getFilterForId(ID_CF_MAIN_ALL),
                        groupsFactory.getFilterForId(ID_CF_MAIN_CONFERENCES),
                        groupsFactory.getFilterForId(ID_CF_MAIN_EVENTS),
                }, null));
    }
}
