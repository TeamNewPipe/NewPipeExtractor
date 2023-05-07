// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.ExtractorAsserts;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.linkhandler.ChannelTabs;
import org.schabi.newpipe.extractor.services.BaseChannelExtractorTest;

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
    @Override
    public void testDescription() throws Exception {
        assertEquals("making music:)", extractor.getDescription());
    }

    @Test
    @Override
    public void testAvatarUrl() throws Exception {
        assertTrue(extractor.getAvatarUrl().contains("://f4.bcbits.com/"), "unexpected avatar URL");
    }

    @Test
    @Override
    public void testBannerUrl() throws Exception {
        assertTrue(extractor.getBannerUrl().contains("://f4.bcbits.com/"), "unexpected banner URL");
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
    public void testTabs() throws Exception {
        ExtractorAsserts.assertTabs(extractor.getTabs(), ChannelTabs.ALBUMS);
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
}
