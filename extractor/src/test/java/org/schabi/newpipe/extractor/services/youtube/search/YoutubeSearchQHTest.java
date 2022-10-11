package org.schabi.newpipe.extractor.services.youtube.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static java.util.Collections.singletonList;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.search.filter.FilterContainer;
import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.extractor.services.DefaultSearchExtractorTest;
import org.schabi.newpipe.extractor.services.youtube.search.filter.YoutubeFilters;

public class YoutubeSearchQHTest {

    @Test
    public void testDefaultSearch() throws Exception {
        assertEquals("https://www.youtube.com/results?search_query=asdf&sp=8AEB", YouTube.getSearchQHFactory().fromQuery("asdf").getUrl());
        assertEquals("https://www.youtube.com/results?search_query=hans&sp=8AEB", YouTube.getSearchQHFactory().fromQuery("hans").getUrl());
        assertEquals("https://www.youtube.com/results?search_query=Poifj%26jaijf&sp=8AEB", YouTube.getSearchQHFactory().fromQuery("Poifj&jaijf").getUrl());
        assertEquals("https://www.youtube.com/results?search_query=G%C3%BCl%C3%BCm&sp=8AEB", YouTube.getSearchQHFactory().fromQuery("Gülüm").getUrl());
        assertEquals("https://www.youtube.com/results?search_query=%3Fj%24%29H%C2%A7B&sp=8AEB", YouTube.getSearchQHFactory().fromQuery("?j$)H§B").getUrl());
    }

    @Test
    public void testMusicSongsSearch() throws Exception {
        final FilterItem item = DefaultSearchExtractorTest.getFilterItem(YouTube, YoutubeFilters.ID_CF_MAIN_YOUTUBE_MUSIC_SONGS);
        assertEquals("https://music.youtube.com/search?q=asdf", YouTube.getSearchQHFactory().fromQuery("asdf", singletonList(item), null).getUrl());
        assertEquals("https://music.youtube.com/search?q=hans", YouTube.getSearchQHFactory().fromQuery("hans", singletonList(item), null).getUrl());
        assertEquals("https://music.youtube.com/search?q=Poifj%26jaijf", YouTube.getSearchQHFactory().fromQuery("Poifj&jaijf", singletonList(item), null).getUrl());
        assertEquals("https://music.youtube.com/search?q=G%C3%BCl%C3%BCm", YouTube.getSearchQHFactory().fromQuery("Gülüm", singletonList(item), null).getUrl());
        assertEquals("https://music.youtube.com/search?q=%3Fj%24%29H%C2%A7B", YouTube.getSearchQHFactory().fromQuery("?j$)H§B", singletonList(item), null).getUrl());
    }

    @Test
    public void testGetContentFilter() throws Exception {
        final FilterItem videoFilterItem = DefaultSearchExtractorTest.getFilterItem(YouTube, YoutubeFilters.ID_CF_MAIN_VIDEOS);
        final FilterItem channelsFilterItem = DefaultSearchExtractorTest.getFilterItem(YouTube, YoutubeFilters.ID_CF_MAIN_CHANNELS);
        assertEquals(YoutubeFilters.ID_CF_MAIN_VIDEOS, YouTube.getSearchQHFactory()
                .fromQuery("", singletonList(videoFilterItem), null).getContentFilters().get(0).getIdentifier());
        assertEquals(YoutubeFilters.ID_CF_MAIN_CHANNELS, YouTube.getSearchQHFactory()
                .fromQuery("asdf", singletonList(channelsFilterItem), null).getContentFilters().get(0).getIdentifier());

        final FilterItem musicSongsFilterItem = DefaultSearchExtractorTest.getFilterItem(YouTube, YoutubeFilters.ID_CF_MAIN_YOUTUBE_MUSIC_SONGS);
        assertEquals(YoutubeFilters.ID_CF_MAIN_YOUTUBE_MUSIC_SONGS, YouTube.getSearchQHFactory()
                .fromQuery("asdf", singletonList(musicSongsFilterItem), null).getContentFilters().get(0).getIdentifier());
    }

    @Test
    public void testWithContentfilter() throws Exception {
        final FilterItem videoFilterItem = DefaultSearchExtractorTest.getFilterItem(YouTube, YoutubeFilters.ID_CF_MAIN_VIDEOS);
        final FilterItem channelsFilterItem = DefaultSearchExtractorTest.getFilterItem(YouTube, YoutubeFilters.ID_CF_MAIN_CHANNELS);
        final FilterItem playlistsFilterItem = DefaultSearchExtractorTest.getFilterItem(YouTube, YoutubeFilters.ID_CF_MAIN_PLAYLISTS);
        final FilterItem musicSongsFilterItem = DefaultSearchExtractorTest.getFilterItem(YouTube, YoutubeFilters.ID_CF_MAIN_YOUTUBE_MUSIC_SONGS);
        assertEquals("https://www.youtube.com/results?search_query=asdf&sp=EgIQAfABAQ%3D%3D", YouTube.getSearchQHFactory()
                .fromQuery("asdf", singletonList(videoFilterItem), null).getUrl());
        assertEquals("https://www.youtube.com/results?search_query=asdf&sp=EgIQAvABAQ%3D%3D", YouTube.getSearchQHFactory()
                .fromQuery("asdf", singletonList(channelsFilterItem), null).getUrl());
        assertEquals("https://www.youtube.com/results?search_query=asdf&sp=EgIQA_ABAQ%3D%3D", YouTube.getSearchQHFactory()
                .fromQuery("asdf", singletonList(playlistsFilterItem), null).getUrl());
        assertEquals("https://www.youtube.com/results?search_query=asdf&sp=8AEB", YouTube.getSearchQHFactory()
                .fromQuery("asdf", singletonList(null), null).getUrl());

        assertEquals("https://music.youtube.com/search?q=asdf", YouTube.getSearchQHFactory()
                .fromQuery("asdf", singletonList(musicSongsFilterItem), null).getUrl());
    }

    @Test
    public void testGetAvailableContentFilter() {
        final FilterContainer contentFilter =
                YouTube.getSearchQHFactory().getAvailableContentFilter();

        final int noOfContentFilters = DefaultSearchExtractorTest.getNoOfFilterItems(contentFilter);
        final FilterItem[] filterItems = contentFilter.getFilterGroups()[0].getFilterItems();
        assertEquals(10, noOfContentFilters);
        assertEquals(YoutubeFilters.ID_CF_MAIN_ALL, filterItems[0].getIdentifier());
        assertEquals(YoutubeFilters.ID_CF_MAIN_VIDEOS, filterItems[1].getIdentifier());
        assertEquals(YoutubeFilters.ID_CF_MAIN_CHANNELS, filterItems[2].getIdentifier());
        assertEquals(YoutubeFilters.ID_CF_MAIN_PLAYLISTS, filterItems[3].getIdentifier());
        assertTrue(filterItems[4] instanceof FilterItem.DividerItem);
        assertEquals(YoutubeFilters.ID_CF_MAIN_YOUTUBE_MUSIC_SONGS, filterItems[5].getIdentifier());
        assertEquals(YoutubeFilters.ID_CF_MAIN_YOUTUBE_MUSIC_VIDEOS, filterItems[6].getIdentifier());
        assertEquals(YoutubeFilters.ID_CF_MAIN_YOUTUBE_MUSIC_ALBUMS, filterItems[7].getIdentifier());
        assertEquals(YoutubeFilters.ID_CF_MAIN_YOUTUBE_MUSIC_PLAYLISTS, filterItems[8].getIdentifier());
        assertEquals(YoutubeFilters.ID_CF_MAIN_YOUTUBE_MUSIC_ARTISTS, filterItems[9].getIdentifier());
    }

    @Test
    public void testGetAvailableSortFilter() {
        final FilterContainer contentFilterContainer =
                YouTube.getSearchQHFactory().getAvailableContentFilter();
        final int noOfSortFilters =
                DefaultSearchExtractorTest.getNoOfSortFilterItems(contentFilterContainer);
        assertEquals(24, noOfSortFilters);
    }
}
