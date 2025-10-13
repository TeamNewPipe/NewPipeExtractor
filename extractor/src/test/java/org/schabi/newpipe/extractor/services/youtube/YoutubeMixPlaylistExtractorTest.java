package org.schabi.newpipe.extractor.services.youtube;

import com.grack.nanojson.JsonWriter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.ExtractorAsserts;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistInfo;
import org.schabi.newpipe.extractor.services.DefaultSimpleExtractorTest;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeMixPlaylistExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.*;

@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "NewClassNamingConvention"})
public class YoutubeMixPlaylistExtractorTest {

    private static final Map<String, String> dummyCookie = Map.of(YoutubeMixPlaylistExtractor.COOKIE_NAME, "whatever");

    static abstract class Base extends DefaultSimpleExtractorTest<YoutubeMixPlaylistExtractor>
        implements InitYoutubeTest {

        @BeforeAll
        @Override
        public void setUp() throws Exception {
            InitYoutubeTest.super.setUp();
            YoutubeParsingHelper.setConsentAccepted(true);
        }

        @Override
        protected YoutubeMixPlaylistExtractor createExtractor() throws Exception {
            return (YoutubeMixPlaylistExtractor) YouTube.getPlaylistExtractor(extractorUrl());
        }

        protected abstract String extractorUrl();
    }

    public static class Mix extends Base {
        private static final String VIDEO_ID = "FAqYW76GLPA";
        private static final String VIDEO_TITLE = "Mix – ";

        @Override
        protected String extractorUrl() {
            return "https://www.youtube.com/watch?v=" + VIDEO_ID + "&list=RD" + VIDEO_ID;
        }

        @Test
        void getServiceId() {
            assertEquals(YouTube.getServiceId(), extractor().getServiceId());
        }

        @Test
        void getName() throws Exception {
            final String name = extractor().getName();
            ExtractorAsserts.assertContains("Mix", name);
            ExtractorAsserts.assertContains(VIDEO_TITLE, name);
        }

        @Test
        void getThumbnails() throws Exception {
            YoutubeTestsUtils.testImages(extractor().getThumbnails());
            extractor().getThumbnails().forEach(thumbnail ->
                    ExtractorAsserts.assertContains(VIDEO_ID, thumbnail.getUrl()));
        }

        @Test
        void getInitialPage() throws Exception {
            final InfoItemsPage<StreamInfoItem> streams = extractor().getInitialPage();
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        void getPage() throws Exception {
            final byte[] body = JsonWriter.string(prepareDesktopJsonBuilder(
                    NewPipe.getPreferredLocale(), NewPipe.getPreferredContentCountry())
                    .value("videoId", VIDEO_ID)
                    .value("playlistId", "RD" + VIDEO_ID)
                    .value("params", "OAE%3D")
                    .done())
                    .getBytes(StandardCharsets.UTF_8);

            final InfoItemsPage<StreamInfoItem> streams = extractor().getPage(new Page(
                    YOUTUBEI_V1_URL + "next?" + DISABLE_PRETTY_PRINT_PARAMETER,
                    null, null, dummyCookie, body));
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        void getContinuations() throws Exception {
            InfoItemsPage<StreamInfoItem> streams = extractor().getInitialPage();
            final Set<String> urls = new HashSet<>();

            // Should work infinitely, but for testing purposes only 3 times
            for (int i = 0; i < 3; i++) {
                assertTrue(streams.hasNextPage());
                assertFalse(streams.getItems().isEmpty());

                for (final StreamInfoItem item : streams.getItems()) {
                    // TODO Duplicates are appearing
                    // assertFalse(urls.contains(item.getUrl()));
                    urls.add(item.getUrl());
                }

                streams = extractor().getPage(streams.getNextPage());
            }
            assertTrue(streams.hasNextPage());
            assertFalse(streams.getItems().isEmpty());
        }

        @Test
        void getStreamCount() {
            assertEquals(ListExtractor.ITEM_COUNT_INFINITE, extractor().getStreamCount());
        }

        @Test
        void getPlaylistType() throws ParsingException {
            assertEquals(PlaylistInfo.PlaylistType.MIX_STREAM, extractor().getPlaylistType());
        }
    }

    public static class MixWithIndex extends Base {
        private static final String VIDEO_ID = "FAqYW76GLPA";
        private static final String VIDEO_TITLE = "Mix – ";
        private static final int INDEX = 7; // YT starts the index with 1...
        private static final String VIDEO_ID_AT_INDEX = "F90Cw4l-8NY";

        @Override
        protected String extractorUrl() {
            return "https://www.youtube.com/watch?v=" + VIDEO_ID_AT_INDEX
                + "&list=RD" + VIDEO_ID + "&index=" + INDEX;
        }

        @Test
        void getName() throws Exception {
            final String name = extractor().getName();
            ExtractorAsserts.assertContains("Mix", name);
            ExtractorAsserts.assertContains(VIDEO_TITLE, name);
        }

        @Test
        void getThumbnails() throws Exception {
            YoutubeTestsUtils.testImages(extractor().getThumbnails());
            extractor().getThumbnails().forEach(thumbnail ->
                    ExtractorAsserts.assertContains(VIDEO_ID, thumbnail.getUrl()));
        }

        @Test
        void getInitialPage() throws Exception {
            final InfoItemsPage<StreamInfoItem> streams = extractor().getInitialPage();
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        void getPage() throws Exception {
            final byte[] body = JsonWriter.string(prepareDesktopJsonBuilder(
                    NewPipe.getPreferredLocale(), NewPipe.getPreferredContentCountry())
                    .value("videoId", VIDEO_ID)
                    .value("playlistId", "RD" + VIDEO_ID)
                    .value("playlistIndex", INDEX)
                    .value("params", "OAE%3D")
                    .done())
                    .getBytes(StandardCharsets.UTF_8);

            final InfoItemsPage<StreamInfoItem> streams = extractor().getPage(new Page(
                    YOUTUBEI_V1_URL + "next?" + DISABLE_PRETTY_PRINT_PARAMETER,
                    null, null, dummyCookie, body));
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        void getContinuations() throws Exception {
            InfoItemsPage<StreamInfoItem> streams = extractor().getInitialPage();
            final Set<String> urls = new HashSet<>();

            // Should work infinitely, but for testing purposes only 3 times
            for (int i = 0; i < 3; i++) {
                assertTrue(streams.hasNextPage());
                assertFalse(streams.getItems().isEmpty());
                for (final StreamInfoItem item : streams.getItems()) {
                    // TODO Duplicates are appearing
                    // assertFalse(urls.contains(item.getUrl()));
                    urls.add(item.getUrl());
                }

                streams = extractor().getPage(streams.getNextPage());
            }
            assertTrue(streams.hasNextPage());
            assertFalse(streams.getItems().isEmpty());
        }

        @Test
        void getStreamCount() {
            assertEquals(ListExtractor.ITEM_COUNT_INFINITE, extractor().getStreamCount());
        }

        @Test
        void getPlaylistType() throws ParsingException {
            assertEquals(PlaylistInfo.PlaylistType.MIX_STREAM, extractor().getPlaylistType());
        }
    }

    public static class MyMix extends Base {
        private static final String VIDEO_ID = "YVkUvmDQ3HY";

        @Override
        protected String extractorUrl() {
            return "https://www.youtube.com/watch?v=" + VIDEO_ID
                + "&list=RDMM" + VIDEO_ID;
        }

        @Test
        void getServiceId() {
            assertEquals(YouTube.getServiceId(), extractor().getServiceId());
        }

        @Test
        void getName() throws Exception {
            final String name = extractor().getName();
            assertEquals("My Mix", name);
        }

        @Test
        void getThumbnails() throws Exception {
            YoutubeTestsUtils.testImages(extractor().getThumbnails());
            extractor().getThumbnails().forEach(thumbnail ->
                    ExtractorAsserts.assertContains(VIDEO_ID, thumbnail.getUrl()));
        }

        @Test
        void getInitialPage() throws Exception {
            final InfoItemsPage<StreamInfoItem> streams = extractor().getInitialPage();
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        void getPage() throws Exception {
            final byte[] body = JsonWriter.string(prepareDesktopJsonBuilder(
                    NewPipe.getPreferredLocale(), NewPipe.getPreferredContentCountry())
                    .value("videoId", VIDEO_ID)
                    .value("playlistId", "RDMM" + VIDEO_ID)
                    .value("params", "OAE%3D")
                    .done())
                    .getBytes(StandardCharsets.UTF_8);

            final InfoItemsPage<StreamInfoItem> streams = extractor().getPage(new Page(
                    YOUTUBEI_V1_URL + "next?" + DISABLE_PRETTY_PRINT_PARAMETER,
                    null, null, dummyCookie, body));
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        void getContinuations() throws Exception {
            InfoItemsPage<StreamInfoItem> streams = extractor().getInitialPage();
            final Set<String> urls = new HashSet<>();

            // Should work infinitely, but for testing purposes only 3 times
            for (int i = 0; i < 3; i++) {
                assertTrue(streams.hasNextPage());
                assertFalse(streams.getItems().isEmpty());

                for (final StreamInfoItem item : streams.getItems()) {
                    // TODO Duplicates are appearing
                    // assertFalse(urls.contains(item.getUrl()));
                    urls.add(item.getUrl());
                }

                streams = extractor().getPage(streams.getNextPage());
            }
            assertTrue(streams.hasNextPage());
            assertFalse(streams.getItems().isEmpty());
        }

        @Test
        void getStreamCount() {
            assertEquals(ListExtractor.ITEM_COUNT_INFINITE, extractor().getStreamCount());
        }

        @Test
        void getPlaylistType() throws ParsingException {
            assertEquals(PlaylistInfo.PlaylistType.MIX_STREAM, extractor().getPlaylistType());
        }
    }

    public static class InvalidPageEmpty extends Base {
        private static final String VIDEO_ID = "QMVCAPd5cwBcg";

        @Override
        protected String extractorUrl() {
            return "https://www.youtube.com/watch?v=" + VIDEO_ID
                + "&list=RD" + VIDEO_ID;
        }

        @Test
        void getPageEmptyUrl() {
            assertThrows(IllegalArgumentException.class, () -> extractor().getPage(new Page("")));
        }
    }

    public static class InvalidVideoId extends Base {
        @Override
        protected String extractorUrl() {
            return "https://www.youtube.com/watch?v=" + "abcde"
                + "&list=RD" + "abcde";
        }

        @Override
        protected void fetchExtractor(final YoutubeMixPlaylistExtractor extractor) throws Exception {
            // Do nothing, done by test below
        }

        @Test
        void invalidVideoId() {
            assertThrows(ExtractionException.class, extractor()::fetchPage);
        }
    }

    public static class GenreMix extends Base {
        private static final String VIDEO_ID = "kINJeTNFbpg";
        private static final String MIX_TITLE = "Mix – Electronic music";

        @Override
        protected String extractorUrl() {
            return "https://www.youtube.com/watch?v=" + VIDEO_ID
                + "&list=RDGMEMYH9CUrFO7CfLJpaD7UR85w";
        }

        @Test
        void getServiceId() {
            assertEquals(YouTube.getServiceId(), extractor().getServiceId());
        }

        @Test
        void getName() throws Exception {
            assertEquals(MIX_TITLE, extractor().getName());
        }

        @Test
        void getThumbnails() throws Exception {
            YoutubeTestsUtils.testImages(extractor().getThumbnails());
            extractor().getThumbnails().forEach(thumbnail ->
                    ExtractorAsserts.assertContains(VIDEO_ID, thumbnail.getUrl()));
        }

        @Test
        void getInitialPage() throws Exception {
            final InfoItemsPage<StreamInfoItem> streams = extractor().getInitialPage();
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        void getPage() throws Exception {
            final byte[] body = JsonWriter.string(prepareDesktopJsonBuilder(
                    NewPipe.getPreferredLocale(), NewPipe.getPreferredContentCountry())
                    .value("videoId", VIDEO_ID)
                    .value("playlistId", "RD" + VIDEO_ID)
                    .value("params", "OAE%3D")
                    .done())
                    .getBytes(StandardCharsets.UTF_8);

            final InfoItemsPage<StreamInfoItem> streams = extractor().getPage(new Page(
                    YOUTUBEI_V1_URL + "next?" + DISABLE_PRETTY_PRINT_PARAMETER,
                    null, null, dummyCookie, body));
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        void getContinuations() throws Exception {
            InfoItemsPage<StreamInfoItem> streams = extractor().getInitialPage();
            final Set<String> urls = new HashSet<>();

            // Should work infinitely, but for testing purposes only 3 times
            for (int i = 0; i < 3; i++) {
                assertTrue(streams.hasNextPage());
                assertFalse(streams.getItems().isEmpty());

                for (final StreamInfoItem item : streams.getItems()) {
                    // TODO Duplicates are appearing
                    // assertFalse(urls.contains(item.getUrl()));
                    urls.add(item.getUrl());
                }

                streams = extractor().getPage(streams.getNextPage());
            }
            assertTrue(streams.hasNextPage());
            assertFalse(streams.getItems().isEmpty());
        }

        @Test
        void getStreamCount() {
            assertEquals(ListExtractor.ITEM_COUNT_INFINITE, extractor().getStreamCount());
        }

        @Test
        void getPlaylistType() throws ParsingException {
            assertEquals(PlaylistInfo.PlaylistType.MIX_GENRE, extractor().getPlaylistType());
        }
    }

    public static class Music extends Base {
        private static final String VIDEO_ID = "dQw4w9WgXcQ";
        private static final String MIX_TITLE = "Mix – Rick Astley - Never Gonna Give You Up (Official Video) (4K Remaster)";

        @Override
        protected String extractorUrl() {
            return "https://m.youtube.com/watch?v=" + VIDEO_ID
                + "&list=RDAMVM" + VIDEO_ID;
        }

        @Test
        void getServiceId() {
            assertEquals(YouTube.getServiceId(), extractor().getServiceId());
        }

        @Test
        void getName() throws Exception {
            assertEquals(MIX_TITLE, extractor().getName());
        }

        @Test
        void getThumbnailUrl() throws Exception {
            YoutubeTestsUtils.testImages(extractor().getThumbnails());
            extractor().getThumbnails().forEach(thumbnail ->
                    ExtractorAsserts.assertContains(VIDEO_ID, thumbnail.getUrl()));
        }

        @Test
        void getInitialPage() throws Exception {
            final InfoItemsPage<StreamInfoItem> streams = extractor().getInitialPage();
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        void getPage() throws Exception {
            final byte[] body = JsonWriter.string(prepareDesktopJsonBuilder(
                            NewPipe.getPreferredLocale(), NewPipe.getPreferredContentCountry())
                            .value("videoId", VIDEO_ID)
                            .value("playlistId", "RD" + VIDEO_ID)
                            .value("params", "OAE%3D")
                            .done())
                    .getBytes(StandardCharsets.UTF_8);

            final InfoItemsPage<StreamInfoItem> streams = extractor().getPage(new Page(
                    YOUTUBEI_V1_URL + "next?" + DISABLE_PRETTY_PRINT_PARAMETER,
                    null, null, dummyCookie, body));
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        void getContinuations() throws Exception {
            InfoItemsPage<StreamInfoItem> streams = extractor().getInitialPage();
            final Set<String> urls = new HashSet<>();

            // Should work infinitely, but for testing purposes only 3 times
            for (int i = 0; i < 3; i++) {
                assertTrue(streams.hasNextPage());
                assertFalse(streams.getItems().isEmpty());

                for (final StreamInfoItem item : streams.getItems()) {
                    // TODO Duplicates are appearing
                    // assertFalse(urls.contains(item.getUrl()));
                    urls.add(item.getUrl());
                }

                streams = extractor().getPage(streams.getNextPage());
            }
            assertTrue(streams.hasNextPage());
            assertFalse(streams.getItems().isEmpty());
        }

        @Test
        void getStreamCount() {
            assertEquals(ListExtractor.ITEM_COUNT_INFINITE, extractor().getStreamCount());
        }

        @Test
        void getPlaylistType() throws ParsingException {
            assertEquals(PlaylistInfo.PlaylistType.MIX_MUSIC, extractor().getPlaylistType());
        }
    }
}
