// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampChannelExtractor;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.Bandcamp;

public class BandcampChannelExtractorTest {

    private static BandcampChannelExtractor extractor;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = (BandcampChannelExtractor) Bandcamp
                .getChannelExtractor("https://zachbenson.bandcamp.com/");
    }

    @Test
    public void testImageUrl() {
        assertEquals("https://f4.bcbits.com/img/a2405652335_10.jpg", BandcampChannelExtractor.getImageUrl(2405652335L, true));
        assertEquals("https://f4.bcbits.com/img/17433693_10.jpg", BandcampChannelExtractor.getImageUrl(17433693L, false));
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
        assertEquals("", Bandcamp.getChannelExtractor("https://powertothequeerkids.bandcamp.com/").getAvatarUrl());
    }

    @Test
    public void testGetNoBanner() throws ExtractionException {
        assertEquals("", Bandcamp.getChannelExtractor("https://powertothequeerkids.bandcamp.com/").getBannerUrl());
    }
}
