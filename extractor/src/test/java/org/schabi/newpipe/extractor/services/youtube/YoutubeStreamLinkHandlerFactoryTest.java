package org.schabi.newpipe.extractor.services.youtube;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.FoundAdException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for {@link YoutubeStreamLinkHandlerFactory}
 */
public class YoutubeStreamLinkHandlerFactoryTest {
    private static String AD_URL = "https://googleads.g.doubleclick.net/aclk?sa=l&ai=C-2IPgeVTWPf4GcOStgfOnIOADf78n61GvKmmobYDrgIQASDj-5MDKAJg9ZXOgeAEoAGgy_T-A8gBAakC2gkpmquIsT6oAwGqBJMBT9BgD5kVgbN0dX602bFFaDw9vsxq-We-S8VkrXVBi6W_e7brZ36GCz1WO3EPEeklYuJjXLUowwCOKsd-8xr1UlS_tusuFJv9iX35xoBHKTRvs8-0aDbfEIm6in37QDfFuZjqgEMB8-tg0Jn_Pf1RU5OzbuU40B4Gy25NUTnOxhDKthOhKBUSZEksCEerUV8GMu10iAXCxquwApIFBggDEAEYAaAGGsgGlIjthrUDgAfItIsBqAemvhvYBwHSCAUIgGEQAbgT6AE&num=1&sig=AOD64_1DybDd4qAm5O7o9UAbTNRdqXXHFQ&ctype=21&video_id=dMO_IXYPZew&client=ca-pub-6219811747049371&adurl=http://www.youtube.com/watch%3Fv%3DdMO_IXYPZew";
    private static YoutubeStreamLinkHandlerFactory linkHandler;

    @BeforeAll
    public static void setUp() {
        linkHandler = YoutubeStreamLinkHandlerFactory.getInstance();
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    public void getIdWithNullAsUrl() {
        assertThrows(IllegalArgumentException.class, () -> linkHandler.fromId(null));
    }

    @Test
    public void getIdForAd() throws ParsingException {
        assertThrows(FoundAdException.class, () -> linkHandler.fromUrl(AD_URL));
    }

    @Test
    public void getIdForInvalidUrls() {
        List<String> invalidUrls = new ArrayList<>(50);
        invalidUrls.add("https://www.youtube.com/watch?v=jZViOEv90d");
        invalidUrls.add("https://www.youtube.com/watchjZViOEv90d");
        invalidUrls.add("https://www.youtube.com/");
        invalidUrls.add("https://www.youtube.com/channel/UCBR8-60-B28hp2BmDPdntcQ");
        invalidUrls.add("https://invidio.us/channel/UCBR8-60-B28hp2BmDPdntcQ");
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
    public void getIdfromYt() throws Exception {
        assertEquals("jZViOEv90dI", linkHandler.fromUrl("https://www.youtube.com/watch?v=jZViOEv90dI").getId());
        assertEquals("W-fFHeTX70Q", linkHandler.fromUrl("https://www.youtube.com/watch?v=W-fFHeTX70Q").getId());
        assertEquals("jZViOEv90dI", linkHandler.fromUrl("https://www.youtube.com/watch?v=jZViOEv90dI&t=100").getId());
        assertEquals("jZViOEv90dI", linkHandler.fromUrl("https://WWW.YouTube.com/watch?v=jZViOEv90dI&t=100").getId());
        assertEquals("jZViOEv90dI", linkHandler.fromUrl("HTTPS://www.youtube.com/watch?v=jZViOEv90dI&t=100").getId());
        assertEquals("jZViOEv90dI", linkHandler.fromUrl("https://youtu.be/jZViOEv90dI?t=9s").getId());
        assertEquals("jZViOEv90dI", linkHandler.fromUrl("HTTPS://Youtu.be/jZViOEv90dI?t=9s").getId());
        assertEquals("uEJuoEs1UxY", linkHandler.fromUrl("http://www.youtube.com/watch_popup?v=uEJuoEs1UxY").getId());
        assertEquals("uEJuoEs1UxY", linkHandler.fromUrl("http://www.Youtube.com/watch_popup?v=uEJuoEs1UxY").getId());
        assertEquals("jZViOEv90dI", linkHandler.fromUrl("https://www.youtube.com/embed/jZViOEv90dI").getId());
        assertEquals("jZViOEv90dI", linkHandler.fromUrl("https://www.youtube-nocookie.com/embed/jZViOEv90dI").getId());
        assertEquals("jZViOEv90dI", linkHandler.fromUrl("http://www.youtube.com/watch?v=jZViOEv90dI").getId());
        assertEquals("jZViOEv90dI", linkHandler.fromUrl("http://youtube.com/watch?v=jZViOEv90dI").getId());
        assertEquals("jZViOEv90dI", linkHandler.fromUrl("http://youtu.be/jZViOEv90dI?t=9s").getId());
        assertEquals("7_WWz2DSnT8", linkHandler.fromUrl("https://youtu.be/7_WWz2DSnT8").getId());
        assertEquals("oy6NvWeVruY", linkHandler.fromUrl("https://m.youtube.com/watch?v=oy6NvWeVruY").getId());
        assertEquals("jZViOEv90dI", linkHandler.fromUrl("http://www.youtube.com/embed/jZViOEv90dI").getId());
        assertEquals("jZViOEv90dI", linkHandler.fromUrl("http://www.Youtube.com/embed/jZViOEv90dI").getId());
        assertEquals("jZViOEv90dI", linkHandler.fromUrl("http://www.youtube-nocookie.com/embed/jZViOEv90dI").getId());
        assertEquals("EhxJLojIE_o", linkHandler.fromUrl("http://www.youtube.com/attribution_link?a=JdfC0C9V6ZI&u=%2Fwatch%3Fv%3DEhxJLojIE_o%26feature%3Dshare").getId());
        assertEquals("jZViOEv90dI", linkHandler.fromUrl("vnd.youtube://www.youtube.com/watch?v=jZViOEv90dI").getId());
        assertEquals("jZViOEv90dI", linkHandler.fromUrl("vnd.youtube:jZViOEv90dI").getId());
        assertEquals("n8X9_MgEdCg", linkHandler.fromUrl("vnd.youtube://n8X9_MgEdCg").getId());
        assertEquals("O0EDx9WAelc", linkHandler.fromUrl("https://music.youtube.com/watch?v=O0EDx9WAelc").getId());
        assertEquals("-cdveCh1kQk", linkHandler.fromUrl("https://m.youtube.com/watch?v=-cdveCh1kQk)").getId());
        assertEquals("-cdveCh1kQk", linkHandler.fromUrl("https://www.youtube.com/watch?v=-cdveCh1kQk-").getId());
        assertEquals("-cdveCh1kQk", linkHandler.fromUrl("https://WWW.YouTube.com/watch?v=-cdveCh1kQkwhatever").getId());
        assertEquals("O0EDx9WAelc", linkHandler.fromUrl("HTTPS://www.youtube.com/watch?v=O0EDx9WAelc]").getId());
        assertEquals("-cdveCh1kQk", linkHandler.fromUrl("https://youtu.be/-cdveCh1kQk)hello").getId());
        assertEquals("OGS7c0-CmRs", linkHandler.fromUrl("https://YouTu.be/OGS7c0-CmRswhatever)").getId());
        assertEquals("-cdveCh1kQk", linkHandler.fromUrl("HTTPS://youtu.be/-cdveCh1kQk)").getId());
        assertEquals("IOS2fqxwYbA", linkHandler.fromUrl("https://www.youtube.com/shorts/IOS2fqxwYbAhi").getId());
        assertEquals("IOS2fqxwYbA", linkHandler.fromUrl("http://www.youtube.com/shorts/IOS2fqxwYbA").getId());
        assertEquals("IOS2fqxwYbA", linkHandler.fromUrl("http://www.youtube.com/v/IOS2fqxwYbA").getId());
        assertEquals("IOS2fqxwYbA", linkHandler.fromUrl("http://www.youtube.com/w/IOS2fqxwYbA").getId());
        assertEquals("IOS2fqxwYbA", linkHandler.fromUrl("http://www.youtube.com/watch/IOS2fqxwYbA").getId());
    }

    @Test
    public void testAcceptYtUrl() throws ParsingException {
        assertTrue(linkHandler.acceptUrl("https://www.youtube.com/watch?v=jZViOEv90dI"));
        assertTrue(linkHandler.acceptUrl("https://www.youtube.com/watch?v=jZViOEv90dI&t=100"));
        assertTrue(linkHandler.acceptUrl("https://WWW.YouTube.com/watch?v=jZViOEv90dI&t=100"));
        assertTrue(linkHandler.acceptUrl("HTTPS://www.youtube.com/watch?v=jZViOEv90dI&t=100"));
        assertTrue(linkHandler.acceptUrl("https://youtu.be/jZViOEv90dI?t=9s"));
        assertTrue(linkHandler.acceptUrl("https://www.youtube.com/embed/jZViOEv90dI"));
        assertTrue(linkHandler.acceptUrl("https://www.youtube-nocookie.com/embed/jZViOEv90dI"));
        assertTrue(linkHandler.acceptUrl("http://www.youtube.com/watch?v=jZViOEv90dI"));
        assertTrue(linkHandler.acceptUrl("http://youtu.be/jZViOEv90dI?t=9s"));
        assertTrue(linkHandler.acceptUrl("http://www.youtube.com/embed/jZViOEv90dI"));
        assertTrue(linkHandler.acceptUrl("http://www.youtube-nocookie.com/embed/jZViOEv90dI"));
        assertTrue(linkHandler.acceptUrl("http://www.youtube.com/attribution_link?a=JdfC0C9V6ZI&u=%2Fwatch%3Fv%3DEhxJLojIE_o%26feature%3Dshare"));
        assertTrue(linkHandler.acceptUrl("vnd.youtube://www.youtube.com/watch?v=jZViOEv90dI"));
        assertTrue(linkHandler.acceptUrl("vnd.youtube:jZViOEv90dI"));
        assertTrue(linkHandler.acceptUrl("vnd.youtube.launch:jZViOEv90dI"));
        assertTrue(linkHandler.acceptUrl("https://music.youtube.com/watch?v=O0EDx9WAelc"));
        assertTrue(linkHandler.acceptUrl("https://www.youtube.com/shorts/IOS2fqxwYbA"));
        assertTrue(linkHandler.acceptUrl("https://www.youtube.com/v/IOS2fqxwYbA"));
        assertTrue(linkHandler.acceptUrl("https://www.youtube.com/w/IOS2fqxwYbA"));
        assertTrue(linkHandler.acceptUrl("https://www.youtube.com/watch/IOS2fqxwYbA"));
    }

    @Test
    public void testAcceptHookUrl() throws ParsingException {
        assertTrue(linkHandler.acceptUrl("https://hooktube.com/watch?v=TglNG-yjabU"));
        assertTrue(linkHandler.acceptUrl("http://hooktube.com/watch?v=TglNG-yjabU"));
        assertTrue(linkHandler.acceptUrl("hooktube.com/watch?v=3msbfr6pBNE"));
        assertTrue(linkHandler.acceptUrl("https://hooktube.com/watch?v=ocH3oSnZG3c&list=PLS2VU1j4vzuZwooPjV26XM9UEBY2CPNn2"));
        assertTrue(linkHandler.acceptUrl("hooktube.com/watch/3msbfr6pBNE"));
        assertTrue(linkHandler.acceptUrl("hooktube.com/v/3msbfr6pBNE"));
        assertTrue(linkHandler.acceptUrl("hooktube.com/embed/3msbfr6pBNE"));
    }

    @Test
    public void testGetHookIdfromUrl() throws ParsingException {
        assertEquals("TglNG-yjabU", linkHandler.fromUrl("https://hooktube.com/watch?v=TglNG-yjabU").getId());
        assertEquals("TglNG-yjabU", linkHandler.fromUrl("http://hooktube.com/watch?v=TglNG-yjabU").getId());
        assertEquals("3msbfr6pBNE", linkHandler.fromUrl("hooktube.com/watch?v=3msbfr6pBNE").getId());
        assertEquals("ocH3oSnZG3c", linkHandler.fromUrl("https://hooktube.com/watch?v=ocH3oSnZG3c&list=PLS2VU1j4vzuZwooPjV26XM9UEBY2CPNn2").getId());
        assertEquals("3msbfr6pBNE", linkHandler.fromUrl("hooktube.com/watch/3msbfr6pBNE").getId());
        assertEquals("3msbfr6pBNE", linkHandler.fromUrl("hooktube.com/v/3msbfr6pBNE").getId());
        assertEquals("3msbfr6pBNE", linkHandler.fromUrl("hooktube.com/w/3msbfr6pBNE").getId());
        assertEquals("3msbfr6pBNE", linkHandler.fromUrl("hooktube.com/embed/3msbfr6pBNE").getId());
    }

    @Test
    public void testAcceptInvidiousUrl() throws ParsingException {
        assertTrue(linkHandler.acceptUrl("https://invidio.us/watch?v=TglNG-yjabU"));
        assertTrue(linkHandler.acceptUrl("http://www.invidio.us/watch?v=TglNG-yjabU"));
        assertTrue(linkHandler.acceptUrl("http://invidio.us/watch?v=TglNG-yjabU"));
        assertTrue(linkHandler.acceptUrl("invidio.us/watch?v=3msbfr6pBNE"));
        assertTrue(linkHandler.acceptUrl("https://invidio.us/watch?v=ocH3oSnZG3c&test=PLS2VU1j4vzuZwooPjV26XM9UEBY2CPNn2"));
        assertTrue(linkHandler.acceptUrl("invidio.us/embed/3msbfr6pBNE"));
        assertTrue(linkHandler.acceptUrl("invidio.us/watch/3msbfr6pBNE"));
        assertTrue(linkHandler.acceptUrl("invidio.us/v/3msbfr6pBNE"));
        assertTrue(linkHandler.acceptUrl("invidio.us/w/3msbfr6pBNE"));
    }

    @Test
    public void testGetInvidiousIdfromUrl() throws ParsingException {
        assertEquals("TglNG-yjabU", linkHandler.fromUrl("https://invidio.us/watch?v=TglNG-yjabU").getId());
        assertEquals("TglNG-yjabU", linkHandler.fromUrl("http://www.invidio.us/watch?v=TglNG-yjabU").getId());
        assertEquals("TglNG-yjabU", linkHandler.fromUrl("http://invidio.us/watch?v=TglNG-yjabU").getId());
        assertEquals("3msbfr6pBNE", linkHandler.fromUrl("invidio.us/watch?v=3msbfr6pBNE").getId());
        assertEquals("ocH3oSnZG3c", linkHandler.fromUrl("https://invidio.us/watch?v=ocH3oSnZG3c&test=PLS2VU1j4vzuZwooPjV26XM9UEBY2CPNn2").getId());
        assertEquals("3msbfr6pBNE", linkHandler.fromUrl("invidio.us/embed/3msbfr6pBNE").getId());
        assertEquals("3msbfr6pBNE", linkHandler.fromUrl("invidio.us/v/3msbfr6pBNE").getId());
        assertEquals("3msbfr6pBNE", linkHandler.fromUrl("invidio.us/w/3msbfr6pBNE").getId());
        assertEquals("3msbfr6pBNE", linkHandler.fromUrl("invidio.us/watch/3msbfr6pBNE").getId());
    }
}