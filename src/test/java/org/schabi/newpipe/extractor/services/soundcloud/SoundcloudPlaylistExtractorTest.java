package org.schabi.newpipe.extractor.services.soundcloud;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.services.BaseListExtractorTest;
import org.schabi.newpipe.extractor.services.BasePlaylistExtractorTest;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;

/**
 * Test for {@link PlaylistExtractor}
 */
@RunWith(Enclosed.class)
public class SoundcloudPlaylistExtractorTest {
    public static class LuvTape implements BasePlaylistExtractorTest {
        private static SoundcloudPlaylistExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(Downloader.getInstance());
            extractor = (SoundcloudPlaylistExtractor) SoundCloud
                    .getPlaylistExtractor("https://soundcloud.com/liluzivert/sets/the-perfect-luv-tape-r?test=123");
            extractor.fetchPage();
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testServiceId() {
            assertEquals(SoundCloud.getServiceId(), extractor.getServiceId());
        }

        @Test
        public void testName() {
            assertEquals("THE PERFECT LUV TAPE®️", extractor.getName());
        }

        @Test
        public void testId() {
            assertEquals("246349810", extractor.getId());
        }

        @Test
        public void testCleanUrl() {
            assertEquals("https://soundcloud.com/liluzivert/sets/the-perfect-luv-tape-r", extractor.getCleanUrl());
        }

        @Test
        public void testOriginalUrl() {
            assertEquals("https://soundcloud.com/liluzivert/sets/the-perfect-luv-tape-r?test=123", extractor.getOriginalUrl());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ListExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testRelatedItems() throws Exception {
            BaseListExtractorTest.defaultTestRelatedItems(extractor, SoundCloud.getServiceId());
        }

        @Test
        public void testMoreRelatedItems() {
            try {
                BaseListExtractorTest.defaultTestMoreItems(extractor, SoundCloud.getServiceId());
            } catch (Throwable ignored) {
                return;
            }

            fail("This playlist doesn't have more items, it should throw an error");
        }

        /*//////////////////////////////////////////////////////////////////////////
        // PlaylistExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testThumbnailUrl() {
            assertIsSecureUrl(extractor.getThumbnailUrl());
        }

        @Ignore
        @Test
        public void testBannerUrl() {
            assertIsSecureUrl(extractor.getBannerUrl());
        }

        @Test
        public void testUploaderUrl() {
            final String uploaderUrl = extractor.getUploaderUrl();
            assertIsSecureUrl(uploaderUrl);
            assertTrue(uploaderUrl, uploaderUrl.contains("liluzivert"));
        }

        @Test
        public void testUploaderName() {
            assertTrue(extractor.getUploaderName().contains("LIL UZI VERT"));
        }

        @Test
        public void testUploaderAvatarUrl() {
            assertIsSecureUrl(extractor.getUploaderAvatarUrl());
        }

        @Test
        public void testStreamCount() {
            assertTrue("Error in the streams count", extractor.getStreamCount() >= 10);
        }
    }

    public static class RandomHouseDanceMusic implements BasePlaylistExtractorTest {
        private static SoundcloudPlaylistExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(Downloader.getInstance());
            extractor = (SoundcloudPlaylistExtractor) SoundCloud
                    .getPlaylistExtractor("http://soundcloud.com/finn-trapple/sets/random-house-dance-music-2");
            extractor.fetchPage();
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testServiceId() {
            assertEquals(SoundCloud.getServiceId(), extractor.getServiceId());
        }

        @Test
        public void testName() {
            assertEquals("Random House & Dance Music #2", extractor.getName());
        }

        @Test
        public void testId() {
            assertEquals("436855608", extractor.getId());
        }

        @Test
        public void testCleanUrl() {
            assertEquals("https://soundcloud.com/finn-trapple/sets/random-house-dance-music-2", extractor.getCleanUrl());
        }

        @Test
        public void testOriginalUrl() {
            assertEquals("http://soundcloud.com/finn-trapple/sets/random-house-dance-music-2", extractor.getOriginalUrl());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ListExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testRelatedItems() throws Exception {
            BaseListExtractorTest.defaultTestRelatedItems(extractor, SoundCloud.getServiceId());
        }

        @Test
        public void testMoreRelatedItems() throws Exception {
            BaseListExtractorTest.defaultTestMoreItems(extractor, SoundCloud.getServiceId());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // PlaylistExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testThumbnailUrl() {
            assertIsSecureUrl(extractor.getThumbnailUrl());
        }

        @Ignore
        @Test
        public void testBannerUrl() {
            assertIsSecureUrl(extractor.getBannerUrl());
        }

        @Test
        public void testUploaderUrl() {
            final String uploaderUrl = extractor.getUploaderUrl();
            assertIsSecureUrl(uploaderUrl);
            assertTrue(uploaderUrl, uploaderUrl.contains("finn-trapple"));
        }

        @Test
        public void testUploaderName() {
            assertEquals("Finn TrApple", extractor.getUploaderName());
        }

        @Test
        public void testUploaderAvatarUrl() {
            assertIsSecureUrl(extractor.getUploaderAvatarUrl());
        }

        @Test
        public void testStreamCount() {
            assertTrue("Error in the streams count", extractor.getStreamCount() >= 10);
        }
    }

    public static class EDMxxx implements BasePlaylistExtractorTest {
        private static SoundcloudPlaylistExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(Downloader.getInstance());
            extractor = (SoundcloudPlaylistExtractor) SoundCloud
                    .getPlaylistExtractor("https://soundcloud.com/user350509423/sets/edm-xxx");
            extractor.fetchPage();
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Additional Testing
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testGetPageInNewExtractor() throws Exception {
            final PlaylistExtractor newExtractor = SoundCloud.getPlaylistExtractor(extractor.getCleanUrl());
            BaseListExtractorTest.defaultTestGetPageInNewExtractor(extractor, newExtractor, SoundCloud.getServiceId());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testServiceId() {
            assertEquals(SoundCloud.getServiceId(), extractor.getServiceId());
        }

        @Test
        public void testName() {
            assertEquals("EDM xXx", extractor.getName());
        }

        @Test
        public void testId() {
            assertEquals("136000376", extractor.getId());
        }

        @Test
        public void testCleanUrl() {
            assertEquals("https://soundcloud.com/user350509423/sets/edm-xxx", extractor.getCleanUrl());
        }

        @Test
        public void testOriginalUrl() {
            assertEquals("https://soundcloud.com/user350509423/sets/edm-xxx", extractor.getOriginalUrl());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ListExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testRelatedItems() throws Exception {
            BaseListExtractorTest.defaultTestRelatedItems(extractor, SoundCloud.getServiceId());
        }

        @Test
        public void testMoreRelatedItems() throws Exception {
            ListExtractor.InfoItemPage<? extends InfoItem> currentPage = BaseListExtractorTest.defaultTestMoreItems(extractor, ServiceList.SoundCloud.getServiceId());
            // Test for 2 more levels
            for (int i = 0; i < 2; i++) {
                currentPage = extractor.getPage(currentPage.getNextPageUrl());
                BaseListExtractorTest.defaultTestListOfItems(SoundCloud.getServiceId(), currentPage.getItemsList(), currentPage.getErrors());
            }
        }

        /*//////////////////////////////////////////////////////////////////////////
        // PlaylistExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testThumbnailUrl() {
            assertIsSecureUrl(extractor.getThumbnailUrl());
        }

        @Ignore
        @Test
        public void testBannerUrl() {
            assertIsSecureUrl(extractor.getBannerUrl());
        }

        @Test
        public void testUploaderUrl() {
            final String uploaderUrl = extractor.getUploaderUrl();
            assertIsSecureUrl(uploaderUrl);
            assertTrue(uploaderUrl, uploaderUrl.contains("user350509423"));
        }

        @Test
        public void testUploaderName() {
            assertEquals("user350509423", extractor.getUploaderName());
        }

        @Test
        public void testUploaderAvatarUrl() {
            assertIsSecureUrl(extractor.getUploaderAvatarUrl());
        }

        @Test
        public void testStreamCount() {
            assertTrue("Error in the streams count", extractor.getStreamCount() >= 3900);
        }
    }
}
