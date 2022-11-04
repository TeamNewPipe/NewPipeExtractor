package org.schabi.newpipe.extractor.services.soundcloud;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ChannelTabs;
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudChannelTabExtractor;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestMoreItems;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestRelatedItems;

public class SoundcloudChannelTabExtractorTest {
    public static class Playlists {
        private static SoundcloudChannelTabExtractor extractor;

        @BeforeAll
        public static void setUp() throws IOException, ExtractionException {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (SoundcloudChannelTabExtractor) SoundCloud
                    .getChannelTabExtractorFromId("323371733", ChannelTabs.PLAYLISTS);
            extractor.fetchPage();
        }

        @Test
        public void testServiceId() {
            assertEquals(SoundCloud.getServiceId(), extractor.getServiceId());
        }

        @Test
        public void testTab() {
            assertEquals(ChannelTabs.PLAYLISTS, extractor.getTab());
        }

        @Test
        public void testId() throws ParsingException {
            assertEquals("323371733", extractor.getId());
        }

        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://soundcloud.com/trackaholic/sets", extractor.getUrl());
        }

        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor);
        }

        @Test
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(extractor);
        }
    }

    public static class Albums {
        private static SoundcloudChannelTabExtractor extractor;

        @BeforeAll
        public static void setUp() throws IOException, ExtractionException {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (SoundcloudChannelTabExtractor) SoundCloud
                    .getChannelTabExtractorFromId("4803918", ChannelTabs.ALBUMS);
            extractor.fetchPage();
        }

        @Test
        public void testServiceId() {
            assertEquals(SoundCloud.getServiceId(), extractor.getServiceId());
        }

        @Test
        public void testTab() {
            assertEquals(ChannelTabs.ALBUMS, extractor.getTab());
        }

        @Test
        public void testId() throws ParsingException {
            assertEquals("4803918", extractor.getId());
        }

        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://soundcloud.com/bigsean-1/albums", extractor.getUrl());
        }

        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor);
        }

        @Test
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(extractor);
        }
    }
}
