package org.schabi.newpipe.extractor.services.peertube;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.BaseListExtractorTest;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeChannelTabExtractor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestMoreItems;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestRelatedItems;

class PeertubeAccountTabExtractorTest {

    static class Videos implements BaseListExtractorTest {
        private static PeertubeChannelTabExtractor extractor;

        @BeforeAll
        static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            // setting instance might break test when running in parallel
            PeerTube.setInstance(new PeertubeInstance("https://framatube.org", "Framatube"));
            extractor = (PeertubeChannelTabExtractor) PeerTube
                    .getChannelTabExtractorFromId("accounts/framasoft", ChannelTabs.VIDEOS);
            extractor.fetchPage();
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        @Override
        public void testServiceId() {
            assertEquals(PeerTube.getServiceId(), extractor.getServiceId());
        }

        @Test
        @Override
        public void testName() throws ParsingException {
            assertEquals(ChannelTabs.VIDEOS, extractor.getName());
        }

        @Test
        @Override
        public void testId() throws ParsingException {
            assertEquals("accounts/framasoft", extractor.getId());
        }

        @Test
        @Override
        public void testUrl() throws ParsingException {
            assertEquals("https://framatube.org/accounts/framasoft/videos",
                    extractor.getUrl());
        }

        @Test
        @Override
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://framatube.org/accounts/framasoft/videos",
                    extractor.getOriginalUrl());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ListExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        @Override
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor);
        }

        @Test
        @Override
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(extractor);
        }
    }

    static class Channels implements BaseListExtractorTest {
        private static PeertubeChannelTabExtractor extractor;

        @BeforeAll
        static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            // setting instance might break test when running in parallel
            PeerTube.setInstance(new PeertubeInstance("https://framatube.org", "Framatube"));
            extractor = (PeertubeChannelTabExtractor) PeerTube
                    .getChannelTabExtractorFromId("accounts/framasoft", ChannelTabs.CHANNELS);
            extractor.fetchPage();
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        @Override
        public void testServiceId() {
            assertEquals(PeerTube.getServiceId(), extractor.getServiceId());
        }

        @Test
        @Override
        public void testName() throws ParsingException {
            assertEquals(ChannelTabs.CHANNELS, extractor.getName());
        }

        @Test
        @Override
        public void testId() throws ParsingException {
            assertEquals("accounts/framasoft", extractor.getId());
        }

        @Test
        @Override
        public void testUrl() throws ParsingException {
            assertEquals("https://framatube.org/accounts/framasoft/video-channels",
                    extractor.getUrl());
        }

        @Test
        @Override
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://framatube.org/accounts/framasoft/video-channels",
                    extractor.getOriginalUrl());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ListExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        @Override
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor);
        }

        @Test
        @Override
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(extractor);
        }
    }
}
