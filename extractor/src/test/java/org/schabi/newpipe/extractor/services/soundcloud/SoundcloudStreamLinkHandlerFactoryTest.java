package org.schabi.newpipe.extractor.services.soundcloud;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test for {@link SoundcloudStreamLinkHandlerFactory}
 */
public class SoundcloudStreamLinkHandlerFactoryTest {
    private static SoundcloudStreamLinkHandlerFactory urlIdHandler;

    @BeforeClass
    public static void setUp() throws Exception {
        urlIdHandler = SoundcloudStreamLinkHandlerFactory.getInstance();
        NewPipe.init(Downloader.getInstance());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdWithNullAsUrl() throws ParsingException {
        urlIdHandler.fromUrl(null).getId();
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
                urlIdHandler.fromUrl(invalidUrl).getId();
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
        assertEquals("309689103", urlIdHandler.fromUrl("https://soundcloud.com/liluzivert/15-ysl").getId());
        assertEquals("309689082", urlIdHandler.fromUrl("https://www.soundcloud.com/liluzivert/15-luv-scars-ko").getId());
        assertEquals("309689035", urlIdHandler.fromUrl("http://soundcloud.com/liluzivert/15-boring-shit").getId());
        assertEquals("294488599", urlIdHandler.fromUrl("http://www.soundcloud.com/liluzivert/secure-the-bag-produced-by-glohan-beats").getId());
        assertEquals("294488438", urlIdHandler.fromUrl("HtTpS://sOuNdClOuD.cOm/LiLuZiVeRt/In-O4-pRoDuCeD-bY-dP-bEaTz").getId());
        assertEquals("294488147", urlIdHandler.fromUrl("https://soundcloud.com/liluzivert/fresh-produced-by-zaytoven#t=69").getId());
        assertEquals("294487876", urlIdHandler.fromUrl("https://soundcloud.com/liluzivert/threesome-produced-by-zaytoven#t=1:09").getId());
        assertEquals("294487684", urlIdHandler.fromUrl("https://soundcloud.com/liluzivert/blonde-brigitte-produced-manny-fresh#t=1:9").getId());
        assertEquals("294487428", urlIdHandler.fromUrl("https://soundcloud.com/liluzivert/today-produced-by-c-note#t=1m9s").getId());
        assertEquals("294487157", urlIdHandler.fromUrl("https://soundcloud.com/liluzivert/changed-my-phone-produced-by-c-note#t=1m09s").getId());
    }


    @Test
    public void testAcceptUrl() throws ParsingException {
        assertTrue(urlIdHandler.acceptUrl("https://soundcloud.com/liluzivert/15-ysl"));
        assertTrue(urlIdHandler.acceptUrl("https://www.soundcloud.com/liluzivert/15-luv-scars-ko"));
        assertTrue(urlIdHandler.acceptUrl("http://soundcloud.com/liluzivert/15-boring-shit"));
        assertTrue(urlIdHandler.acceptUrl("http://www.soundcloud.com/liluzivert/secure-the-bag-produced-by-glohan-beats"));
        assertTrue(urlIdHandler.acceptUrl("HtTpS://sOuNdClOuD.cOm/LiLuZiVeRt/In-O4-pRoDuCeD-bY-dP-bEaTz"));
        assertTrue(urlIdHandler.acceptUrl("https://soundcloud.com/liluzivert/fresh-produced-by-zaytoven#t=69"));
        assertTrue(urlIdHandler.acceptUrl("https://soundcloud.com/liluzivert/threesome-produced-by-zaytoven#t=1:09"));
        assertTrue(urlIdHandler.acceptUrl("https://soundcloud.com/liluzivert/blonde-brigitte-produced-manny-fresh#t=1:9"));
        assertTrue(urlIdHandler.acceptUrl("https://soundcloud.com/liluzivert/today-produced-by-c-note#t=1m9s"));
        assertTrue(urlIdHandler.acceptUrl("https://soundcloud.com/liluzivert/changed-my-phone-produced-by-c-note#t=1m09s"));
    }
}