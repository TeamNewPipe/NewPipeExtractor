package org.schabi.newpipe.extractor.services.niconico;

import static org.junit.Assert.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.Niconico;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.services.BaseChannelExtractorTest;
import org.schabi.newpipe.extractor.services.niconico.extractors.NiconicoUserExtractor;

public class NiconicoUserExtractorTest {
    public static class U8420196 implements BaseChannelExtractorTest {
        private static NiconicoUserExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (NiconicoUserExtractor) Niconico
                    .getChannelExtractor("https://www.nicovideo.jp/user/8420196");
            extractor.fetchPage();
        }

        @Override
        @Test
        public void testDescription() throws Exception {

        }

        @Override
        @Test
        public void testAvatarUrl() throws Exception {

        }

        @Override
        @Test
        public void testBannerUrl() throws Exception {

        }

        @Override
        @Test
        public void testFeedUrl() throws Exception {

        }

        @Override
        @Test
        public void testSubscriberCount() throws Exception {

        }

        @Override
        @Test
        public void testVerified() throws Exception {

        }

        @Override
        @Test
        public void testServiceId() throws Exception {

        }

        @Override
        @Test
        public void testName() throws Exception {
            assertEquals("零～ゼロ～", extractor.getName());
        }

        @Override
        @Test
        public void testId() throws Exception {
            assertEquals("8420196", extractor.getId());
        }

        @Override
        @Test
        public void testUrl() throws Exception {
            assertEquals("https://www.nicovideo.jp/user/8420196", extractor.getUrl());
        }

        @Override
        @Test
        public void testOriginalUrl() throws Exception {
            assertEquals("https://www.nicovideo.jp/user/8420196", extractor.getUrl());
        }

        @Override
        @Test
        public void testRelatedItems() throws Exception {

        }

        @Override
        @Test
        public void testMoreRelatedItems() throws Exception {

        }
    }
}
