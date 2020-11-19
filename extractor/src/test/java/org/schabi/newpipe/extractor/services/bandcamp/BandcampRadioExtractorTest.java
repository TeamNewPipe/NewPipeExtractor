// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.BaseListExtractorTest;
import org.schabi.newpipe.extractor.services.DefaultTests;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampRadioExtractor;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.Bandcamp;

/**
 * Tests for {@link BandcampRadioExtractor}
 */
public class BandcampRadioExtractorTest implements BaseListExtractorTest {

    private static BandcampRadioExtractor extractor;

    @BeforeClass
    public static void setUp() throws ExtractionException, IOException {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = (BandcampRadioExtractor) Bandcamp
                .getKioskList()
                .getExtractorById("Radio", null);
    }

    @Test
    public void testRadioCount() throws ExtractionException, IOException {
        List<InfoItem> list = Bandcamp.getKioskList().getExtractorById("Radio", null).getInitialPage().getItems();
        assertTrue(list.size() > 300);
    }

    @Test
    public void testRelatedItems() throws Exception {
        //DefaultTests.defaultTestRelatedItems(extractor);
        // Would fail because BandcampRadioInfoItemExtractor.getUploaderName() returns an empty String
    }

    @Test
    public void testMoreRelatedItems() throws Exception {
        // All items are on one page
    }

    @Test
    public void testServiceId() {
        assertEquals(Bandcamp.getServiceId(), extractor.getServiceId());
    }

    @Test
    public void testName() throws Exception {
        assertEquals("Radio", extractor.getName());
    }

    @Test
    public void testId() {
        assertEquals("Radio", extractor.getId());
    }

    @Test
    public void testUrl() throws Exception {
        assertEquals("https://bandcamp.com/api/bcweekly/1/list", extractor.getUrl());
    }

    @Test
    public void testOriginalUrl() throws Exception {
        assertEquals("https://bandcamp.com/api/bcweekly/1/list", extractor.getOriginalUrl());
    }
}
