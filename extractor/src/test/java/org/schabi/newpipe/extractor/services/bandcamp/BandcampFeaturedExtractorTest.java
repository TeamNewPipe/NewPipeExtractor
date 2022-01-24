// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem;
import org.schabi.newpipe.extractor.services.BaseListExtractorTest;
import org.schabi.newpipe.extractor.services.DefaultTests;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampFeaturedExtractor;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.Bandcamp;

/**
 * Tests for {@link BandcampFeaturedExtractor}
 */
public class BandcampFeaturedExtractorTest implements BaseListExtractorTest {

    private static BandcampFeaturedExtractor extractor;

    @BeforeAll
    public static void setUp() throws ExtractionException, IOException {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = (BandcampFeaturedExtractor) Bandcamp
                .getKioskList().getDefaultKioskExtractor();
        extractor.fetchPage();
    }

    @Test
    public void testFeaturedCount() throws ExtractionException, IOException {
        final List<PlaylistInfoItem> list = extractor.getInitialPage().getItems();
        assertTrue(list.size() > 5);
    }

    @Test
    public void testHttps() throws ExtractionException, IOException {
        final List<PlaylistInfoItem> list = extractor.getInitialPage().getItems();
        assertTrue(list.get(0).getUrl().contains("https://"));
    }

    @Test
    public void testMorePages() throws ExtractionException, IOException {

        final Page page2 = extractor.getInitialPage().getNextPage();
        final Page page3 = extractor.getPage(page2).getNextPage();

        assertTrue(extractor.getPage(page2).getItems().size() > 5);

        // Compare first item of second page with first item of third page
        assertNotEquals(
                extractor.getPage(page2).getItems().get(0),
                extractor.getPage(page3).getItems().get(0)
        );
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
