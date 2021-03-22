package org.schabi.newpipe.extractor.services.youtube;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.downloader.DownloaderFactory;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.AccountTerminatedException;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.BaseListExtractorTest;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeFeedExtractor;

import java.io.IOException;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.assertNoMoreItems;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestRelatedItems;

public class YoutubeFeedExtractorTest {

    private static final String RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/feed/";

    public static class Kurzgesagt implements BaseListExtractorTest {
        private static YoutubeFeedExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            YoutubeParsingHelper.resetClientVersionAndKey();
            YoutubeParsingHelper.setNumberGenerator(new Random(1));
            NewPipe.init(new DownloaderFactory().getDownloader(RESOURCE_PATH));
            extractor = (YoutubeFeedExtractor) YouTube
                    .getFeedExtractor("https://www.youtube.com/user/Kurzgesagt");
            extractor.fetchPage();
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testServiceId() {
            assertEquals(YouTube.getServiceId(), extractor.getServiceId());
        }

        @Test
        public void testName() {
            String name = extractor.getName();
            assertTrue(name, name.startsWith("Kurzgesagt"));
        }

        @Test
        public void testId() {
            assertEquals("UCsXVk37bltHxD1rDPwtNM8Q", extractor.getId());
        }

        @Test
        public void testUrl() {
            assertEquals("https://www.youtube.com/channel/UCsXVk37bltHxD1rDPwtNM8Q", extractor.getUrl());
        }

        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/user/Kurzgesagt", extractor.getOriginalUrl());
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
            assertNoMoreItems(extractor);
        }
    }

    public static class NotAvailable {

        @BeforeClass
        public static void setUp() throws IOException {
            NewPipe.init(new DownloaderFactory().getDownloader(RESOURCE_PATH + "notAvailable/"));
        }

        @Test(expected = ContentNotAvailableException.class)
        public void AccountTerminatedFetch() throws Exception {
            YoutubeFeedExtractor extractor = (YoutubeFeedExtractor) YouTube
                    .getFeedExtractor("https://www.youtube.com/channel/UCTGjY2I-ZUGnwVoWAGRd7XQ");
            extractor.fetchPage();
        }

    }
}