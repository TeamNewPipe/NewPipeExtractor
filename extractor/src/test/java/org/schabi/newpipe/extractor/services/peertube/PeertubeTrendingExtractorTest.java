package org.schabi.newpipe.extractor.services.peertube;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.BaseListExtractorTest;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeTrendingExtractor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.*;

public class PeertubeTrendingExtractorTest {

    public static class Trending implements BaseListExtractorTest {
        private static PeertubeTrendingExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            // setting instance might break test when running in parallel
            PeerTube.setInstance(new PeertubeInstance("https://framatube.org", "Framatube"));
            extractor = (PeertubeTrendingExtractor) PeerTube.getKioskList()
                    .getExtractorById("Trending", null);
            extractor.fetchPage();
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testServiceId() {
            assertEquals(PeerTube.getServiceId(), extractor.getServiceId());
        }

        @Test
        public void testName() throws Exception {
            assertEquals("Trending", extractor.getName());
        }

        @Test
        public void testId() throws Exception {
            assertEquals("Trending", extractor.getId());
        }

        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://framatube.org/api/v1/videos?sort=-trending", extractor.getUrl());
        }

        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://framatube.org/api/v1/videos?sort=-trending", extractor.getOriginalUrl());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ListExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor);
        }

        @Test
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(extractor);
        }
    }
}
