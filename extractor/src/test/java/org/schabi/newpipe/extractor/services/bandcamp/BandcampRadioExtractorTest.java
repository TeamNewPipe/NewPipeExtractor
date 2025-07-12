// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.Bandcamp;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.services.BaseListExtractorTest;
import org.schabi.newpipe.extractor.services.DefaultSimpleExtractorTest;
import org.schabi.newpipe.extractor.services.DefaultTests;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampRadioExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.util.List;

/**
 * Tests for {@link BandcampRadioExtractor}
 */
public class BandcampRadioExtractorTest extends DefaultSimpleExtractorTest<BandcampRadioExtractor>
    implements BaseListExtractorTest {

    @Override
    protected BandcampRadioExtractor createExtractor() throws Exception {
        return (BandcampRadioExtractor) Bandcamp
            .getKioskList()
            .getExtractorById("Radio", null);
    }

    @Test
    public void testRadioCount() {
        final List<StreamInfoItem> list = extractor().getInitialPage().getItems();
        assertTrue(list.size() > 300);
    }

    @Override
    @Test
    public void testRelatedItems() throws Exception {
        DefaultTests.defaultTestRelatedItems(extractor());
    }

    @Override
    @Test
    public void testMoreRelatedItems() throws Exception {
        // All items are on one page
    }

    @Override
    @Test
    public void testServiceId() {
        assertEquals(Bandcamp.getServiceId(), extractor().getServiceId());
    }

    @Override
    @Test
    public void testName() throws Exception {
        assertEquals("Radio", extractor().getName());
    }

    @Override
    @Test
    public void testId() {
        assertEquals("Radio", extractor().getId());
    }

    @Override
    @Test
    public void testUrl() throws Exception {
        assertEquals("https://bandcamp.com/api/bcweekly/3/list", extractor().getUrl());
    }

    @Override
    @Test
    public void testOriginalUrl() throws Exception {
        assertEquals("https://bandcamp.com/api/bcweekly/3/list", extractor().getOriginalUrl());
    }
}
