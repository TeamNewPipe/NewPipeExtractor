package org.schabi.newpipe.extractor.services.peertube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.InitNewPipeTest;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeTrendingLinkHandlerFactory;

/**
 * Test for {@link PeertubeTrendingLinkHandlerFactory}
 */
public class PeertubeTrendingLinkHandlerFactoryTest {
    private static LinkHandlerFactory LinkHandlerFactory;

    @BeforeAll
    public static void setUp() throws Exception {
        // setting instance might break test when running in parallel
        InitNewPipeTest.initEmpty();
        PeerTube.setInstance(new PeertubeInstance("https://peertube.mastodon.host", "PeerTube on Mastodon.host"));
        LinkHandlerFactory = PeertubeTrendingLinkHandlerFactory.getInstance();
    }

    @Test
    public void getUrl()
            throws Exception {
        assertEquals("https://peertube.mastodon.host/api/v1/videos?sort=-trending", LinkHandlerFactory.fromId("Trending").getUrl());
        assertEquals("https://peertube.mastodon.host/api/v1/videos?sort=-likes", LinkHandlerFactory.fromId("Most liked").getUrl());
        assertEquals("https://peertube.mastodon.host/api/v1/videos?sort=-publishedAt", LinkHandlerFactory.fromId("Recently added").getUrl());
        assertEquals("https://peertube.mastodon.host/api/v1/videos?sort=-publishedAt&isLocal=true", LinkHandlerFactory.fromId("Local").getUrl());
    }

    @Test
    public void getId()
            throws Exception {
        assertEquals("Trending", LinkHandlerFactory.fromUrl("https://peertube.mastodon.host/videos/trending").getId());
        assertEquals("Most liked", LinkHandlerFactory.fromUrl("https://peertube.mastodon.host/videos/most-liked").getId());
        assertEquals("Recently added", LinkHandlerFactory.fromUrl("https://peertube.mastodon.host/videos/recently-added").getId());
        assertEquals("Local", LinkHandlerFactory.fromUrl("https://peertube.mastodon.host/videos/local").getId());
    }

    @Test
    public void acceptUrl() throws ParsingException {
        assertTrue(LinkHandlerFactory.acceptUrl("https://peertube.mastodon.host/videos/trending"));
        assertTrue(LinkHandlerFactory.acceptUrl("https://peertube.mastodon.host/videos/trending?adsf=fjaj#fhe"));

        assertTrue(LinkHandlerFactory.acceptUrl("https://peertube.mastodon.host/videos/most-liked"));
        assertTrue(LinkHandlerFactory.acceptUrl("https://peertube.mastodon.host/videos/most-liked?adsf=fjaj#fhe"));

        assertTrue(LinkHandlerFactory.acceptUrl("https://peertube.mastodon.host/videos/recently-added"));
        assertTrue(LinkHandlerFactory.acceptUrl("https://peertube.mastodon.host/videos/recently-added?adsf=fjaj#fhe"));

        assertTrue(LinkHandlerFactory.acceptUrl("https://peertube.mastodon.host/videos/local"));
        assertTrue(LinkHandlerFactory.acceptUrl("https://peertube.mastodon.host/videos/local?adsf=fjaj#fhe"));

        PeertubeLinkHandlerFactoryTestHelper.assertDoNotAcceptNonURLs(LinkHandlerFactory);
    }
}
