// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.bandcamp.linkHandler.BandcampCommentsLinkHandlerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for {@link BandcampCommentsLinkHandlerFactory}
 */
public class BandcampCommentsLinkHandlerFactoryTest {

    private static BandcampCommentsLinkHandlerFactory linkHandler;

    @BeforeAll
    public static void setUp() {
        linkHandler = new BandcampCommentsLinkHandlerFactory();
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    public void testAcceptUrl() throws ParsingException {
        assertFalse(linkHandler.acceptUrl("http://interovgm.com/releases/"));
        assertFalse(linkHandler.acceptUrl("https://interovgm.com/releases"));
        assertFalse(linkHandler.acceptUrl("http://zachbenson.bandcamp.com"));
        assertFalse(linkHandler.acceptUrl("https://bandcamp.com"));
        assertFalse(linkHandler.acceptUrl("https://zachbenson.bandcamp.com/"));
        assertFalse(linkHandler.acceptUrl("https://example.com/track/sampletrack"));

        assertTrue(linkHandler.acceptUrl("http://bandcamP.com/?show=38"));
        assertTrue(linkHandler.acceptUrl("https://powertothequeerkids.bandcamp.com/album/power-to-the-queer-kids"));
        assertTrue(linkHandler.acceptUrl("https://zachbenson.bandcamp.com/track/kitchen"));
        assertTrue(linkHandler.acceptUrl("http://ZachBenson.Bandcamp.COM/Track/U-I-Tonite/"));
        assertTrue(linkHandler.acceptUrl("https://interovgm.bandcamp.com/track/title"));
        assertTrue(linkHandler.acceptUrl("https://goodgoodblood-tl.bandcamp.com/track/when-it-all-wakes-up"));
        assertTrue(linkHandler.acceptUrl("https://lobstertheremin.com/track/unfinished"));
    }
}
