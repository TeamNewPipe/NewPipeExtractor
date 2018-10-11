package org.schabi.newpipe.extractor.services.peertube;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeTrendingLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Localization;

/**
 * Test for {@link PeertubeTrendingLinkHandlerFactory}
 */
public class PeertubeTrendingLinkHandlerFactoryTest {
    private static LinkHandlerFactory LinkHandlerFactory;

    @BeforeClass
    public static void setUp() throws Exception {
        LinkHandlerFactory = new PeertubeTrendingLinkHandlerFactory();
        NewPipe.init(Downloader.getInstance(), new Localization("GB", "en"));
    }

    @Test
    public void getUrl()
            throws Exception {
        assertEquals(LinkHandlerFactory.fromId("Trending").getUrl(), "https://peertube.mastodon.host/api/v1/videos?sort=-views");
        assertEquals(LinkHandlerFactory.fromId("Recently added").getUrl(), "https://peertube.mastodon.host/api/v1/videos?sort=-publishedAt");
        assertEquals(LinkHandlerFactory.fromId("Local").getUrl(), "https://peertube.mastodon.host/api/v1/videos?filter=local");
    }

    @Test
    public void getId()
            throws Exception {
        assertEquals(LinkHandlerFactory.fromUrl("https://peertube.mastodon.host/videos/trending").getId(), "Trending");
        assertEquals(LinkHandlerFactory.fromUrl("https://peertube.mastodon.host/videos/recently-added").getId(), "Recently added");
        assertEquals(LinkHandlerFactory.fromUrl("https://peertube.mastodon.host/videos/local").getId(), "Local");
    }

    @Test
    public void acceptUrl() throws ParsingException {
        assertTrue(LinkHandlerFactory.acceptUrl("https://peertube.mastodon.host/videos/trending"));
        assertTrue(LinkHandlerFactory.acceptUrl("https://peertube.mastodon.host/videos/trending?adsf=fjaj#fhe"));
        
        assertTrue(LinkHandlerFactory.acceptUrl("https://peertube.mastodon.host/videos/recently-added"));
        assertTrue(LinkHandlerFactory.acceptUrl("https://peertube.mastodon.host/videos/recently-added?adsf=fjaj#fhe"));
        
        assertTrue(LinkHandlerFactory.acceptUrl("https://peertube.mastodon.host/videos/local"));
        assertTrue(LinkHandlerFactory.acceptUrl("https://peertube.mastodon.host/videos/local?adsf=fjaj#fhe"));
    }
}
