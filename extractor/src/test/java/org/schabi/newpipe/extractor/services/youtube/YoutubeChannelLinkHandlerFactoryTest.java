package org.schabi.newpipe.extractor.services.youtube;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelLinkHandlerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for {@link YoutubeChannelLinkHandlerFactory}
 */
public class YoutubeChannelLinkHandlerFactoryTest {

    private static YoutubeChannelLinkHandlerFactory linkHandler;

    @BeforeAll
    public static void setUp() {
        linkHandler = YoutubeChannelLinkHandlerFactory.getInstance();
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    void acceptUrlTest() throws ParsingException {
        assertTrue(linkHandler.acceptUrl("https://www.youtube.com/user/Gronkh"));
        assertTrue(linkHandler.acceptUrl("https://www.youtube.com/user/Netzkino/videos"));

        assertTrue(linkHandler.acceptUrl("https://www.youtube.com/c/creatoracademy"));
        assertTrue(linkHandler.acceptUrl("https://www.youtube.com/c/%EB%85%B8%EB%A7%88%EB%93%9C%EC%BD%94%EB%8D%94NomadCoders"));

        assertTrue(linkHandler.acceptUrl("https://youtube.com/DIMENSI0N"));

        assertTrue(linkHandler.acceptUrl("https://www.youtube.com/channel/UClq42foiSgl7sSpLupnugGA"));
        assertTrue(linkHandler.acceptUrl("https://www.youtube.com/channel/UClq42foiSgl7sSpLupnugGA/videos?disable_polymer=1"));

        assertTrue(linkHandler.acceptUrl("https://hooktube.com/user/Gronkh"));
        assertTrue(linkHandler.acceptUrl("https://hooktube.com/user/Netzkino/videos"));

        assertTrue(linkHandler.acceptUrl("https://hooktube.com/channel/UClq42foiSgl7sSpLupnugGA"));
        assertTrue(linkHandler.acceptUrl("https://hooktube.com/channel/UClq42foiSgl7sSpLupnugGA/videos?disable_polymer=1"));

        assertTrue(linkHandler.acceptUrl("https://invidio.us/user/Gronkh"));
        assertTrue(linkHandler.acceptUrl("https://invidio.us/user/Netzkino/videos"));

        assertTrue(linkHandler.acceptUrl("https://invidio.us/channel/UClq42foiSgl7sSpLupnugGA"));
        assertTrue(linkHandler.acceptUrl("https://invidio.us/channel/UClq42foiSgl7sSpLupnugGA/videos?disable_polymer=1"));
        assertTrue(linkHandler.acceptUrl("https://www.youtube.com/watchismo"));

        assertTrue(linkHandler.acceptUrl("https://www.youtube.com/@YouTube"));

        // do not accept URLs which are not channels
        assertFalse(linkHandler.acceptUrl("https://www.youtube.com/watch?v=jZViOEv90dI&t=100"));
        assertFalse(linkHandler.acceptUrl("http://www.youtube.com/watch_popup?v=uEJuoEs1UxY"));
        assertFalse(linkHandler.acceptUrl("http://www.youtube.com/attribution_link?a=JdfC0C9V6ZI&u=%2Fwatch%3Fv%3DEhxJLojIE_o%26feature%3Dshare"));
        assertFalse(linkHandler.acceptUrl("https://www.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1d"));
        assertFalse(linkHandler.acceptUrl("https://www.youtube.com/embed/jZViOEv90dI"));
        assertFalse(linkHandler.acceptUrl("https://www.youtube.com/feed/subscriptions?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV"));
        assertFalse(linkHandler.acceptUrl("https://www.youtube.com/?app=desktop&persist_app=1"));
        assertFalse(linkHandler.acceptUrl("https://m.youtube.com/select_site"));
    }

    @Test
    void getIdFromUrl() throws ParsingException {
        assertEquals("user/Gronkh", linkHandler.fromUrl("https://www.youtube.com/user/Gronkh").getId());
        assertEquals("user/Netzkino", linkHandler.fromUrl("https://www.youtube.com/user/Netzkino/videos").getId());

        assertEquals("channel/UClq42foiSgl7sSpLupnugGA", linkHandler.fromUrl("https://www.youtube.com/channel/UClq42foiSgl7sSpLupnugGA").getId());
        assertEquals("channel/UClq42foiSgl7sSpLupnugGA", linkHandler.fromUrl("https://www.youtube.com/channel/UClq42foiSgl7sSpLupnugGA/videos?disable_polymer=1").getId());

        assertEquals("user/Gronkh", linkHandler.fromUrl("https://hooktube.com/user/Gronkh").getId());
        assertEquals("user/Netzkino", linkHandler.fromUrl("https://hooktube.com/user/Netzkino/videos").getId());

        assertEquals("channel/UClq42foiSgl7sSpLupnugGA", linkHandler.fromUrl("https://hooktube.com/channel/UClq42foiSgl7sSpLupnugGA").getId());
        assertEquals("channel/UClq42foiSgl7sSpLupnugGA", linkHandler.fromUrl("https://hooktube.com/channel/UClq42foiSgl7sSpLupnugGA/videos?disable_polymer=1").getId());

        assertEquals("user/Gronkh", linkHandler.fromUrl("https://invidio.us/user/Gronkh").getId());
        assertEquals("user/Netzkino", linkHandler.fromUrl("https://invidio.us/user/Netzkino/videos").getId());

        assertEquals("channel/UClq42foiSgl7sSpLupnugGA", linkHandler.fromUrl("https://invidio.us/channel/UClq42foiSgl7sSpLupnugGA").getId());
        assertEquals("channel/UClq42foiSgl7sSpLupnugGA", linkHandler.fromUrl("https://invidio.us/channel/UClq42foiSgl7sSpLupnugGA/videos?disable_polymer=1").getId());

        assertEquals("c/creatoracademy", linkHandler.fromUrl("https://www.youtube.com/c/creatoracademy").getId());
        assertEquals("c/YouTubeCreators", linkHandler.fromUrl("https://www.youtube.com/c/YouTubeCreators").getId());
        assertEquals("c/%EB%85%B8%EB%A7%88%EB%93%9C%EC%BD%94%EB%8D%94NomadCoders", linkHandler.fromUrl("https://www.youtube.com/c/%EB%85%B8%EB%A7%88%EB%93%9C%EC%BD%94%EB%8D%94NomadCoders").getId());

        assertEquals("@Gronkh", linkHandler.fromUrl("https://www.youtube.com/@Gronkh?ucbcb=1").getId());
        assertEquals("@YouTubeCreators", linkHandler.fromUrl("https://www.youtube.com/@YouTubeCreators/shorts").getId());
    }
}
