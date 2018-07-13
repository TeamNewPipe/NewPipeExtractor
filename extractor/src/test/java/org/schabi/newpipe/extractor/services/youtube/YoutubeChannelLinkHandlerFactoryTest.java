package org.schabi.newpipe.extractor.services.youtube;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelLinkHandlerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link YoutubeChannelLinkHandlerFactory}
 */
public class YoutubeChannelLinkHandlerFactoryTest {

    private static YoutubeChannelLinkHandlerFactory urlIdHandler;

    @BeforeClass
    public static void setUp() {
        urlIdHandler = YoutubeChannelLinkHandlerFactory.getInstance();
        NewPipe.init(Downloader.getInstance());
    }

    @Test
    public void acceptrUrlTest() throws ParsingException {
        assertTrue(urlIdHandler.acceptUrl("https://www.youtube.com/user/Gronkh"));
        assertTrue(urlIdHandler.acceptUrl("https://www.youtube.com/user/Netzkino/videos"));

        assertTrue(urlIdHandler.acceptUrl("https://www.youtube.com/channel/UClq42foiSgl7sSpLupnugGA"));
        assertTrue(urlIdHandler.acceptUrl("https://www.youtube.com/channel/UClq42foiSgl7sSpLupnugGA/videos?disable_polymer=1"));

        assertTrue(urlIdHandler.acceptUrl("https://hooktube.com/user/Gronkh"));
        assertTrue(urlIdHandler.acceptUrl("https://hooktube.com/user/Netzkino/videos"));

        assertTrue(urlIdHandler.acceptUrl("https://hooktube.com/channel/UClq42foiSgl7sSpLupnugGA"));
        assertTrue(urlIdHandler.acceptUrl("https://hooktube.com/channel/UClq42foiSgl7sSpLupnugGA/videos?disable_polymer=1"));
    }

    @Test
    public void getIdFromUrl() throws ParsingException {
        assertEquals("user/Gronkh", urlIdHandler.fromUrl("https://www.youtube.com/user/Gronkh").getId());
        assertEquals("user/Netzkino", urlIdHandler.fromUrl("https://www.youtube.com/user/Netzkino/videos").getId());

        assertEquals("channel/UClq42foiSgl7sSpLupnugGA", urlIdHandler.fromUrl("https://www.youtube.com/channel/UClq42foiSgl7sSpLupnugGA").getId());
        assertEquals("channel/UClq42foiSgl7sSpLupnugGA", urlIdHandler.fromUrl("https://www.youtube.com/channel/UClq42foiSgl7sSpLupnugGA/videos?disable_polymer=1").getId());


        assertEquals("user/Gronkh", urlIdHandler.fromUrl("https://hooktube.com/user/Gronkh").getId());
        assertEquals("user/Netzkino", urlIdHandler.fromUrl("https://hooktube.com/user/Netzkino/videos").getId());

        assertEquals("channel/UClq42foiSgl7sSpLupnugGA", urlIdHandler.fromUrl("https://hooktube.com/channel/UClq42foiSgl7sSpLupnugGA").getId());
        assertEquals("channel/UClq42foiSgl7sSpLupnugGA", urlIdHandler.fromUrl("https://hooktube.com/channel/UClq42foiSgl7sSpLupnugGA/videos?disable_polymer=1").getId());
    }
}
