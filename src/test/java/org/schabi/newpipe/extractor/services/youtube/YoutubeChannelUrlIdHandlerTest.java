package org.schabi.newpipe.extractor.services.youtube;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link YoutubeChannelUrlIdHandler}
 */
public class YoutubeChannelUrlIdHandlerTest {

    private static YoutubeChannelUrlIdHandler urlIdHandler;

    @BeforeClass
    public static void setUp() {
        urlIdHandler = YoutubeChannelUrlIdHandler.getInstance();
        NewPipe.init(Downloader.getInstance());
    }

    @Test
    public void acceptrUrlTest() {
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
        assertEquals("user/Gronkh", urlIdHandler.getId("https://www.youtube.com/user/Gronkh"));
        assertEquals("user/Netzkino", urlIdHandler.getId("https://www.youtube.com/user/Netzkino/videos"));

        assertEquals("channel/UClq42foiSgl7sSpLupnugGA", urlIdHandler.getId("https://www.youtube.com/channel/UClq42foiSgl7sSpLupnugGA"));
        assertEquals("channel/UClq42foiSgl7sSpLupnugGA", urlIdHandler.getId("https://www.youtube.com/channel/UClq42foiSgl7sSpLupnugGA/videos?disable_polymer=1"));


        assertEquals("user/Gronkh", urlIdHandler.getId("https://hooktube.com/user/Gronkh"));
        assertEquals("user/Netzkino", urlIdHandler.getId("https://hooktube.com/user/Netzkino/videos"));

        assertEquals("channel/UClq42foiSgl7sSpLupnugGA", urlIdHandler.getId("https://hooktube.com/channel/UClq42foiSgl7sSpLupnugGA"));
        assertEquals("channel/UClq42foiSgl7sSpLupnugGA", urlIdHandler.getId("https://hooktube.com/channel/UClq42foiSgl7sSpLupnugGA/videos?disable_polymer=1"));
    }
}
