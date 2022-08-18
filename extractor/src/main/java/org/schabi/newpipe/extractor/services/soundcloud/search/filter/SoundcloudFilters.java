// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.soundcloud.search.filter;

import org.schabi.newpipe.extractor.search.filter.BaseSearchFilters;
import org.schabi.newpipe.extractor.search.filter.FilterContainer;
import org.schabi.newpipe.extractor.search.filter.FilterGroup;
import org.schabi.newpipe.extractor.search.filter.FilterItem;

public final class SoundcloudFilters extends BaseSearchFilters {

    public static final int ID_CF_MAIN_GRP = 0;
    public static final int ID_CF_MAIN_ALL = 1;
    public static final int ID_CF_MAIN_TRACKS = 2;
    public static final int ID_CF_MAIN_USERS = 3;
    public static final int ID_CF_MAIN_PLAYLISTS = 4;
    public static final int ID_SF_DATE_GRP = 5;
    public static final int ID_SF_DATE_ALL = 6;
    public static final int ID_SF_DATE_LAST_HOUR = 7;
    public static final int ID_SF_DATE_LAST_DAY = 8;
    public static final int ID_SF_DATE_LAST_WEEK = 9;
    public static final int ID_SF_DATE_LAST_MONTH = 10;
    public static final int ID_SF_DATE_LAST_YEAR = 11;
    public static final int ID_SF_DURATION_GRP = 12;
    public static final int ID_SF_DURATION_ALL = 13;
    public static final int ID_SF_DURATION_SHORT = 14;
    public static final int ID_SF_DURATION_MEDIUM = 15;
    public static final int ID_SF_DURATION_LONG = 16;
    public static final int ID_SF_DURATION_EPIC = 17;
    public static final int ID_SF_LICENSE_GRP = 18;
    public static final int ID_SF_LICENSE_ALL = 19;
    public static final int ID_SF_LICENSE_COMMERCE = 20;

    public static final String TRACKS = "tracks";
    public static final String USERS = "users";
    public static final String PLAYLISTS = "playlists";
    public static final String ALL = "all";

    @Override
    public String evaluateSelectedContentFilters() {
        if (selectedContentFilter != null && !selectedContentFilter.isEmpty()) {
            final SoundcloudContentFilterItem contentFilter =
                    // we assume there is just one content filter
                    (SoundcloudContentFilterItem) selectedContentFilter.get(0);
            if (null != contentFilter) {
                return contentFilter.urlEndpoint;
            }
        }
        return "";
    }

    @Override
    protected void init() {
        /* content filters */
        groupsFactory.addFilterItem(new SoundcloudContentFilterItem(
                ID_CF_MAIN_ALL, ALL, ""));
        groupsFactory.addFilterItem(new SoundcloudContentFilterItem(
                ID_CF_MAIN_TRACKS, TRACKS, "/tracks"));
        groupsFactory.addFilterItem(new SoundcloudContentFilterItem(
                ID_CF_MAIN_USERS, USERS, "/users"));
        groupsFactory.addFilterItem(new SoundcloudContentFilterItem(
                ID_CF_MAIN_PLAYLISTS, PLAYLISTS, "/playlists"));


        /* Sort filters */
        /* 'Date' filter items */
        groupsFactory.addFilterItem(new SoundcloudSortFilterItem(
                ID_SF_DATE_ALL, "all", ""));
        groupsFactory.addFilterItem(new SoundcloudSortFilterItem(
                ID_SF_DATE_LAST_HOUR, "Past hour", "filter.created_at=last_hour"));
        groupsFactory.addFilterItem(new SoundcloudSortFilterItem(
                ID_SF_DATE_LAST_DAY, "Past day", "filter.created_at=last_day"));
        groupsFactory.addFilterItem(new SoundcloudSortFilterItem(
                ID_SF_DATE_LAST_WEEK, "Past week", "filter.created_at=last_week"));
        groupsFactory.addFilterItem(new SoundcloudSortFilterItem(
                ID_SF_DATE_LAST_MONTH, "Past month", "filter.created_at=last_month"));
        groupsFactory.addFilterItem(new SoundcloudSortFilterItem(
                ID_SF_DATE_LAST_YEAR, "Past year", "filter.created_at=last_year"));

        /* duration' filter items */
        groupsFactory.addFilterItem(new SoundcloudSortFilterItem(
                ID_SF_DURATION_ALL, "all", ""));
        groupsFactory.addFilterItem(new SoundcloudSortFilterItem(
                ID_SF_DURATION_SHORT, "< 2 min", "filter.duration=short"));
        groupsFactory.addFilterItem(new SoundcloudSortFilterItem(
                ID_SF_DURATION_MEDIUM, "2-10 min", "filter.duration=medium"));
        groupsFactory.addFilterItem(new SoundcloudSortFilterItem(
                ID_SF_DURATION_LONG, "10-30 min", "filter.duration=long"));
        groupsFactory.addFilterItem(new SoundcloudSortFilterItem(
                ID_SF_DURATION_EPIC, "> 30 min", "filter.duration=epic"));

        /* license */
        groupsFactory.addFilterItem(new SoundcloudSortFilterItem(
                ID_SF_LICENSE_ALL, "all", ""));
        groupsFactory.addFilterItem(new SoundcloudSortFilterItem(
                ID_SF_LICENSE_COMMERCE, "To modify commercially",
                "filter.license=to_modify_commercially"));

        final FilterContainer allMainCFGrpSortFilters = new FilterContainer(new FilterGroup[]{
                groupsFactory.createFilterGroup(ID_SF_DATE_GRP, "Sort by", true,
                        ID_SF_DATE_ALL, new FilterItem[]{
                                groupsFactory.getFilterForId(ID_SF_DATE_ALL),
                                groupsFactory.getFilterForId(ID_SF_DATE_LAST_HOUR),
                                groupsFactory.getFilterForId(ID_SF_DATE_LAST_DAY),
                                groupsFactory.getFilterForId(ID_SF_DATE_LAST_WEEK),
                                groupsFactory.getFilterForId(ID_SF_DATE_LAST_MONTH),
                                groupsFactory.getFilterForId(ID_SF_DATE_LAST_YEAR),
                        }, null),
                groupsFactory.createFilterGroup(ID_SF_DURATION_GRP, "Length", true,
                        ID_SF_DURATION_ALL, new FilterItem[]{
                                groupsFactory.getFilterForId(ID_SF_DURATION_ALL),
                                groupsFactory.getFilterForId(ID_SF_DURATION_SHORT),
                                groupsFactory.getFilterForId(ID_SF_DURATION_MEDIUM),
                                groupsFactory.getFilterForId(ID_SF_DURATION_LONG),
                                groupsFactory.getFilterForId(ID_SF_DURATION_EPIC),
                        }, null),
                groupsFactory.createFilterGroup(ID_SF_LICENSE_GRP, "License", true,
                        ID_SF_LICENSE_ALL, new FilterItem[]{
                                groupsFactory.getFilterForId(ID_SF_LICENSE_ALL),
                                groupsFactory.getFilterForId(ID_SF_LICENSE_COMMERCE),
                        }, null),
        });

        /* content filters */
        addContentFilterGroup(groupsFactory.createFilterGroup(ID_CF_MAIN_GRP, null, true,
                ID_CF_MAIN_ALL, new FilterItem[]{
                        groupsFactory.getFilterForId(ID_CF_MAIN_ALL),
                        groupsFactory.getFilterForId(ID_CF_MAIN_TRACKS),
                        groupsFactory.getFilterForId(ID_CF_MAIN_USERS),
                        groupsFactory.getFilterForId(ID_CF_MAIN_PLAYLISTS),
                }, allMainCFGrpSortFilters));

        addContentFilterSortVariant(ID_CF_MAIN_TRACKS, allMainCFGrpSortFilters);
    }

    @Override
    public String evaluateSelectedSortFilters() {
        final StringBuilder sortQuery = new StringBuilder();
        if (selectedSortFilter != null) {
            for (final FilterItem item : selectedSortFilter) {
                final SoundcloudSortFilterItem sortItem =
                        (SoundcloudSortFilterItem) item;
                if (sortItem != null && !sortItem.query.isEmpty()) {
                    sortQuery.append("&").append(sortItem.query);
                }
            }
        }

        return sortQuery.toString();
    }

    private static class SoundcloudSortFilterItem extends FilterItem {
        private final String query;

        SoundcloudSortFilterItem(final int identifier, final String name, final String query) {
            super(identifier, name);
            this.query = query;
        }
    }

    private static final class SoundcloudContentFilterItem extends FilterItem {
        private final String urlEndpoint;

        private SoundcloudContentFilterItem(final int identifier, final String name,
                                            final String urlEndpoint) {
            super(identifier, name);
            this.urlEndpoint = urlEndpoint;
        }
    }
}
