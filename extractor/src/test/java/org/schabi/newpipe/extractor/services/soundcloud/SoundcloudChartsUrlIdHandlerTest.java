package org.schabi.newpipe.extractor.services.soundcloud;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link SoundcloudChartsUrlIdHandler}
 */
public class SoundcloudChartsUrlIdHandlerTest {
    private static SoundcloudChartsUrlIdHandler urlIdHandler;

    @BeforeClass
    public static void setUp() throws Exception {
        urlIdHandler = new SoundcloudChartsUrlIdHandler();
        NewPipe.init(Downloader.getInstance());
    }

    @Test
    public void getUrl() throws Exception {
        assertEquals(urlIdHandler.setId("Top 50").getUrl(), "https://soundcloud.com/charts/top");
        assertEquals(urlIdHandler.setId("New & hot").getUrl(), "https://soundcloud.com/charts/new");
    }

    @Test
    public void getId() throws ParsingException {
        assertEquals(urlIdHandler.setUrl("http://soundcloud.com/charts/top?genre=all-music").getId(), "Top 50");
        assertEquals(urlIdHandler.setUrl("HTTP://www.soundcloud.com/charts/new/?genre=all-music&country=all-countries").getId(), "New & hot");
    }

    @Test
    public void acceptUrl() throws ParsingException {
        assertTrue(urlIdHandler.acceptUrl("https://soundcloud.com/charts"));
        assertTrue(urlIdHandler.acceptUrl("https://soundcloud.com/charts/"));
        assertTrue(urlIdHandler.acceptUrl("https://www.soundcloud.com/charts/new"));
        assertTrue(urlIdHandler.acceptUrl("http://soundcloud.com/charts/top?genre=all-music"));
        assertTrue(urlIdHandler.acceptUrl("HTTP://www.soundcloud.com/charts/new/?genre=all-music&country=all-countries"));

        assertFalse(urlIdHandler.acceptUrl("kdskjfiiejfia"));
        assertFalse(urlIdHandler.acceptUrl("soundcloud.com/charts askjkf"));
        assertFalse(urlIdHandler.acceptUrl("    soundcloud.com/charts"));
        assertFalse(urlIdHandler.acceptUrl(""));
    }
}
