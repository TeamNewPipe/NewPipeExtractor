package org.schabi.newpipe.extractor.services.peertube.search;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.extractor.services.peertube.PeertubeInstance;

import static org.junit.Assert.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;

public class PeertubeSearchQHTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        // setting instance might break test when running in parallel
        PeerTube.setInstance(new PeertubeInstance("https://peertube.mastodon.host", "PeerTube on Mastodon.host"));
    }

    @Test
    public void testRegularValues() throws Exception {
        assertEquals("https://peertube.mastodon.host/api/v1/search/videos?search=asdf", PeerTube.getSearchQHFactory().fromQuery("asdf").getUrl());
        assertEquals("https://peertube.mastodon.host/api/v1/search/videos?search=hans", PeerTube.getSearchQHFactory().fromQuery("hans").getUrl());
        assertEquals("https://peertube.mastodon.host/api/v1/search/videos?search=Poifj%26jaijf", PeerTube.getSearchQHFactory().fromQuery("Poifj&jaijf").getUrl());
        assertEquals("https://peertube.mastodon.host/api/v1/search/videos?search=G%C3%BCl%C3%BCm", PeerTube.getSearchQHFactory().fromQuery("Gülüm").getUrl());
        assertEquals("https://peertube.mastodon.host/api/v1/search/videos?search=%3Fj%24%29H%C2%A7B", PeerTube.getSearchQHFactory().fromQuery("?j$)H§B").getUrl());
    }
}