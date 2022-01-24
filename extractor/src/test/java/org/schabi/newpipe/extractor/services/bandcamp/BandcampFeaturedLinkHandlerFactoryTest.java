// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.bandcamp.linkHandler.BandcampFeaturedLinkHandlerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link BandcampFeaturedLinkHandlerFactory}
 */
public class BandcampFeaturedLinkHandlerFactoryTest {

    private static BandcampFeaturedLinkHandlerFactory linkHandler;

    @BeforeAll
    public static void setUp() {
        linkHandler = new BandcampFeaturedLinkHandlerFactory();
    }


    @Test
    public void testAcceptUrl() throws ParsingException {
        assertTrue(linkHandler.acceptUrl("http://bandcamp.com/?show=1"));
        assertTrue(linkHandler.acceptUrl("https://bandcamp.com/?show=1"));
        assertTrue(linkHandler.acceptUrl("http://bandcamp.com/?show=2"));
        assertTrue(linkHandler.acceptUrl("https://bandcamp.com/api/mobile/24/bootstrap_data"));
        assertTrue(linkHandler.acceptUrl("https://bandcamp.com/api/bcweekly/1/list"));

        assertFalse(linkHandler.acceptUrl("https://bandcamp.com/?show="));
        assertFalse(linkHandler.acceptUrl("https://bandcamp.com/?show=a"));
        assertFalse(linkHandler.acceptUrl("https://bandcamp.com/"));
    }

    @Test
    public void testGetUrl() throws ParsingException {
        assertEquals("https://bandcamp.com/api/mobile/24/bootstrap_data", linkHandler.getUrl("Featured"));
        assertEquals("https://bandcamp.com/api/bcweekly/1/list", linkHandler.getUrl("Radio"));
    }

    @Test
    public void testGetId() {
        assertEquals("Featured", linkHandler.getId("http://bandcamp.com/api/mobile/24/bootstrap_data"));
        assertEquals("Featured", linkHandler.getId("https://bandcamp.com/api/mobile/24/bootstrap_data"));
        assertEquals("Radio", linkHandler.getId("http://bandcamp.com/?show=1"));
        assertEquals("Radio", linkHandler.getId("https://bandcamp.com/api/bcweekly/1/list"));
    }

}
