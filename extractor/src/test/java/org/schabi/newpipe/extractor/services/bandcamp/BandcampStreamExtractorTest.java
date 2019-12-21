// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampStreamExtractor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.bandcamp;

public class BandcampStreamExtractorTest {

    private static BandcampStreamExtractor extractor;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = (BandcampStreamExtractor) bandcamp
                .getStreamExtractor("https://zachbenson.bandcamp.com/track/kitchen");
        extractor.fetchPage();
    }

    @Test(expected = ExtractionException.class)
    public void testAlbum() throws ExtractionException {
        bandcamp.getStreamExtractor("https://zachbenson.bandcamp.com/album/prom");
    }

    @Test
    public void testServiceId() {
    }

    @Test
    public void testName() throws ParsingException {
        assertEquals("kitchen", extractor.getName());
    }

    @Test
    public void testUrl() throws ParsingException {
        assertEquals("https://zachbenson.bandcamp.com/track/kitchen", extractor.getUrl());
    }

    @Test
    public void testArtistUrl() throws ParsingException {
        assertEquals("https://zachbenson.bandcamp.com/", extractor.getUploaderUrl());
    }

    @Test
    public void testDescription() {
        assertEquals(831, extractor.getDescription().length());
    }

    @Test
    public void testArtistProfilePicture() {
        String url = extractor.getUploaderAvatarUrl();
        assertTrue(url.contains("://f4.bcbits.com/img/") && url.endsWith(".jpg"));
    }

}
