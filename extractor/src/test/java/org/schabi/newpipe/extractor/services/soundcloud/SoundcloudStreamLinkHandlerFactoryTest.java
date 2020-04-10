package org.schabi.newpipe.extractor.services.soundcloud;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.soundcloud.linkHandler.SoundcloudStreamLinkHandlerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test for {@link SoundcloudStreamLinkHandlerFactory}
 */
public class SoundcloudStreamLinkHandlerFactoryTest {
    private static SoundcloudStreamLinkHandlerFactory linkHandler;

    @BeforeClass
    public static void setUp() throws Exception {
        linkHandler = SoundcloudStreamLinkHandlerFactory.getInstance();
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdWithNullAsUrl() throws ParsingException {
        linkHandler.fromUrl(null).getId();
    }

    @Test
    public void getIdForInvalidUrls() {
        List<String> invalidUrls = new ArrayList<>(50);
        invalidUrls.add("https://soundcloud.com/liluzivert/t.e.s.t");
        invalidUrls.add("https://soundcloud.com/liluzivert/tracks");
        invalidUrls.add("https://soundcloud.com/");
        for (String invalidUrl : invalidUrls) {
            Throwable exception = null;
            try {
                linkHandler.fromUrl(invalidUrl).getId();
            } catch (ParsingException e) {
                exception = e;
            }
            if (exception == null) {
                fail("Expected ParsingException for url: " + invalidUrl);
            }
        }
    }

    @Test
    public void getId() throws Exception {
        assertEquals("309689103", linkHandler.fromUrl("https://soundcloud.com/liluzivert/15-ysl").getId());
        assertEquals("309689082", linkHandler.fromUrl("https://www.soundcloud.com/liluzivert/15-luv-scars-ko").getId());
        assertEquals("309689035", linkHandler.fromUrl("http://soundcloud.com/liluzivert/15-boring-shit").getId());
        assertEquals("294488599", linkHandler.fromUrl("http://www.soundcloud.com/liluzivert/secure-the-bag-produced-by-glohan-beats").getId());
        assertEquals("294488438", linkHandler.fromUrl("HtTpS://sOuNdClOuD.cOm/LiLuZiVeRt/In-O4-pRoDuCeD-bY-dP-bEaTz").getId());
        assertEquals("294488147", linkHandler.fromUrl("https://soundcloud.com/liluzivert/fresh-produced-by-zaytoven#t=69").getId());
        assertEquals("294487876", linkHandler.fromUrl("https://soundcloud.com/liluzivert/threesome-produced-by-zaytoven#t=1:09").getId());
        assertEquals("294487684", linkHandler.fromUrl("https://soundcloud.com/liluzivert/blonde-brigitte-produced-manny-fresh#t=1:9").getId());
        assertEquals("294487428", linkHandler.fromUrl("https://soundcloud.com/liluzivert/today-produced-by-c-note#t=1m9s").getId());
        assertEquals("294487157", linkHandler.fromUrl("https://soundcloud.com/liluzivert/changed-my-phone-produced-by-c-note#t=1m09s").getId());
    }


    @Test
    public void testAcceptUrl() throws ParsingException {
        assertTrue(linkHandler.acceptUrl("https://soundcloud.com/liluzivert/15-ysl"));
        assertTrue(linkHandler.acceptUrl("https://www.soundcloud.com/liluzivert/15-luv-scars-ko"));
        assertTrue(linkHandler.acceptUrl("http://soundcloud.com/liluzivert/15-boring-shit"));
        assertTrue(linkHandler.acceptUrl("http://www.soundcloud.com/liluzivert/secure-the-bag-produced-by-glohan-beats"));
        assertTrue(linkHandler.acceptUrl("HtTpS://sOuNdClOuD.cOm/LiLuZiVeRt/In-O4-pRoDuCeD-bY-dP-bEaTz"));
        assertTrue(linkHandler.acceptUrl("https://soundcloud.com/liluzivert/fresh-produced-by-zaytoven#t=69"));
        assertTrue(linkHandler.acceptUrl("https://soundcloud.com/liluzivert/threesome-produced-by-zaytoven#t=1:09"));
        assertTrue(linkHandler.acceptUrl("https://soundcloud.com/liluzivert/blonde-brigitte-produced-manny-fresh#t=1:9"));
        assertTrue(linkHandler.acceptUrl("https://soundcloud.com/liluzivert/today-produced-by-c-note#t=1m9s"));
        assertTrue(linkHandler.acceptUrl("https://soundcloud.com/liluzivert/changed-my-phone-produced-by-c-note#t=1m09s"));
    }
}