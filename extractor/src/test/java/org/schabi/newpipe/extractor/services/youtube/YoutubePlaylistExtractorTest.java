package org.schabi.newpipe.extractor.services.youtube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.assertNoMoreItems;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestGetPageInNewExtractor;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestListOfItems;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestMoreItems;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestRelatedItems;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderFactory;
import org.schabi.newpipe.extractor.ExtractorAsserts;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.playlist.PlaylistInfo;
import org.schabi.newpipe.extractor.services.BasePlaylistExtractorTest;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubePlaylistExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.io.IOException;

/**
 * Test for {@link YoutubePlaylistExtractor}
 */
public class YoutubePlaylistExtractorTest {

    private static final String RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/playlist/";

    public static class NotAvailable {
        @BeforeAll
        public static void setUp() throws IOException {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "notAvailable"));
        }

        @Test
        void nonExistentFetch() throws Exception {
            final PlaylistExtractor extractor =
                    YouTube.getPlaylistExtractor("https://www.youtube.com/playlist?list=PL11111111111111111111111111111111");
            assertThrows(ContentNotAvailableException.class, extractor::fetchPage);
        }

        @Test
        void invalidId() throws Exception {
            final PlaylistExtractor extractor =
                    YouTube.getPlaylistExtractor("https://www.youtube.com/playlist?list=INVALID_ID");
            assertThrows(ContentNotAvailableException.class, extractor::fetchPage);
        }
    }

    public static class TimelessPopHits implements BasePlaylistExtractorTest {
        private static YoutubePlaylistExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "TimelessPopHits"));
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
            assertTrue(extractor.getName().startsWith("Pop Music Playlist"));
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
            ExtractorAsserts.assertContains("yt", thumbnailUrl);
        }

        @Disabled
        @Test
        public void testBannerUrl() throws ParsingException {
            final String bannerUrl = extractor.getBannerUrl();
            assertIsSecureUrl(bannerUrl);
            ExtractorAsserts.assertContains("yt", bannerUrl);
        }

        @Test
        public void testUploaderUrl() throws Exception {
            assertEquals("https://www.youtube.com/channel/UCs72iRpTEuwV3y6pdWYLgiw", extractor.getUploaderUrl());
        }

        @Test
        public void testUploaderName() throws Exception {
            final String uploaderName = extractor.getUploaderName();
            ExtractorAsserts.assertContains("Just Hits", uploaderName);
        }

        @Test
        public void testUploaderAvatarUrl() throws Exception {
            final String uploaderAvatarUrl = extractor.getUploaderAvatarUrl();
            ExtractorAsserts.assertContains("yt", uploaderAvatarUrl);
        }

        @Test
        public void testStreamCount() throws Exception {
            ExtractorAsserts.assertGreater(100, extractor.getStreamCount());
        }

        @Override
        public void testUploaderVerified() throws Exception {
            assertFalse(extractor.isUploaderVerified());
        }

        @Test
        void getPlaylistType() throws ParsingException {
            assertEquals(PlaylistInfo.PlaylistType.NORMAL, extractor.getPlaylistType());
        }
    }

    public static class HugePlaylist implements BasePlaylistExtractorTest {
        private static YoutubePlaylistExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "huge"));
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
            ExtractorAsserts.assertContains("yt", thumbnailUrl);
        }

        @Disabled
        @Test
        public void testBannerUrl() throws ParsingException {
            final String bannerUrl = extractor.getBannerUrl();
            assertIsSecureUrl(bannerUrl);
            ExtractorAsserts.assertContains("yt", bannerUrl);
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
            ExtractorAsserts.assertContains("yt", uploaderAvatarUrl);
        }

        @Test
        public void testStreamCount() throws Exception {
            ExtractorAsserts.assertGreater(100, extractor.getStreamCount());
        }

        @Override
        public void testUploaderVerified() throws Exception {
            assertTrue(extractor.isUploaderVerified());
        }

        @Test
        void getPlaylistType() throws ParsingException {
            assertEquals(PlaylistInfo.PlaylistType.NORMAL, extractor.getPlaylistType());
        }
    }

    public static class LearningPlaylist implements BasePlaylistExtractorTest {
        private static YoutubePlaylistExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "learning"));
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
            assertTrue(extractor.getName().startsWith("Anatomy & Physiology"));
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

        @Disabled
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
            ExtractorAsserts.assertContains("yt", thumbnailUrl);
        }

        @Disabled
        @Test
        public void testBannerUrl() throws ParsingException {
            final String bannerUrl = extractor.getBannerUrl();
            assertIsSecureUrl(bannerUrl);
            ExtractorAsserts.assertContains("yt", bannerUrl);
        }

        @Test
        public void testUploaderUrl() throws Exception {
            assertEquals("https://www.youtube.com/channel/UCX6b17PVsYBQ0ip5gyeme-Q", extractor.getUploaderUrl());
        }

        @Test
        public void testUploaderName() throws Exception {
            final String uploaderName = extractor.getUploaderName();
            ExtractorAsserts.assertContains("CrashCourse", uploaderName);
        }

        @Test
        public void testUploaderAvatarUrl() throws Exception {
            final String uploaderAvatarUrl = extractor.getUploaderAvatarUrl();
            ExtractorAsserts.assertContains("yt", uploaderAvatarUrl);
        }

        @Test
        public void testStreamCount() throws Exception {
            ExtractorAsserts.assertGreater(40, extractor.getStreamCount());
        }

        @Override
        public void testUploaderVerified() throws Exception {
            assertTrue(extractor.isUploaderVerified());
        }

        @Test
        void getPlaylistType() throws ParsingException {
            assertEquals(PlaylistInfo.PlaylistType.NORMAL, extractor.getPlaylistType());
        }
    }

    public static class ContinuationsTests {

        @BeforeAll
        public static void setUp() throws IOException {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "continuations"));
        }

        @Test
        public void testNoContinuations() throws Exception {
            final YoutubePlaylistExtractor extractor = (YoutubePlaylistExtractor) YouTube
                    .getPlaylistExtractor(
                            "https://www.youtube.com/playlist?list=PLXJg25X-OulsVsnvZ7RVtSDW-id9_RzAO");
            extractor.fetchPage();

            assertNoMoreItems(extractor);
        }

        @Test
        public void testOnlySingleContinuation() throws Exception {
            final YoutubePlaylistExtractor extractor = (YoutubePlaylistExtractor) YouTube
                    .getPlaylistExtractor(
                            "https://www.youtube.com/playlist?list=PLoumn5BIsUDeGF1vy5Nylf_RJKn5aL_nr");
            extractor.fetchPage();

            final ListExtractor.InfoItemsPage<StreamInfoItem> page = defaultTestMoreItems(
                    extractor);
            assertFalse(page.hasNextPage(), "More items available when it shouldn't");
        }
    }
}
