package org.schabi.newpipe.extractor.services.soundcloud.search;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.search.filter.FilterContainer;
import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.extractor.services.DefaultSearchExtractorTest;
import org.schabi.newpipe.extractor.services.soundcloud.search.filter.SoundcloudFilters;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;

public class SoundcloudSearchQHTest {

    @BeforeAll
    public static void setUpClass() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    private static String removeClientId(String url) {
        String[] splitUrl = url.split("client_id=[a-zA-Z0-9]*&");
        return splitUrl[0] + splitUrl[1];
    }

    @Test
    public void testRegularValues() throws Exception {
        assertEquals("https://api-v2.soundcloud.com/search?q=asdf&limit=10&offset=0",
                removeClientId(SoundCloud.getSearchQHFactory().fromQuery("asdf").getUrl()));
        assertEquals("https://api-v2.soundcloud.com/search?q=hans&limit=10&offset=0",
                removeClientId(SoundCloud.getSearchQHFactory().fromQuery("hans").getUrl()));
        assertEquals("https://api-v2.soundcloud.com/search?q=Poifj%26jaijf&limit=10&offset=0",
                removeClientId(SoundCloud.getSearchQHFactory().fromQuery("Poifj&jaijf").getUrl()));
        assertEquals("https://api-v2.soundcloud.com/search?q=G%C3%BCl%C3%BCm&limit=10&offset=0",
                removeClientId(SoundCloud.getSearchQHFactory().fromQuery("Gülüm").getUrl()));
        assertEquals("https://api-v2.soundcloud.com/search?q=%3Fj%24%29H%C2%A7B&limit=10&offset=0",
                removeClientId(SoundCloud.getSearchQHFactory().fromQuery("?j$)H§B").getUrl()));
    }

    @Test
    public void testGetContentFilter() throws Exception {
        final FilterItem trackFilterItem = DefaultSearchExtractorTest.getFilterItem(
                SoundCloud, SoundcloudFilters.ID_CF_MAIN_TRACKS);
        final FilterItem usersFilterItem = DefaultSearchExtractorTest.getFilterItem(
                SoundCloud, SoundcloudFilters.ID_CF_MAIN_USERS);

        assertEquals(SoundcloudFilters.ID_CF_MAIN_TRACKS, SoundCloud.getSearchQHFactory()
                .fromQuery("", singletonList(trackFilterItem), null)
                .getContentFilters().get(0).getIdentifier());
        assertEquals(SoundcloudFilters.ID_CF_MAIN_USERS, SoundCloud.getSearchQHFactory()
                .fromQuery("asdf", singletonList(usersFilterItem), null)
                .getContentFilters().get(0).getIdentifier());
    }

    @Test
    public void testWithContentfilter() throws Exception {
        final FilterItem trackFilterItem = DefaultSearchExtractorTest.getFilterItem(
                SoundCloud, SoundcloudFilters.ID_CF_MAIN_TRACKS);
        final FilterItem usersFilterItem = DefaultSearchExtractorTest.getFilterItem(
                SoundCloud, SoundcloudFilters.ID_CF_MAIN_USERS);
        final FilterItem playlistsFilterItem = DefaultSearchExtractorTest.getFilterItem(
                SoundCloud, SoundcloudFilters.ID_CF_MAIN_PLAYLISTS);

        assertEquals("https://api-v2.soundcloud.com/search/tracks?q=asdf&limit=10&offset=0", removeClientId(SoundCloud.getSearchQHFactory()
                .fromQuery("asdf", singletonList(trackFilterItem), null).getUrl()));
        assertEquals("https://api-v2.soundcloud.com/search/users?q=asdf&limit=10&offset=0", removeClientId(SoundCloud.getSearchQHFactory()
                .fromQuery("asdf", singletonList(usersFilterItem), null).getUrl()));
        assertEquals("https://api-v2.soundcloud.com/search/playlists?q=asdf&limit=10&offset=0", removeClientId(SoundCloud.getSearchQHFactory()
                .fromQuery("asdf", singletonList(playlistsFilterItem), null).getUrl()));
        assertEquals("https://api-v2.soundcloud.com/search?q=asdf&limit=10&offset=0", removeClientId(SoundCloud.getSearchQHFactory()
                .fromQuery("asdf", singletonList(null), null).getUrl()));
    }

    @Test
    public void testGetAvailableContentFilter() {
        final FilterContainer contentFilter =
                SoundCloud.getSearchQHFactory().getAvailableContentFilter();
        final int noOfContentFilters = DefaultSearchExtractorTest.getNoOfFilterItems(contentFilter);

        assertEquals(4, noOfContentFilters);
        assertEquals(SoundcloudFilters.ID_CF_MAIN_ALL,
                contentFilter.getFilterGroups().get(0).getFilterItems().get(0).getIdentifier());
        assertEquals(SoundcloudFilters.ID_CF_MAIN_TRACKS,
                contentFilter.getFilterGroups().get(0).getFilterItems().get(1).getIdentifier());
        assertEquals(SoundcloudFilters.ID_CF_MAIN_USERS,
                contentFilter.getFilterGroups().get(0).getFilterItems().get(2).getIdentifier());
        assertEquals(SoundcloudFilters.ID_CF_MAIN_PLAYLISTS,
                contentFilter.getFilterGroups().get(0).getFilterItems().get(3).getIdentifier());
    }

    @Test
    public void testGetAvailableSortFilter() {
        final FilterContainer contentFilterContainer =
                SoundCloud.getSearchQHFactory().getAvailableContentFilter();
        final int noOfSortFilters =
                DefaultSearchExtractorTest.getNoOfSortFilterItems(contentFilterContainer);
        assertEquals(13, noOfSortFilters);
    }
}
