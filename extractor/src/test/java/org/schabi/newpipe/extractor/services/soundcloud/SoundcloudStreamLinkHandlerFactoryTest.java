package org.schabi.newpipe.extractor.services.soundcloud;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.soundcloud.linkHandler.SoundcloudStreamLinkHandlerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for {@link SoundcloudStreamLinkHandlerFactory}
 */
public class SoundcloudStreamLinkHandlerFactoryTest {
    private static SoundcloudStreamLinkHandlerFactory linkHandler;

    @BeforeAll
    public static void setUp() throws Exception {
        linkHandler = SoundcloudStreamLinkHandlerFactory.getInstance();
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    void getIdWithNullAsUrl() {
        assertThrows(IllegalArgumentException.class, () -> linkHandler.fromUrl(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://soundcloud.com/liluzivert/t.e.s.t",
            "https://soundcloud.com/liluzivert/tracks",
            "https://soundcloud.com/"
    })
    void getIdForInvalidUrls(final String invalidUrl) {
        assertThrows(ParsingException.class, () -> linkHandler.fromUrl(invalidUrl).getId());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "309689103,https://soundcloud.com/liluzivert/15-ysl",
            "309689082,https://www.soundcloud.com/liluzivert/15-luv-scars-ko",
            "309689035,http://soundcloud.com/liluzivert/15-boring-shit",
            "259273264,https://soundcloud.com/liluzivert/ps-qs-produced-by-don-cannon/",
            "294488599,http://www.soundcloud.com/liluzivert/secure-the-bag-produced-by-glohan-beats",
            "245710200,HtTpS://sOuNdClOuD.cOm/lIeuTeNaNt_rAe/bOtS-wAs-wOlLeN-wIr-tRinKeN",
            "294488147,https://soundcloud.com/liluzivert/fresh-produced-by-zaytoven#t=69",
            "294487876,https://soundcloud.com/liluzivert/threesome-produced-by-zaytoven#t=1:09",
            "294487684,https://soundcloud.com/liluzivert/blonde-brigitte-produced-manny-fresh#t=1:9",
            "294487428,https://soundcloud.com/liluzivert/today-produced-by-c-note#t=1m9s",
            "294487157,https://soundcloud.com/liluzivert/changed-my-phone-produced-by-c-note#t=1m09s",
            "44556776,https://soundcloud.com/kechuspider-sets-1/last-days"
    })
    void getId(final String expectedId, final String url) throws ParsingException {
        assertEquals(expectedId, linkHandler.fromUrl(url).getId());
    }


    @ParameterizedTest
    @ValueSource(strings = {
            "https://soundcloud.com/liluzivert/15-ysl",
            "https://www.soundcloud.com/liluzivert/15-luv-scars-ko",
            "http://soundcloud.com/liluzivert/15-boring-shit",
            "http://www.soundcloud.com/liluzivert/secure-the-bag-produced-by-glohan-beats",
            "HtTpS://sOuNdClOuD.cOm/LiLuZiVeRt/In-O4-pRoDuCeD-bY-dP-bEaTz",
            "https://soundcloud.com/liluzivert/fresh-produced-by-zaytoven#t=69",
            "https://soundcloud.com/liluzivert/threesome-produced-by-zaytoven#t=1:09",
            "https://soundcloud.com/liluzivert/blonde-brigitte-produced-manny-fresh#t=1:9",
            "https://soundcloud.com/liluzivert/today-produced-by-c-note#t=1m9s",
            "https://soundcloud.com/liluzivert/changed-my-phone-produced-by-c-note#t=1m09s"
    })
    void testAcceptUrl(final String url) throws ParsingException {
        assertTrue(linkHandler.acceptUrl(url));
    }
}