// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.bandcamp.linkHandler.BandcampStreamLinkHandlerFactory;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

/**
 * Test for {@link BandcampStreamLinkHandlerFactory}
 */
public class BandcampStreamLinkHandlerFactoryTest {

    private static BandcampStreamLinkHandlerFactory linkHandler;

    @BeforeClass
    public static void setUp() {
        linkHandler = new BandcampStreamLinkHandlerFactory();
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    public void testUrlCleanup() {
        assertEquals("https://zachbenson.bandcamp.com/track/u-i-tonite", linkHandler.getUrl("http://ZachBenson.Bandcamp.COM/Track/U-I-Tonite/"));
    }

    @Test
    public void testAcceptUrl() throws ParsingException {
        // Tests expecting false
        assertFalse(linkHandler.acceptUrl("http://interovgm.com/releases/"));
        assertFalse(linkHandler.acceptUrl("https://interovgm.com/releases"));
        assertFalse(linkHandler.acceptUrl("http://zachbenson.bandcamp.com"));
        assertFalse(linkHandler.acceptUrl("https://bandcamp.com"));
        assertFalse(linkHandler.acceptUrl("https://zachbenson.bandcamp.com/"));

        // Tests expecting true
        assertTrue(linkHandler.acceptUrl("https://zachbenson.bandcamp.com/track/kitchen"));
        assertTrue(linkHandler.acceptUrl("http://ZachBenson.Bandcamp.COM/Track/U-I-Tonite/"));
        assertTrue(linkHandler.acceptUrl("https://interovgm.com/track/title"));
    }
}
