// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.soundcloud.search.filter;

import org.schabi.newpipe.extractor.search.filter.BaseSearchFilters;
import org.schabi.newpipe.extractor.search.filter.FilterContainer;
import org.schabi.newpipe.extractor.search.filter.FilterGroup;
import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.extractor.search.filter.LibraryStringIds;

import javax.annotation.Nonnull;

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
                ID_CF_MAIN_ALL,
                LibraryStringIds.SEARCH_FILTERS_ALL, ""));
        groupsFactory.addFilterItem(new SoundcloudContentFilterItem(
                ID_CF_MAIN_TRACKS,
                LibraryStringIds.SEARCH_FILTERS_TRACKS, "/tracks"));
        groupsFactory.addFilterItem(new SoundcloudContentFilterItem(
                ID_CF_MAIN_USERS,
                LibraryStringIds.SEARCH_FILTERS_USERS, "/users"));
        groupsFactory.addFilterItem(new SoundcloudContentFilterItem(
                ID_CF_MAIN_PLAYLISTS,
                LibraryStringIds.SEARCH_FILTERS_PLAYLISTS, "/playlists"));


        /* Sort filters */
        /* 'Date' filter items */
        groupsFactory.addFilterItem(new SoundcloudSortFilterItem(
                ID_SF_DATE_ALL,
                LibraryStringIds.SEARCH_FILTERS_ANY_TIME, ""));
        groupsFactory.addFilterItem(new SoundcloudSortFilterItem(
                ID_SF_DATE_LAST_HOUR,
                LibraryStringIds.SEARCH_FILTERS_PAST_HOUR, "filter.created_at=last_hour"));
        groupsFactory.addFilterItem(new SoundcloudSortFilterItem(
                ID_SF_DATE_LAST_DAY,
                LibraryStringIds.SEARCH_FILTERS_PAST_DAY, "filter.created_at=last_day"));
        groupsFactory.addFilterItem(new SoundcloudSortFilterItem(
                ID_SF_DATE_LAST_WEEK,
                LibraryStringIds.SEARCH_FILTERS_PAST_WEEK, "filter.created_at=last_week"));
        groupsFactory.addFilterItem(new SoundcloudSortFilterItem(
                ID_SF_DATE_LAST_MONTH,
                LibraryStringIds.SEARCH_FILTERS_PAST_MONTH, "filter.created_at=last_month"));
        groupsFactory.addFilterItem(new SoundcloudSortFilterItem(
                ID_SF_DATE_LAST_YEAR,
                LibraryStringIds.SEARCH_FILTERS_PAST_YEAR, "filter.created_at=last_year"));

        /* duration' filter items */
        groupsFactory.addFilterItem(new SoundcloudSortFilterItem(
                ID_SF_DURATION_ALL,
                LibraryStringIds.SEARCH_FILTERS_ALL, ""));
        groupsFactory.addFilterItem(new SoundcloudSortFilterItem(
                ID_SF_DURATION_SHORT,
                LibraryStringIds.SEARCH_FILTERS_LESS_2_MIN, "filter.duration=short"));
        groupsFactory.addFilterItem(new SoundcloudSortFilterItem(
                ID_SF_DURATION_MEDIUM,
                LibraryStringIds.SEARCH_FILTERS_2_10_MIN, "filter.duration=medium"));
        groupsFactory.addFilterItem(new SoundcloudSortFilterItem(
                ID_SF_DURATION_LONG,
                LibraryStringIds.SEARCH_FILTERS_10_30_MIN, "filter.duration=long"));
        groupsFactory.addFilterItem(new SoundcloudSortFilterItem(
                ID_SF_DURATION_EPIC,
                LibraryStringIds.SEARCH_FILTERS_GREATER_30_MIN, "filter.duration=epic"));

        /* license */
        groupsFactory.addFilterItem(new SoundcloudSortFilterItem(
                ID_SF_LICENSE_ALL,
                LibraryStringIds.SEARCH_FILTERS_ALL, ""));
        groupsFactory.addFilterItem(new SoundcloudSortFilterItem(
                ID_SF_LICENSE_COMMERCE,
                LibraryStringIds.SEARCH_FILTERS_TO_MODIFY_COMMERCIALLY,
                "filter.license=to_modify_commercially"));

        final FilterContainer allMainCFGrpSortFilters = new FilterContainer(new FilterGroup[]{
                groupsFactory.createFilterGroup(ID_SF_DATE_GRP,
                        LibraryStringIds.SEARCH_FILTERS_ADDED, true,
                        ID_SF_DATE_ALL, new FilterItem[]{
                                groupsFactory.getFilterForId(ID_SF_DATE_ALL),
                                groupsFactory.getFilterForId(ID_SF_DATE_LAST_HOUR),
                                groupsFactory.getFilterForId(ID_SF_DATE_LAST_DAY),
                                groupsFactory.getFilterForId(ID_SF_DATE_LAST_WEEK),
                                groupsFactory.getFilterForId(ID_SF_DATE_LAST_MONTH),
                                groupsFactory.getFilterForId(ID_SF_DATE_LAST_YEAR),
                        }, null),
                groupsFactory.createFilterGroup(ID_SF_DURATION_GRP,
                        LibraryStringIds.SEARCH_FILTERS_LENGTH, true,
                        ID_SF_DURATION_ALL, new FilterItem[]{
                                groupsFactory.getFilterForId(ID_SF_DURATION_ALL),
                                groupsFactory.getFilterForId(ID_SF_DURATION_SHORT),
                                groupsFactory.getFilterForId(ID_SF_DURATION_MEDIUM),
                                groupsFactory.getFilterForId(ID_SF_DURATION_LONG),
                                groupsFactory.getFilterForId(ID_SF_DURATION_EPIC),
                        }, null),
                groupsFactory.createFilterGroup(ID_SF_LICENSE_GRP,
                        LibraryStringIds.SEARCH_FILTERS_LICENSE, true,
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

        SoundcloudSortFilterItem(final int identifier,
                                 @Nonnull final LibraryStringIds nameId,
                                 final String query) {
            super(identifier, nameId);
            this.query = query;
        }
    }

    private static final class SoundcloudContentFilterItem extends FilterItem {
        private final String urlEndpoint;

        private SoundcloudContentFilterItem(final int identifier,
                                            @Nonnull final LibraryStringIds nameId,
                                            final String urlEndpoint) {
            super(identifier, nameId);
            this.urlEndpoint = urlEndpoint;
        }
    }
}
