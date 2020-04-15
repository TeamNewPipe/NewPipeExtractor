package org.schabi.newpipe.extractor.services.youtube;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;

import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.services.BasePlaylistExtractorTest;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubePlaylistExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.*;

/**
 * Test for {@link YoutubePlaylistExtractor}
 */
public class YoutubePlaylistExtractorTest {

    public static class NotAvailable {
        @BeforeClass
        public static void setUp() {
            NewPipe.init(DownloaderTestImpl.getInstance());
        }

        @Test(expected = ContentNotAvailableException.class)
        public void nonExistentFetch() throws Exception {
            final PlaylistExtractor extractor =
                    YouTube.getPlaylistExtractor("https://www.youtube.com/playlist?list=PL11111111111111111111111111111111");
            extractor.fetchPage();
        }

        @Test(expected = ContentNotAvailableException.class)
        public void invalidId() throws Exception {
            final PlaylistExtractor extractor =
                    YouTube.getPlaylistExtractor("https://www.youtube.com/playlist?list=INVALID_ID");
            extractor.fetchPage();
        }
    }

    public static class TimelessPopHits implements BasePlaylistExtractorTest {
        private static YoutubePlaylistExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (YoutubePlaylistExtractor) YouTube
                    .getPlaylistExtractor("http://www.youtube.com/watch?v=lp-EO5I60KA&list=PLMC9KNkIncKtPzgY-5rmhvj7fax8fdxoj");
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
        public void testName() throws Exception {
            String name = extractor.getName();
            assertTrue(name, name.startsWith("Pop Music Playlist"));
        }

        @Test
        public void testId() throws Exception {
            assertEquals("PLMC9KNkIncKtPzgY-5rmhvj7fax8fdxoj", extractor.getId());
        }

        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/playlist?list=PLMC9KNkIncKtPzgY-5rmhvj7fax8fdxoj", extractor.getUrl());
        }

        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("http://www.youtube.com/watch?v=lp-EO5I60KA&list=PLMC9KNkIncKtPzgY-5rmhvj7fax8fdxoj", extractor.getOriginalUrl());
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
        public void testThumbnailUrl() throws Exception {
            final String thumbnailUrl = extractor.getThumbnailUrl();
            assertIsSecureUrl(thumbnailUrl);
            assertTrue(thumbnailUrl, thumbnailUrl.contains("yt"));
        }

        @Ignore
        @Test
        public void testBannerUrl() throws Exception {
            final String bannerUrl = extractor.getBannerUrl();
            assertIsSecureUrl(bannerUrl);
            assertTrue(bannerUrl, bannerUrl.contains("yt"));
        }

        @Test
        public void testUploaderUrl() throws Exception {
            assertEquals("https://www.youtube.com/channel/UCs72iRpTEuwV3y6pdWYLgiw", extractor.getUploaderUrl());
        }

        @Test
        public void testUploaderName() throws Exception {
            final String uploaderName = extractor.getUploaderName();
            assertTrue(uploaderName, uploaderName.contains("Just Hits"));
        }

        @Test
        public void testUploaderAvatarUrl() throws Exception {
            final String uploaderAvatarUrl = extractor.getUploaderAvatarUrl();
            assertTrue(uploaderAvatarUrl, uploaderAvatarUrl.contains("yt"));
        }

        @Test
        public void testStreamCount() throws Exception {
            assertTrue("Error in the streams count", extractor.getStreamCount() > 100);
        }
    }

    public static class HugePlaylist implements BasePlaylistExtractorTest {
        private static YoutubePlaylistExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (YoutubePlaylistExtractor) YouTube
                    .getPlaylistExtractor("https://www.youtube.com/watch?v=8SbUC-UaAxE&list=PLWwAypAcFRgKAIIFqBr9oy-ZYZnixa_Fj");
            extractor.fetchPage();
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Additional Testing
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testGetPageInNewExtractor() throws Exception {
            final PlaylistExtractor newExtractor = YouTube.getPlaylistExtractor(extractor.getUrl());
            defaultTestGetPageInNewExtractor(extractor, newExtractor);
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testServiceId() {
            assertEquals(YouTube.getServiceId(), extractor.getServiceId());
        }

        @Test
        public void testName() throws Exception {
            final String name = extractor.getName();
            assertEquals("I Wanna Rock Super Gigantic Playlist 1: Hardrock, AOR, Metal and more !!! 5000 music videos !!!", name);
        }

        @Test
        public void testId() throws Exception {
            assertEquals("PLWwAypAcFRgKAIIFqBr9oy-ZYZnixa_Fj", extractor.getId());
        }

        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/playlist?list=PLWwAypAcFRgKAIIFqBr9oy-ZYZnixa_Fj", extractor.getUrl());
        }

        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/watch?v=8SbUC-UaAxE&list=PLWwAypAcFRgKAIIFqBr9oy-ZYZnixa_Fj", extractor.getOriginalUrl());
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

            // test for 2 more levels
            for (int i = 0; i < 2; i++) {
                currentPage = extractor.getPage(currentPage.getNextPage());
                defaultTestListOfItems(YouTube, currentPage.getItems(), currentPage.getErrors());
            }
        }

        /*//////////////////////////////////////////////////////////////////////////
        // PlaylistExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testThumbnailUrl() throws Exception {
            final String thumbnailUrl = extractor.getThumbnailUrl();
            assertIsSecureUrl(thumbnailUrl);
            assertTrue(thumbnailUrl, thumbnailUrl.contains("yt"));
        }

        @Ignore
        @Test
        public void testBannerUrl() throws Exception {
            final String bannerUrl = extractor.getBannerUrl();
            assertIsSecureUrl(bannerUrl);
            assertTrue(bannerUrl, bannerUrl.contains("yt"));
        }

        @Test
        public void testUploaderUrl() throws Exception {
            assertEquals("https://www.youtube.com/channel/UCHSPWoY1J5fbDVbcnyeqwdw", extractor.getUploaderUrl());
        }

        @Test
        public void testUploaderName() throws Exception {
            assertEquals("Tomas Nilsson TOMPA571", extractor.getUploaderName());
        }

        @Test
        public void testUploaderAvatarUrl() throws Exception {
            final String uploaderAvatarUrl = extractor.getUploaderAvatarUrl();
            assertTrue(uploaderAvatarUrl, uploaderAvatarUrl.contains("yt"));
        }

        @Test
        public void testStreamCount() throws Exception {
            assertTrue("Error in the streams count", extractor.getStreamCount() > 100);
        }
    }

    public static class LearningPlaylist implements BasePlaylistExtractorTest {
        private static YoutubePlaylistExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (YoutubePlaylistExtractor) YouTube
                    .getPlaylistExtractor("https://www.youtube.com/playlist?list=PL8dPuuaLjXtOAKed_MxxWBNaPno5h3Zs8");
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
        public void testName() throws Exception {
            String name = extractor.getName();
            assertTrue(name, name.startsWith("Anatomy & Physiology"));
        }

        @Test
        public void testId() throws Exception {
            assertEquals("PL8dPuuaLjXtOAKed_MxxWBNaPno5h3Zs8", extractor.getId());
        }

        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/playlist?list=PL8dPuuaLjXtOAKed_MxxWBNaPno5h3Zs8", extractor.getUrl());
        }

        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/playlist?list=PL8dPuuaLjXtOAKed_MxxWBNaPno5h3Zs8", extractor.getOriginalUrl());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ListExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor);
        }

        @Ignore
        @Test
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(extractor);
        }

        /*//////////////////////////////////////////////////////////////////////////
        // PlaylistExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testThumbnailUrl() throws Exception {
            final String thumbnailUrl = extractor.getThumbnailUrl();
            assertIsSecureUrl(thumbnailUrl);
            assertTrue(thumbnailUrl, thumbnailUrl.contains("yt"));
        }

        @Ignore
        @Test
        public void testBannerUrl() throws Exception {
            final String bannerUrl = extractor.getBannerUrl();
            assertIsSecureUrl(bannerUrl);
            assertTrue(bannerUrl, bannerUrl.contains("yt"));
        }

        @Test
        public void testUploaderUrl() throws Exception {
            assertEquals("https://www.youtube.com/channel/UCX6b17PVsYBQ0ip5gyeme-Q", extractor.getUploaderUrl());
        }

        @Test
        public void testUploaderName() throws Exception {
            final String uploaderName = extractor.getUploaderName();
            assertTrue(uploaderName, uploaderName.contains("CrashCourse"));
        }

        @Test
        public void testUploaderAvatarUrl() throws Exception {
            final String uploaderAvatarUrl = extractor.getUploaderAvatarUrl();
            assertTrue(uploaderAvatarUrl, uploaderAvatarUrl.contains("yt"));
        }

        @Test
        public void testStreamCount() throws Exception {
            assertTrue("Error in the streams count", extractor.getStreamCount() > 40);
        }
    }
}
