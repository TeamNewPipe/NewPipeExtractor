// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampChannelExtractor;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.Bandcamp;

public class BandcampChannelExtractorTest {

    private static BandcampChannelExtractor extractor;
    private static ChannelExtractor noAvatarExtractor;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = (BandcampChannelExtractor) Bandcamp
                .getChannelExtractor("https://zachbenson.bandcamp.com/");
        extractor.fetchPage();

        noAvatarExtractor = Bandcamp.getChannelExtractor("https://powertothequeerkids.bandcamp.com/");
        noAvatarExtractor.fetchPage();
    }

    @Test
    public void testTranslateIdsToUrl() throws ParsingException {
        assertEquals("https://zachbenson.bandcamp.com/album/covers", BandcampExtractorHelper.getStreamUrlFromIds(2862267535L, 2063639444L, "album"));
        // TODO write more test cases
    }

    @Test
    public void testLength() throws ParsingException {
        assertTrue(extractor.getInitialPage().getItems().size() > 2);
    }

    @Test
    public void testGetBannerUrl() throws ParsingException {
        // Why is this picture in png format when all other pictures are jpg?
        assertTrue(extractor.getBannerUrl().endsWith(".png"));
    }

    @Test
    public void testGetNoAvatar() throws ExtractionException {
        assertEquals("", noAvatarExtractor.getAvatarUrl());
    }

    @Test
    public void testGetNoBanner() throws ExtractionException {
        assertEquals("", noAvatarExtractor.getBannerUrl());
    }
}
