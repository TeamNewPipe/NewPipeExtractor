package org.schabi.newpipe.extractor.services.soundcloud;

import org.hamcrest.CoreMatchers;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.services.BasePlaylistExtractorTest;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;
import static org.schabi.newpipe.extractor.services.DefaultTests.*;

/**
 * Test for {@link PlaylistExtractor}
 */
public class SoundcloudPlaylistExtractorTest {
    public static class LuvTape implements BasePlaylistExtractorTest {
        private static SoundcloudPlaylistExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
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
        public void testUrl() throws Exception {
            assertEquals("https://soundcloud.com/liluzivert/sets/the-perfect-luv-tape-r", extractor.getUrl());
        }

        @Test
        public void testOriginalUrl() throws Exception {
            assertEquals("https://soundcloud.com/liluzivert/sets/the-perfect-luv-tape-r?test=123", extractor.getOriginalUrl());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ListExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor, SoundCloud.getServiceId());
        }

        @Test
        public void testMoreRelatedItems() {
            try {
                defaultTestMoreItems(extractor, SoundCloud.getServiceId());
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

    public static class RandomHouseMusic implements BasePlaylistExtractorTest {
        private static SoundcloudPlaylistExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (SoundcloudPlaylistExtractor) SoundCloud
                    .getPlaylistExtractor("https://soundcloud.com/micky96/sets/house");
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
            assertEquals("House", extractor.getName());
        }

        @Test
        public void testId() {
            assertEquals("123062856", extractor.getId());
        }

        @Test
        public void testUrl() throws Exception {
            assertEquals("https://soundcloud.com/micky96/sets/house", extractor.getUrl());
        }

        @Test
        public void testOriginalUrl() throws Exception {
            assertEquals("https://soundcloud.com/micky96/sets/house", extractor.getOriginalUrl());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ListExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor, SoundCloud.getServiceId());
        }

        @Test
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(extractor, SoundCloud.getServiceId());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // PlaylistExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testThumbnailUrl() {
            assertIsSecureUrl(extractor.getThumbnailUrl());
        }

        @Ignore("not implemented")
        @Test
        public void testBannerUrl() {
            assertIsSecureUrl(extractor.getBannerUrl());
        }

        @Test
        public void testUploaderUrl() {
            final String uploaderUrl = extractor.getUploaderUrl();
            assertIsSecureUrl(uploaderUrl);
            assertThat(uploaderUrl, CoreMatchers.containsString("micky96"));
        }

        @Test
        public void testUploaderName() {
            assertEquals("_mickyyy", extractor.getUploaderName());
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
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (SoundcloudPlaylistExtractor) SoundCloud
                    .getPlaylistExtractor("https://soundcloud.com/user350509423/sets/edm-xxx");
            extractor.fetchPage();
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Additional Testing
        //////////////////////////////////////////////////////////////////////////*/

        @Ignore
        @Test
        public void testGetPageInNewExtractor() throws Exception {
            final PlaylistExtractor newExtractor = SoundCloud.getPlaylistExtractor(extractor.getUrl());
            defaultTestGetPageInNewExtractor(extractor, newExtractor, SoundCloud.getServiceId());
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
        public void testUrl() throws Exception {
            assertEquals("https://soundcloud.com/user350509423/sets/edm-xxx", extractor.getUrl());
        }

        @Test
        public void testOriginalUrl() throws Exception {
            assertEquals("https://soundcloud.com/user350509423/sets/edm-xxx", extractor.getOriginalUrl());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ListExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Ignore
        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor, SoundCloud.getServiceId());
        }

        //TODO: FUCK THIS: This triggers a 500 at sever
        @Ignore
        @Test
        public void testMoreRelatedItems() throws Exception {
            ListExtractor.InfoItemsPage<StreamInfoItem> currentPage = defaultTestMoreItems(extractor, ServiceList.SoundCloud.getServiceId());
            // Test for 2 more levels
            for (int i = 0; i < 2; i++) {
                currentPage = extractor.getPage(currentPage.getNextPageUrl());
                defaultTestListOfItems(SoundCloud.getServiceId(), currentPage.getItems(), currentPage.getErrors());
            }
        }

        /*//////////////////////////////////////////////////////////////////////////
        // PlaylistExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Ignore
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
