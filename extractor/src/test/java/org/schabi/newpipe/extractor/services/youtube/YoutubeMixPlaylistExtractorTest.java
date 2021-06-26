package org.schabi.newpipe.extractor.services.youtube;

import com.grack.nanojson.JsonWriter;
import org.hamcrest.MatcherAssert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.schabi.newpipe.downloader.DownloaderFactory;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.youtube.YoutubeMixPlaylistExtractorTest.ChannelMix;
import org.schabi.newpipe.extractor.services.youtube.YoutubeMixPlaylistExtractorTest.Invalid;
import org.schabi.newpipe.extractor.services.youtube.YoutubeMixPlaylistExtractorTest.Mix;
import org.schabi.newpipe.extractor.services.youtube.YoutubeMixPlaylistExtractorTest.MixWithIndex;
import org.schabi.newpipe.extractor.services.youtube.YoutubeMixPlaylistExtractorTest.MyMix;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeMixPlaylistExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.*;
import static org.schabi.newpipe.extractor.utils.Utils.UTF_8;

@RunWith(Suite.class)
@SuiteClasses({Mix.class, MixWithIndex.class, MyMix.class, Invalid.class, ChannelMix.class})
public class YoutubeMixPlaylistExtractorTest {

    private static final String VIDEO_ID = "_AzeUSL9lZc";
    private static final String VIDEO_TITLE =
            "Most Beautiful And Emotional  Piano: Anime Music Shigatsu wa Kimi no Uso OST IMO";
    private static final String RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/mix/";
    private static final Map<String, String> dummyCookie = new HashMap<>();

    private static YoutubeMixPlaylistExtractor extractor;

    public static class Mix {

        @BeforeClass
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
        public void getServiceId() {
            assertEquals(YouTube.getServiceId(), extractor.getServiceId());
        }

        @Test
        public void getName() throws Exception {
            final String name = extractor.getName();
            assertThat(name, startsWith("Mix"));
            assertThat(name, containsString(VIDEO_TITLE));
        }

        @Test
        public void getThumbnailUrl() throws Exception {
            final String thumbnailUrl = extractor.getThumbnailUrl();
            assertIsSecureUrl(thumbnailUrl);
            MatcherAssert.assertThat(thumbnailUrl, containsString("yt"));
            assertThat(thumbnailUrl, containsString(VIDEO_ID));
        }

        @Test
        public void getInitialPage() throws Exception {
            final InfoItemsPage<StreamInfoItem> streams = extractor.getInitialPage();
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        public void getPage() throws Exception {
            final byte[] body = JsonWriter.string(prepareJsonBuilder(
                    NewPipe.getPreferredLocalization(), NewPipe.getPreferredContentCountry())
                    .value("videoId", VIDEO_ID)
                    .value("playlistId", "RD" + VIDEO_ID)
                    .value("params", "OAE%3D")
                    .done())
                    .getBytes(UTF_8);

            final InfoItemsPage<StreamInfoItem> streams = extractor.getPage(new Page(
                    YOUTUBEI_V1_URL + "next?key=" + getKey(), null, null, dummyCookie, body));
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        public void getContinuations() throws Exception {
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
        public void getStreamCount() {
            assertEquals(ListExtractor.ITEM_COUNT_INFINITE, extractor.getStreamCount());
        }
    }

    @Ignore("Test broken, video was removed by the uploader")
    public static class MixWithIndex {

        private static final int INDEX = 13;
        private static final String VIDEO_ID_NUMBER_13 = "qHtzO49SDmk";

        @BeforeClass
        public static void setUp() throws Exception {
            YoutubeParsingHelper.resetClientVersionAndKey();
            YoutubeParsingHelper.setNumberGenerator(new Random(1));
            NewPipe.init(new DownloaderFactory().getDownloader(RESOURCE_PATH + "mixWithIndex"));
            dummyCookie.put(YoutubeMixPlaylistExtractor.COOKIE_NAME, "whatever");
            extractor = (YoutubeMixPlaylistExtractor) YouTube
                    .getPlaylistExtractor("https://www.youtube.com/watch?v=" + VIDEO_ID_NUMBER_13
                            + "&list=RD" + VIDEO_ID + "&index=" + INDEX);
            extractor.fetchPage();
        }

        @Test
        public void getName() throws Exception {
            final String name = extractor.getName();
            assertThat(name, startsWith("Mix"));
            assertThat(name, containsString(VIDEO_TITLE));
        }

        @Test
        public void getThumbnailUrl() throws Exception {
            final String thumbnailUrl = extractor.getThumbnailUrl();
            assertIsSecureUrl(thumbnailUrl);
            assertThat(thumbnailUrl, containsString("yt"));
            assertThat(thumbnailUrl, containsString(VIDEO_ID));
        }

        @Test
        public void getInitialPage() throws Exception {
            final InfoItemsPage<StreamInfoItem> streams = extractor.getInitialPage();
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        public void getPage() throws Exception {
            final byte[] body = JsonWriter.string(prepareJsonBuilder(
                    NewPipe.getPreferredLocalization(), NewPipe.getPreferredContentCountry())
                    .value("videoId", VIDEO_ID)
                    .value("playlistId", "RD" + VIDEO_ID)
                    .value("playlistIndex", INDEX)
                    .value("params", "OAE%3D")
                    .done())
                    .getBytes(UTF_8);

            final InfoItemsPage<StreamInfoItem> streams = extractor.getPage(new Page(
                    YOUTUBEI_V1_URL + "next?key=" + getKey(), null, null, dummyCookie, body));
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        public void getContinuations() throws Exception {
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
        public void getStreamCount() {
            assertEquals(ListExtractor.ITEM_COUNT_INFINITE, extractor.getStreamCount());
        }
    }

    public static class MyMix {

        @BeforeClass
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
        public void getServiceId() {
            assertEquals(YouTube.getServiceId(), extractor.getServiceId());
        }

        @Test
        public void getName() throws Exception {
            final String name = extractor.getName();
            assertEquals("My Mix", name);
        }

        @Test
        public void getThumbnailUrl() throws Exception {
            final String thumbnailUrl = extractor.getThumbnailUrl();
            assertIsSecureUrl(thumbnailUrl);
            assertThat(thumbnailUrl, startsWith("https://i.ytimg.com/vi/_AzeUSL9lZc"));
        }

        @Test
        public void getInitialPage() throws Exception {
            final InfoItemsPage<StreamInfoItem> streams = extractor.getInitialPage();
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        public void getPage() throws Exception {
            final byte[] body = JsonWriter.string(prepareJsonBuilder(
                    NewPipe.getPreferredLocalization(), NewPipe.getPreferredContentCountry())
                    .value("videoId", VIDEO_ID)
                    .value("playlistId", "RDMM" + VIDEO_ID)
                    .value("params", "OAE%3D")
                    .done())
                    .getBytes(UTF_8);

            final InfoItemsPage<StreamInfoItem> streams = extractor.getPage(new Page(
                    YOUTUBEI_V1_URL + "next?key=" + getKey(), null, null, dummyCookie, body));
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        public void getContinuations() throws Exception {
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
        public void getStreamCount() {
            assertEquals(ListExtractor.ITEM_COUNT_INFINITE, extractor.getStreamCount());
        }
    }

    public static class Invalid {

        @BeforeClass
        public static void setUp() throws IOException {
            YoutubeParsingHelper.resetClientVersionAndKey();
            YoutubeParsingHelper.setNumberGenerator(new Random(1));
            NewPipe.init(new DownloaderFactory().getDownloader(RESOURCE_PATH + "invalid"));
            dummyCookie.put(YoutubeMixPlaylistExtractor.COOKIE_NAME, "whatever");
        }

        @Test(expected = IllegalArgumentException.class)
        public void getPageEmptyUrl() throws Exception {
            extractor = (YoutubeMixPlaylistExtractor) YouTube
                    .getPlaylistExtractor("https://www.youtube.com/watch?v=" + VIDEO_ID
                            + "&list=RD" + VIDEO_ID);
            extractor.fetchPage();
            extractor.getPage(new Page(""));
        }

        @Test(expected = ExtractionException.class)
        public void invalidVideoId() throws Exception {
            extractor = (YoutubeMixPlaylistExtractor) YouTube
                    .getPlaylistExtractor("https://www.youtube.com/watch?v=" + "abcde"
                            + "&list=RD" + "abcde");
            extractor.fetchPage();
            extractor.getName();
        }
    }

    public static class ChannelMix {

        private static final String CHANNEL_ID = "UCXuqSBlHAE6Xw-yeJA0Tunw";
        private static final String VIDEO_ID_OF_CHANNEL = "mnk6gnOBYIo";
        private static final String CHANNEL_TITLE = "Linus Tech Tips";


        @BeforeClass
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
        public void getName() throws Exception {
            final String name = extractor.getName();
            assertThat(name, startsWith("Mix"));
            assertThat(name, containsString(CHANNEL_TITLE));
        }

        @Test
        public void getThumbnailUrl() throws Exception {
            final String thumbnailUrl = extractor.getThumbnailUrl();
            assertIsSecureUrl(thumbnailUrl);
            assertThat(thumbnailUrl, containsString("yt"));
        }

        @Test
        public void getInitialPage() throws Exception {
            final InfoItemsPage<StreamInfoItem> streams = extractor.getInitialPage();
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        public void getPage() throws Exception {
            final byte[] body = JsonWriter.string(prepareJsonBuilder(
                    NewPipe.getPreferredLocalization(), NewPipe.getPreferredContentCountry())
                    .value("videoId", VIDEO_ID_OF_CHANNEL)
                    .value("playlistId", "RDCM" + CHANNEL_ID)
                    .value("params", "OAE%3D")
                    .done())
                    .getBytes(UTF_8);

            final InfoItemsPage<StreamInfoItem> streams = extractor.getPage(new Page(
                    YOUTUBEI_V1_URL + "next?key=" + getKey(), null, null, dummyCookie, body));
            assertFalse(streams.getItems().isEmpty());
            assertTrue(streams.hasNextPage());
        }

        @Test
        public void getStreamCount() {
            assertEquals(ListExtractor.ITEM_COUNT_INFINITE, extractor.getStreamCount());
        }
    }
}
