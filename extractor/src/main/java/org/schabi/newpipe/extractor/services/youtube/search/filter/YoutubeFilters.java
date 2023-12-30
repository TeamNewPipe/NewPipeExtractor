// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.youtube.search.filter;

import org.schabi.newpipe.extractor.search.filter.BaseSearchFilters;
import org.schabi.newpipe.extractor.search.filter.FilterContainer;
import org.schabi.newpipe.extractor.search.filter.FilterGroup;
import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.extractor.services.youtube.search.filter.protobuf.DateFilter;
import org.schabi.newpipe.extractor.services.youtube.search.filter.protobuf.Features;
import org.schabi.newpipe.extractor.services.youtube.search.filter.protobuf.LengthFilter;
import org.schabi.newpipe.extractor.services.youtube.search.filter.protobuf.SortOrder;
import org.schabi.newpipe.extractor.services.youtube.search.filter.protobuf.TypeFilter;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


public class YoutubeFilters extends BaseSearchFilters {
    public static final String UTF_8 = "UTF-8";

    /**
     * 'ALL' this is the default search content filter.
     * It has all sort filters that are available.
     */
    public static final String ALL = "all";
    public static final String VIDEOS = "videos";
    public static final String CHANNELS = "channels";
    public static final String PLAYLISTS = "playlists";
    // public static final String MOVIES = "movies";

    public static final int ID_CF_MAIN_GRP = 0;
    public static final int ID_CF_MAIN_ALL = 1;
    public static final int ID_CF_MAIN_VIDEOS = 2;
    public static final int ID_CF_MAIN_CHANNELS = 3;
    public static final int ID_CF_MAIN_PLAYLISTS = 4;
    // public static final int ID_CF_MAIN_MOVIES = 5;
    public static final int ID_CF_MAIN_YOUTUBE_MUSIC_SONGS = 6;
    public static final int ID_CF_MAIN_YOUTUBE_MUSIC_VIDEOS = 7;
    public static final int ID_CF_MAIN_YOUTUBE_MUSIC_ALBUMS = 8;
    public static final int ID_CF_MAIN_YOUTUBE_MUSIC_PLAYLISTS = 9;
    public static final int ID_CF_MAIN_YOUTUBE_MUSIC_ARTISTS = 10;
    public static final int ID_SF_SORT_BY_GRP = 11;
    public static final int ID_SF_SORT_BY_RELEVANCE = 12;
    public static final int ID_SF_SORT_BY_RATING = 13;
    public static final int ID_SF_SORT_BY_DATE = 14;
    public static final int ID_SF_SORT_BY_VIEWS = 15;
    public static final int ID_SF_UPLOAD_DATE_GRP = 16;
    public static final int ID_SF_UPLOAD_DATE_ALL = 17;
    public static final int ID_SF_UPLOAD_DATE_HOUR = 18;
    public static final int ID_SF_UPLOAD_DATE_DAY = 19;
    public static final int ID_SF_UPLOAD_DATE_WEEK = 20;
    public static final int ID_SF_UPLOAD_DATE_MONTH = 21;
    public static final int ID_SF_UPLOAD_DATE_YEAR = 22;
    public static final int ID_SF_DURATION_GRP = 23;
    public static final int ID_SF_DURATION_ALL = 24;
    public static final int ID_SF_DURATION_SHORT = 25;
    public static final int ID_SF_DURATION_MEDIUM = 26;
    public static final int ID_SF_DURATION_LONG = 27;
    public static final int ID_SF_FEATURES_GRP = 28;
    public static final int ID_SF_FEATURES_LIVE = 29;
    public static final int ID_SF_FEATURES_4K = 30;
    public static final int ID_SF_FEATURES_HD = 31;
    public static final int ID_SF_FEATURES_SUBTITLES = 32;
    public static final int ID_SF_FEATURES_CCOMMONS = 33;
    public static final int ID_SF_FEATURES_360 = 34;
    public static final int ID_SF_FEATURES_VR180 = 35;
    public static final int ID_SF_FEATURES_3D = 36;
    public static final int ID_SF_FEATURES_HDR = 37;
    public static final int ID_SF_FEATURES_LOCATION = 38;
    public static final int ID_SF_FEATURES_PURCHASED = 39;

    public static final String MUSIC_SONGS = "music_songs";
    public static final String MUSIC_VIDEOS = "music_videos";
    public static final String MUSIC_ALBUMS = "music_albums";
    public static final String MUSIC_PLAYLISTS = "music_playlists";
    public static final String MUSIC_ARTISTS = "music_artists";

    private static final String SEARCH_URL = "https://www.youtube.com/results?search_query=";
    private static final String MUSIC_SEARCH_URL = "https://music.youtube.com/search?q=";

    /**
     * generate the search parameter protobuf 'sp' string that is appended to the search URL.
     *
     * @param contentFilterItem the active content filter item
     * @return the protobuf base64 encoded 'sp' parameter
     */
    private String generateYoutubeSpParameter(final YoutubeContentFilterItem contentFilterItem) {
        boolean atLeastOneParamSet = false;
        final YoutubeProtoBufferSearchParameterAccessor.Builder builder =
                new YoutubeProtoBufferSearchParameterAccessor.Builder();
        final TypeFilter typeFilter = (contentFilterItem != null)
                ? contentFilterItem.getContentType()
                : null;

        // set content filter item in builder
        if (contentFilterItem != null) {
            atLeastOneParamSet = true;
            builder.setTypeFilter(typeFilter);
        }

        if (selectedSortFilter != null) {
            for (final FilterItem sortItem : selectedSortFilter) {
                if (checkSortFilterItemAndSetInBuilder(builder, sortItem)) {
                    atLeastOneParamSet = true;
                }
            }
        }

        try {
            if (atLeastOneParamSet) {
                return builder.build().getSp();
            } else {
                return null;
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Check if suitable youtube sort filter and set it in the builder.
     *
     * @param builder  the builder for protobuf
     * @param sortItem the item to check and add
     * @return true if item was set in the builder
     */
    private boolean checkSortFilterItemAndSetInBuilder(
            final YoutubeProtoBufferSearchParameterAccessor.Builder builder,
            final FilterItem sortItem) {
        boolean atLeastOneParamSet = false;
        if (sortItem instanceof YoutubeSortOrderSortFilterItem) {
            final SortOrder sortOrder = ((YoutubeSortOrderSortFilterItem) sortItem).get();
            if (null != sortOrder) {
                builder.setSortOrder(sortOrder);
                atLeastOneParamSet = true;
            }
        } else if (sortItem instanceof YoutubeDateSortFilterItem) {
            final DateFilter dateFilter = ((YoutubeDateSortFilterItem) sortItem).get();
            if (null != dateFilter) {
                builder.setDateFilter(dateFilter);
                atLeastOneParamSet = true;
            }
        } else if (sortItem instanceof YoutubeLenSortFilterItem) {
            final LengthFilter lengthFilter = ((YoutubeLenSortFilterItem) sortItem).get();
            if (null != lengthFilter) {
                builder.setLengthFilter(lengthFilter);
                atLeastOneParamSet = true;
            }
        } else if (sortItem instanceof YoutubeFeatureSortFilterItem) {
            final Features feature = ((YoutubeFeatureSortFilterItem) sortItem).get();
            if (null != feature) {
                builder.addFeature(feature);
                atLeastOneParamSet = true;
            }
        }
        return atLeastOneParamSet;
    }

    @Override
    public String evaluateSelectedFilters(final String searchString) {
        String sp = null;
        if (selectedContentFilter != null && !selectedContentFilter.isEmpty()) {
            // as of now there is just one content filter available
            final YoutubeContentFilterItem contentFilterItem =
                    (YoutubeContentFilterItem) selectedContentFilter.get(0);

            sp = generateYoutubeSpParameter(contentFilterItem);

            if (contentFilterItem instanceof MusicYoutubeContentFilterItem) {
                try {
                    return MUSIC_SEARCH_URL
                            + Utils.encodeUrlUtf8(searchString);
                } catch (final UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            } else {
                if (contentFilterItem != null) {
                    contentFilterItem.setParams(sp);
                }
            }
        }

        try {
            return SEARCH_URL
                    + Utils.encodeUrlUtf8(searchString)
                    + ((null != sp && !sp.isEmpty()) ? "&sp=" + sp : "");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("checkstyle:MethodLength")
    @Override
    protected void init() {
        /* sort filters */

        /* 'Sort order' filter items */
        groupsFactory.addFilterItem(new YoutubeSortOrderSortFilterItem(
                ID_SF_SORT_BY_RELEVANCE, "Relevance", SortOrder.relevance));
        groupsFactory.addFilterItem(new YoutubeSortOrderSortFilterItem(
                ID_SF_SORT_BY_RATING, "Rating", SortOrder.rating));
        groupsFactory.addFilterItem(new YoutubeSortOrderSortFilterItem(
                ID_SF_SORT_BY_DATE, "Date", SortOrder.date));
        groupsFactory.addFilterItem(new YoutubeSortOrderSortFilterItem(
                ID_SF_SORT_BY_VIEWS, "Views", SortOrder.views));

        /* 'Date' filter items */
        groupsFactory.addFilterItem(new YoutubeDateSortFilterItem(
                ID_SF_UPLOAD_DATE_ALL, "All", null));
        groupsFactory.addFilterItem(new YoutubeDateSortFilterItem(
                ID_SF_UPLOAD_DATE_HOUR, "Hour", DateFilter.hour));
        groupsFactory.addFilterItem(new YoutubeDateSortFilterItem(
                ID_SF_UPLOAD_DATE_DAY, "Day", DateFilter.day));
        groupsFactory.addFilterItem(new YoutubeDateSortFilterItem(
                ID_SF_UPLOAD_DATE_WEEK, "Week", DateFilter.week));
        groupsFactory.addFilterItem(new YoutubeDateSortFilterItem(
                ID_SF_UPLOAD_DATE_MONTH, "Month", DateFilter.month));
        groupsFactory.addFilterItem(new YoutubeDateSortFilterItem(
                ID_SF_UPLOAD_DATE_YEAR, "Year", DateFilter.year));

        /* 'Duration' filter items */
        groupsFactory.addFilterItem(new YoutubeLenSortFilterItem(
                ID_SF_DURATION_ALL, "All", null));
        groupsFactory.addFilterItem(new YoutubeLenSortFilterItem(
                ID_SF_DURATION_SHORT, "Under 4 min", LengthFilter.duration_short));
        groupsFactory.addFilterItem(new YoutubeLenSortFilterItem(
                ID_SF_DURATION_MEDIUM, "4-20 min", LengthFilter.duration_medium));
        groupsFactory.addFilterItem(new YoutubeLenSortFilterItem(
                ID_SF_DURATION_LONG, "Over 20 min", LengthFilter.duration_long));

        /* 'features' filter items */
        groupsFactory.addFilterItem(new YoutubeFeatureSortFilterItem(
                ID_SF_FEATURES_LIVE, "Live", Features.live));
        groupsFactory.addFilterItem(new YoutubeFeatureSortFilterItem(
                ID_SF_FEATURES_4K, "4k", Features.is_4k));
        groupsFactory.addFilterItem(new YoutubeFeatureSortFilterItem(
                ID_SF_FEATURES_HD, "HD", Features.is_hd));
        groupsFactory.addFilterItem(new YoutubeFeatureSortFilterItem(
                ID_SF_FEATURES_SUBTITLES, "Subtitles", Features.subtitles));
        groupsFactory.addFilterItem(new YoutubeFeatureSortFilterItem(
                ID_SF_FEATURES_CCOMMONS, "Ccommons", Features.ccommons));
        groupsFactory.addFilterItem(new YoutubeFeatureSortFilterItem(
                ID_SF_FEATURES_360, "360°", Features.is_360));
        groupsFactory.addFilterItem(new YoutubeFeatureSortFilterItem(
                ID_SF_FEATURES_VR180, "VR180", Features.is_vr180));
        groupsFactory.addFilterItem(new YoutubeFeatureSortFilterItem(
                ID_SF_FEATURES_3D, "3d", Features.is_3d));
        groupsFactory.addFilterItem(new YoutubeFeatureSortFilterItem(
                ID_SF_FEATURES_HDR, "Hdr", Features.is_hdr));
        groupsFactory.addFilterItem(new YoutubeFeatureSortFilterItem(
                ID_SF_FEATURES_LOCATION, "Location", Features.location));
        groupsFactory.addFilterItem(new YoutubeFeatureSortFilterItem(
                ID_SF_FEATURES_PURCHASED, "Purchased", Features.purchased));

        final FilterGroup sortByGroup =
                groupsFactory.createFilterGroup(ID_SF_SORT_BY_GRP, "Sort by", true,
                        ID_SF_SORT_BY_RELEVANCE, new FilterItem[]{
                                groupsFactory.getFilterForId(ID_SF_SORT_BY_RELEVANCE),
                                groupsFactory.getFilterForId(ID_SF_SORT_BY_RATING),
                                groupsFactory.getFilterForId(ID_SF_SORT_BY_DATE),
                                groupsFactory.getFilterForId(ID_SF_SORT_BY_VIEWS),
                        }, null);

        final FilterGroup uploadDateGroup =
                groupsFactory.createFilterGroup(ID_SF_UPLOAD_DATE_GRP, "Upload Date", true,
                        ID_SF_UPLOAD_DATE_ALL, new FilterItem[]{
                                groupsFactory.getFilterForId(ID_SF_UPLOAD_DATE_ALL),
                                groupsFactory.getFilterForId(ID_SF_UPLOAD_DATE_HOUR),
                                groupsFactory.getFilterForId(ID_SF_UPLOAD_DATE_DAY),
                                groupsFactory.getFilterForId(ID_SF_UPLOAD_DATE_WEEK),
                                groupsFactory.getFilterForId(ID_SF_UPLOAD_DATE_MONTH),
                                groupsFactory.getFilterForId(ID_SF_UPLOAD_DATE_YEAR),
                        }, null);

        final FilterGroup durationGroup =
                groupsFactory.createFilterGroup(ID_SF_DURATION_GRP, "Duration", true,
                        ID_SF_DURATION_ALL, new FilterItem[]{
                                groupsFactory.getFilterForId(ID_SF_DURATION_ALL),
                                groupsFactory.getFilterForId(ID_SF_DURATION_SHORT),
                                groupsFactory.getFilterForId(ID_SF_DURATION_MEDIUM),
                                groupsFactory.getFilterForId(ID_SF_DURATION_LONG),
                        }, null);

        final FilterGroup featureGroup =
                groupsFactory.createFilterGroup(ID_SF_FEATURES_GRP, "Features", false,
                        FilterContainer.ITEM_IDENTIFIER_UNKNOWN, new FilterItem[]{
                                groupsFactory.getFilterForId(ID_SF_FEATURES_LIVE),
                                groupsFactory.getFilterForId(ID_SF_FEATURES_4K),
                                groupsFactory.getFilterForId(ID_SF_FEATURES_HD),
                                groupsFactory.getFilterForId(ID_SF_FEATURES_SUBTITLES),
                                groupsFactory.getFilterForId(ID_SF_FEATURES_CCOMMONS),
                                groupsFactory.getFilterForId(ID_SF_FEATURES_360),
                                groupsFactory.getFilterForId(ID_SF_FEATURES_VR180),
                                groupsFactory.getFilterForId(ID_SF_FEATURES_3D),
                                groupsFactory.getFilterForId(ID_SF_FEATURES_HDR),
                                groupsFactory.getFilterForId(ID_SF_FEATURES_LOCATION),
                                // there is not use for that feature ATM.
                                // groupsFactory.getFilterForId(ID_SF_FEATURES_PURCHASED),
                        }, null);

        final FilterGroup[] videoFilters = new FilterGroup[]{
                sortByGroup,
                uploadDateGroup,
                durationGroup,
                featureGroup
        };

        final FilterGroup[] channelPlaylistFilters = new FilterGroup[]{sortByGroup};

        /* videoFilters contains all sort filters available */
        final FilterContainer allSortFilters = new FilterContainer(videoFilters);
        final FilterContainer sortFiltersForChannelAndPlaylists =
                new FilterContainer(channelPlaylistFilters);

        addContentFilterTypeAndSortVariant(ID_CF_MAIN_ALL, allSortFilters);
        addContentFilterTypeAndSortVariant(ID_CF_MAIN_VIDEOS, allSortFilters);
        addContentFilterTypeAndSortVariant(ID_CF_MAIN_CHANNELS, sortFiltersForChannelAndPlaylists);
        addContentFilterTypeAndSortVariant(ID_CF_MAIN_PLAYLISTS, sortFiltersForChannelAndPlaylists);

        /*
        // -> movies are only available for logged in users
        addContentFilterTypeAndSortVariant(ID_CF_MAIN_MOVIES, new FilterContainer(new FilterGroup[]{
                sortByGroup,
                uploadDateGroup,
                durationGroup,
                groupsFactory.createSortGroup(ID_SF_FEATURES_GRP, "Features", false,
                        Filter.ITEM_IDENTIFIER_UNKNOWN, new FilterItem[]{
                                groupsFactory.getFilterForId(ID_SF_FEATURES_4K),
                                groupsFactory.getFilterForId(ID_SF_FEATURES_HD),
                                groupsFactory.getFilterForId(ID_SF_FEATURES_360),
                                groupsFactory.getFilterForId(ID_SF_FEATURES_VR180),
                                groupsFactory.getFilterForId(ID_SF_FEATURES_HDR),
                                groupsFactory.getFilterForId(ID_SF_FEATURES_LOCATION),
                                // there is not use for that feature ATM.
                                // groupsFactory.getFilterForId(ID_SF_FEATURES_PURCHASED),



                        }, null)
        });
         */

        /* content filters with sort filters */
        groupsFactory.addFilterItem(new YoutubeContentFilterItem(
                ID_CF_MAIN_ALL, ALL, null));
        groupsFactory.addFilterItem(new YoutubeContentFilterItem(
                ID_CF_MAIN_VIDEOS, VIDEOS, TypeFilter.video));
        groupsFactory.addFilterItem(new YoutubeContentFilterItem(
                ID_CF_MAIN_CHANNELS, CHANNELS, TypeFilter.channel));
        groupsFactory.addFilterItem(new YoutubeContentFilterItem(
                ID_CF_MAIN_PLAYLISTS, PLAYLISTS, TypeFilter.playlist));
        /*
        // movies are only available for logged in users
        builder.addFilterItem(new YoutubeContentFilterItem(
                ID_CF_MAIN_MOVIES, MOVIES, TypeFilter.movie));
         */

        /* Youtube Music content filters */
        groupsFactory.addFilterItem(new MusicYoutubeContentFilterItem(
                ID_CF_MAIN_YOUTUBE_MUSIC_SONGS, MUSIC_SONGS,
                "Eg-KAQwIARAAGAAgACgAMABqChAEEAUQAxAKEAk%3D"
        ));
        groupsFactory.addFilterItem(new MusicYoutubeContentFilterItem(
                ID_CF_MAIN_YOUTUBE_MUSIC_VIDEOS, MUSIC_VIDEOS,
                "Eg-KAQwIABABGAAgACgAMABqChAEEAUQAxAKEAk%3D"
        ));
        groupsFactory.addFilterItem(new MusicYoutubeContentFilterItem(
                ID_CF_MAIN_YOUTUBE_MUSIC_ALBUMS, MUSIC_ALBUMS,
                "Eg-KAQwIABAAGAEgACgAMABqChAEEAUQAxAKEAk%3D"
        ));
        groupsFactory.addFilterItem(new MusicYoutubeContentFilterItem(
                ID_CF_MAIN_YOUTUBE_MUSIC_PLAYLISTS, MUSIC_PLAYLISTS,
                "Eg-KAQwIABAAGAAgACgBMABqChAEEAUQAxAKEAk%3D"
        ));
        groupsFactory.addFilterItem(new MusicYoutubeContentFilterItem(
                ID_CF_MAIN_YOUTUBE_MUSIC_ARTISTS, MUSIC_ARTISTS,
                "Eg-KAQwIABAAGAAgASgAMABqChAEEAUQAxAKEAk%3D"
        ));

        final FilterGroup contentFilterGroup =
                groupsFactory.createFilterGroup(ID_CF_MAIN_GRP, null, true,
                        ID_CF_MAIN_ALL, new FilterItem[]{
                                groupsFactory.getFilterForId(ID_CF_MAIN_ALL),
                                groupsFactory.getFilterForId(ID_CF_MAIN_VIDEOS),
                                groupsFactory.getFilterForId(ID_CF_MAIN_CHANNELS),
                                groupsFactory.getFilterForId(ID_CF_MAIN_PLAYLISTS),
                                // groupsFactory.getFilterForId(ID_CF_MAIN_MOVIES),
                                groupsFactory.getFilterForId(groupsFactory.addFilterItem(
                                        new FilterItem.DividerItem("YouTube Music"))),
                                groupsFactory.getFilterForId(ID_CF_MAIN_YOUTUBE_MUSIC_SONGS),
                                groupsFactory.getFilterForId(ID_CF_MAIN_YOUTUBE_MUSIC_VIDEOS),
                                groupsFactory.getFilterForId(ID_CF_MAIN_YOUTUBE_MUSIC_ALBUMS),
                                groupsFactory.getFilterForId(ID_CF_MAIN_YOUTUBE_MUSIC_PLAYLISTS),
                                groupsFactory.getFilterForId(ID_CF_MAIN_YOUTUBE_MUSIC_ARTISTS),
                        }, allSortFilters);
        addContentFilterGroup(contentFilterGroup);
    }

    private void addContentFilterTypeAndSortVariant(final int contentFilterId,
                                                    final FilterContainer variant) {
        addContentFilterSortVariant(contentFilterId, variant);
    }

    private static class YoutubeSortOrderSortFilterItem extends YoutubeSortFilterItem {
        private final SortOrder sortOrder;

        YoutubeSortOrderSortFilterItem(final int identifier, final String name,
                                       final SortOrder sortOrder) {
            super(identifier, name);
            this.sortOrder = sortOrder;
        }

        public SortOrder get() {
            return sortOrder;
        }
    }

    private static class YoutubeDateSortFilterItem extends YoutubeSortFilterItem {
        private final DateFilter dateFilter;

        YoutubeDateSortFilterItem(final int identifier, final String name,
                                  final DateFilter dateFilter) {
            super(identifier, name);
            this.dateFilter = dateFilter;
        }

        public DateFilter get() {
            return this.dateFilter;
        }
    }

    private static class YoutubeLenSortFilterItem extends YoutubeSortFilterItem {
        private final LengthFilter lengthFilter;

        YoutubeLenSortFilterItem(final int identifier, final String name,
                                 final LengthFilter lengthFilter) {
            super(identifier, name);
            this.lengthFilter = lengthFilter;
        }

        public LengthFilter get() {
            return this.lengthFilter;
        }
    }

    private static class YoutubeFeatureSortFilterItem extends YoutubeSortFilterItem {
        private final Features feature;

        YoutubeFeatureSortFilterItem(final int identifier, final String name,
                                     final Features feature) {
            super(identifier, name);
            this.feature = feature;
        }

        public Features get() {
            return this.feature;
        }
    }

    public static class YoutubeSortFilterItem extends FilterItem {

        public YoutubeSortFilterItem(final int identifier, final String name) {
            super(identifier, name);
        }
    }

    public static class YoutubeContentFilterItem extends YoutubeSortFilterItem {
        protected String params;
        private TypeFilter contentType = null;

        public YoutubeContentFilterItem(final int identifier, final String name) {
            super(identifier, name);
        }

        public YoutubeContentFilterItem(final int identifier, final String name,
                                        final TypeFilter contentType) {
            super(identifier, name);
            this.params = "";
            this.contentType = contentType;
        }

        public String getParams() {
            return params;
        }

        public void setParams(final String params) {
            this.params = params;
        }

        private TypeFilter getContentType() {
            return contentType;
        }
    }

    public static class MusicYoutubeContentFilterItem extends YoutubeContentFilterItem {
        public MusicYoutubeContentFilterItem(final int identifier, final String name,
                                             final String params) {
            super(identifier, name);
            this.params = params;
        }
    }
}
