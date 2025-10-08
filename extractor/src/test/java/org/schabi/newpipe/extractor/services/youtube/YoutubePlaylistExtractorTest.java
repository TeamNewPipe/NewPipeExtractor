package org.schabi.newpipe.extractor.services.youtube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertContains;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.assertNoMoreItems;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestGetPageInNewExtractor;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestListOfItems;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestMoreItems;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestRelatedItems;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.ExtractorAsserts;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.playlist.PlaylistInfo;
import org.schabi.newpipe.extractor.services.BasePlaylistExtractorTest;
import org.schabi.newpipe.extractor.services.DefaultSimpleExtractorTest;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubePlaylistExtractor;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.ContentAvailability;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Test for {@link YoutubePlaylistExtractor}
 */
public class YoutubePlaylistExtractorTest {

    public static class NotAvailable implements InitYoutubeTest {

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

    abstract static class Base extends DefaultSimpleExtractorTest<YoutubePlaylistExtractor>
        implements BasePlaylistExtractorTest, InitYoutubeTest {

        @Override
        protected YoutubePlaylistExtractor createExtractor() throws Exception {
            return (YoutubePlaylistExtractor) YouTube.getPlaylistExtractor(urlForExtraction());
        }

        protected abstract String urlForExtraction();
    }

    public static class TimelessPopHits extends Base {
        @Override
        protected String urlForExtraction() {
            return "http://www.youtube.com/watch?v=lp-EO5I60KA&list=PLMC9KNkIncKtPzgY-5rmhvj7fax8fdxoj";
        }

        @Override
        @Test
        public void testServiceId() {
            assertEquals(YouTube.getServiceId(), extractor().getServiceId());
        }

        @Override
        @Test
        public void testName() throws Exception {
            assertTrue(extractor().getName().startsWith("Pop Music Playlist"));
        }

        @Override
        @Test
        public void testId() throws Exception {
            assertEquals("PLMC9KNkIncKtPzgY-5rmhvj7fax8fdxoj", extractor().getId());
        }

        @Override
        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/playlist?list=PLMC9KNkIncKtPzgY-5rmhvj7fax8fdxoj", extractor().getUrl());
        }

        @Override
        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("http://www.youtube.com/watch?v=lp-EO5I60KA&list=PLMC9KNkIncKtPzgY-5rmhvj7fax8fdxoj", extractor().getOriginalUrl());
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
        public void testThumbnails() throws Exception {
            YoutubeTestsUtils.testImages(extractor().getThumbnails());
        }

        @Override
        @Test
        public void testBanners() throws ParsingException {
            YoutubeTestsUtils.testImages(extractor().getBanners());
        }

        @Test
        void testUploaderUrl() throws Exception {
            assertEquals("https://www.youtube.com/channel/UCs72iRpTEuwV3y6pdWYLgiw", extractor().getUploaderUrl());
        }

        @Override
        @Test
        public void testUploaderName() throws Exception {
            final String uploaderName = extractor().getUploaderName();
            ExtractorAsserts.assertContains("Just Hits", uploaderName);
        }

        @Override
        @Test
        public void testUploaderAvatars() throws Exception {
            YoutubeTestsUtils.testImages(extractor().getUploaderAvatars());
        }

        @Override
        @Test
        public void testStreamCount() throws Exception {
            ExtractorAsserts.assertGreater(100, extractor().getStreamCount());
        }

        @Test
        @Override
        public void testUploaderVerified() throws Exception {
            assertFalse(extractor().isUploaderVerified());
        }

        @Test
        void getPlaylistType() throws ParsingException {
            assertEquals(PlaylistInfo.PlaylistType.NORMAL, extractor().getPlaylistType());
        }

        @Test
        public void testDescription() throws ParsingException {
            final Description description = extractor().getDescription();
            assertContains("pop songs list", description.getContent());
        }
    }

    public static class HugePlaylist extends Base {
        @Override
        protected String urlForExtraction() {
            return "https://www.youtube.com/watch?v=8SbUC-UaAxE&list=PLWwAypAcFRgKAIIFqBr9oy-ZYZnixa_Fj";
        }

        @Test
        void testGetPageInNewExtractor() throws Exception {
            final PlaylistExtractor newExtractor = YouTube.getPlaylistExtractor(extractor().getUrl());
            defaultTestGetPageInNewExtractor(extractor(), newExtractor);
        }

        @Override
        @Test
        public void testServiceId() {
            assertEquals(YouTube.getServiceId(), extractor().getServiceId());
        }

        @Override
        @Test
        public void testName() throws Exception {
            final String name = extractor().getName();
            assertEquals("I Wanna Rock Super Gigantic Playlist 1: Hardrock, AOR, Metal and more !!! 5000 music videos !!!", name);
        }

        @Override
        @Test
        public void testId() throws Exception {
            assertEquals("PLWwAypAcFRgKAIIFqBr9oy-ZYZnixa_Fj", extractor().getId());
        }

        @Override
        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/playlist?list=PLWwAypAcFRgKAIIFqBr9oy-ZYZnixa_Fj", extractor().getUrl());
        }

        @Override
        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/watch?v=8SbUC-UaAxE&list=PLWwAypAcFRgKAIIFqBr9oy-ZYZnixa_Fj", extractor().getOriginalUrl());
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

            // test for 2 more levels
            for (int i = 0; i < 2; i++) {
                currentPage = extractor().getPage(currentPage.getNextPage());
                defaultTestListOfItems(YouTube, currentPage.getItems(), currentPage.getErrors());
            }
        }

        @Override
        @Test
        public void testThumbnails() throws Exception {
            YoutubeTestsUtils.testImages(extractor().getThumbnails());
        }

        @Override
        @Test
        public void testBanners() throws ParsingException {
            YoutubeTestsUtils.testImages(extractor().getBanners());
        }

        @Test
        void testUploaderUrl() throws Exception {
            assertEquals("https://www.youtube.com/channel/UCHSPWoY1J5fbDVbcnyeqwdw", extractor().getUploaderUrl());
        }

        @Override
        @Test
        public void testUploaderName() throws Exception {
            assertEquals("Tomas Nilsson TOMPA571", extractor().getUploaderName());
        }

        @Override
        @Test
        public void testUploaderAvatars() throws Exception {
            YoutubeTestsUtils.testImages(extractor().getUploaderAvatars());
        }

        @Override
        @Test
        public void testStreamCount() throws Exception {
            ExtractorAsserts.assertGreater(100, extractor().getStreamCount());
        }

        @Test
        @Override
        public void testUploaderVerified() throws Exception {
            assertFalse(extractor().isUploaderVerified());
        }

        @Test
        void getPlaylistType() throws ParsingException {
            assertEquals(PlaylistInfo.PlaylistType.NORMAL, extractor().getPlaylistType());
        }

        @Test
        public void testDescription() throws ParsingException {
            final Description description = extractor().getDescription();
            assertContains("I Wanna Rock Super Gigantic Playlist", description.getContent());
        }
    }

    public static class LearningPlaylist extends Base {
        @Override
        protected String urlForExtraction() {
            return "https://www.youtube.com/playlist?list=PL8dPuuaLjXtOAKed_MxxWBNaPno5h3Zs8";
        }

        @Override
        @Test
        public void testServiceId() {
            assertEquals(YouTube.getServiceId(), extractor().getServiceId());
        }

        @Override
        @Test
        public void testName() throws Exception {
            assertTrue(extractor().getName().startsWith("Anatomy & Physiology"));
        }

        @Override
        @Test
        public void testId() throws Exception {
            assertEquals("PL8dPuuaLjXtOAKed_MxxWBNaPno5h3Zs8", extractor().getId());
        }

        @Override
        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/playlist?list=PL8dPuuaLjXtOAKed_MxxWBNaPno5h3Zs8", extractor().getUrl());
        }

        @Override
        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/playlist?list=PL8dPuuaLjXtOAKed_MxxWBNaPno5h3Zs8", extractor().getOriginalUrl());
        }

        @Override
        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor());
        }

        @Override
        @Test
        public void testMoreRelatedItems() throws Exception {
            assertFalse(extractor().getInitialPage().hasNextPage());
        }

        @Override
        @Test
        public void testThumbnails() throws Exception {
            YoutubeTestsUtils.testImages(extractor().getThumbnails());
        }

        @Override
        @Test
        public void testBanners() throws ParsingException {
            YoutubeTestsUtils.testImages(extractor().getBanners());
        }

        @Test
        void testUploaderUrl() throws Exception {
            assertEquals("https://www.youtube.com/channel/UCX6b17PVsYBQ0ip5gyeme-Q", extractor().getUploaderUrl());
        }

        @Override
        @Test
        public void testUploaderName() throws Exception {
            final String uploaderName = extractor().getUploaderName();
            ExtractorAsserts.assertContains("CrashCourse", uploaderName);
        }

        @Override
        @Test
        public void testUploaderAvatars() throws Exception {
            YoutubeTestsUtils.testImages(extractor().getUploaderAvatars());
        }

        @Override
        @Test
        public void testStreamCount() throws Exception {
            ExtractorAsserts.assertGreater(40, extractor().getStreamCount());
        }

        @Test
        @Override
        public void testUploaderVerified() throws Exception {
            assertFalse(extractor().isUploaderVerified());
        }

        @Test
        void getPlaylistType() throws ParsingException {
            assertEquals(PlaylistInfo.PlaylistType.NORMAL, extractor().getPlaylistType());
        }

        @Test
        public void testDescription() throws ParsingException {
            final Description description = extractor().getDescription();
            assertContains("47 episodes", description.getContent());
        }
    }

    static class ShortsUI extends Base {

        @Override
        protected String urlForExtraction() {
            return "https://www.youtube.com/playlist?list=UUSHBR8-60-B28hp2BmDPdntcQ";
        }

        @Test
        @Override
        public void testServiceId() throws Exception {
            assertEquals(YouTube.getServiceId(), extractor().getServiceId());
        }

        @Test
        @Override
        public void testName() throws Exception {
            assertEquals("Short videos", extractor().getName());
        }

        @Test
        @Override
        public void testId() throws Exception {
            assertEquals("UUSHBR8-60-B28hp2BmDPdntcQ", extractor().getId());
        }

        @Test
        @Override
        public void testUrl() throws Exception {
            assertEquals("https://www.youtube.com/playlist?list=UUSHBR8-60-B28hp2BmDPdntcQ",
                extractor().getUrl());
        }

        @Test
        @Override
        public void testOriginalUrl() throws Exception {
            assertEquals("https://www.youtube.com/playlist?list=UUSHBR8-60-B28hp2BmDPdntcQ",
                extractor().getOriginalUrl());
        }

        @Test
        @Override
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor());
        }

        @Test
        @Override
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(extractor());
        }

        @Test
        @Override
        public void testThumbnails() throws Exception {
            YoutubeTestsUtils.testImages(extractor().getThumbnails());
        }

        @Test
        @Override
        public void testBanners() throws Exception {
            YoutubeTestsUtils.testImages(extractor().getBanners());
        }

        @Test
        @Override
        public void testUploaderName() throws Exception {
            assertEquals("YouTube", extractor().getUploaderName());
        }

        @Override
        @Test
        public void testUploaderAvatars() throws Exception {
            YoutubeTestsUtils.testImages(extractor().getUploaderAvatars());
        }

        @Test
        @Override
        public void testStreamCount() throws Exception {
            ExtractorAsserts.assertGreater(250, extractor().getStreamCount());
        }

        @Test
        @Override
        public void testUploaderVerified() throws Exception {
            // YouTube doesn't provide this information for playlists
            assertFalse(extractor().isUploaderVerified());
        }

        @Test
        void getPlaylistType() throws ParsingException {
            assertEquals(PlaylistInfo.PlaylistType.NORMAL, extractor().getPlaylistType());
        }

        @Test
        void testDescription() throws ParsingException {
            assertTrue(Utils.isNullOrEmpty(extractor().getDescription().getContent()));
        }
    }

    public static class ContinuationsTests implements InitYoutubeTest {

        @Test
        void testNoContinuations() throws Exception {
            final YoutubePlaylistExtractor extractor = (YoutubePlaylistExtractor) YouTube
                    .getPlaylistExtractor(
                            "https://www.youtube.com/playlist?list=PLXJg25X-OulsVsnvZ7RVtSDW-id9_RzAO");
            extractor.fetchPage();

            assertNoMoreItems(extractor);
        }

        @Test
        void testOnlySingleContinuation() throws Exception {
            final YoutubePlaylistExtractor extractor = (YoutubePlaylistExtractor) YouTube
                    .getPlaylistExtractor(
                            "https://www.youtube.com/playlist?list=PLoumn5BIsUDeGF1vy5Nylf_RJKn5aL_nr");
            extractor.fetchPage();

            final ListExtractor.InfoItemsPage<StreamInfoItem> page = defaultTestMoreItems(
                    extractor);
            assertFalse(page.hasNextPage(), "More items available when it shouldn't");
        }
    }

    public static class MembersOnlyTests implements InitYoutubeTest {

        @Test
        void testOnlyMembersOnlyVideos() throws Exception {
            final YoutubePlaylistExtractor extractor = (YoutubePlaylistExtractor) YouTube
                    .getPlaylistExtractor(
                // auto-generated playlist with only membersOnly videos
                            "https://www.youtube.com/playlist?list=UUMOQuLXlFNAeDJMSmuzHU5axw");
            extractor.fetchPage();

            final List<StreamInfoItem> allItems = extractor.getInitialPage().getItems()
                    .stream()
                    .filter(StreamInfoItem.class::isInstance)
                    .map(StreamInfoItem.class::cast)
                    .collect(Collectors.toUnmodifiableList());
            final List<StreamInfoItem> membershipVideos = allItems.stream()
                    .filter(item -> item.getContentAvailability() != ContentAvailability.MEMBERSHIP)
                    .collect(Collectors.toUnmodifiableList());

            assertFalse(allItems.isEmpty());
            assertTrue(membershipVideos.isEmpty());
        }
    }
}
