package org.schabi.newpipe.extractor.services.soundcloud.search;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;
import static org.schabi.newpipe.extractor.services.soundcloud.linkHandler.SoundcloudSearchQueryHandlerFactory.*;

public class SoundcloudSearchQHTest {

    @BeforeClass
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
        assertEquals("tracks", SoundCloud.getSearchQHFactory()
                .fromQuery("", asList(new String[]{"tracks"}), "").getContentFilters().get(0));
        assertEquals("users", SoundCloud.getSearchQHFactory()
                .fromQuery("asdf", asList(new String[]{"users"}), "").getContentFilters().get(0));
    }

    @Test
    public void testWithContentfilter() throws Exception {
        assertEquals("https://api-v2.soundcloud.com/search/tracks?q=asdf&limit=10&offset=0", removeClientId(SoundCloud.getSearchQHFactory()
                .fromQuery("asdf", asList(new String[]{TRACKS}), "").getUrl()));
        assertEquals("https://api-v2.soundcloud.com/search/users?q=asdf&limit=10&offset=0", removeClientId(SoundCloud.getSearchQHFactory()
                .fromQuery("asdf", asList(new String[]{USERS}), "").getUrl()));
        assertEquals("https://api-v2.soundcloud.com/search/playlists?q=asdf&limit=10&offset=0", removeClientId(SoundCloud.getSearchQHFactory()
                .fromQuery("asdf", asList(new String[]{PLAYLISTS}), "").getUrl()));
        assertEquals("https://api-v2.soundcloud.com/search?q=asdf&limit=10&offset=0", removeClientId(SoundCloud.getSearchQHFactory()
                .fromQuery("asdf", asList(new String[]{"fjiijie"}), "").getUrl()));
    }

    @Test
    public void testGetAvailableContentFilter() {
        final String[] contentFilter = SoundCloud.getSearchQHFactory().getAvailableContentFilter();
        assertEquals(4, contentFilter.length);
        assertEquals("all", contentFilter[0]);
        assertEquals("tracks", contentFilter[1]);
        assertEquals("users", contentFilter[2]);
        assertEquals("playlists", contentFilter[3]);
    }

    @Test
    public void testGetAvailableSortFilter() {
        final String[] contentFilter = SoundCloud.getSearchQHFactory().getAvailableSortFilter();
        assertEquals(0, contentFilter.length);
    }
}
