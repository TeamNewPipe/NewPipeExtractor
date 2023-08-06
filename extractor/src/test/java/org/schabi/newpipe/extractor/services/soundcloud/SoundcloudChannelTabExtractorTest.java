package org.schabi.newpipe.extractor.services.soundcloud;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.BaseListExtractorTest;
import org.schabi.newpipe.extractor.services.DefaultListExtractorTest;
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudChannelTabExtractor;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestGetPageInNewExtractor;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestMoreItems;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestRelatedItems;

class SoundcloudChannelTabExtractorTest {

    static class Tracks extends DefaultListExtractorTest<ChannelTabExtractor> {
        private static SoundcloudChannelTabExtractor extractor;

        @BeforeAll
        static void setUp() throws IOException, ExtractionException {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (SoundcloudChannelTabExtractor) SoundCloud
                    .getChannelTabExtractorFromId("10494998", ChannelTabs.TRACKS);
            extractor.fetchPage();
        }

        @Override public ChannelTabExtractor extractor() throws Exception { return extractor; }
        @Override public StreamingService expectedService() throws Exception { return SoundCloud; }
        @Override public String expectedName() throws Exception { return ChannelTabs.TRACKS; }
        @Override public String expectedId() throws Exception { return "10494998"; }
        @Override public String expectedUrlContains() throws Exception { return "https://soundcloud.com/liluzivert/tracks"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://soundcloud.com/liluzivert/tracks"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
        @Override public boolean expectedHasMoreItems() { return true; }

        @Test
        void testGetPageInNewExtractor() throws Exception {
            final ChannelTabExtractor newTabExtractor =
                    SoundCloud.getChannelTabExtractorFromId("10494998", ChannelTabs.TRACKS);
            defaultTestGetPageInNewExtractor(extractor, newTabExtractor);
        }
    }

    static class Playlists extends DefaultListExtractorTest<ChannelTabExtractor> {
        private static SoundcloudChannelTabExtractor extractor;

        @BeforeAll
        static void setUp() throws IOException, ExtractionException {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (SoundcloudChannelTabExtractor) SoundCloud
                    .getChannelTabExtractorFromId("323371733", ChannelTabs.PLAYLISTS);
            extractor.fetchPage();
        }

        @Override public ChannelTabExtractor extractor() throws Exception { return extractor; }
        @Override public StreamingService expectedService() throws Exception { return SoundCloud; }
        @Override public String expectedName() throws Exception { return ChannelTabs.PLAYLISTS; }
        @Override public String expectedId() throws Exception { return "323371733"; }
        @Override public String expectedUrlContains() throws Exception { return "https://soundcloud.com/trackaholic/sets"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://soundcloud.com/trackaholic/sets"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.PLAYLIST; }
        @Override public boolean expectedHasMoreItems() { return true; }
    }

    static class Albums extends DefaultListExtractorTest<ChannelTabExtractor> {
        private static SoundcloudChannelTabExtractor extractor;

        @BeforeAll
        static void setUp() throws IOException, ExtractionException {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (SoundcloudChannelTabExtractor) SoundCloud
                    .getChannelTabExtractorFromId("4803918", ChannelTabs.ALBUMS);
            extractor.fetchPage();
        }

        @Override public ChannelTabExtractor extractor() throws Exception { return extractor; }
        @Override public StreamingService expectedService() throws Exception { return SoundCloud; }
        @Override public String expectedName() throws Exception { return ChannelTabs.ALBUMS; }
        @Override public String expectedId() throws Exception { return "4803918"; }
        @Override public String expectedUrlContains() throws Exception { return "https://soundcloud.com/bigsean-1/albums"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://soundcloud.com/bigsean-1/albums"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.PLAYLIST; }
        @Override public boolean expectedHasMoreItems() { return true; }
    }
}
