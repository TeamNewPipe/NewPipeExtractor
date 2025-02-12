package org.schabi.newpipe.extractor.services.youtube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.DISABLE_PRETTY_PRINT_PARAMETER;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.YOUTUBEI_V1_URL;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder;

import com.grack.nanojson.JsonWriter;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderFactory;
import org.schabi.newpipe.extractor.ExtractorAsserts;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistInfo;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeMixPlaylistExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "NewClassNamingConvention"})
public class YoutubeMixPlaylistExtractorTest {

    private static final String RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/mix/";
    private static final Map<String, String> dummyCookie = Map.of(YoutubeMixPlaylistExtractor.COOKIE_NAME, "whatever");
    private static YoutubeMixPlaylistExtractor extractor;

    public static class Mix {
        private static final String VIDEO_ID = "FAqYW76GLPA";
        private static final String VIDEO_TITLE = "Mix – ";

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            YoutubeParsingHelper.setConsentAccepted(true);
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "mix"));
            extractor = (YoutubeMixPlaylistExtractor) YouTube
                    .getPlaylistExtractor("https://www.youtube.com/watch?v=" + VIDEO_ID
                            + "&list=RD" + VIDEO_ID);
            extractor.fetchPage();
        }

        @Test
        void getServiceId() {
            assertEquals(YouTube.getServiceId(), extractor.getServiceId());
        }

        @Test
        void getName() throws Exception {
            final String name = extractor.getName();
            ExtractorAsserts.assertContains("Mix", name);
            ExtractorAsserts.assertContains(VIDEO_TITLE, name);
        }

        @Test
        void getThumbnails() throws Exception {
            YoutubeTestsUtils.testImages(extractor.getThumbnails());
            extractor.getThumbnails().forEach(thumbnail ->
                    ExtractorAsserts.assertContains(VIDEO_ID, thumbnail.getUrl()));
        }

        @Test
        void getInitialPage() throws Exception {
            final InfoItemsPage<StreamInfoItem> streams = extractor.getInitialPage();
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        void getPage() throws Exception {
            final byte[] body = JsonWriter.string(prepareDesktopJsonBuilder(
                    NewPipe.getPreferredLocalization(), NewPipe.getPreferredContentCountry())
                    .value("videoId", VIDEO_ID)
                    .value("playlistId", "RD" + VIDEO_ID)
                    .value("params", "OAE%3D")
                    .done())
                    .getBytes(StandardCharsets.UTF_8);

            final InfoItemsPage<StreamInfoItem> streams = extractor.getPage(new Page(
                    YOUTUBEI_V1_URL + "next?" + DISABLE_PRETTY_PRINT_PARAMETER,
                    null, null, dummyCookie, body));
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        void getContinuations() throws Exception {
            InfoItemsPage<StreamInfoItem> streams = extractor.getInitialPage();
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

                streams = extractor.getPage(streams.getNextPage());
            }
            assertTrue(streams.hasNextPage());
            assertFalse(streams.getItems().isEmpty());
        }

        @Test
        void getStreamCount() {
            assertEquals(ListExtractor.ITEM_COUNT_INFINITE, extractor.getStreamCount());
        }

        @Test
        void getPlaylistType() throws ParsingException {
            assertEquals(PlaylistInfo.PlaylistType.MIX_STREAM, extractor.getPlaylistType());
        }
    }

    public static class MixWithIndex {
        private static final String VIDEO_ID = "FAqYW76GLPA";
        private static final String VIDEO_TITLE = "Mix – ";
        private static final int INDEX = 7; // YT starts the index with 1...
        private static final String VIDEO_ID_AT_INDEX = "F90Cw4l-8NY";

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            YoutubeParsingHelper.setConsentAccepted(true);
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "mixWithIndex"));
            extractor = (YoutubeMixPlaylistExtractor) YouTube
                    .getPlaylistExtractor("https://www.youtube.com/watch?v=" + VIDEO_ID_AT_INDEX
                            + "&list=RD" + VIDEO_ID + "&index=" + INDEX);
            extractor.fetchPage();
        }

        @Test
        void getName() throws Exception {
            final String name = extractor.getName();
            ExtractorAsserts.assertContains("Mix", name);
            ExtractorAsserts.assertContains(VIDEO_TITLE, name);
        }

        @Test
        void getThumbnails() throws Exception {
            YoutubeTestsUtils.testImages(extractor.getThumbnails());
            extractor.getThumbnails().forEach(thumbnail ->
                    ExtractorAsserts.assertContains(VIDEO_ID, thumbnail.getUrl()));
        }

        @Test
        void getInitialPage() throws Exception {
            final InfoItemsPage<StreamInfoItem> streams = extractor.getInitialPage();
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        void getPage() throws Exception {
            final byte[] body = JsonWriter.string(prepareDesktopJsonBuilder(
                    NewPipe.getPreferredLocalization(), NewPipe.getPreferredContentCountry())
                    .value("videoId", VIDEO_ID)
                    .value("playlistId", "RD" + VIDEO_ID)
                    .value("playlistIndex", INDEX)
                    .value("params", "OAE%3D")
                    .done())
                    .getBytes(StandardCharsets.UTF_8);

            final InfoItemsPage<StreamInfoItem> streams = extractor.getPage(new Page(
                    YOUTUBEI_V1_URL + "next?" + DISABLE_PRETTY_PRINT_PARAMETER,
                    null, null, dummyCookie, body));
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        void getContinuations() throws Exception {
            InfoItemsPage<StreamInfoItem> streams = extractor.getInitialPage();
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

                streams = extractor.getPage(streams.getNextPage());
            }
            assertTrue(streams.hasNextPage());
            assertFalse(streams.getItems().isEmpty());
        }

        @Test
        void getStreamCount() {
            assertEquals(ListExtractor.ITEM_COUNT_INFINITE, extractor.getStreamCount());
        }

        @Test
        void getPlaylistType() throws ParsingException {
            assertEquals(PlaylistInfo.PlaylistType.MIX_STREAM, extractor.getPlaylistType());
        }
    }

    public static class MyMix {
        private static final String VIDEO_ID = "YVkUvmDQ3HY";

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            YoutubeParsingHelper.setConsentAccepted(true);
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "myMix"));
            extractor = (YoutubeMixPlaylistExtractor) YouTube
                    .getPlaylistExtractor("https://www.youtube.com/watch?v=" + VIDEO_ID
                            + "&list=RDMM" + VIDEO_ID);
            extractor.fetchPage();
        }

        @Test
        void getServiceId() {
            assertEquals(YouTube.getServiceId(), extractor.getServiceId());
        }

        @Test
        void getName() throws Exception {
            final String name = extractor.getName();
            assertEquals("My Mix", name);
        }

        @Test
        void getThumbnails() throws Exception {
            YoutubeTestsUtils.testImages(extractor.getThumbnails());
            extractor.getThumbnails().forEach(thumbnail ->
                    ExtractorAsserts.assertContains(VIDEO_ID, thumbnail.getUrl()));
        }

        @Test
        void getInitialPage() throws Exception {
            final InfoItemsPage<StreamInfoItem> streams = extractor.getInitialPage();
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        void getPage() throws Exception {
            final byte[] body = JsonWriter.string(prepareDesktopJsonBuilder(
                    NewPipe.getPreferredLocalization(), NewPipe.getPreferredContentCountry())
                    .value("videoId", VIDEO_ID)
                    .value("playlistId", "RDMM" + VIDEO_ID)
                    .value("params", "OAE%3D")
                    .done())
                    .getBytes(StandardCharsets.UTF_8);

            final InfoItemsPage<StreamInfoItem> streams = extractor.getPage(new Page(
                    YOUTUBEI_V1_URL + "next?" + DISABLE_PRETTY_PRINT_PARAMETER,
                    null, null, dummyCookie, body));
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        void getContinuations() throws Exception {
            InfoItemsPage<StreamInfoItem> streams = extractor.getInitialPage();
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

                streams = extractor.getPage(streams.getNextPage());
            }
            assertTrue(streams.hasNextPage());
            assertFalse(streams.getItems().isEmpty());
        }

        @Test
        void getStreamCount() {
            assertEquals(ListExtractor.ITEM_COUNT_INFINITE, extractor.getStreamCount());
        }

        @Test
        void getPlaylistType() throws ParsingException {
            assertEquals(PlaylistInfo.PlaylistType.MIX_STREAM, extractor.getPlaylistType());
        }
    }

    public static class Invalid {
        private static final String VIDEO_ID = "QMVCAPd5cwBcg";

        @BeforeAll
        public static void setUp() {
            YoutubeTestsUtils.ensureStateless();
            YoutubeParsingHelper.setConsentAccepted(true);
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "invalid"));
        }

        @Test
        void getPageEmptyUrl() throws Exception {
            extractor = (YoutubeMixPlaylistExtractor) YouTube
                    .getPlaylistExtractor("https://www.youtube.com/watch?v=" + VIDEO_ID
                            + "&list=RD" + VIDEO_ID);

            extractor.fetchPage();
            assertThrows(IllegalArgumentException.class, () -> extractor.getPage(new Page("")));
        }

        @Test
        void invalidVideoId() throws Exception {
            extractor = (YoutubeMixPlaylistExtractor) YouTube
                    .getPlaylistExtractor("https://www.youtube.com/watch?v=" + "abcde"
                            + "&list=RD" + "abcde");

            assertThrows(ExtractionException.class, extractor::fetchPage);
        }
    }

    public static class GenreMix {
        private static final String VIDEO_ID = "kINJeTNFbpg";
        private static final String MIX_TITLE = "Mix – Electronic music";

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            YoutubeParsingHelper.setConsentAccepted(true);
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "genreMix"));
            extractor = (YoutubeMixPlaylistExtractor) YouTube
                    .getPlaylistExtractor("https://www.youtube.com/watch?v=" + VIDEO_ID
                            + "&list=RDGMEMYH9CUrFO7CfLJpaD7UR85w");
            extractor.fetchPage();
        }

        @Test
        void getServiceId() {
            assertEquals(YouTube.getServiceId(), extractor.getServiceId());
        }

        @Test
        void getName() throws Exception {
            assertEquals(MIX_TITLE, extractor.getName());
        }

        @Test
        void getThumbnails() throws Exception {
            YoutubeTestsUtils.testImages(extractor.getThumbnails());
            extractor.getThumbnails().forEach(thumbnail ->
                    ExtractorAsserts.assertContains(VIDEO_ID, thumbnail.getUrl()));
        }

        @Test
        void getInitialPage() throws Exception {
            final InfoItemsPage<StreamInfoItem> streams = extractor.getInitialPage();
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        void getPage() throws Exception {
            final byte[] body = JsonWriter.string(prepareDesktopJsonBuilder(
                    NewPipe.getPreferredLocalization(), NewPipe.getPreferredContentCountry())
                    .value("videoId", VIDEO_ID)
                    .value("playlistId", "RD" + VIDEO_ID)
                    .value("params", "OAE%3D")
                    .done())
                    .getBytes(StandardCharsets.UTF_8);

            final InfoItemsPage<StreamInfoItem> streams = extractor.getPage(new Page(
                    YOUTUBEI_V1_URL + "next?" + DISABLE_PRETTY_PRINT_PARAMETER,
                    null, null, dummyCookie, body));
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        void getContinuations() throws Exception {
            InfoItemsPage<StreamInfoItem> streams = extractor.getInitialPage();
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

                streams = extractor.getPage(streams.getNextPage());
            }
            assertTrue(streams.hasNextPage());
            assertFalse(streams.getItems().isEmpty());
        }

        @Test
        void getStreamCount() {
            assertEquals(ListExtractor.ITEM_COUNT_INFINITE, extractor.getStreamCount());
        }

        @Test
        void getPlaylistType() throws ParsingException {
            assertEquals(PlaylistInfo.PlaylistType.MIX_GENRE, extractor.getPlaylistType());
        }
    }

    public static class Music {
        private static final String VIDEO_ID = "dQw4w9WgXcQ";
        private static final String MIX_TITLE = "Mix – Rick Astley - Never Gonna Give You Up (Official Music Video)";

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            YoutubeParsingHelper.setConsentAccepted(true);
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "musicMix"));
            extractor = (YoutubeMixPlaylistExtractor)
                    YouTube.getPlaylistExtractor("https://m.youtube.com/watch?v=" + VIDEO_ID
                            + "&list=RDAMVM" + VIDEO_ID);
            extractor.fetchPage();
        }

        @Test
        void getServiceId() {
            assertEquals(YouTube.getServiceId(), extractor.getServiceId());
        }

        @Test
        void getName() throws Exception {
            assertEquals(MIX_TITLE, extractor.getName());
        }

        @Test
        void getThumbnailUrl() throws Exception {
            YoutubeTestsUtils.testImages(extractor.getThumbnails());
            extractor.getThumbnails().forEach(thumbnail ->
                    ExtractorAsserts.assertContains(VIDEO_ID, thumbnail.getUrl()));
        }

        @Test
        void getInitialPage() throws Exception {
            final InfoItemsPage<StreamInfoItem> streams = extractor.getInitialPage();
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        void getPage() throws Exception {
            final byte[] body = JsonWriter.string(prepareDesktopJsonBuilder(
                            NewPipe.getPreferredLocalization(), NewPipe.getPreferredContentCountry())
                            .value("videoId", VIDEO_ID)
                            .value("playlistId", "RD" + VIDEO_ID)
                            .value("params", "OAE%3D")
                            .done())
                    .getBytes(StandardCharsets.UTF_8);

            final InfoItemsPage<StreamInfoItem> streams = extractor.getPage(new Page(
                    YOUTUBEI_V1_URL + "next?" + DISABLE_PRETTY_PRINT_PARAMETER,
                    null, null, dummyCookie, body));
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        void getContinuations() throws Exception {
            InfoItemsPage<StreamInfoItem> streams = extractor.getInitialPage();
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

                streams = extractor.getPage(streams.getNextPage());
            }
            assertTrue(streams.hasNextPage());
            assertFalse(streams.getItems().isEmpty());
        }

        @Test
        void getStreamCount() {
            assertEquals(ListExtractor.ITEM_COUNT_INFINITE, extractor.getStreamCount());
        }

        @Test
        void getPlaylistType() throws ParsingException {
            assertEquals(PlaylistInfo.PlaylistType.MIX_MUSIC, extractor.getPlaylistType());
        }
    }
}
