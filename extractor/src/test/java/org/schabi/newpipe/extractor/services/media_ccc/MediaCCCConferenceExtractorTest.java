package org.schabi.newpipe.extractor.services.media_ccc;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCConferenceExtractor;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.MediaCCC;

/**
 * Test {@link MediaCCCConferenceExtractor}
 */
public class MediaCCCConferenceExtractorTest {
    public static class FrOSCon2017 {
        private static MediaCCCConferenceExtractor extractor;

        @BeforeClass
        public static void setUpClass() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (MediaCCCConferenceExtractor) MediaCCC.getChannelExtractor("https://media.ccc.de/c/froscon2017");
            extractor.fetchPage();
        }

        @Test
        public void testName() throws Exception {
            assertEquals("FrOSCon 2017", extractor.getName());
        }

        @Test
        public void testGetUrl() throws Exception {
            assertEquals("https://media.ccc.de/public/conferences/froscon2017", extractor.getUrl());
        }

        @Test
        public void testGetOriginalUrl() throws Exception {
            assertEquals("https://media.ccc.de/c/froscon2017", extractor.getOriginalUrl());
        }

        @Test
        public void testGetThumbnailUrl() throws Exception {
            assertEquals("https://static.media.ccc.de/media/events/froscon/2017/logo.png", extractor.getAvatarUrl());
        }

        @Test
        public void testGetInitalPage() throws Exception {
            assertEquals(97, extractor.getInitialPage().getItems().size());
        }
    }

    public static class Oscal2019 {
        private static MediaCCCConferenceExtractor extractor;

        @BeforeClass
        public static void setUpClass() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (MediaCCCConferenceExtractor) MediaCCC.getChannelExtractor("https://media.ccc.de/c/oscal19");
            extractor.fetchPage();
        }

        @Test
        public void testName() throws Exception {
            assertEquals("Open Source Conference Albania 2019", extractor.getName());
        }

        @Test
        public void testGetUrl() throws Exception {
            assertEquals("https://media.ccc.de/public/conferences/oscal19", extractor.getUrl());
        }

        @Test
        public void testGetOriginalUrl() throws Exception {
            assertEquals("https://media.ccc.de/c/oscal19", extractor.getOriginalUrl());
        }

        @Test
        public void testGetThumbnailUrl() throws Exception {
            assertEquals("https://static.media.ccc.de/media/events/oscal/2019/oscal-19.png", extractor.getAvatarUrl());
        }

        @Test
        public void testGetInitalPage() throws Exception {
            assertTrue(extractor.getInitialPage().getItems().size() >= 21);
        }
    }
}
