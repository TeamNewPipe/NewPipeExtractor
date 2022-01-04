package org.schabi.newpipe.extractor.services.youtube;

import com.grack.nanojson.JsonWriter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderFactory;
import org.schabi.newpipe.extractor.ExtractorAsserts;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeMixPlaylistExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.*;

public class YoutubeMixPlaylistExtractorTest {

    private static final String RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/mix/";
    private static final Map<String, String> dummyCookie = new HashMap<>();

    private static YoutubeMixPlaylistExtractor extractor;

    public static class Mix {
        private static final String VIDEO_ID = "QMVCAPd5cwBcg";
        private static final String VIDEO_TITLE = "Mix – ";

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeParsingHelper.resetClientVersionAndKey();
            YoutubeParsingHelper.setNumberGenerator(new Random(1));
            NewPipe.init(new DownloaderFactory().getDownloader(RESOURCE_PATH + "mix"));
            dummyCookie.put(YoutubeMixPlaylistExtractor.COOKIE_NAME, "whatever");
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
        void getThumbnailUrl() throws Exception {
            final String thumbnailUrl = extractor.getThumbnailUrl();
            assertIsSecureUrl(thumbnailUrl);
            ExtractorAsserts.assertContains("yt", thumbnailUrl);
            ExtractorAsserts.assertContains(VIDEO_ID, thumbnailUrl);
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
                    YOUTUBEI_V1_URL + "next?key=" + getKey(), null, null, dummyCookie, body));
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
    }

    public static class MixWithIndex {

        private static final String VIDEO_ID = "QMVCAPd5cwBcg";
        private static final String VIDEO_TITLE = "Mix – ";
        private static final int INDEX = 4;
        private static final String VIDEO_ID_NUMBER_4 = "lWA2pjMjpBs";

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeParsingHelper.resetClientVersionAndKey();
            YoutubeParsingHelper.setNumberGenerator(new Random(1));
            NewPipe.init(new DownloaderFactory().getDownloader(RESOURCE_PATH + "mixWithIndex"));
            dummyCookie.put(YoutubeMixPlaylistExtractor.COOKIE_NAME, "whatever");
            extractor = (YoutubeMixPlaylistExtractor) YouTube
                    .getPlaylistExtractor("https://www.youtube.com/watch?v=" + VIDEO_ID_NUMBER_4
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
        void getThumbnailUrl() throws Exception {
            final String thumbnailUrl = extractor.getThumbnailUrl();
            assertIsSecureUrl(thumbnailUrl);
            ExtractorAsserts.assertContains("yt", thumbnailUrl);
            ExtractorAsserts.assertContains(VIDEO_ID, thumbnailUrl);
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
                    YOUTUBEI_V1_URL + "next?key=" + getKey(), null, null, dummyCookie, body));
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
    }

    public static class MyMix {
        private static final String VIDEO_ID = "_AzeUSL9lZc";

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeParsingHelper.resetClientVersionAndKey();
            YoutubeParsingHelper.setNumberGenerator(new Random(1));
            NewPipe.init(new DownloaderFactory().getDownloader(RESOURCE_PATH + "myMix"));
            dummyCookie.put(YoutubeMixPlaylistExtractor.COOKIE_NAME, "whatever");
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
        void getThumbnailUrl() throws Exception {
            final String thumbnailUrl = extractor.getThumbnailUrl();
            assertIsSecureUrl(thumbnailUrl);
            assertTrue(thumbnailUrl.startsWith("https://i.ytimg.com/vi/_AzeUSL9lZc"));
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
                    YOUTUBEI_V1_URL + "next?key=" + getKey(), null, null, dummyCookie, body));
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
    }

    public static class Invalid {

        private static final String VIDEO_ID = "QMVCAPd5cwBcg";

        @BeforeAll
        public static void setUp() throws IOException {
            YoutubeParsingHelper.resetClientVersionAndKey();
            YoutubeParsingHelper.setNumberGenerator(new Random(1));
            NewPipe.init(new DownloaderFactory().getDownloader(RESOURCE_PATH + "invalid"));
            dummyCookie.put(YoutubeMixPlaylistExtractor.COOKIE_NAME, "whatever");
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

    public static class ChannelMix {

        private static final String CHANNEL_ID = "UCXuqSBlHAE6Xw-yeJA0Tunw";
        private static final String VIDEO_ID_OF_CHANNEL = "mnk6gnOBYIo";
        private static final String CHANNEL_TITLE = "Linus Tech Tips";


        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeParsingHelper.resetClientVersionAndKey();
            YoutubeParsingHelper.setNumberGenerator(new Random(1));
            NewPipe.init(new DownloaderFactory().getDownloader(RESOURCE_PATH + "channelMix"));
            dummyCookie.put(YoutubeMixPlaylistExtractor.COOKIE_NAME, "whatever");
            extractor = (YoutubeMixPlaylistExtractor) YouTube
                    .getPlaylistExtractor("https://www.youtube.com/watch?v=" + VIDEO_ID_OF_CHANNEL
                            + "&list=RDCM" + CHANNEL_ID);
            extractor.fetchPage();
        }

        @Test
        void getName() throws Exception {
            final String name = extractor.getName();
            ExtractorAsserts.assertContains("Mix", name);
            ExtractorAsserts.assertContains(CHANNEL_TITLE, name);
        }

        @Test
        void getThumbnailUrl() throws Exception {
            final String thumbnailUrl = extractor.getThumbnailUrl();
            assertIsSecureUrl(thumbnailUrl);
            ExtractorAsserts.assertContains("yt", thumbnailUrl);
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
                    .value("videoId", VIDEO_ID_OF_CHANNEL)
                    .value("playlistId", "RDCM" + CHANNEL_ID)
                    .value("params", "OAE%3D")
                    .done())
                    .getBytes(StandardCharsets.UTF_8);

            final InfoItemsPage<StreamInfoItem> streams = extractor.getPage(new Page(
                    YOUTUBEI_V1_URL + "next?key=" + getKey(), null, null, dummyCookie, body));
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        void getStreamCount() {
            assertEquals(ListExtractor.ITEM_COUNT_INFINITE, extractor.getStreamCount());
        }
    }
}
