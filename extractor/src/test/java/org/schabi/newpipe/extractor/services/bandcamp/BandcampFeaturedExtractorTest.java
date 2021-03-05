// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem;
import org.schabi.newpipe.extractor.services.BaseListExtractorTest;
import org.schabi.newpipe.extractor.services.DefaultTests;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampFeaturedExtractor;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.Bandcamp;

/**
 * Tests for {@link BandcampFeaturedExtractor}
 */
public class BandcampFeaturedExtractorTest implements BaseListExtractorTest {

    private static BandcampFeaturedExtractor extractor;

    @BeforeClass
    public static void setUp() throws ExtractionException, IOException {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = (BandcampFeaturedExtractor) Bandcamp
                .getKioskList().getDefaultKioskExtractor();
        extractor.fetchPage();
    }

    @Test
    public void testFeaturedCount() throws ExtractionException, IOException {
        final List<PlaylistInfoItem> list = extractor.getInitialPage().getItems();
        assertTrue(list.size() > 1);
    }

    @Test
    public void testHttps() throws ExtractionException, IOException {
        final List<PlaylistInfoItem> list = extractor.getInitialPage().getItems();
        assertTrue(list.get(0).getUrl().contains("https://"));
    }

    @Override
    public void testRelatedItems() throws Exception {
        DefaultTests.defaultTestRelatedItems(extractor);
    }

    @Override
    public void testMoreRelatedItems() throws Exception {
        // more items not implemented
    }

    @Override
    public void testServiceId() {
        assertEquals(Bandcamp.getServiceId(), extractor.getServiceId());
    }

    @Override
    public void testName() throws Exception {
        assertEquals("Featured", extractor.getName());
    }

    @Override
    public void testId() {
        assertEquals("", extractor.getId());
    }

    @Override
    public void testUrl() throws Exception {
        assertEquals("", extractor.getUrl());
    }

    @Override
    public void testOriginalUrl() throws Exception {
        assertEquals("", extractor.getOriginalUrl());
    }
}
