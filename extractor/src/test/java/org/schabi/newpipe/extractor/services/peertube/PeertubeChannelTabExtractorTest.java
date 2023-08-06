package org.schabi.newpipe.extractor.services.peertube;

import static org.schabi.newpipe.extractor.ServiceList.PeerTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestGetPageInNewExtractor;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.DefaultListExtractorTest;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeChannelTabExtractor;

import java.io.IOException;

class PeertubeChannelTabExtractorTest {

    static class Videos extends DefaultListExtractorTest<ChannelTabExtractor> {
        private static PeertubeChannelTabExtractor extractor;

        @BeforeAll
        static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            // setting instance might break test when running in parallel
            PeerTube.setInstance(new PeertubeInstance("https://framatube.org", "Framatube"));
            extractor = (PeertubeChannelTabExtractor) PeerTube.getChannelTabExtractorFromId(
                    "video-channels/lqdn_channel@video.lqdn.fr", ChannelTabs.VIDEOS);
            extractor.fetchPage();
        }

        @Override public ChannelTabExtractor extractor() throws Exception { return extractor; }
        @Override public StreamingService expectedService() throws Exception { return PeerTube; }
        @Override public String expectedName() throws Exception { return ChannelTabs.VIDEOS; }
        @Override public String expectedId() throws Exception { return "video-channels/lqdn_channel@video.lqdn.fr"; }
        @Override public String expectedUrlContains() throws Exception { return "https://framatube.org/video-channels/lqdn_channel@video.lqdn.fr/videos"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://framatube.org/video-channels/lqdn_channel@video.lqdn.fr/videos"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
        @Override public boolean expectedHasMoreItems() { return true; }

        @Test
        void testGetPageInNewExtractor() throws Exception {
            final ChannelTabExtractor newTabExtractor = PeerTube.getChannelTabExtractorFromId(
                    "video-channels/lqdn_channel@video.lqdn.fr", ChannelTabs.VIDEOS);
            defaultTestGetPageInNewExtractor(extractor, newTabExtractor);
        }
    }

    static class Playlists extends DefaultListExtractorTest<ChannelTabExtractor> {
        private static PeertubeChannelTabExtractor extractor;

        @BeforeAll
        static void setUp() throws IOException, ExtractionException {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (PeertubeChannelTabExtractor)
                    PeerTube.getChannelTabExtractorFromIdAndBaseUrl(
                            "video-channels/lqdn_channel@video.lqdn.fr", ChannelTabs.PLAYLISTS,
                            "https://framatube.org");
            extractor.fetchPage();
        }

        @Override public ChannelTabExtractor extractor() throws Exception { return extractor; }
        @Override public StreamingService expectedService() throws Exception { return PeerTube; }
        @Override public String expectedName() throws Exception { return ChannelTabs.PLAYLISTS; }
        @Override public String expectedId() throws Exception { return "video-channels/lqdn_channel@video.lqdn.fr"; }
        @Override public String expectedUrlContains() throws Exception { return "https://framatube.org/video-channels/lqdn_channel@video.lqdn.fr/video-playlists"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://framatube.org/video-channels/lqdn_channel@video.lqdn.fr/video-playlists"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.PLAYLIST; }
        @Override public boolean expectedHasMoreItems() { return false; }
    }

    static class Channels extends DefaultListExtractorTest<ChannelTabExtractor> {
        private static PeertubeChannelTabExtractor extractor;

        @BeforeAll
        static void setUp() throws IOException, ExtractionException {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (PeertubeChannelTabExtractor)
                    PeerTube.getChannelTabExtractorFromIdAndBaseUrl("accounts/framasoft",
                            ChannelTabs.CHANNELS, "https://framatube.org");
            extractor.fetchPage();
        }

        @Override public ChannelTabExtractor extractor() throws Exception { return extractor; }
        @Override public StreamingService expectedService() throws Exception { return PeerTube; }
        @Override public String expectedName() throws Exception { return ChannelTabs.CHANNELS; }
        @Override public String expectedId() throws Exception { return "accounts/framasoft"; }
        @Override public String expectedUrlContains() throws Exception { return "https://framatube.org/accounts/framasoft/video-channels"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://framatube.org/accounts/framasoft/video-channels"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.CHANNEL; }
        @Override public boolean expectedHasMoreItems() { return true; }
    }
}
