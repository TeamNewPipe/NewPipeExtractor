// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.linkhandler.ChannelTabs;
import org.schabi.newpipe.extractor.services.BaseChannelExtractorTest;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.schabi.newpipe.extractor.ServiceList.Bandcamp;

public class BandcampChannelExtractorTest implements BaseChannelExtractorTest {

    private static ChannelExtractor extractor;

    @BeforeAll
    public static void setUp() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = Bandcamp.getChannelExtractor("https://toupie.bandcamp.com/releases");
        extractor.fetchPage();
    }

    @Test
    public void testLength() throws ExtractionException, IOException {
        assertTrue(extractor.getInitialPage().getItems().size() >= 0);
    }

    @Test
    public void testDescription() throws Exception {
        assertEquals("making music:)", extractor.getDescription());
    }

    @Test
    public void testAvatarUrl() throws Exception {
        assertTrue(extractor.getAvatarUrl().contains("://f4.bcbits.com/"), "unexpected avatar URL");
    }

    @Test
    public void testBannerUrl() throws Exception {
        assertTrue(extractor.getBannerUrl().contains("://f4.bcbits.com/"), "unexpected banner URL");
    }

    @Test
    public void testFeedUrl() throws Exception {
        assertNull(extractor.getFeedUrl());
    }

    @Test
    public void testSubscriberCount() throws Exception {
        assertEquals(-1, extractor.getSubscriberCount());
    }

    @Test
    public void testVerified() throws Exception {
        assertFalse(extractor.isVerified());
    }

    @Test
    public void testTabs() throws Exception {
        Set<String> tabs = extractor.getTabs().stream()
                .map(linkHandler -> linkHandler.getContentFilters().get(0)).collect(Collectors.toSet());
        assertTrue(tabs.contains(ChannelTabs.ALBUMS));
    }

    @Override
    public void testRelatedItems() throws Exception {
        // not implemented
    }

    @Override
    public void testMoreRelatedItems() throws Exception {
        // not implemented
    }

    @Test
    public void testServiceId() {
        assertEquals(Bandcamp.getServiceId(), extractor.getServiceId());
    }

    @Test
    public void testName() throws Exception {
        assertEquals("toupie", extractor.getName());
    }

    @Test
    public void testId() throws Exception {
        assertEquals("2450875064", extractor.getId());
    }

    @Test
    public void testUrl() throws Exception {
        assertEquals("https://toupie.bandcamp.com", extractor.getUrl());
    }

    @Test
    public void testOriginalUrl() throws Exception {
        assertEquals("https://toupie.bandcamp.com", extractor.getUrl());
    }
}
