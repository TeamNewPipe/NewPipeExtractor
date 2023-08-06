package org.schabi.newpipe.extractor.services.soundcloud;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.BaseListExtractorTest;
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudChannelTabExtractor;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestGetPageInNewExtractor;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestMoreItems;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestRelatedItems;

class SoundcloudChannelTabExtractorTest {

    static class Tracks implements BaseListExtractorTest {
        private static SoundcloudChannelTabExtractor extractor;

        @BeforeAll
        static void setUp() throws IOException, ExtractionException {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (SoundcloudChannelTabExtractor) SoundCloud
                    .getChannelTabExtractorFromId("10494998", ChannelTabs.TRACKS);
            extractor.fetchPage();
        }

        @Test
        @Override
        public void testServiceId() throws Exception {
            assertEquals(SoundCloud.getServiceId(), extractor.getServiceId());
        }

        @Test
        @Override
        public void testName() throws Exception {
            assertEquals(ChannelTabs.TRACKS, extractor.getName());
        }

        @Test
        @Override
        public void testId() throws Exception {
            assertEquals("10494998", extractor.getId());
        }

        @Test
        @Override
        public void testUrl() throws Exception {
            assertEquals("https://soundcloud.com/liluzivert/tracks", extractor.getUrl());
        }

        @Test
        @Override
        public void testOriginalUrl() throws Exception {
            assertEquals("https://soundcloud.com/liluzivert/tracks", extractor.getOriginalUrl());
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

        /*//////////////////////////////////////////////////////////////////////////
        // Additional Testing
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        void testGetPageInNewExtractor() throws Exception {
            final ChannelTabExtractor newTabExtractor =
                    SoundCloud.getChannelTabExtractorFromId("10494998", ChannelTabs.TRACKS);
            defaultTestGetPageInNewExtractor(extractor, newTabExtractor);
        }
    }

    static class Playlists implements BaseListExtractorTest {
        private static SoundcloudChannelTabExtractor extractor;

        @BeforeAll
        static void setUp() throws IOException, ExtractionException {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (SoundcloudChannelTabExtractor) SoundCloud
                    .getChannelTabExtractorFromId("323371733", ChannelTabs.PLAYLISTS);
            extractor.fetchPage();
        }

        @Test
        @Override
        public void testServiceId() {
            assertEquals(SoundCloud.getServiceId(), extractor.getServiceId());
        }

        @Test
        @Override
        public void testName() throws Exception {
            assertEquals(ChannelTabs.PLAYLISTS, extractor.getName());
        }

        @Test
        @Override
        public void testId() throws ParsingException {
            assertEquals("323371733", extractor.getId());
        }

        @Test
        @Override
        public void testUrl() throws ParsingException {
            assertEquals("https://soundcloud.com/trackaholic/sets", extractor.getUrl());
        }

        @Test
        @Override
        public void testOriginalUrl() throws Exception {
            assertEquals("https://soundcloud.com/trackaholic/sets", extractor.getOriginalUrl());
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

    static class Albums implements BaseListExtractorTest {
        private static SoundcloudChannelTabExtractor extractor;

        @BeforeAll
        static void setUp() throws IOException, ExtractionException {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (SoundcloudChannelTabExtractor) SoundCloud
                    .getChannelTabExtractorFromId("4803918", ChannelTabs.ALBUMS);
            extractor.fetchPage();
        }

        @Test
        @Override
        public void testServiceId() {
            assertEquals(SoundCloud.getServiceId(), extractor.getServiceId());
        }

        @Test
        @Override
        public void testName() throws Exception {
            assertEquals(ChannelTabs.ALBUMS, extractor.getName());
        }

        @Test
        @Override
        public void testId() throws ParsingException {
            assertEquals("4803918", extractor.getId());
        }

        @Test
        @Override
        public void testUrl() throws ParsingException {
            assertEquals("https://soundcloud.com/bigsean-1/albums", extractor.getUrl());
        }

        @Test
        @Override
        public void testOriginalUrl() throws Exception {
            assertEquals("https://soundcloud.com/bigsean-1/albums", extractor.getOriginalUrl());
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
