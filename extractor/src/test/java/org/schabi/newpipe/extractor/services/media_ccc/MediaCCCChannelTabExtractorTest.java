package org.schabi.newpipe.extractor.services.media_ccc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.MediaCCC;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;

/**
 * Test that it is possible to create and use a channel tab extractor ({@link
 * org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCChannelTabExtractor}) without
 * passing through the conference extractor
 */
public class MediaCCCChannelTabExtractorTest {
    public static class CCCamp2023 {
        private static ChannelTabExtractor extractor;

        @BeforeAll
        public static void setUpClass() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = MediaCCC.getChannelTabExtractorFromId("camp2023", ChannelTabs.VIDEOS);
            extractor.fetchPage();
        }

        @Test
        void testName() {
            assertEquals(ChannelTabs.VIDEOS, extractor.getName());
        }

        @Test
        void testGetUrl() throws Exception {
            assertEquals("https://media.ccc.de/c/camp2023", extractor.getUrl());
        }

        @Test
        void testGetOriginalUrl() throws Exception {
            assertEquals("https://media.ccc.de/c/camp2023", extractor.getOriginalUrl());
        }

        @Test
        void testGetInitalPage() throws Exception {
            assertEquals(177, extractor.getInitialPage().getItems().size());
        }
    }
}
