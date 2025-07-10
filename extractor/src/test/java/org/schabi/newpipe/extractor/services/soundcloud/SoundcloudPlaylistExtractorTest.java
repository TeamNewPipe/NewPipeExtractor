package org.schabi.newpipe.extractor.services.soundcloud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertEmpty;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestGetPageInNewExtractor;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestImageCollection;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestListOfItems;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestMoreItems;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestRelatedItems;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.ExtractorAsserts;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.services.BasePlaylistExtractorTest;
import org.schabi.newpipe.extractor.services.DefaultSimpleExtractorTest;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

/**
 * Test for {@link PlaylistExtractor}
 */
public class SoundcloudPlaylistExtractorTest {
    static abstract class Base extends DefaultSimpleExtractorTest<PlaylistExtractor>
        implements BasePlaylistExtractorTest {

        @Override
        protected PlaylistExtractor createExtractor() throws Exception {
            return SoundCloud.getPlaylistExtractor(extractorUrl());
        }

        protected abstract String extractorUrl();
    }

    public static class LuvTape extends Base {
        @Override
        protected String extractorUrl() {
            return "https://soundcloud.com/liluzivert/sets/the-perfect-luv-tape-r?test=123";
        }

        @Override
        @Test
        public void testServiceId() {
            assertEquals(SoundCloud.getServiceId(), extractor().getServiceId());
        }

        @Override
        @Test
        public void testName() throws ParsingException {
            assertEquals("THE PERFECT LUV TAPE®️", extractor().getName());
        }

        @Override
        @Test
        public void testId() throws ParsingException {
            assertEquals("246349810", extractor().getId());
        }

        @Override
        @Test
        public void testUrl() throws Exception {
            assertEquals("https://soundcloud.com/liluzivert/sets/the-perfect-luv-tape-r", extractor().getUrl());
        }

        @Override
        @Test
        public void testOriginalUrl() throws Exception {
            assertEquals("https://soundcloud.com/liluzivert/sets/the-perfect-luv-tape-r?test=123", extractor().getOriginalUrl());
        }

        @Override
        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor());
        }

        @Override
        @Test
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(extractor());
        }

        @Override
        @Test
        public void testThumbnails() throws ParsingException {
            defaultTestImageCollection(extractor().getThumbnails());
        }

        @Override
        @Test
        public void testBanners() throws ParsingException {
            // SoundCloud playlists do not have a banner
            assertEmpty(extractor().getBanners());
        }

        @Test
        void testUploaderUrl() throws ParsingException {
            final String uploaderUrl = extractor().getUploaderUrl();
            assertIsSecureUrl(uploaderUrl);
            ExtractorAsserts.assertContains("liluzivert", uploaderUrl);
        }

        @Override
        @Test
        public void testUploaderName() throws ParsingException {
            assertTrue(extractor().getUploaderName().contains("Lil Uzi Vert"));
        }

        @Override
        @Test
        public void testUploaderAvatars() throws ParsingException {
            defaultTestImageCollection(extractor().getUploaderAvatars());
        }

        @Override
        @Test
        public void testStreamCount() throws ParsingException {
            ExtractorAsserts.assertGreaterOrEqual(10, extractor().getStreamCount());
        }

        @Override
        @Test
        public void testUploaderVerified() throws Exception {
            assertTrue(extractor().isUploaderVerified());
        }
    }

    public static class RandomHouseMusic extends Base {
        @Override
        protected String extractorUrl() {
            return "https://soundcloud.com/micky96/sets/house";
        }

        @Override
        @Test
        public void testServiceId() {
            assertEquals(SoundCloud.getServiceId(), extractor().getServiceId());
        }

        @Override
        @Test
        public void testName() throws ParsingException {
            assertEquals("House", extractor().getName());
        }

        @Override
        @Test
        public void testId() throws ParsingException {
            assertEquals("123062856", extractor().getId());
        }

        @Override
        @Test
        public void testUrl() throws Exception {
            assertEquals("https://soundcloud.com/micky96/sets/house", extractor().getUrl());
        }

        @Override
        @Test
        public void testOriginalUrl() throws Exception {
            assertEquals("https://soundcloud.com/micky96/sets/house", extractor().getOriginalUrl());
        }

        @Override
        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor());
        }

        @Override
        @Test
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(extractor());
        }

        @Override
        @Test
        public void testThumbnails() throws ParsingException {
            defaultTestImageCollection(extractor().getThumbnails());
        }

        @Override
        @Test
        public void testBanners() throws ParsingException {
            // SoundCloud playlists do not have a banner
            assertEmpty(extractor().getBanners());
        }

        @Test
        void testUploaderUrl() throws ParsingException {
            final String uploaderUrl = extractor().getUploaderUrl();
            assertIsSecureUrl(uploaderUrl);
            ExtractorAsserts.assertContains("micky96", uploaderUrl);
        }

        @Override
        @Test
        public void testUploaderName() throws ParsingException {
            assertEquals("_mickyyy", extractor().getUploaderName());
        }

        @Override
        @Test
        public void testUploaderAvatars() throws ParsingException {
            defaultTestImageCollection(extractor().getUploaderAvatars());
        }

        @Override
        @Test
        public void testStreamCount() throws ParsingException {
            ExtractorAsserts.assertGreaterOrEqual(10, extractor().getStreamCount());
        }

        @Override
        @Test
        public void testUploaderVerified() throws Exception {
            assertFalse(extractor().isUploaderVerified());
        }
    }

    public static class EDMxxx extends Base {
        @Override
        protected String extractorUrl() {
            return "https://soundcloud.com/user350509423/sets/edm-xxx";
        }

        @Test
        void testGetPageInNewExtractor() throws Exception {
            final PlaylistExtractor newExtractor = SoundCloud.getPlaylistExtractor(extractor().getUrl());
            defaultTestGetPageInNewExtractor(extractor(), newExtractor);
        }

        @Override
        @Test
        public void testServiceId() {
            assertEquals(SoundCloud.getServiceId(), extractor().getServiceId());
        }

        @Override
        @Test
        public void testName() throws ParsingException {
            assertEquals("EDM xXx", extractor().getName());
        }

        @Override
        @Test
        public void testId() throws ParsingException {
            assertEquals("136000376", extractor().getId());
        }

        @Override
        @Test
        public void testUrl() throws Exception {
            assertEquals("https://soundcloud.com/user350509423/sets/edm-xxx", extractor().getUrl());
        }

        @Override
        @Test
        public void testOriginalUrl() throws Exception {
            assertEquals("https://soundcloud.com/user350509423/sets/edm-xxx", extractor().getOriginalUrl());
        }

        @Override
        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor());
        }

        @Override
        @Test
        public void testMoreRelatedItems() throws Exception {
            ListExtractor.InfoItemsPage<StreamInfoItem> currentPage = defaultTestMoreItems(extractor());
            // Test for 2 more levels
            for (int i = 0; i < 2; i++) {
                currentPage = extractor().getPage(currentPage.getNextPage());
                defaultTestListOfItems(SoundCloud, currentPage.getItems(), currentPage.getErrors());
            }
        }


        @Override
        @Test
        public void testThumbnails() throws ParsingException {
            defaultTestImageCollection(extractor().getThumbnails());
        }

        @Override
        @Test
        public void testBanners() throws ParsingException {
            // SoundCloud playlists do not have a banner
            assertEmpty(extractor().getBanners());
        }

        @Test
        void testUploaderUrl() throws ParsingException {
            final String uploaderUrl = extractor().getUploaderUrl();
            assertIsSecureUrl(uploaderUrl);
            ExtractorAsserts.assertContains("user350509423", uploaderUrl);
        }

        @Override
        @Test
        public void testUploaderName() throws ParsingException {
            assertEquals("Chaazyy", extractor().getUploaderName());
        }

        @Override
        @Test
        public void testUploaderAvatars() throws ParsingException {
            defaultTestImageCollection(extractor().getUploaderAvatars());
        }

        @Override
        @Test
        public void testStreamCount() throws ParsingException {
            ExtractorAsserts.assertGreaterOrEqual(370, extractor().getStreamCount());
        }

        @Override
        @Test
        public void testUploaderVerified() throws Exception {
            assertFalse(extractor().isUploaderVerified());
        }
    }

    public static class SmallPlaylist extends Base {
        @Override
        protected String extractorUrl() {
            return "https://soundcloud.com/breezy-123/sets/empty-playlist?test=123";
        }

        @Override
        @Test
        public void testServiceId() {
            assertEquals(SoundCloud.getServiceId(), extractor().getServiceId());
        }

        @Override
        @Test
        public void testName() throws ParsingException {
            assertEquals("EMPTY PLAYLIST", extractor().getName());
        }

        @Override
        @Test
        public void testId() throws ParsingException {
            assertEquals("23483459", extractor().getId());
        }

        @Override
        @Test
        public void testUrl() throws Exception {
            assertEquals("https://soundcloud.com/breezy-123/sets/empty-playlist", extractor().getUrl());
        }

        @Override
        @Test
        public void testOriginalUrl() throws Exception {
            assertEquals("https://soundcloud.com/breezy-123/sets/empty-playlist?test=123", extractor().getOriginalUrl());
        }

        @Override
        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor());
        }

        @Override
        @Test
        @Disabled("Test broken? Playlist has 2 entries, each page has 1 entry meaning it has 2 pages.")
        public void testMoreRelatedItems() throws Exception {
            try {
                defaultTestMoreItems(extractor());
            } catch (final Throwable ignored) {
                return;
            }

            fail("This playlist doesn't have more items, it should throw an error");
        }

        @Override
        @Test
        public void testThumbnails() throws ParsingException {
            defaultTestImageCollection(extractor().getThumbnails());
        }

        @Override
        @Test
        public void testBanners() throws ParsingException {
            // SoundCloud playlists do not have a banner
            assertEmpty(extractor().getBanners());
        }

        @Test
        void testUploaderUrl() throws ParsingException {
            final String uploaderUrl = extractor().getUploaderUrl();
            assertIsSecureUrl(uploaderUrl);
            ExtractorAsserts.assertContains("breezy-123", uploaderUrl);
        }

        @Override
        @Test
        public void testUploaderName() throws ParsingException {
            assertEquals("breezy-123", extractor().getUploaderName());
        }

        @Override
        @Test
        public void testUploaderAvatars() throws ParsingException {
            defaultTestImageCollection(extractor().getUploaderAvatars());
        }

        @Override
        @Test
        public void testStreamCount() throws ParsingException {
            assertEquals(2, extractor().getStreamCount());
        }

        @Override
        @Test
        public void testUploaderVerified() throws Exception {
            assertFalse(extractor().isUploaderVerified());
        }
    }
}
