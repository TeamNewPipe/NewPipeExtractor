package org.schabi.newpipe.extractor.services.youtube;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelLinkHandlerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link YoutubeChannelLinkHandlerFactory}
 */
public class YoutubeChannelLinkHandlerFactoryTest {

    private static YoutubeChannelLinkHandlerFactory linkHandler;

    @BeforeClass
    public static void setUp() {
        linkHandler = YoutubeChannelLinkHandlerFactory.getInstance();
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    public void acceptUrlTest() throws ParsingException {
        assertTrue(linkHandler.acceptUrl("https://www.youtube.com/user/Gronkh"));
        assertTrue(linkHandler.acceptUrl("https://www.youtube.com/user/Netzkino/videos"));

        assertTrue(linkHandler.acceptUrl("https://www.youtube.com/c/creatoracademy"));

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
    }

    @Test
    public void getIdFromUrl() throws ParsingException {
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
    }
}
