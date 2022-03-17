package org.schabi.newpipe.extractor.services.soundcloud;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.ExtractorAsserts;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.services.BasePlaylistExtractorTest;
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudPlaylistExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import static org.junit.jupiter.api.Assertions.*;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertEmpty;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;
import static org.schabi.newpipe.extractor.services.DefaultTests.*;

/**
 * Test for {@link PlaylistExtractor}
 */
public class SoundcloudPlaylistExtractorTest {
    public static class LuvTape implements BasePlaylistExtractorTest {
        private static SoundcloudPlaylistExtractor extractor;

        @BeforeAll
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
            defaultTestRelatedItems(extractor);
        }

        @Test
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(extractor);
        }

        /*//////////////////////////////////////////////////////////////////////////
        // PlaylistExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testThumbnailUrl() {
            assertIsSecureUrl(extractor.getThumbnailUrl());
        }

        @Test
        public void testBannerUrl() throws ParsingException {
            // SoundCloud playlists do not have a banner
            assertEmpty(extractor.getBannerUrl());
        }

        @Test
        void testUploaderUrl() {
            final String uploaderUrl = extractor.getUploaderUrl();
            assertIsSecureUrl(uploaderUrl);
            ExtractorAsserts.assertContains("liluzivert", uploaderUrl);
        }

        @Test
        public void testUploaderName() {
            assertTrue(extractor.getUploaderName().contains("Lil Uzi Vert"));
        }

        @Test
        public void testUploaderAvatarUrl() {
            assertIsSecureUrl(extractor.getUploaderAvatarUrl());
        }

        @Test
        public void testStreamCount() {
            ExtractorAsserts.assertGreaterOrEqual(10, extractor.getStreamCount());
        }

        @Test
        public void testUploaderVerified() throws Exception {
            assertTrue(extractor.isUploaderVerified());
        }
    }

    public static class RandomHouseMusic implements BasePlaylistExtractorTest {
        private static SoundcloudPlaylistExtractor extractor;

        @BeforeAll
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
            defaultTestRelatedItems(extractor);
        }

        @Test
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(extractor);
        }

        /*//////////////////////////////////////////////////////////////////////////
        // PlaylistExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testThumbnailUrl() {
            assertIsSecureUrl(extractor.getThumbnailUrl());
        }

        @Test
        public void testBannerUrl() throws ParsingException {
            // SoundCloud playlists do not have a banner
            assertEmpty(extractor.getBannerUrl());
        }

        @Test
        void testUploaderUrl() {
            final String uploaderUrl = extractor.getUploaderUrl();
            assertIsSecureUrl(uploaderUrl);
            ExtractorAsserts.assertContains("micky96", uploaderUrl);
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
            ExtractorAsserts.assertGreaterOrEqual(10, extractor.getStreamCount());
        }

        @Test
        public void testUploaderVerified() throws Exception {
            assertFalse(extractor.isUploaderVerified());
        }
    }

    public static class EDMxxx implements BasePlaylistExtractorTest {
        private static SoundcloudPlaylistExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (SoundcloudPlaylistExtractor) SoundCloud
                    .getPlaylistExtractor("https://soundcloud.com/user350509423/sets/edm-xxx");
            extractor.fetchPage();
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Additional Testing
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        void testGetPageInNewExtractor() throws Exception {
            PlaylistExtractor newExtractor = SoundCloud.getPlaylistExtractor(extractor.getUrl());
            defaultTestGetPageInNewExtractor(extractor, newExtractor);
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

        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor);
        }

        @Test
        public void testMoreRelatedItems() throws Exception {
            ListExtractor.InfoItemsPage<StreamInfoItem> currentPage = defaultTestMoreItems(extractor);
            // Test for 2 more levels
            for (int i = 0; i < 2; i++) {
                currentPage = extractor.getPage(currentPage.getNextPage());
                defaultTestListOfItems(SoundCloud, currentPage.getItems(), currentPage.getErrors());
            }
        }

        /*//////////////////////////////////////////////////////////////////////////
        // PlaylistExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testThumbnailUrl() {
            assertIsSecureUrl(extractor.getThumbnailUrl());
        }

        @Test
        public void testBannerUrl() throws ParsingException {
            // SoundCloud playlists do not have a banner
            assertEmpty(extractor.getBannerUrl());
        }

        @Test
        void testUploaderUrl() {
            final String uploaderUrl = extractor.getUploaderUrl();
            assertIsSecureUrl(uploaderUrl);
            ExtractorAsserts.assertContains("user350509423", uploaderUrl);
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
            ExtractorAsserts.assertGreaterOrEqual(370, extractor.getStreamCount());
        }

        @Test
        public void testUploaderVerified() throws Exception {
            assertFalse(extractor.isUploaderVerified());
        }
    }

    public static class SmallPlaylist implements BasePlaylistExtractorTest {
        private static SoundcloudPlaylistExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (SoundcloudPlaylistExtractor) SoundCloud
                    .getPlaylistExtractor("https://soundcloud.com/breezy-123/sets/empty-playlist?test=123");
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
            assertEquals("EMPTY PLAYLIST", extractor.getName());
        }

        @Test
        public void testId() {
            assertEquals("23483459", extractor.getId());
        }

        @Test
        public void testUrl() throws Exception {
            assertEquals("https://soundcloud.com/breezy-123/sets/empty-playlist", extractor.getUrl());
        }

        @Test
        public void testOriginalUrl() throws Exception {
            assertEquals("https://soundcloud.com/breezy-123/sets/empty-playlist?test=123", extractor.getOriginalUrl());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ListExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor);
        }

        @Test
        @Disabled("Test broken? Playlist has 2 entries, each page has 1 entry meaning it has 2 pages.")
        public void testMoreRelatedItems() throws Exception {
            try {
                defaultTestMoreItems(extractor);
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

        @Test
        public void testBannerUrl() throws ParsingException {
            // SoundCloud playlists do not have a banner
            assertEmpty(extractor.getBannerUrl());
        }

        @Test
        void testUploaderUrl() {
            final String uploaderUrl = extractor.getUploaderUrl();
            assertIsSecureUrl(uploaderUrl);
            ExtractorAsserts.assertContains("breezy-123", uploaderUrl);
        }

        @Test
        public void testUploaderName() {
            assertEquals("breezy-123", extractor.getUploaderName());
        }

        @Test
        public void testUploaderAvatarUrl() {
            assertIsSecureUrl(extractor.getUploaderAvatarUrl());
        }

        @Test
        public void testStreamCount() {
            assertEquals(2, extractor.getStreamCount());
        }

        @Test
        public void testUploaderVerified() throws Exception {
            assertFalse(extractor.isUploaderVerified());
        }
    }
}
