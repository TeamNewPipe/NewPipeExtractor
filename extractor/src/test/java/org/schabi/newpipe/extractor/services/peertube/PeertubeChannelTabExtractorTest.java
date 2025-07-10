package org.schabi.newpipe.extractor.services.peertube;

import static org.schabi.newpipe.extractor.ServiceList.PeerTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestGetPageInNewExtractor;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.services.DefaultListExtractorTest;

class PeertubeChannelTabExtractorTest {

    static class Videos extends DefaultListExtractorTest<ChannelTabExtractor> {

        @Override
        protected ChannelTabExtractor createExtractor() throws Exception {
            // setting instance might break test when running in parallel
            PeerTube.setInstance(new PeertubeInstance("https://framatube.org", "Framatube"));
            return PeerTube.getChannelTabExtractorFromId(
                "video-channels/lqdn_channel@video.lqdn.fr", ChannelTabs.VIDEOS);
        }

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
            defaultTestGetPageInNewExtractor(extractor(), newTabExtractor);
        }
    }

    static class Playlists extends DefaultListExtractorTest<ChannelTabExtractor> {

        @Override
        protected ChannelTabExtractor createExtractor() throws Exception {
            return PeerTube.getChannelTabExtractorFromIdAndBaseUrl(
                "video-channels/lqdn_channel@video.lqdn.fr", ChannelTabs.PLAYLISTS,
                "https://framatube.org");
        }

        @Override public StreamingService expectedService() throws Exception { return PeerTube; }
        @Override public String expectedName() throws Exception { return ChannelTabs.PLAYLISTS; }
        @Override public String expectedId() throws Exception { return "video-channels/lqdn_channel@video.lqdn.fr"; }
        @Override public String expectedUrlContains() throws Exception { return "https://framatube.org/video-channels/lqdn_channel@video.lqdn.fr/video-playlists"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://framatube.org/video-channels/lqdn_channel@video.lqdn.fr/video-playlists"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.PLAYLIST; }
        @Override public boolean expectedHasMoreItems() { return false; }
    }

    static class Channels extends DefaultListExtractorTest<ChannelTabExtractor> {

        @Override
        protected ChannelTabExtractor createExtractor() throws Exception {
            return PeerTube.getChannelTabExtractorFromIdAndBaseUrl("accounts/framasoft",
                ChannelTabs.CHANNELS, "https://framatube.org");
        }

        @Override public StreamingService expectedService() throws Exception { return PeerTube; }
        @Override public String expectedName() throws Exception { return ChannelTabs.CHANNELS; }
        @Override public String expectedId() throws Exception { return "accounts/framasoft"; }
        @Override public String expectedUrlContains() throws Exception { return "https://framatube.org/accounts/framasoft/video-channels"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://framatube.org/accounts/framasoft/video-channels"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.CHANNEL; }
        @Override public boolean expectedHasMoreItems() { return true; }
    }
}
