package org.schabi.newpipe.extractor.services.peertube;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.BaseListExtractorTest;
import org.schabi.newpipe.extractor.services.DefaultListExtractorTest;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeChannelTabExtractor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestMoreItems;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestRelatedItems;

class PeertubeAccountTabExtractorTest {

    static class Videos extends DefaultListExtractorTest<ChannelTabExtractor> {
        private static PeertubeChannelTabExtractor extractor;

        @BeforeAll
        static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            // setting instance might break test when running in parallel
            PeerTube.setInstance(new PeertubeInstance("https://framatube.org", "Framatube"));
            extractor = (PeertubeChannelTabExtractor) PeerTube
                    .getChannelTabExtractorFromId("accounts/framasoft", ChannelTabs.VIDEOS);
            extractor.fetchPage();
        }

        @Override public ChannelTabExtractor extractor() throws Exception { return extractor; }
        @Override public StreamingService expectedService() throws Exception { return PeerTube; }
        @Override public String expectedName() throws Exception { return ChannelTabs.VIDEOS; }
        @Override public String expectedId() throws Exception { return "accounts/framasoft"; }
        @Override public String expectedUrlContains() throws Exception { return "https://framatube.org/accounts/framasoft/videos"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://framatube.org/accounts/framasoft/videos"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
        @Override public boolean expectedHasMoreItems() { return true; }
    }

    static class Channels extends DefaultListExtractorTest<ChannelTabExtractor> {
        private static PeertubeChannelTabExtractor extractor;

        @BeforeAll
        static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            // setting instance might break test when running in parallel
            PeerTube.setInstance(new PeertubeInstance("https://framatube.org", "Framatube"));
            extractor = (PeertubeChannelTabExtractor) PeerTube
                    .getChannelTabExtractorFromId("accounts/framasoft", ChannelTabs.CHANNELS);
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
