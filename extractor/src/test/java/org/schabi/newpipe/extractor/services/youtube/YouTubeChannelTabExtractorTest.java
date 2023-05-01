package org.schabi.newpipe.extractor.services.youtube;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderFactory;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ChannelTabs;
import org.schabi.newpipe.extractor.services.BaseListExtractorTest;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeChannelTabExtractor;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestMoreItems;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestRelatedItems;

public class YouTubeChannelTabExtractorTest {
    private static final String RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/channelTab/";

    public static class Playlists implements BaseListExtractorTest {
        private static YoutubeChannelTabExtractor extractor;

        @BeforeAll
        public static void setUp() throws IOException, ExtractionException {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "playlists"));
            extractor = (YoutubeChannelTabExtractor) YouTube.getChannelTabExtractorFromId(
                    "channel/UC2DjFE7Xf11URZqWBigcVOQ", ChannelTabs.PLAYLISTS);
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
        public void testTab() {
            assertEquals(ChannelTabs.PLAYLISTS, extractor.getTab());
        }

        @Test
        @Override
        public void testId() throws ParsingException {
            assertEquals("UC2DjFE7Xf11URZqWBigcVOQ", extractor.getId());
        }

        @Test
        @Override
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UC2DjFE7Xf11URZqWBigcVOQ/playlists", extractor.getUrl());
        }

        @Test
        @Override
        public void testOriginalUrl() throws Exception {
            assertEquals("https://www.youtube.com/channel/UC2DjFE7Xf11URZqWBigcVOQ/playlists", extractor.getOriginalUrl());
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

    public static class Channels implements BaseListExtractorTest {
        private static YoutubeChannelTabExtractor extractor;

        @BeforeAll
        public static void setUp() throws IOException, ExtractionException {
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
        public void testTab() {
            assertEquals(ChannelTabs.CHANNELS, extractor.getTab());
        }

        @Test
        @Override
        public void testId() throws ParsingException {
            assertEquals("UC2DjFE7Xf11URZqWBigcVOQ", extractor.getId());
        }

        @Test
        @Override
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UC2DjFE7Xf11URZqWBigcVOQ/channels", extractor.getUrl());
        }

        @Override
        public void testOriginalUrl() throws Exception {

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

    public static class Livestreams implements BaseListExtractorTest {
        private static YoutubeChannelTabExtractor extractor;

        @BeforeAll
        public static void setUp() throws IOException, ExtractionException {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "livestreams"));
            extractor = (YoutubeChannelTabExtractor) YouTube.getChannelTabExtractorFromId(
                    "channel/UCR-DXc1voovS8nhAvccRZhg", ChannelTabs.LIVESTREAMS);
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
        public void testTab() {
            assertEquals(ChannelTabs.LIVESTREAMS, extractor.getTab());
        }

        @Test
        @Override
        public void testId() throws ParsingException {
            assertEquals("UCR-DXc1voovS8nhAvccRZhg", extractor.getId());
        }

        @Test
        @Override
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UCR-DXc1voovS8nhAvccRZhg/streams", extractor.getUrl());
        }

        @Override
        public void testOriginalUrl() throws Exception {
            assertEquals("https://www.youtube.com/channel/UCR-DXc1voovS8nhAvccRZhg/streams", extractor.getOriginalUrl());
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

    public static class Shorts implements BaseListExtractorTest {
        private static YoutubeChannelTabExtractor extractor;

        @BeforeAll
        public static void setUp() throws IOException, ExtractionException {
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

        @Override
        public void testName() throws Exception {
            assertEquals(ChannelTabs.SHORTS, extractor.getName());
        }

        @Test
        public void testTab() {
            assertEquals(ChannelTabs.SHORTS, extractor.getTab());
        }

        @Test
        @Override
        public void testId() throws ParsingException {
            assertEquals("UCh8gHdtzO2tXd593_bjErWg", extractor.getId());
        }

        @Test
        @Override
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UCh8gHdtzO2tXd593_bjErWg/shorts", extractor.getUrl());
        }

        @Test
        @Override
        public void testOriginalUrl() throws Exception {
            assertEquals("https://www.youtube.com/channel/UCh8gHdtzO2tXd593_bjErWg/shorts", extractor.getOriginalUrl());
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
}
