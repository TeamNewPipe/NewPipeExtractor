package org.schabi.newpipe.extractor.services.soundcloud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.InitNewPipeTest;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.soundcloud.linkHandler.SoundcloudChartsLinkHandlerFactory;

/**
 * Test for {@link SoundcloudChartsLinkHandlerFactory}
 */
public class SoundcloudChartsLinkHandlerFactoryTest {
    private static SoundcloudChartsLinkHandlerFactory linkHandler;

    @BeforeAll
    public static void setUp() {
        InitNewPipeTest.initEmpty();
        linkHandler = SoundcloudChartsLinkHandlerFactory.getInstance();
    }

    @Test
    public void getUrl() throws Exception {
        assertEquals("https://soundcloud.com/charts/new", linkHandler.fromId("New & hot").getUrl());
    }

    @Test
    public void getId() throws ParsingException {
        assertEquals("New & hot", linkHandler.fromUrl("HTTP://www.soundcloud.com/charts/new/?genre=all-music&country=all-countries").getId());
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
