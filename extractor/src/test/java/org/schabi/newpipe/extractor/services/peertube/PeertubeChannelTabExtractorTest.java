package org.schabi.newpipe.extractor.services.peertube;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ChannelTabs;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeAccountTabExtractor;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeChannelTabExtractor;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestRelatedItems;

public class PeertubeChannelTabExtractorTest {
    public static class Playlists {
        private static PeertubeChannelTabExtractor extractor;

        @BeforeAll
        public static void setUp() throws IOException, ExtractionException {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (PeertubeChannelTabExtractor) PeerTube
                    .getChannelTabExtractorFromId("video-channels/lqdn_channel@video.lqdn.fr",
                            ChannelTabs.PLAYLISTS, "https://framatube.org");
            extractor.fetchPage();
        }

        @Test
        public void testServiceId() {
            assertEquals(PeerTube.getServiceId(), extractor.getServiceId());
        }

        @Test
        public void testTab() {
            assertEquals(ChannelTabs.PLAYLISTS, extractor.getTab());
        }

        @Test
        public void testId() throws ParsingException {
            assertEquals("video-channels/lqdn_channel@video.lqdn.fr", extractor.getId());
        }

        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://framatube.org/video-channels/lqdn_channel@video.lqdn.fr/video-playlists", extractor.getUrl());
        }

        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor);
        }
    }

    public static class Channels {
        private static PeertubeAccountTabExtractor extractor;

        @BeforeAll
        public static void setUp() throws IOException, ExtractionException {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (PeertubeAccountTabExtractor) PeerTube
                    .getChannelTabExtractorFromId("accounts/framasoft",
                            ChannelTabs.CHANNELS, "https://framatube.org");
            extractor.fetchPage();
        }

        @Test
        public void testServiceId() {
            assertEquals(PeerTube.getServiceId(), extractor.getServiceId());
        }

        @Test
        public void testTab() {
            assertEquals(ChannelTabs.CHANNELS, extractor.getTab());
        }

        @Test
        public void testId() throws ParsingException {
            assertEquals("accounts/framasoft", extractor.getId());
        }

        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://framatube.org/accounts/framasoft/video-channels", extractor.getUrl());
        }

        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor);
        }
    }
}
