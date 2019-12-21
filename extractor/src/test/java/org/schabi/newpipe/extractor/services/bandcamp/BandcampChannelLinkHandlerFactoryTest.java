// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.bandcamp.linkHandler.BandcampChannelLinkHandlerFactory;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link BandcampChannelLinkHandlerFactory}
 */
public class BandcampChannelLinkHandlerFactoryTest {
    private static BandcampChannelLinkHandlerFactory linkHandler;

    @BeforeClass
    public static void setUp() {
        linkHandler = new BandcampChannelLinkHandlerFactory();
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    public void testAcceptUrl() throws ParsingException {
        // Tests expecting true
        assertTrue(linkHandler.acceptUrl("http://interovgm.com/releases/"));
        assertTrue(linkHandler.acceptUrl("https://interovgm.com/releases"));
        assertTrue(linkHandler.acceptUrl("http://zachbenson.bandcamp.com"));

        // Tests expecting false
        assertFalse(linkHandler.acceptUrl("https://bandcamp.com"));
        assertFalse(linkHandler.acceptUrl("https://zachbenson.bandcamp.com/track/kitchen"));
        assertFalse(linkHandler.acceptUrl("https://zachbenson.bandcamp.com/"));
    }

}
