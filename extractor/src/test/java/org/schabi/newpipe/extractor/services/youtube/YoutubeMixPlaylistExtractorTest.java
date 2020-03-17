package org.schabi.newpipe.extractor.services.youtube;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeMixPlaylistExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

public class YoutubeMixPlaylistExtractorTest {

    private static YoutubeMixPlaylistExtractor extractor;
    private static String videoId = "_AzeUSL9lZc";
    private static String videoTitle = "Most Beautiful And Emotional  Piano: Anime Music Shigatsu wa Kimi no Uso OST IMO";

    public static class Mix {

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (YoutubeMixPlaylistExtractor) YouTube
                .getPlaylistExtractor(
                    "https://www.youtube.com/watch?v=" + videoId + "&list=RD" + videoId);
            extractor.fetchPage();
        }

        @Test
        public void getServiceId() {
            assertEquals(YouTube.getServiceId(), extractor.getServiceId());
        }

        @Test
        public void getName() throws Exception {
            String name = extractor.getName();
            assertThat(name, startsWith("Mix"));
            assertThat(name, containsString(videoTitle));
        }

        @Test
        public void getThumbnailUrl() throws Exception {
            final String thumbnailUrl = extractor.getThumbnailUrl();
            assertIsSecureUrl(thumbnailUrl);
            assertThat(thumbnailUrl, containsString("yt"));
            assertThat(thumbnailUrl, containsString(videoId));
        }

        @Test
        public void getNextPageUrl() throws Exception {
            final String nextPageUrl = extractor.getNextPageUrl();
            assertIsSecureUrl(nextPageUrl);
            assertThat(nextPageUrl, containsString("list=RD" + videoId));
        }

        @Test
        public void getInitialPage() throws Exception {
            InfoItemsPage<StreamInfoItem> streams = extractor.getInitialPage();
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        public void getPage() throws Exception {
            InfoItemsPage<StreamInfoItem> streams = extractor.getPage(extractor.getNextPageUrl());
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        public void getPageMultipleTimes() throws Exception {
            InfoItemsPage<StreamInfoItem> streams = extractor.getPage(extractor.getNextPageUrl());

            //Should work infinitely, but for testing purposes only 3 times
            for (int i = 0; i < 3; i++) {
                assertTrue(streams.hasNextPage());
                assertFalse(streams.getItems().isEmpty());

                streams = extractor.getPage(streams.getNextPageUrl());
            }
            assertTrue(streams.hasNextPage());
            assertFalse(streams.getItems().isEmpty());
        }

        @Test
        public void getStreamCount() throws Exception {
            assertEquals(ListExtractor.ITEM_COUNT_INFINITE, extractor.getStreamCount());
        }

        @Test
        public void getStreamCount() throws Exception {
            assertEquals(ListExtractor.ITEM_COUNT_INFINITE, extractor.getStreamCount());
        }
    }

    public static class MixWithIndex {

        private static String index = "&index=13";
        private static String videoIdNumber13 = "qHtzO49SDmk";

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (YoutubeMixPlaylistExtractor) YouTube
                .getPlaylistExtractor(
                    "https://www.youtube.com/watch?v=" + videoIdNumber13 + "&list=RD" + videoId
                        + index);
            extractor.fetchPage();
        }

        @Test
        public void getName() throws Exception {
            String name = extractor.getName();
            assertThat(name, startsWith("Mix"));
            assertThat(name, containsString(videoTitle));
        }

        @Test
        public void getThumbnailUrl() throws Exception {
            final String thumbnailUrl = extractor.getThumbnailUrl();
            assertIsSecureUrl(thumbnailUrl);
            assertThat(thumbnailUrl, containsString("yt"));
            assertThat(thumbnailUrl, containsString(videoId));
        }

        @Test
        public void getNextPageUrl() throws Exception {
            final String nextPageUrl = extractor.getNextPageUrl();
            assertIsSecureUrl(nextPageUrl);
            assertThat(nextPageUrl, containsString("list=RD" + videoId));
        }

        @Test
        public void getInitialPage() throws Exception {
            InfoItemsPage<StreamInfoItem> streams = extractor.getInitialPage();
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        public void getPage() throws Exception {
            InfoItemsPage<StreamInfoItem> streams = extractor.getPage(extractor.getNextPageUrl());
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        public void getPageMultipleTimes() throws Exception {
            InfoItemsPage<StreamInfoItem> streams = extractor.getPage(extractor.getNextPageUrl());

            //Should work infinitely, but for testing purposes only 3 times
            for (int i = 0; i < 3; i++) {
                assertTrue(streams.hasNextPage());
                assertFalse(streams.getItems().isEmpty());

                streams = extractor.getPage(streams.getNextPageUrl());
            }
            assertTrue(streams.hasNextPage());
            assertFalse(streams.getItems().isEmpty());
        }

        @Test
        public void getStreamCount() {
            assertEquals(ListExtractor.ITEM_COUNT_INFINITE, extractor.getStreamCount());
        }

        @Test
        public void getStreamCount() throws Exception {
            assertEquals(ListExtractor.ITEM_COUNT_INFINITE, extractor.getStreamCount());
        }
    }

    public static class MyMix {

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (YoutubeMixPlaylistExtractor) YouTube
                .getPlaylistExtractor(
                    "https://www.youtube.com/watch?v=" + videoId + "&list=RDMM" + videoId);
            extractor.fetchPage();
        }

        @Test
        public void getServiceId() {
            assertEquals(YouTube.getServiceId(), extractor.getServiceId());
        }

        @Test
        public void getName() throws Exception {
            String name = extractor.getName();
            assertEquals("My Mix", name);
        }

        @Test
        public void getThumbnailUrl() throws Exception {
            final String thumbnailUrl = extractor.getThumbnailUrl();
            assertIsSecureUrl(thumbnailUrl);
            assertThat(thumbnailUrl, startsWith("https://i.ytimg.com/vi/_AzeUSL9lZc"));
        }

        @Test
        public void getNextPageUrl() throws Exception {
            final String nextPageUrl = extractor.getNextPageUrl();
            assertIsSecureUrl(nextPageUrl);
            assertThat(nextPageUrl, containsString("list=RDMM" + videoId));
        }

        @Test
        public void getInitialPage() throws Exception {
            InfoItemsPage<StreamInfoItem> streams = extractor.getInitialPage();
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        public void getPage() throws Exception {
            InfoItemsPage<StreamInfoItem> streams = extractor.getPage(extractor.getNextPageUrl());
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        public void getPageMultipleTimes() throws Exception {
            InfoItemsPage<StreamInfoItem> streams = extractor.getPage(extractor.getNextPageUrl());

            //Should work infinitely, but for testing purposes only 3 times
            for (int i = 0; i < 3; i++) {
                assertTrue(streams.hasNextPage());
                assertFalse(streams.getItems().isEmpty());

                streams = extractor.getPage(streams.getNextPageUrl());
            }
            assertTrue(streams.hasNextPage());
            assertFalse(streams.getItems().isEmpty());
        }

        @Test
        public void getStreamCount() throws Exception {
            assertEquals(ListExtractor.ITEM_COUNT_INFINITE, extractor.getStreamCount());
        }
    }

    public static class Invalid {

        @BeforeClass
        public static void setUp() {
            NewPipe.init(DownloaderTestImpl.getInstance());
        }

        @Test(expected = ExtractionException.class)
        public void getPageEmptyUrl() throws Exception {
            extractor = (YoutubeMixPlaylistExtractor) YouTube
                .getPlaylistExtractor(
                    "https://www.youtube.com/watch?v=" + videoId + "&list=RD" + videoId);
            extractor.fetchPage();
            extractor.getPage("");
        }

        @Test(expected = NullPointerException.class)
        public void invalidVideoId() throws Exception {
            extractor = (YoutubeMixPlaylistExtractor) YouTube
                .getPlaylistExtractor(
                    "https://www.youtube.com/watch?v=" + "abcde" + "&list=RD" + "abcde");
            extractor.fetchPage();
        }
    }

    public static class ChannelMix {

        private static String channelId = "UCXuqSBlHAE6Xw-yeJA0Tunw";
        private static String videoIdOfChannel = "mnk6gnOBYIo";
        private static String channelTitle = "Linus Tech Tips";



        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (YoutubeMixPlaylistExtractor) YouTube
                .getPlaylistExtractor(
                    "https://www.youtube.com/watch?v=" + videoIdOfChannel + "&list=RDCM" + channelId);
            extractor.fetchPage();
        }

        @Test
        public void getName() throws Exception {
            String name = extractor.getName();
            assertThat(name, startsWith("Mix"));
            assertThat(name, containsString(channelTitle));
        }

        @Test
        public void getThumbnailUrl() throws Exception {
            final String thumbnailUrl = extractor.getThumbnailUrl();
            assertIsSecureUrl(thumbnailUrl);
            assertThat(thumbnailUrl, containsString("yt"));
        }

        @Test
        public void getNextPageUrl() throws Exception {
            final String nextPageUrl = extractor.getNextPageUrl();
            assertIsSecureUrl(nextPageUrl);
            assertThat(nextPageUrl, containsString("list=RDCM" + channelId));
        }

        @Test
        public void getInitialPage() throws Exception {
            InfoItemsPage<StreamInfoItem> streams = extractor.getInitialPage();
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        public void getPage() throws Exception {
            InfoItemsPage<StreamInfoItem> streams = extractor.getPage(extractor.getNextPageUrl());
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        public void getStreamCount() throws Exception {
            assertEquals(ListExtractor.ITEM_COUNT_INFINITE, extractor.getStreamCount());
        }

        @Test
        public void getStreamCount() throws Exception {
            assertEquals(ListExtractor.ITEM_COUNT_INFINITE, extractor.getStreamCount());
        }
    }
}