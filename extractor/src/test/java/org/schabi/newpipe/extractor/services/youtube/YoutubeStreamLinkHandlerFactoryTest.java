package org.schabi.newpipe.extractor.services.youtube;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.FoundAdException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test for {@link YoutubeStreamLinkHandlerFactory}
 */
public class YoutubeStreamLinkHandlerFactoryTest {
    private static String AD_URL = "https://googleads.g.doubleclick.net/aclk?sa=l&ai=C-2IPgeVTWPf4GcOStgfOnIOADf78n61GvKmmobYDrgIQASDj-5MDKAJg9ZXOgeAEoAGgy_T-A8gBAakC2gkpmquIsT6oAwGqBJMBT9BgD5kVgbN0dX602bFFaDw9vsxq-We-S8VkrXVBi6W_e7brZ36GCz1WO3EPEeklYuJjXLUowwCOKsd-8xr1UlS_tusuFJv9iX35xoBHKTRvs8-0aDbfEIm6in37QDfFuZjqgEMB8-tg0Jn_Pf1RU5OzbuU40B4Gy25NUTnOxhDKthOhKBUSZEksCEerUV8GMu10iAXCxquwApIFBggDEAEYAaAGGsgGlIjthrUDgAfItIsBqAemvhvYBwHSCAUIgGEQAbgT6AE&num=1&sig=AOD64_1DybDd4qAm5O7o9UAbTNRdqXXHFQ&ctype=21&video_id=dMO_IXYPZew&client=ca-pub-6219811747049371&adurl=http://www.youtube.com/watch%3Fv%3DdMO_IXYPZew";
    private static YoutubeStreamLinkHandlerFactory urlIdHandler;

    @BeforeClass
    public static void setUp() {
        urlIdHandler = YoutubeStreamLinkHandlerFactory.getInstance();
        NewPipe.init(Downloader.getInstance());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdWithNullAsUrl() throws ParsingException {
        urlIdHandler.fromId(null);
    }

    @Test(expected = FoundAdException.class)
    public void getIdForAd() throws ParsingException {
        urlIdHandler.fromUrl(AD_URL).getId();
    }

    @Test
    public void getIdForInvalidUrls() {
        List<String> invalidUrls = new ArrayList<>(50);
        invalidUrls.add("https://www.youtube.com/watch?v=jZViOEv90d");
        invalidUrls.add("https://www.youtube.com/watchjZViOEv90d");
        invalidUrls.add("https://www.youtube.com/");
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
    public void getIdfromYt() throws Exception {
        assertEquals("jZViOEv90dI", urlIdHandler.fromUrl("https://www.youtube.com/watch?v=jZViOEv90dI").getId());
        assertEquals("W-fFHeTX70Q", urlIdHandler.fromUrl("https://www.youtube.com/watch?v=W-fFHeTX70Q").getId());
        assertEquals("jZViOEv90dI", urlIdHandler.fromUrl("https://www.youtube.com/watch?v=jZViOEv90dI?t=100").getId());
        assertEquals("jZViOEv90dI", urlIdHandler.fromUrl("https://WWW.YouTube.com/watch?v=jZViOEv90dI?t=100").getId());
        assertEquals("jZViOEv90dI", urlIdHandler.fromUrl("HTTPS://www.youtube.com/watch?v=jZViOEv90dI?t=100").getId());
        assertEquals("jZViOEv90dI", urlIdHandler.fromUrl("https://youtu.be/jZViOEv90dI?t=9s").getId());
        assertEquals("jZViOEv90dI", urlIdHandler.fromUrl("HTTPS://Youtu.be/jZViOEv90dI?t=9s").getId());
        assertEquals("uEJuoEs1UxY", urlIdHandler.fromUrl("http://www.youtube.com/watch_popup?v=uEJuoEs1UxY").getId());
        assertEquals("uEJuoEs1UxY", urlIdHandler.fromUrl("http://www.Youtube.com/watch_popup?v=uEJuoEs1UxY").getId());
        assertEquals("jZViOEv90dI", urlIdHandler.fromUrl("https://www.youtube.com/embed/jZViOEv90dI").getId());
        assertEquals("jZViOEv90dI", urlIdHandler.fromUrl("https://www.youtube-nocookie.com/embed/jZViOEv90dI").getId());
        assertEquals("jZViOEv90dI", urlIdHandler.fromUrl("http://www.youtube.com/watch?v=jZViOEv90dI").getId());
        assertEquals("jZViOEv90dI", urlIdHandler.fromUrl("http://youtube.com/watch?v=jZViOEv90dI").getId());
        assertEquals("jZViOEv90dI", urlIdHandler.fromUrl("http://youtu.be/jZViOEv90dI?t=9s").getId());
        assertEquals("7_WWz2DSnT8", urlIdHandler.fromUrl("https://youtu.be/7_WWz2DSnT8").getId());
        assertEquals("oy6NvWeVruY", urlIdHandler.fromUrl("https://m.youtube.com/watch?v=oy6NvWeVruY").getId());
        assertEquals("jZViOEv90dI", urlIdHandler.fromUrl("http://www.youtube.com/embed/jZViOEv90dI").getId());
        assertEquals("jZViOEv90dI", urlIdHandler.fromUrl("http://www.Youtube.com/embed/jZViOEv90dI").getId());
        assertEquals("jZViOEv90dI", urlIdHandler.fromUrl("http://www.youtube-nocookie.com/embed/jZViOEv90dI").getId());
        assertEquals("EhxJLojIE_o", urlIdHandler.fromUrl("http://www.youtube.com/attribution_link?a=JdfC0C9V6ZI&u=%2Fwatch%3Fv%3DEhxJLojIE_o%26feature%3Dshare").getId());
        assertEquals("jZViOEv90dI", urlIdHandler.fromUrl("vnd.youtube://www.youtube.com/watch?v=jZViOEv90dI").getId());
        assertEquals("jZViOEv90dI", urlIdHandler.fromUrl("vnd.youtube:jZViOEv90dI").getId());
    }

    @Test
    public void getIdfromSharedLinksYt() throws Exception {
        String sharedId = "7JIArTByb3E";
        String realId = "Q7JsK50NGaA";
        assertEquals(realId, urlIdHandler.fromUrl("vnd.youtube://www.YouTube.com/shared?ci=" + sharedId + "&feature=twitter-deep-link").getId());
        assertEquals(realId, urlIdHandler.fromUrl("vnd.youtube://www.youtube.com/shared?ci=" + sharedId).getId());
        assertEquals(realId, urlIdHandler.fromUrl("https://www.youtube.com/shared?ci=" + sharedId).getId());
    }


    @Test
    public void testAcceptYtUrl() throws ParsingException {
        assertTrue(urlIdHandler.acceptUrl("https://www.youtube.com/watch?v=jZViOEv90dI"));
        assertTrue(urlIdHandler.acceptUrl("https://www.youtube.com/watch?v=jZViOEv90dI?t=100"));
        assertTrue(urlIdHandler.acceptUrl("https://WWW.YouTube.com/watch?v=jZViOEv90dI?t=100"));
        assertTrue(urlIdHandler.acceptUrl("HTTPS://www.youtube.com/watch?v=jZViOEv90dI?t=100"));
        assertTrue(urlIdHandler.acceptUrl("https://youtu.be/jZViOEv90dI?t=9s"));
        assertTrue(urlIdHandler.acceptUrl("https://www.youtube.com/embed/jZViOEv90dI"));
        assertTrue(urlIdHandler.acceptUrl("https://www.youtube-nocookie.com/embed/jZViOEv90dI"));
        assertTrue(urlIdHandler.acceptUrl("http://www.youtube.com/watch?v=jZViOEv90dI"));
        assertTrue(urlIdHandler.acceptUrl("http://youtu.be/jZViOEv90dI?t=9s"));
        assertTrue(urlIdHandler.acceptUrl("http://www.youtube.com/embed/jZViOEv90dI"));
        assertTrue(urlIdHandler.acceptUrl("http://www.youtube-nocookie.com/embed/jZViOEv90dI"));
        assertTrue(urlIdHandler.acceptUrl("http://www.youtube.com/attribution_link?a=JdfC0C9V6ZI&u=%2Fwatch%3Fv%3DEhxJLojIE_o%26feature%3Dshare"));
        assertTrue(urlIdHandler.acceptUrl("vnd.youtube://www.youtube.com/watch?v=jZViOEv90dI"));
        assertTrue(urlIdHandler.acceptUrl("vnd.youtube:jZViOEv90dI"));

        assertTrue(urlIdHandler.acceptUrl("vnd.youtube:jZViOEv90dI"));
    }

    @Test
    public void testAcceptSharedYtUrl() throws ParsingException {
        String sharedId = "8A940MXKFmQ";
        assertTrue(urlIdHandler.acceptUrl("vnd.youtube://www.youtube.com/shared?ci=" + sharedId + "&feature=twitter-deep-link"));
        assertTrue(urlIdHandler.acceptUrl("vnd.youtube://www.youtube.com/shared?ci=" + sharedId));
        assertTrue(urlIdHandler.acceptUrl("https://www.youtube.com/shared?ci=" + sharedId));
    }

    @Test
    public void testAcceptHookUrl() throws ParsingException {
        assertTrue(urlIdHandler.acceptUrl("https://hooktube.com/watch?v=TglNG-yjabU"));
        assertTrue(urlIdHandler.acceptUrl("hooktube.com/watch?v=3msbfr6pBNE"));
        assertTrue(urlIdHandler.acceptUrl("https://hooktube.com/watch?v=ocH3oSnZG3c&list=PLS2VU1j4vzuZwooPjV26XM9UEBY2CPNn2"));
        assertTrue(urlIdHandler.acceptUrl("hooktube.com/watch/3msbfr6pBNE"));
        assertTrue(urlIdHandler.acceptUrl("hooktube.com/v/3msbfr6pBNE"));
        assertTrue(urlIdHandler.acceptUrl("hooktube.com/embed/3msbfr6pBNE"));
    }

    @Test
    public void testGetHookIdfromUrl() throws ParsingException {
        assertEquals("TglNG-yjabU", urlIdHandler.fromUrl("https://hooktube.com/watch?v=TglNG-yjabU").getId());
        assertEquals("3msbfr6pBNE", urlIdHandler.fromUrl("hooktube.com/watch?v=3msbfr6pBNE").getId());
        assertEquals("ocH3oSnZG3c", urlIdHandler.fromUrl("https://hooktube.com/watch?v=ocH3oSnZG3c&list=PLS2VU1j4vzuZwooPjV26XM9UEBY2CPNn2").getId());
        assertEquals("3msbfr6pBNE", urlIdHandler.fromUrl("hooktube.com/watch/3msbfr6pBNE").getId());
        assertEquals("3msbfr6pBNE", urlIdHandler.fromUrl("hooktube.com/v/3msbfr6pBNE").getId());
        assertEquals("3msbfr6pBNE", urlIdHandler.fromUrl("hooktube.com/embed/3msbfr6pBNE").getId());
    }
}