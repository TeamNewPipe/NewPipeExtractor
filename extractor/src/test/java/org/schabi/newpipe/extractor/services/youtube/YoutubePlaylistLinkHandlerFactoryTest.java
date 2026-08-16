package org.schabi.newpipe.extractor.services.youtube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.schabi.newpipe.extractor.InitNewPipeTest;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubePlaylistLinkHandlerFactory;

/**
 * Test for {@link YoutubePlaylistLinkHandlerFactory}
 */
public class YoutubePlaylistLinkHandlerFactoryTest {
    private static YoutubePlaylistLinkHandlerFactory linkHandler;

    @BeforeAll
    public static void setUp() {
        InitNewPipeTest.initEmpty();
        linkHandler = YoutubePlaylistLinkHandlerFactory.getInstance();
    }

    @Test
    void getIdWithNullAsUrl() {
        assertThrows(NullPointerException.class, () -> linkHandler.fromId(null));
    }

    @ParameterizedTest
    @CsvSource({
            "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,https://www.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
            "PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV,https://www.youtube.com/playlist?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV",
            "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,https://www.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC&t=100",
            "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,https://WWW.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC&t=100",
            "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,HTTPS://www.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC&t=100",
            "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,https://www.youtube.com/watch?v=0JFM3PRZH-k&index=8&list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
            "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,http://www.youtube.com/watch?v=0JFM3PRZH-k&index=8&list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
            "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,https://m.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
            "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,https://youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
            "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,www.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
            "PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV,www.youtube.com/playlist?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV",
            "OLAK5uy_lEBUW9iTwqf0IlYPxZ8LrzpgqjAHZgZpM,https://music.youtube.com/playlist?list=OLAK5uy_lEBUW9iTwqf0IlYPxZ8LrzpgqjAHZgZpM"
    })
    void getIdfromYt(final String expectedId, final String url) throws ParsingException {
        assertEquals(expectedId, linkHandler.fromUrl(url).getId());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
            "https://www.youtube.com/playlist?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV",
            "https://WWW.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dCI",
            "HTTPS://www.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
            "https://www.youtube.com/watch?v=0JFM3PRZH-k&index=8&list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
            "http://www.youtube.com/watch?v=0JFM3PRZH-k&index=8&list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
            "https://m.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
            "https://youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
            "www.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
            "www.youtube.com/playlist?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV",
            "https://music.youtube.com/playlist?list=OLAK5uy_lEBUW9iTwqf0IlYPxZ8LrzpgqjAHZgZpM",
            "https://www.youtube.com/playlist?list=RDCLAK5uy_ly6s4irLuZAcjEDwJmqcA_UtSipMyGgbQ", // YouTube Music playlist
            "https://www.youtube.com/watch?v=2kZVEUGLgy4&list=RDdoEcQv1wlsI&index=2, " // YouTube Mix
    })
    void acceptYtUrl(final String url) throws ParsingException {
        assertTrue(linkHandler.acceptUrl(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.youtube.com/feed/trending?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV",
            "https://www.youtube.com/feed/subscriptions?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV",
            "ftp://www.youtube.com/feed/trending?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV",
            "www.youtube.com:22/feed/trending?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV",
            "youtube  .    com/feed/trending?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV",
            "?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV"
    })
    void deniesInvalidYtUrl(final String url) throws ParsingException {
        assertFalse(linkHandler.acceptUrl(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://redirect.invidious.io/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
            "https://redirect.invidious.io/playlist?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV",
            "https://invidious.nerdvpn.de/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dCI",
            "https://YEWTU.BE/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dCI",
            "HTTPS://yewtu.be/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
            "https://yewtu.be/watch?v=0JFM3PRZH-k&index=8&list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
            "http://yewtu.be/watch?v=0JFM3PRZH-k&index=8&list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
            "https://yewtu.be/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
            "yewtu.be/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
            "yewtu.be/playlist?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV"
    })
    void acceptInvidioUrl(final String url) throws ParsingException {
        assertTrue(linkHandler.acceptUrl(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://redirect.invidious.io/feed/trending?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV",
            "https://invidious.nerdvpn.de/feed/subscriptions?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV",
            "ftp:/redirect.invidious.io/feed/trending?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV",
            "redirect.invidious.io:22/feed/trending?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV",
            "invidious  .    nerdvpn.de/feed/trending?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV",
            "?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV"
    })
    void deniesInvalidInvidioUrl(final String url) throws ParsingException {
        assertFalse(linkHandler.acceptUrl(url));
    }

    @ParameterizedTest
    @CsvSource({
            "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,https://redirect.invidious.io/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
            "PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV,https://invidious.nerdvpn.de/playlist?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV",
            "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,https://redirect.invidious.io/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC&t=100",
            "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,https://invidious.nerdvpn.de/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC&t=100",
            "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,HTTPS://invidious.nerdvpn.de/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC&t=100",
            "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,https://invidious.nerdvpn.de/watch?v=0JFM3PRZH-k&index=8&list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
            "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,http://invidious.nerdvpn.de/watch?v=0JFM3PRZH-k&index=8&list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
            "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,https://invidious.nerdvpn.de/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
            "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,invidious.nerdvpn.de/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
            "PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV,invidious.nerdvpn.de/playlist?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV"
    })
    void getInvidioIdfromUrl(final String expectedId, final String url) throws ParsingException {
        assertEquals(expectedId, linkHandler.fromUrl(url).getId());
    }

    @Test
    void fromUrlIsMixVideo() throws Exception {
        final String videoId = "_AzeUSL9lZc";
        String url = "https://www.youtube.com/watch?v=" + videoId + "&list=RD" + videoId;
        assertEquals(url, linkHandler.fromUrl(url).getUrl());

        final String mixVideoId = "qHtzO49SDmk";
        url = "https://www.youtube.com/watch?v=" + mixVideoId + "&list=RD" + videoId;
        assertEquals(url, linkHandler.fromUrl(url).getUrl());
    }

    @Test
    void fromUrlIsMixPlaylist() throws Exception {
        final String videoId = "_AzeUSL9lZc";
        final String url = "https://www.youtube.com/watch?v=" + videoId + "&list=RD" + videoId;
        assertEquals(url,
            linkHandler.fromUrl("https://www.youtube.com/watch?list=RD" + videoId).getUrl());
    }
}