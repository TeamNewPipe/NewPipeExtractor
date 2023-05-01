package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ChannelTabs;
import org.schabi.newpipe.extractor.services.BaseListExtractorTest;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampChannelTabExtractor;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.Bandcamp;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestRelatedItems;

public class BandcampChannelTabExtractorTest {
    public static class Tracks implements BaseListExtractorTest {
        private static BandcampChannelTabExtractor extractor;

        @BeforeAll
        public static void setUp() throws IOException, ExtractionException {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (BandcampChannelTabExtractor) Bandcamp
                    .getChannelTabExtractorFromId("2464198920", ChannelTabs.TRACKS);
            extractor.fetchPage();
        }

        @Test
        @Override
        public void testServiceId() throws Exception {
            assertEquals(Bandcamp.getServiceId(), extractor.getServiceId());
        }

        @Test
        @Override
        public void testName() throws Exception {
            assertEquals(ChannelTabs.TRACKS, extractor.getName());
        }

        @Test
        public void testTab() {
            assertEquals(ChannelTabs.TRACKS, extractor.getTab());
        }

        @Test
        @Override
        public void testId() throws ParsingException {
            assertEquals("2464198920", extractor.getId());
        }

        @Test
        @Override
        public void testUrl() throws ParsingException {
            assertEquals("https://wintergatan.bandcamp.com/track", extractor.getUrl());
        }

        @Test
        @Override
        public void testOriginalUrl() throws Exception {
            assertEquals("https://wintergatan.bandcamp.com/track", extractor.getOriginalUrl());
        }

        @Test
        @Override
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor);
        }

        @Override
        public void testMoreRelatedItems() throws Exception {
            // Bandcamp only returns a single page
        }
    }

    public static class Albums implements BaseListExtractorTest {
        private static BandcampChannelTabExtractor extractor;

        @BeforeAll
        public static void setUp() throws IOException, ExtractionException {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (BandcampChannelTabExtractor) Bandcamp
                    .getChannelTabExtractorFromId("2450875064", ChannelTabs.ALBUMS);
            extractor.fetchPage();
        }

        @Test
        @Override
        public void testServiceId() {
            assertEquals(Bandcamp.getServiceId(), extractor.getServiceId());
        }

        @Test
        @Override
        public void testName() throws Exception {
            assertEquals(ChannelTabs.ALBUMS, extractor.getName());
        }

        @Test
        public void testTab() {
            assertEquals(ChannelTabs.ALBUMS, extractor.getTab());
        }

        @Test
        @Override
        public void testId() throws ParsingException {
            assertEquals("2450875064", extractor.getId());
        }

        @Test
        @Override
        public void testUrl() throws ParsingException {
            assertEquals("https://toupie.bandcamp.com/album", extractor.getUrl());
        }

        @Test
        @Override
        public void testOriginalUrl() throws Exception {
            assertEquals("https://toupie.bandcamp.com/album", extractor.getOriginalUrl());
        }

        @Test
        @Override
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor);
        }

        @Override
        public void testMoreRelatedItems() throws Exception {
            // Bandcamp only returns a single page
        }
    }
}
