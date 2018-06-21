package org.schabi.newpipe.extractor.services.soundcloud.search;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.NewPipe;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;

public class SoundcloudSearchQUHTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        NewPipe.init(Downloader.getInstance());
    }

    @Test
    public void testRegularValues() throws Exception {
        assertEquals("https://api-v2.soundcloud.com/search?q=asdf&client_id=rc0HfXXgVnLSGEuQMs1F8xxuAR2AL431&limit=10&offset=0", SoundCloud.getSearchQIHFactory().setQuery("asdf").getUrl());
        assertEquals("https://api-v2.soundcloud.com/search?q=hans&client_id=rc0HfXXgVnLSGEuQMs1F8xxuAR2AL431&limit=10&offset=0",SoundCloud.getSearchQIHFactory().setQuery("hans").getUrl());
        assertEquals("https://api-v2.soundcloud.com/search?q=Poifj%26jaijf&client_id=rc0HfXXgVnLSGEuQMs1F8xxuAR2AL431&limit=10&offset=0", SoundCloud.getSearchQIHFactory().setQuery("Poifj&jaijf").getUrl());
        assertEquals("https://api-v2.soundcloud.com/search?q=G%C3%BCl%C3%BCm&client_id=rc0HfXXgVnLSGEuQMs1F8xxuAR2AL431&limit=10&offset=0", SoundCloud.getSearchQIHFactory().setQuery("Gülüm").getUrl());
        assertEquals("https://api-v2.soundcloud.com/search?q=%3Fj%24%29H%C2%A7B&client_id=rc0HfXXgVnLSGEuQMs1F8xxuAR2AL431&limit=10&offset=0", SoundCloud.getSearchQIHFactory().setQuery("?j$)H§B").getUrl());
    }

    @Test
    public void testGetContentFilter() throws Exception {
        assertEquals("tracks", SoundCloud.getSearchQIHFactory()
                .setQuery("", asList(new String[]{"tracks"}), "").getContentFilter().get(0));
        assertEquals("users", SoundCloud.getSearchQIHFactory()
                .setQuery("asdf", asList(new String[]{"users"}), "").getContentFilter().get(0));
    }

    @Test
    public void testWithContentfilter() throws Exception {
        assertEquals("https://api-v2.soundcloud.com/search/tracks?q=asdf&client_id=rc0HfXXgVnLSGEuQMs1F8xxuAR2AL431&limit=10&offset=0", SoundCloud.getSearchQIHFactory()
                .setQuery("asdf", asList(new String[]{"tracks"}), "").getUrl());
        assertEquals("https://api-v2.soundcloud.com/search/users?q=asdf&client_id=rc0HfXXgVnLSGEuQMs1F8xxuAR2AL431&limit=10&offset=0", SoundCloud.getSearchQIHFactory()
                .setQuery("asdf", asList(new String[]{"users"}), "").getUrl());
        assertEquals("https://api-v2.soundcloud.com/search/playlists?q=asdf&client_id=rc0HfXXgVnLSGEuQMs1F8xxuAR2AL431&limit=10&offset=0", SoundCloud.getSearchQIHFactory()
                .setQuery("asdf", asList(new String[]{"playlist"}), "").getUrl());
        assertEquals("https://api-v2.soundcloud.com/search?q=asdf&client_id=rc0HfXXgVnLSGEuQMs1F8xxuAR2AL431&limit=10&offset=0", SoundCloud.getSearchQIHFactory()
                .setQuery("asdf", asList(new String[]{"fjiijie"}), "").getUrl());
    }

    @Test
    public void testGetAvailableContentFilter() {
        final String[] contentFilter = SoundCloud.getSearchQIHFactory().getAvailableContentFilter();
        assertEquals(4, contentFilter.length);
        assertEquals("tracks", contentFilter[0]);
        assertEquals("users", contentFilter[1]);
        assertEquals("playlist", contentFilter[2]);
        assertEquals("any", contentFilter[3]);
    }

    @Test
    public void testGetAvailableSortFilter() {
        final String[] contentFilter = SoundCloud.getSearchQIHFactory().getAvailableSortFilter();
        assertEquals(0, contentFilter.length);
    }
}
