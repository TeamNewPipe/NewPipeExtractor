package org.schabi.newpipe.extractor.services.bandcamp;

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
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampChannelTabExtractor;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.Bandcamp;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestRelatedItems;

class BandcampChannelTabExtractorTest {

    static class Tracks extends DefaultListExtractorTest<ChannelTabExtractor> {
        private static BandcampChannelTabExtractor extractor;

        @BeforeAll
        static void setUp() throws IOException, ExtractionException {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (BandcampChannelTabExtractor) Bandcamp
                    .getChannelTabExtractorFromId("2464198920", ChannelTabs.TRACKS);
            extractor.fetchPage();
        }

        @Override public ChannelTabExtractor extractor() throws Exception { return extractor; }
        @Override public StreamingService expectedService() throws Exception { return Bandcamp; }
        @Override public String expectedName() throws Exception { return ChannelTabs.TRACKS; }
        @Override public String expectedId() throws Exception { return "2464198920"; }
        @Override public String expectedUrlContains() throws Exception { return "https://wintergatan.bandcamp.com/track"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://wintergatan.bandcamp.com/track"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
        @Override public boolean expectedHasMoreItems() { return false; }
    }

    static class Albums extends DefaultListExtractorTest<ChannelTabExtractor> {
        private static BandcampChannelTabExtractor extractor;

        @BeforeAll
        static void setUp() throws IOException, ExtractionException {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (BandcampChannelTabExtractor) Bandcamp
                    .getChannelTabExtractorFromId("2450875064", ChannelTabs.ALBUMS);
            extractor.fetchPage();
        }

        @Override public ChannelTabExtractor extractor() throws Exception { return extractor; }
        @Override public StreamingService expectedService() throws Exception { return Bandcamp; }
        @Override public String expectedName() throws Exception { return ChannelTabs.ALBUMS; }
        @Override public String expectedId() throws Exception { return "2450875064"; }
        @Override public String expectedUrlContains() throws Exception { return "https://toupie.bandcamp.com/album"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://toupie.bandcamp.com/album"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.PLAYLIST; }
        @Override public boolean expectedHasMoreItems() { return false; }
    }
}