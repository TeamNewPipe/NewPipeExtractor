package org.schabi.newpipe.extractor.services.youtube;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderFactory;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.BaseListExtractorTest;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeChannelTabExtractor;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestMoreItems;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestRelatedItems;

class YoutubeChannelTabExtractorTest {

    private static final String RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH
            + "services/youtube/extractor/channelTabs/";

    static class Videos implements BaseListExtractorTest {
        private static YoutubeChannelTabExtractor extractor;

        @BeforeAll
        static void setUp() throws IOException, ExtractionException {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "videos"));
            extractor = (YoutubeChannelTabExtractor) YouTube.getChannelTabExtractorFromId(
                    "user/creativecommons", ChannelTabs.VIDEOS);
            extractor.fetchPage();
        }

        @Test
        @Override
        public void testServiceId() {
            assertEquals(YouTube.getServiceId(), extractor.getServiceId());
        }

        @Test
        @Override
        public void testName() throws Exception {
            assertEquals(ChannelTabs.VIDEOS, extractor.getName());
        }

        @Test
        @Override
        public void testId() throws ParsingException {
            assertEquals("UCTwECeGqMZee77BjdoYtI2Q", extractor.getId());
        }

        @Test
        @Override
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UCTwECeGqMZee77BjdoYtI2Q/videos",
                    extractor.getUrl());
        }

        @Test
        @Override
        public void testOriginalUrl() throws Exception {
            assertEquals("https://www.youtube.com/user/creativecommons/videos",
                    extractor.getOriginalUrl());
        }

        @Test
        @Override
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor);
        }

        @Test
        @Override
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(extractor);
        }
    }

    static class Playlists implements BaseListExtractorTest {
        private static YoutubeChannelTabExtractor extractor;

        @BeforeAll
        static void setUp() throws IOException, ExtractionException {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "playlists"));
            extractor = (YoutubeChannelTabExtractor) YouTube.getChannelTabExtractorFromId(
                    "@EEVblog", ChannelTabs.PLAYLISTS);
            extractor.fetchPage();
        }

        @Test
        @Override
        public void testServiceId() {
            assertEquals(YouTube.getServiceId(), extractor.getServiceId());
        }

        @Test
        @Override
        public void testName() throws Exception {
            assertEquals(ChannelTabs.PLAYLISTS, extractor.getName());
        }

        @Test
        @Override
        public void testId() throws ParsingException {
            assertEquals("UC2DjFE7Xf11URZqWBigcVOQ", extractor.getId());
        }

        @Test
        @Override
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UC2DjFE7Xf11URZqWBigcVOQ/playlists",
                    extractor.getUrl());
        }

        @Test
        @Override
        public void testOriginalUrl() throws Exception {
            assertEquals("https://www.youtube.com/@EEVblog/playlists",
                    extractor.getOriginalUrl());
        }

        @Test
        @Override
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor);
        }

        @Test
        @Override
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(extractor);
        }
    }

    static class Channels implements BaseListExtractorTest {
        private static YoutubeChannelTabExtractor extractor;

        @BeforeAll
        static void setUp() throws IOException, ExtractionException {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "channels"));
            extractor = (YoutubeChannelTabExtractor) YouTube.getChannelTabExtractorFromId(
                    "channel/UC2DjFE7Xf11URZqWBigcVOQ", ChannelTabs.CHANNELS);
            extractor.fetchPage();
        }

        @Test
        @Override
        public void testServiceId() {
            assertEquals(YouTube.getServiceId(), extractor.getServiceId());
        }

        @Test
        @Override
        public void testName() throws Exception {
            assertEquals(ChannelTabs.CHANNELS, extractor.getName());
        }

        @Test
        @Override
        public void testId() throws ParsingException {
            assertEquals("UC2DjFE7Xf11URZqWBigcVOQ", extractor.getId());
        }

        @Test
        @Override
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UC2DjFE7Xf11URZqWBigcVOQ/channels",
                    extractor.getUrl());
        }

        @Test
        @Override
        public void testOriginalUrl() throws Exception {
            assertEquals("https://www.youtube.com/channel/UC2DjFE7Xf11URZqWBigcVOQ/channels",
                    extractor.getUrl());
        }

        @Test
        @Override
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor);
        }

        @Test
        @Override
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(extractor);
        }
    }

    static class Livestreams implements BaseListExtractorTest {
        private static YoutubeChannelTabExtractor extractor;

        @BeforeAll
        static void setUp() throws IOException, ExtractionException {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "livestreams"));
            extractor = (YoutubeChannelTabExtractor) YouTube.getChannelTabExtractorFromId(
                    "c/JeffGeerling", ChannelTabs.LIVESTREAMS);
            extractor.fetchPage();
        }

        @Test
        @Override
        public void testServiceId() {
            assertEquals(YouTube.getServiceId(), extractor.getServiceId());
        }

        @Test
        @Override
        public void testName() throws Exception {
            assertEquals(ChannelTabs.LIVESTREAMS, extractor.getName());
        }

        @Test
        @Override
        public void testId() throws ParsingException {
            assertEquals("UCR-DXc1voovS8nhAvccRZhg", extractor.getId());
        }

        @Test
        @Override
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UCR-DXc1voovS8nhAvccRZhg/streams",
                    extractor.getUrl());
        }

        @Test
        @Override
        public void testOriginalUrl() throws Exception {
            assertEquals("https://www.youtube.com/c/JeffGeerling/streams",
                    extractor.getOriginalUrl());
        }

        @Test
        @Override
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor);
        }

        @Test
        @Override
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(extractor);
        }
    }

    static class Shorts implements BaseListExtractorTest {
        private static YoutubeChannelTabExtractor extractor;

        @BeforeAll
        static void setUp() throws IOException, ExtractionException {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "shorts"));
            extractor = (YoutubeChannelTabExtractor) YouTube.getChannelTabExtractorFromId(
                    "channel/UCh8gHdtzO2tXd593_bjErWg", ChannelTabs.SHORTS);
            extractor.fetchPage();
        }

        @Test
        @Override
        public void testServiceId() {
            assertEquals(YouTube.getServiceId(), extractor.getServiceId());
        }

        @Test
        @Override
        public void testName() throws Exception {
            assertEquals(ChannelTabs.SHORTS, extractor.getName());
        }

        @Test
        @Override
        public void testId() throws ParsingException {
            assertEquals("UCh8gHdtzO2tXd593_bjErWg", extractor.getId());
        }

        @Test
        @Override
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UCh8gHdtzO2tXd593_bjErWg/shorts",
                    extractor.getUrl());
        }

        @Test
        @Override
        public void testOriginalUrl() throws Exception {
            assertEquals("https://www.youtube.com/channel/UCh8gHdtzO2tXd593_bjErWg/shorts",
                    extractor.getOriginalUrl());
        }

        @Test
        @Override
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor);
        }

        @Test
        @Override
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(extractor);
        }
    }

    static class AgeRestrictedTabs implements BaseListExtractorTest {
        private static ChannelTabExtractor videosTabExtractor;
        private static ChannelTabExtractor shortsTabExtractor;

        @BeforeAll
        static void setUp() throws IOException, ExtractionException {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "ageRestricted"));
            final ChannelExtractor extractor = YouTube.getChannelExtractor(
                    "https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig");
            extractor.fetchPage();

            // Fetching the tabs individually would use the standard tabs without fallback to
            // system playlists for stream tabs, we need to fetch the channel extractor to get the
            // channel playlist tabs
            // TODO: implement system playlists fallback in YoutubeChannelTabExtractor for stream
            //  tabs
            final List<ListLinkHandler> tabs = extractor.getTabs();
            videosTabExtractor = YouTube.getChannelTabExtractor(tabs.get(0));
            videosTabExtractor.fetchPage();
            shortsTabExtractor = YouTube.getChannelTabExtractor(tabs.get(1));
            shortsTabExtractor.fetchPage();
        }

        @Test
        @Override
        public void testServiceId() {
            assertEquals(YouTube.getServiceId(), videosTabExtractor.getServiceId());
            assertEquals(YouTube.getServiceId(), shortsTabExtractor.getServiceId());
        }

        @Test
        @Override
        public void testName() throws Exception {
            assertEquals(ChannelTabs.VIDEOS, videosTabExtractor.getName());
            assertEquals(ChannelTabs.SHORTS, shortsTabExtractor.getName());
        }

        @Test
        @Override
        public void testId() throws ParsingException {
            assertEquals("UCbfnHqxXs_K3kvaH-WlNlig", videosTabExtractor.getId());
            assertEquals("UCbfnHqxXs_K3kvaH-WlNlig", shortsTabExtractor.getId());
        }

        @Test
        @Override
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig/videos",
                    videosTabExtractor.getUrl());
            assertEquals("https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig/shorts",
                    shortsTabExtractor.getUrl());
        }

        @Test
        @Override
        public void testOriginalUrl() throws Exception {
            assertEquals("https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig/videos",
                    videosTabExtractor.getOriginalUrl());
            assertEquals("https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig/shorts",
                    shortsTabExtractor.getOriginalUrl());
        }

        @Test
        @Override
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(videosTabExtractor);
            // No shorts on this channel, the channel tab playlist extractor should return no
            // streams
            assertTrue(shortsTabExtractor.getInitialPage().getItems().isEmpty());
        }

        @Test
        @Override
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(videosTabExtractor);
            // No shorts on this channel, the channel tab playlist extractor should return no
            // streams
            assertFalse(shortsTabExtractor.getInitialPage().hasNextPage());
        }
    }
}
