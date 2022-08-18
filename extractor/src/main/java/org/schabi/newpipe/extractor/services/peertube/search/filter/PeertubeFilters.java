// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.peertube.search.filter;


import org.schabi.newpipe.extractor.search.filter.BaseSearchFilters;
import org.schabi.newpipe.extractor.search.filter.FilterContainer;
import org.schabi.newpipe.extractor.search.filter.FilterGroup;
import org.schabi.newpipe.extractor.search.filter.FilterItem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

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
    public String evaluateSelectedFilters(final String searchString) {
        final StringBuilder sortQuery = new StringBuilder();

        if (selectedSortFilter != null) {
            final Optional<FilterItem> ascendingFilter = selectedSortFilter.stream()
                    .filter(filterItem -> filterItem instanceof PeertubeSortOrderFilterItem)
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
                ID_SF_SORT_ORDER_ASCENDING, "Ascending"));

        /* 'Sort by' filter items */
        groupsFactory.addFilterItem(new PeertubeSortFilterItem(
                ID_SF_SORT_BY_RELEVANCE, "Relevance", "sort=match"));
        groupsFactory.addFilterItem(new PeertubeSortFilterItem(
                ID_SF_SORT_BY_NAME, "Name", "sort=name"));
        groupsFactory.addFilterItem(new PeertubeSortFilterItem(
                ID_SF_SORT_BY_DURATION, "Duration", "sort=duration"));
        groupsFactory.addFilterItem(new PeertubeSortFilterItem(
                ID_SF_SORT_BY_PUBLISH_DATE, "Publish date", "sort=publishedAt"));
        groupsFactory.addFilterItem(new PeertubeSortFilterItem(
                ID_SF_SORT_BY_CREATION_DATE, "Creation date", "sort=createdAt"));
        groupsFactory.addFilterItem(new PeertubeSortFilterItem(
                ID_SF_SORT_BY_VIEWS, "Views", "sort=views"));
        groupsFactory.addFilterItem(new PeertubeSortFilterItem(
                ID_SF_SORT_BY_LIKES, "Likes", "sort=likes"));

        /* stream kind filter items */
        groupsFactory.addFilterItem(new PeertubeFilterItem(
                ID_SF_KIND_ALL, "All", ""));
        groupsFactory.addFilterItem(new PeertubeFilterItem(
                ID_SF_KIND_LIVE, "Live", "isLive=true"));
        groupsFactory.addFilterItem(new PeertubeFilterItem(
                ID_SF_KIND_VOD_VIDEOS, "VOD videos", "isLive=false"));

        /* sensitive filter items */
        groupsFactory.addFilterItem(new PeertubeFilterItem(
                ID_SF_SENSITIVE_ALL, "All", ""));
        groupsFactory.addFilterItem(new PeertubeFilterItem(
                ID_SF_SENSITIVE_YES, "Yes", "nsfw=true"));
        groupsFactory.addFilterItem(new PeertubeFilterItem(
                ID_SF_SENSITIVE_NO, "No", "nsfw=false"));

        /* 'Date' filter items */
        // here query is set to null as the value is generated dynamically
        groupsFactory.addFilterItem(new PeertubePublishedDateFilterItem(
                ID_SF_PUBLISHED_ALL, "All", null,
                PeertubePublishedDateFilterItem.NO_DAYS_SET));
        groupsFactory.addFilterItem(new PeertubePublishedDateFilterItem(
                ID_SF_PUBLISHED_TODAY, "Today", null, 1));
        groupsFactory.addFilterItem(new PeertubePublishedDateFilterItem(
                ID_SF_PUBLISHED_LAST_7_DAYS, "last 7 days", null, 7));
        groupsFactory.addFilterItem(new PeertubePublishedDateFilterItem(
                ID_SF_PUBLISHED_LAST_30_DAYS, "last 30 days", null, 30));
        groupsFactory.addFilterItem(new PeertubePublishedDateFilterItem(
                ID_SF_PUBLISHED_LAST_YEAR, "last year", null, 365));

        /* 'Duration' filter items */
        groupsFactory.addFilterItem(new PeertubeFilterItem(
                ID_SF_DURATION_ALL, "All", ""));
        groupsFactory.addFilterItem(new PeertubeFilterItem(
                ID_SF_DURATION_SHORT, "Short (< 4 min)", "durationMax=240"));
        groupsFactory.addFilterItem(new PeertubeFilterItem(
                ID_SF_DURATION_MEDIUM, "Medium (4-10 min)", "durationMin=240&durationMax=600"));
        groupsFactory.addFilterItem(new PeertubeFilterItem(
                ID_SF_DURATION_LONG, "Long (> 10 min)", "durationMin=600"));

        final FilterContainer allSortFilters = new FilterContainer(new FilterGroup[]{
                groupsFactory.createFilterGroup(ID_SF_SORT_ORDER_GRP, "Sort order", false,
                        ITEM_IDENTIFIER_UNKNOWN, new FilterItem[]{
                                groupsFactory.getFilterForId(ID_SF_SORT_ORDER_ASCENDING),
                        }, null),
                groupsFactory.createFilterGroup(ID_SF_SORT_BY_GRP, "Sort by", true,
                        ID_SF_SORT_BY_RELEVANCE, new FilterItem[]{
                                groupsFactory.getFilterForId(ID_SF_SORT_BY_RELEVANCE),
                                groupsFactory.getFilterForId(ID_SF_SORT_BY_NAME),
                                groupsFactory.getFilterForId(ID_SF_SORT_BY_DURATION),
                                groupsFactory.getFilterForId(ID_SF_SORT_BY_PUBLISH_DATE),
                                groupsFactory.getFilterForId(ID_SF_SORT_BY_CREATION_DATE),
                                groupsFactory.getFilterForId(ID_SF_SORT_BY_VIEWS),
                                groupsFactory.getFilterForId(ID_SF_SORT_BY_LIKES),
                        }, null),
                groupsFactory.createFilterGroup(ID_SF_KIND_GRP, "Kind", true,
                        ID_SF_KIND_ALL, new FilterItem[]{
                                groupsFactory.getFilterForId(ID_SF_KIND_ALL),
                                groupsFactory.getFilterForId(ID_SF_KIND_LIVE),
                                groupsFactory.getFilterForId(ID_SF_KIND_VOD_VIDEOS),
                        }, null),
                groupsFactory.createFilterGroup(ID_SF_SENSITIVE_GRP, "Sensitive", true,
                        ID_SF_SENSITIVE_ALL, new FilterItem[]{
                                groupsFactory.getFilterForId(ID_SF_SENSITIVE_ALL),
                                groupsFactory.getFilterForId(ID_SF_SENSITIVE_YES),
                                groupsFactory.getFilterForId(ID_SF_SENSITIVE_NO),
                        }, null),
                groupsFactory.createFilterGroup(ID_SF_PUBLISHED_GRP, "Published", true,
                        ID_SF_PUBLISHED_ALL, new FilterItem[]{
                                groupsFactory.getFilterForId(ID_SF_PUBLISHED_ALL),
                                groupsFactory.getFilterForId(ID_SF_PUBLISHED_TODAY),
                                groupsFactory.getFilterForId(ID_SF_PUBLISHED_LAST_7_DAYS),
                                groupsFactory.getFilterForId(ID_SF_PUBLISHED_LAST_30_DAYS),
                                groupsFactory.getFilterForId(ID_SF_PUBLISHED_LAST_YEAR),
                        }, null),
                groupsFactory.createFilterGroup(ID_SF_DURATION_GRP, "Duration", true,
                        ID_SF_DURATION_ALL, new FilterItem[]{
                                groupsFactory.getFilterForId(ID_SF_DURATION_ALL),
                                groupsFactory.getFilterForId(ID_SF_DURATION_SHORT),
                                groupsFactory.getFilterForId(ID_SF_DURATION_MEDIUM),
                                groupsFactory.getFilterForId(ID_SF_DURATION_LONG),
                        }, null),
        });


        /* content filters */
        groupsFactory.addFilterItem(new PeertubeFilterItem(
                ID_CF_MAIN_ALL, "All", ""));
        groupsFactory.addFilterItem(new PeertubeFilterItem(
                ID_CF_MAIN_VIDEOS, "Videos", "resultType=videos"));
        groupsFactory.addFilterItem(new PeertubeFilterItem(
                ID_CF_MAIN_CHANNELS, "Channels", "resultType=channels"));
        groupsFactory.addFilterItem(new PeertubeFilterItem(
                ID_CF_MAIN_PLAYLISTS, "Playlists", "resultType=playlists"));


        /* content filter groups */
        addContentFilterGroup(groupsFactory.createFilterGroup(ID_CF_MAIN_GRP, null, true,
                ID_CF_MAIN_ALL, new FilterItem[]{
                        groupsFactory.getFilterForId(ID_CF_MAIN_ALL),
                        groupsFactory.getFilterForId(ID_CF_MAIN_VIDEOS),
                        groupsFactory.getFilterForId(ID_CF_MAIN_CHANNELS),
                        groupsFactory.getFilterForId(ID_CF_MAIN_PLAYLISTS),
                }, allSortFilters));

        groupsFactory.addFilterItem(new PeertubeSepiaFilterItem(
                ID_CF_SEPIA_SEPIASEARCH, "SepiaSearch"));

        addContentFilterGroup(groupsFactory.createFilterGroup(ID_CF_SEPIA_GRP, null, false,
                ITEM_IDENTIFIER_UNKNOWN, new FilterItem[]{
                        groupsFactory.getFilterForId(ID_CF_SEPIA_SEPIASEARCH)}, null));

        addContentFilterSortVariant(ID_CF_MAIN_ALL, allSortFilters);
        addContentFilterSortVariant(ID_CF_MAIN_VIDEOS, allSortFilters);
    }

    private void appendFilterToQueryString(final FilterItem item,
                                           final StringBuilder sortQuery) {
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

        PeertubeFilterItem(final int identifier, final String name, final String query) {
            super(identifier, name);
            this.query = query;
        }

        public String getQueryData() {
            return query;
        }
    }

    static class PeertubePublishedDateFilterItem extends PeertubeFilterItem {
        static final int NO_DAYS_SET = -1;
        private final int days;

        PeertubePublishedDateFilterItem(final int identifier, final String name,
                                        final String query, final int days) {
            super(identifier, name, query);
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
        public PeertubeSepiaFilterItem(final int identifier, final String name) {
            super(identifier, name);
        }
    }

    private static class PeertubeSortOrderFilterItem extends FilterItem {
        PeertubeSortOrderFilterItem(final int identifier, final String name) {
            super(identifier, name);
        }
    }

    private class PeertubeSortFilterItem extends PeertubeFilterItem {
        PeertubeSortFilterItem(final int identifier, final String name, final String query) {
            super(identifier, name, query);
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
