package org.schabi.newpipe.extractor.services.youtube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.youtube.youtube.linkHandler.YoutubePlaylistLinkHandlerFactory;

/**
 * Test for {@link YoutubePlaylistLinkHandlerFactory}
 */
public class YoutubePlaylistLinkHandlerFactoryTest {
    private static YoutubePlaylistLinkHandlerFactory linkHandler;

    @BeforeAll
    public static void setUp() {
        NewPipe.init(DownloaderTestImpl.getInstance());
        linkHandler = YoutubePlaylistLinkHandlerFactory.getInstance();
    }

    @Test
    public void getIdWithNullAsUrl() {
        assertThrows(IllegalArgumentException.class, () -> linkHandler.fromId(null));
    }

    @Test
    public void getIdfromYt() throws Exception {
        assertEquals("PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC", linkHandler.fromUrl("https://www.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC").getId());
        assertEquals("PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV", linkHandler.fromUrl("https://www.youtube.com/playlist?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV").getId());
        assertEquals("PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC", linkHandler.fromUrl("https://www.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC&t=100").getId());
        assertEquals("PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC", linkHandler.fromUrl("https://WWW.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC&t=100").getId());
        assertEquals("PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC", linkHandler.fromUrl("HTTPS://www.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC&t=100").getId());
        assertEquals("PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC", linkHandler.fromUrl("https://www.youtube.com/watch?v=0JFM3PRZH-k&index=8&list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC").getId());
        assertEquals("PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC", linkHandler.fromUrl("http://www.youtube.com/watch?v=0JFM3PRZH-k&index=8&list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC").getId());
        assertEquals("PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC", linkHandler.fromUrl("https://m.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC").getId());
        assertEquals("PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC", linkHandler.fromUrl("https://youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC").getId());
        assertEquals("PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC", linkHandler.fromUrl("www.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC").getId());
        assertEquals("PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV", linkHandler.fromUrl("www.youtube.com/playlist?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV").getId());
        assertEquals("OLAK5uy_lEBUW9iTwqf0IlYPxZ8LrzpgqjAHZgZpM", linkHandler.fromUrl("https://music.youtube.com/playlist?list=OLAK5uy_lEBUW9iTwqf0IlYPxZ8LrzpgqjAHZgZpM").getId());
    }

    @Test
    public void testAcceptYtUrl() throws ParsingException {
        assertTrue(linkHandler.acceptUrl("https://www.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC"));
        assertTrue(linkHandler.acceptUrl("https://www.youtube.com/playlist?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV"));
        assertTrue(linkHandler.acceptUrl("https://WWW.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dCI"));
        assertTrue(linkHandler.acceptUrl("HTTPS://www.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC"));
        assertTrue(linkHandler.acceptUrl("https://www.youtube.com/watch?v=0JFM3PRZH-k&index=8&list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC"));
        assertTrue(linkHandler.acceptUrl("http://www.youtube.com/watch?v=0JFM3PRZH-k&index=8&list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC"));
        assertTrue(linkHandler.acceptUrl("https://m.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC"));
        assertTrue(linkHandler.acceptUrl("https://youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC"));
        assertTrue(linkHandler.acceptUrl("www.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC"));
        assertTrue(linkHandler.acceptUrl("www.youtube.com/playlist?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV"));
        assertTrue(linkHandler.acceptUrl("https://music.youtube.com/playlist?list=OLAK5uy_lEBUW9iTwqf0IlYPxZ8LrzpgqjAHZgZpM"));
        assertTrue(linkHandler.acceptUrl("https://www.youtube.com/playlist?list=RDCLAK5uy_ly6s4irLuZAcjEDwJmqcA_UtSipMyGgbQ")); // YouTube Music playlist
        assertTrue(linkHandler.acceptUrl("https://www.youtube.com/watch?v=2kZVEUGLgy4&list=RDdoEcQv1wlsI&index=2, ")); // YouTube Mix
    }

    @Test
    public void testDeniesInvalidYtUrl() throws ParsingException {
        assertFalse(linkHandler.acceptUrl("https://www.youtube.com/feed/trending?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV"));
        assertFalse(linkHandler.acceptUrl("https://www.youtube.com/feed/subscriptions?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV"));
        assertFalse(linkHandler.acceptUrl("ftp://www.youtube.com/feed/trending?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV"));
        assertFalse(linkHandler.acceptUrl("www.youtube.com:22/feed/trending?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV"));
        assertFalse(linkHandler.acceptUrl("youtube  .    com/feed/trending?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV"));
        assertFalse(linkHandler.acceptUrl("?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV"));
    }

    @Test
    public void testAcceptInvidioUrl() throws ParsingException {
        assertTrue(linkHandler.acceptUrl("https://www.invidio.us/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC"));
        assertTrue(linkHandler.acceptUrl("https://www.invidio.us/playlist?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV"));
        assertTrue(linkHandler.acceptUrl("https://WWW.invidio.us/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dCI"));
        assertTrue(linkHandler.acceptUrl("HTTPS://www.invidio.us/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC"));
        assertTrue(linkHandler.acceptUrl("https://www.invidio.us/watch?v=0JFM3PRZH-k&index=8&list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC"));
        assertTrue(linkHandler.acceptUrl("http://www.invidio.us/watch?v=0JFM3PRZH-k&index=8&list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC"));
        assertTrue(linkHandler.acceptUrl("https://invidio.us/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC"));
        assertTrue(linkHandler.acceptUrl("www.invidio.us/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC"));
        assertTrue(linkHandler.acceptUrl("www.invidio.us/playlist?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV"));
    }

    @Test
    public void testDeniesInvalidInvidioUrl() throws ParsingException {
        assertFalse(linkHandler.acceptUrl("https://invidio.us/feed/trending?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV"));
        assertFalse(linkHandler.acceptUrl("https://invidio.us/feed/subscriptions?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV"));
        assertFalse(linkHandler.acceptUrl("ftp:/invidio.us/feed/trending?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV"));
        assertFalse(linkHandler.acceptUrl("invidio.us:22/feed/trending?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV"));
        assertFalse(linkHandler.acceptUrl("invidio  .    us/feed/trending?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV"));
        assertFalse(linkHandler.acceptUrl("?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV"));
    }

    @Test
    public void testGetInvidioIdfromUrl() throws ParsingException {
        assertEquals("PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC", linkHandler.fromUrl("https://www.invidio.us/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC").getId());
        assertEquals("PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV", linkHandler.fromUrl("https://www.invidio.us/playlist?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV").getId());
        assertEquals("PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC", linkHandler.fromUrl("https://www.invidio.us/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC&t=100").getId());
        assertEquals("PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC", linkHandler.fromUrl("https://WWW.invidio.us/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC&t=100").getId());
        assertEquals("PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC", linkHandler.fromUrl("HTTPS://www.invidio.us/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC&t=100").getId());
        assertEquals("PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC", linkHandler.fromUrl("https://www.invidio.us/watch?v=0JFM3PRZH-k&index=8&list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC").getId());
        assertEquals("PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC", linkHandler.fromUrl("http://www.invidio.us/watch?v=0JFM3PRZH-k&index=8&list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC").getId());
        assertEquals("PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC", linkHandler.fromUrl("https://invidio.us/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC").getId());
        assertEquals("PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC", linkHandler.fromUrl("www.invidio.us/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC").getId());
        assertEquals("PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV", linkHandler.fromUrl("www.invidio.us/playlist?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV").getId());
    }

    @Test
    public void fromUrlIsMixVideo() throws Exception {
        final String videoId = "_AzeUSL9lZc";
        String url = "https://www.youtube.com/watch?v=" + videoId + "&list=RD" + videoId;
        assertEquals(url, linkHandler.fromUrl(url).getUrl());

        final String mixVideoId = "qHtzO49SDmk";
        url = "https://www.youtube.com/watch?v=" + mixVideoId + "&list=RD" + videoId;
        assertEquals(url, linkHandler.fromUrl(url).getUrl());
    }

    @Test
    public void fromUrlIsMixPlaylist() throws Exception {
        final String videoId = "_AzeUSL9lZc";
        final String url = "https://www.youtube.com/watch?v=" + videoId + "&list=RD" + videoId;
        assertEquals(url,
            linkHandler.fromUrl("https://www.youtube.com/watch?list=RD" + videoId).getUrl());
    }
}