package org.schabi.newpipe.extractor.services.soundcloud;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.soundcloud.linkHandler.SoundcloudChartsLinkHandlerFactory;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link SoundcloudChartsLinkHandlerFactory}
 */
public class SoundcloudChartsLinkHandlerFactoryTest {
    private static SoundcloudChartsLinkHandlerFactory linkHandler;

    @BeforeClass
    public static void setUp() {
        linkHandler = new SoundcloudChartsLinkHandlerFactory();
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    public void getUrl() throws Exception {
        assertEquals(linkHandler.fromId("Top 50").getUrl(), "https://soundcloud.com/charts/top");
        assertEquals(linkHandler.fromId("New & hot").getUrl(), "https://soundcloud.com/charts/new");
    }

    @Test
    public void getId() throws ParsingException {
        assertEquals(linkHandler.fromUrl("http://soundcloud.com/charts/top?genre=all-music").getId(), "Top 50");
        assertEquals(linkHandler.fromUrl("HTTP://www.soundcloud.com/charts/new/?genre=all-music&country=all-countries").getId(), "New & hot");
    }

    @Test
    public void acceptUrl() throws ParsingException {
        assertTrue(linkHandler.acceptUrl("https://soundcloud.com/charts"));
        assertTrue(linkHandler.acceptUrl("https://soundcloud.com/charts/"));
        assertTrue(linkHandler.acceptUrl("https://www.soundcloud.com/charts/new"));
        assertTrue(linkHandler.acceptUrl("http://soundcloud.com/charts/top?genre=all-music"));
        assertTrue(linkHandler.acceptUrl("HTTP://www.soundcloud.com/charts/new/?genre=all-music&country=all-countries"));

        assertFalse(linkHandler.acceptUrl("kdskjfiiejfia"));
        assertFalse(linkHandler.acceptUrl("soundcloud.com/charts askjkf"));
        assertFalse(linkHandler.acceptUrl("    soundcloud.com/charts"));
        assertFalse(linkHandler.acceptUrl(""));
    }
}
