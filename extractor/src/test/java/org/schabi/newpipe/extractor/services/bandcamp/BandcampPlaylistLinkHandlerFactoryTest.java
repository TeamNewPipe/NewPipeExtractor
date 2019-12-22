// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.bandcamp.linkHandler.BandcampPlaylistLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.bandcamp.linkHandler.BandcampStreamLinkHandlerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link BandcampPlaylistLinkHandlerFactory}
 */
public class BandcampPlaylistLinkHandlerFactoryTest {

    private static BandcampPlaylistLinkHandlerFactory linkHandler;

    @BeforeClass
    public static void setUp() {
        linkHandler = new BandcampPlaylistLinkHandlerFactory();
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    public void testAcceptUrl() throws ParsingException {
        // Tests expecting false
        assertFalse(linkHandler.acceptUrl("http://interovgm.com/releases/"));
        assertFalse(linkHandler.acceptUrl("https://interovgm.com/releases"));
        assertFalse(linkHandler.acceptUrl("http://zachbenson.bandcamp.com"));
        assertFalse(linkHandler.acceptUrl("https://bandcamp.com"));
        assertFalse(linkHandler.acceptUrl("https://zachbenson.bandcamp.com/"));
        assertFalse(linkHandler.acceptUrl("https://zachbenson.bandcamp.com/track/kitchen"));
        assertFalse(linkHandler.acceptUrl("https://interovgm.com/track/title"));

        // Tests expecting true
        assertTrue(linkHandler.acceptUrl("https://powertothequeerkids.bandcamp.com/album/power-to-the-queer-kids"));
        assertTrue(linkHandler.acceptUrl("https://zachbenson.bandcamp.com/album/prom"));
    }

}
