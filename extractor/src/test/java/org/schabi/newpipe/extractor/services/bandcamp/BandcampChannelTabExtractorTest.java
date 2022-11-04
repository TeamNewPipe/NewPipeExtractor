package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ChannelTabs;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampChannelTabExtractor;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.Bandcamp;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestRelatedItems;

public class BandcampChannelTabExtractorTest {
    public static class Albums {
        private static BandcampChannelTabExtractor extractor;

        @BeforeAll
        public static void setUp() throws IOException, ExtractionException {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (BandcampChannelTabExtractor) Bandcamp
                    .getChannelTabExtractorFromId("2450875064", ChannelTabs.ALBUMS);
            extractor.fetchPage();
        }

        @Test
        public void testServiceId() {
            assertEquals(Bandcamp.getServiceId(), extractor.getServiceId());
        }

        @Test
        public void testTab() {
            assertEquals(ChannelTabs.ALBUMS, extractor.getTab());
        }

        @Test
        public void testId() throws ParsingException {
            assertEquals("2450875064", extractor.getId());
        }

        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://toupie.bandcamp.com/album", extractor.getUrl());
        }

        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor);
        }
    }
}
