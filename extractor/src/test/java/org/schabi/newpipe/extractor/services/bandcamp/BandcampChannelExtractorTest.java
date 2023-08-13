// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.services.BaseChannelExtractorTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertTabsContain;
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
    @Override
    public void testDescription() throws Exception {
        assertEquals("making music:)", extractor.getDescription());
    }

    @Test
    @Override
    public void testAvatars() throws Exception {
        BandcampTestUtils.testImages(extractor.getAvatars());
    }

    @Test
    @Override
    public void testBanners() throws Exception {
        BandcampTestUtils.testImages(extractor.getBanners());
    }

    @Test
    @Override
    public void testFeedUrl() throws Exception {
        assertNull(extractor.getFeedUrl());
    }

    @Test
    @Override
    public void testSubscriberCount() throws Exception {
        assertEquals(-1, extractor.getSubscriberCount());
    }

    @Test
    @Override
    public void testVerified() throws Exception {
        assertFalse(extractor.isVerified());
    }

    @Test
    @Override
    public void testServiceId() {
        assertEquals(Bandcamp.getServiceId(), extractor.getServiceId());
    }

    @Test
    @Override
    public void testName() throws Exception {
        assertEquals("toupie", extractor.getName());
    }

    @Test
    @Override
    public void testId() throws Exception {
        assertEquals("2450875064", extractor.getId());
    }

    @Test
    @Override
    public void testUrl() throws Exception {
        assertEquals("https://toupie.bandcamp.com", extractor.getUrl());
    }

    @Test
    @Override
    public void testOriginalUrl() throws Exception {
        assertEquals("https://toupie.bandcamp.com", extractor.getUrl());
    }

    @Test
    @Override
    public void testTabs() throws Exception {
        assertTabsContain(extractor.getTabs(), ChannelTabs.ALBUMS);
    }

    @Test
    @Override
    public void testTags() throws Exception {
        assertTrue(extractor.getTags().isEmpty());
    }
}
