// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.peertube.search.filter;


import org.schabi.newpipe.extractor.search.filter.BaseSearchFilters;
import org.schabi.newpipe.extractor.search.filter.FilterContainer;
import org.schabi.newpipe.extractor.search.filter.FilterGroup;
import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.extractor.search.filter.LibraryStringIds;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.schabi.newpipe.extractor.search.filter.FilterContainer.ITEM_IDENTIFIER_UNKNOWN;

public final class PeertubeFilters extends BaseSearchFilters {

    public static final int ID_CF_MAIN_GRP = 0;
    public static final int ID_CF_MAIN_ALL = 1;
    public static final int ID_CF_MAIN_VIDEOS = 2;
    public static final int ID_CF_MAIN_CHANNELS = 3;
    public static final int ID_CF_MAIN_PLAYLISTS = 4;
    public static final int ID_CF_SEPIA_GRP = 5;
    public static final int ID_CF_SEPIA_SEPIASEARCH = 6;
    public static final int ID_SF_SORT_ORDER_GRP = 7;
    public static final int ID_SF_SORT_ORDER_ASCENDING = 8;
    public static final int ID_SF_SORT_BY_GRP = 9;
    public static final int ID_SF_SORT_BY_RELEVANCE = 10;
    public static final int ID_SF_SORT_BY_NAME = 11;
    public static final int ID_SF_SORT_BY_DURATION = 12;
    public static final int ID_SF_SORT_BY_PUBLISH_DATE = 13;
    public static final int ID_SF_SORT_BY_CREATION_DATE = 14;
    public static final int ID_SF_SORT_BY_VIEWS = 15;
    public static final int ID_SF_SORT_BY_LIKES = 16;
    public static final int ID_SF_KIND_GRP = 17;
    public static final int ID_SF_KIND_ALL = 18;
    public static final int ID_SF_KIND_LIVE = 19;
    public static final int ID_SF_KIND_VOD_VIDEOS = 20;
    public static final int ID_SF_SENSITIVE_GRP = 21;
    public static final int ID_SF_SENSITIVE_ALL = 22;
    public static final int ID_SF_SENSITIVE_YES = 23;
    public static final int ID_SF_SENSITIVE_NO = 24;
    public static final int ID_SF_PUBLISHED_GRP = 25;
    public static final int ID_SF_PUBLISHED_ALL = 26;
    public static final int ID_SF_PUBLISHED_TODAY = 27;
    public static final int ID_SF_PUBLISHED_LAST_7_DAYS = 28;
    public static final int ID_SF_PUBLISHED_LAST_30_DAYS = 29;
    public static final int ID_SF_PUBLISHED_LAST_YEAR = 30;
    public static final int ID_SF_DURATION_GRP = 31;
    public static final int ID_SF_DURATION_ALL = 32;
    public static final int ID_SF_DURATION_SHORT = 33;
    public static final int ID_SF_DURATION_MEDIUM = 34;
    public static final int ID_SF_DURATION_LONG = 35;


    private boolean isAscending = false;

    @Override
    public String evaluateSelectedFilters(@Nullable final String searchString) {
        final StringBuilder sortQuery = new StringBuilder();

        if (selectedSortFilter != null) {
            final Optional<FilterItem> ascendingFilter = selectedSortFilter.stream()
                    .filter(PeertubeSortOrderFilterItem.class::isInstance)
                    .findFirst();
            isAscending = ascendingFilter.isPresent();
            for (final FilterItem item : selectedSortFilter) {
                appendFilterToQueryString(item, sortQuery);
            }
        }

        if (selectedContentFilter != null) {
            for (final FilterItem item : selectedContentFilter) {
                appendFilterToQueryString(item, sortQuery);
            }
        }

        return sortQuery.toString();
    }

    @SuppressWarnings("checkstyle:MethodLength")
    @Override
    protected void init() {

        /* sort filters */
        /* 'Sort order' filter item */
        groupsFactory.addFilterItem(new PeertubeSortOrderFilterItem(
                ID_SF_SORT_ORDER_ASCENDING,
                LibraryStringIds.SEARCH_FILTERS_ASCENDING));

        /* 'Sort by' filter items */
        groupsFactory.addFilterItem(new PeertubeSortFilterItem(
                ID_SF_SORT_BY_RELEVANCE,
                LibraryStringIds.SEARCH_FILTERS_RELEVANCE, "sort=match"));
        groupsFactory.addFilterItem(new PeertubeSortFilterItem(
                ID_SF_SORT_BY_NAME,
                LibraryStringIds.SEARCH_FILTERS_NAME, "sort=name"));
        groupsFactory.addFilterItem(new PeertubeSortFilterItem(
                ID_SF_SORT_BY_DURATION,
                LibraryStringIds.SEARCH_FILTERS_DURATION, "sort=duration"));
        groupsFactory.addFilterItem(new PeertubeSortFilterItem(
                ID_SF_SORT_BY_PUBLISH_DATE,
                LibraryStringIds.SEARCH_FILTERS_PUBLISH_DATE, "sort=publishedAt"));
        groupsFactory.addFilterItem(new PeertubeSortFilterItem(
                ID_SF_SORT_BY_CREATION_DATE,
                LibraryStringIds.SEARCH_FILTERS_CREATION_DATE, "sort=createdAt"));
        groupsFactory.addFilterItem(new PeertubeSortFilterItem(
                ID_SF_SORT_BY_VIEWS,
                LibraryStringIds.SEARCH_FILTERS_VIEWS, "sort=views"));
        groupsFactory.addFilterItem(new PeertubeSortFilterItem(
                ID_SF_SORT_BY_LIKES,
                LibraryStringIds.SEARCH_FILTERS_LIKES, "sort=likes"));

        /* stream kind filter items */
        groupsFactory.addFilterItem(new PeertubeFilterItem(
                ID_SF_KIND_ALL,
                LibraryStringIds.SEARCH_FILTERS_ALL, ""));
        groupsFactory.addFilterItem(new PeertubeFilterItem(
                ID_SF_KIND_LIVE,
                LibraryStringIds.SEARCH_FILTERS_LIVE, "isLive=true"));
        groupsFactory.addFilterItem(new PeertubeFilterItem(
                ID_SF_KIND_VOD_VIDEOS,
                LibraryStringIds.SEARCH_FILTERS_VOD_VIDEOS, "isLive=false"));

        /* sensitive filter items */
        groupsFactory.addFilterItem(new PeertubeFilterItem(
                ID_SF_SENSITIVE_ALL,
                LibraryStringIds.SEARCH_FILTERS_ALL, ""));
        groupsFactory.addFilterItem(new PeertubeFilterItem(
                ID_SF_SENSITIVE_YES,
                LibraryStringIds.SEARCH_FILTERS_YES, "nsfw=true"));
        groupsFactory.addFilterItem(new PeertubeFilterItem(
                ID_SF_SENSITIVE_NO,
                LibraryStringIds.SEARCH_FILTERS_NO, "nsfw=false"));

        /* 'Date' filter items */
        // here query is set to null as the value is generated dynamically
        groupsFactory.addFilterItem(new PeertubePublishedDateFilterItem(
                ID_SF_PUBLISHED_ALL,
                LibraryStringIds.SEARCH_FILTERS_ALL, null,
                PeertubePublishedDateFilterItem.NO_DAYS_SET));
        groupsFactory.addFilterItem(new PeertubePublishedDateFilterItem(
                ID_SF_PUBLISHED_TODAY,
                LibraryStringIds.SEARCH_FILTERS_TODAY, null, 1));
        groupsFactory.addFilterItem(new PeertubePublishedDateFilterItem(
                ID_SF_PUBLISHED_LAST_7_DAYS,
                LibraryStringIds.SEARCH_FILTERS_LAST_7_DAYS, null, 7));
        groupsFactory.addFilterItem(new PeertubePublishedDateFilterItem(
                ID_SF_PUBLISHED_LAST_30_DAYS,
                LibraryStringIds.SEARCH_FILTERS_LAST_30_DAYS, null, 30));
        groupsFactory.addFilterItem(new PeertubePublishedDateFilterItem(
                ID_SF_PUBLISHED_LAST_YEAR,
                LibraryStringIds.SEARCH_FILTERS_LAST_YEAR, null, 365));

        /* 'Duration' filter items */
        groupsFactory.addFilterItem(new PeertubeFilterItem(
                ID_SF_DURATION_ALL,
                LibraryStringIds.SEARCH_FILTERS_ALL, ""));
        groupsFactory.addFilterItem(new PeertubeFilterItem(
                ID_SF_DURATION_SHORT,
                LibraryStringIds.SEARCH_FILTERS_SHORT_LESS_4_MIN, "durationMax=240"));
        groupsFactory.addFilterItem(new PeertubeFilterItem(
                ID_SF_DURATION_MEDIUM,
                LibraryStringIds.SEARCH_FILTERS_MEDIUM_4_10_MIN,
                "durationMin=240&durationMax=600"));
        groupsFactory.addFilterItem(new PeertubeFilterItem(
                ID_SF_DURATION_LONG,
                LibraryStringIds.SEARCH_FILTERS_LONG_GREATER_10_MIN, "durationMin=600"));

        final FilterContainer allSortFilters = new FilterContainer(new FilterGroup[]{
                groupsFactory.createFilterGroup(ID_SF_SORT_ORDER_GRP,
                        LibraryStringIds.SEARCH_FILTERS_SORT_ORDER, false,
                        ITEM_IDENTIFIER_UNKNOWN, new FilterItem[]{
                                groupsFactory.getFilterForId(ID_SF_SORT_ORDER_ASCENDING),
                        }, null),
                groupsFactory.createFilterGroup(ID_SF_SORT_BY_GRP,
                        LibraryStringIds.SEARCH_FILTERS_SORT_BY, true,
                        ID_SF_SORT_BY_RELEVANCE, new FilterItem[]{
                                groupsFactory.getFilterForId(ID_SF_SORT_BY_RELEVANCE),
                                groupsFactory.getFilterForId(ID_SF_SORT_BY_NAME),
                                groupsFactory.getFilterForId(ID_SF_SORT_BY_DURATION),
                                groupsFactory.getFilterForId(ID_SF_SORT_BY_PUBLISH_DATE),
                                groupsFactory.getFilterForId(ID_SF_SORT_BY_CREATION_DATE),
                                groupsFactory.getFilterForId(ID_SF_SORT_BY_VIEWS),
                                groupsFactory.getFilterForId(ID_SF_SORT_BY_LIKES),
                        }, null),
                groupsFactory.createFilterGroup(ID_SF_KIND_GRP,
                        LibraryStringIds.SEARCH_FILTERS_KIND, true,
                        ID_SF_KIND_ALL, new FilterItem[]{
                                groupsFactory.getFilterForId(ID_SF_KIND_ALL),
                                groupsFactory.getFilterForId(ID_SF_KIND_LIVE),
                                groupsFactory.getFilterForId(ID_SF_KIND_VOD_VIDEOS),
                        }, null),
                groupsFactory.createFilterGroup(ID_SF_SENSITIVE_GRP,
                        LibraryStringIds.SEARCH_FILTERS_SENSITIVE, true,
                        ID_SF_SENSITIVE_ALL, new FilterItem[]{
                                groupsFactory.getFilterForId(ID_SF_SENSITIVE_ALL),
                                groupsFactory.getFilterForId(ID_SF_SENSITIVE_YES),
                                groupsFactory.getFilterForId(ID_SF_SENSITIVE_NO),
                        }, null),
                groupsFactory.createFilterGroup(ID_SF_PUBLISHED_GRP,
                        LibraryStringIds.SEARCH_FILTERS_PUBLISHED, true,
                        ID_SF_PUBLISHED_ALL, new FilterItem[]{
                                groupsFactory.getFilterForId(ID_SF_PUBLISHED_ALL),
                                groupsFactory.getFilterForId(ID_SF_PUBLISHED_TODAY),
                                groupsFactory.getFilterForId(ID_SF_PUBLISHED_LAST_7_DAYS),
                                groupsFactory.getFilterForId(ID_SF_PUBLISHED_LAST_30_DAYS),
                                groupsFactory.getFilterForId(ID_SF_PUBLISHED_LAST_YEAR),
                        }, null),
                groupsFactory.createFilterGroup(ID_SF_DURATION_GRP,
                        LibraryStringIds.SEARCH_FILTERS_DURATION, true,
                        ID_SF_DURATION_ALL, new FilterItem[]{
                                groupsFactory.getFilterForId(ID_SF_DURATION_ALL),
                                groupsFactory.getFilterForId(ID_SF_DURATION_SHORT),
                                groupsFactory.getFilterForId(ID_SF_DURATION_MEDIUM),
                                groupsFactory.getFilterForId(ID_SF_DURATION_LONG),
                        }, null),
        });


        /* content filters */
        groupsFactory.addFilterItem(new PeertubeFilterItem(
                ID_CF_MAIN_ALL,
                LibraryStringIds.SEARCH_FILTERS_ALL, ""));
        groupsFactory.addFilterItem(new PeertubeFilterItem(
                ID_CF_MAIN_VIDEOS,
                LibraryStringIds.SEARCH_FILTERS_VIDEOS, "resultType=videos"));
        groupsFactory.addFilterItem(new PeertubeFilterItem(
                ID_CF_MAIN_CHANNELS,
                LibraryStringIds.SEARCH_FILTERS_CHANNELS, "resultType=channels"));
        groupsFactory.addFilterItem(new PeertubeFilterItem(
                ID_CF_MAIN_PLAYLISTS,
                LibraryStringIds.SEARCH_FILTERS_PLAYLISTS, "resultType=playlists"));


        /* content filter groups */
        addContentFilterGroup(groupsFactory.createFilterGroup(ID_CF_MAIN_GRP, null, true,
                ID_CF_MAIN_ALL, new FilterItem[]{
                        groupsFactory.getFilterForId(ID_CF_MAIN_ALL),
                        groupsFactory.getFilterForId(ID_CF_MAIN_VIDEOS),
                        groupsFactory.getFilterForId(ID_CF_MAIN_CHANNELS),
                        groupsFactory.getFilterForId(ID_CF_MAIN_PLAYLISTS),
                }, allSortFilters));

        groupsFactory.addFilterItem(new PeertubeSepiaFilterItem(
                ID_CF_SEPIA_SEPIASEARCH,
                LibraryStringIds.SEARCH_FILTERS_SEPIASEARCH));

        addContentFilterGroup(groupsFactory.createFilterGroup(ID_CF_SEPIA_GRP, null, false,
                ITEM_IDENTIFIER_UNKNOWN, new FilterItem[]{
                        groupsFactory.getFilterForId(ID_CF_SEPIA_SEPIASEARCH)}, null));

        addContentFilterSortVariant(ID_CF_MAIN_ALL, allSortFilters);
        addContentFilterSortVariant(ID_CF_MAIN_VIDEOS, allSortFilters);
    }

    private void appendFilterToQueryString(@Nonnull final FilterItem item,
                                           @Nonnull final StringBuilder sortQuery) {
        if (item instanceof PeertubeFilterItem) {
            final PeertubeFilterItem sortItem =
                    (PeertubeFilterItem) item;
            final String query = sortItem.getQueryData();
            if (!query.isEmpty()) {
                sortQuery.append("&").append(query);
            }
        }
    }

    private static class PeertubeFilterItem extends FilterItem {
        protected final String query;

        PeertubeFilterItem(final int identifier,
                           @Nonnull final LibraryStringIds nameId,
                           @Nullable final String query) {
            super(identifier, nameId);
            this.query = query;
        }

        public String getQueryData() {
            return query;
        }
    }

    static class PeertubePublishedDateFilterItem extends PeertubeFilterItem {
        static final int NO_DAYS_SET = -1;
        private final int days;

        PeertubePublishedDateFilterItem(final int identifier,
                                        @Nonnull final LibraryStringIds nameId,
                                        @Nullable final String query, final int days) {
            super(identifier, nameId, query);
            this.days = days;
        }

        @Override
        public String getQueryData() {
            // return format eg: startDate=2022-08-01T22:00:00.000

            if (days == NO_DAYS_SET) {
                return "";
            }
            final LocalDateTime localDateTime = LocalDateTime.now().minusDays(days);

            return "startDate=" + localDateTime.format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
        }
    }

    public static class PeertubeSepiaFilterItem extends FilterItem {
        public PeertubeSepiaFilterItem(final int identifier,
                                       @Nonnull final LibraryStringIds nameId) {
            super(identifier, nameId);
        }
    }

    private static class PeertubeSortOrderFilterItem extends FilterItem {
        PeertubeSortOrderFilterItem(final int identifier,
                                    @Nonnull final LibraryStringIds nameId) {
            super(identifier, nameId);
        }
    }

    private class PeertubeSortFilterItem extends PeertubeFilterItem {
        PeertubeSortFilterItem(final int identifier,
                               @Nonnull final LibraryStringIds nameId,
                               @Nonnull final String query) {
            super(identifier, nameId, query);
        }

        @Override
        public String getQueryData() {
            if (!isAscending) {
                return query.replace("=", "=-");
            }
            return super.getQueryData();
        }
    }
}
