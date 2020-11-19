// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.BaseChannelExtractorTest;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ServiceList.Bandcamp;

public class BandcampChannelExtractorTest implements BaseChannelExtractorTest {

    private static ChannelExtractor extractor;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = Bandcamp.getChannelExtractor("https://npet.bandcamp.com/releases");
        extractor.fetchPage();
    }

    @Test
    public void testLength() throws ExtractionException, IOException {
        assertTrue(extractor.getInitialPage().getItems().size() >= 1);
    }

    @Override
    @Test
    public void testDescription() throws Exception {
        assertEquals("This string will be tested for in NewPipeExtractor tests.", extractor.getDescription());
    }

    @Override
    public void testAvatarUrl() throws Exception {
        // Has no avatar
        assertEquals("", extractor.getAvatarUrl());
    }

    @Override
    public void testBannerUrl() throws Exception {
        // Has no banner
        assertEquals("", extractor.getBannerUrl());
    }

    @Override
    public void testFeedUrl() throws Exception {
        assertNull(extractor.getFeedUrl());
    }

    @Override
    public void testSubscriberCount() throws Exception {
        assertEquals(-1, extractor.getSubscriberCount());
    }

    @Override
    public void testRelatedItems() throws Exception {
        // not implemented
    }

    @Override
    public void testMoreRelatedItems() throws Exception {
        // not implemented
    }

    @Override
    public void testServiceId() {
        assertEquals(Bandcamp.getServiceId(), extractor.getServiceId());
    }

    @Override
    public void testName() throws Exception {
        assertEquals("NewPipeExtractorTest", extractor.getName());
    }

    @Override
    public void testId() throws Exception {
        assertEquals("https://npet.bandcamp.com/", extractor.getId());
    }

    @Override
    public void testUrl() throws Exception {
        assertEquals("https://npet.bandcamp.com/releases", extractor.getUrl());
    }

    @Override
    public void testOriginalUrl() throws Exception {
        assertEquals("https://npet.bandcamp.com/releases", extractor.getUrl());
    }
}
