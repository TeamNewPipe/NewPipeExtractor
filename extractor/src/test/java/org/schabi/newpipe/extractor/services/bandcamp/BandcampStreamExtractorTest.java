// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampStreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamExtractor;

import java.io.IOException;

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
        assertEquals(831, extractor.getDescription().getContent().length());
    }

    @Test
    public void testArtistProfilePicture() {
        String url = extractor.getUploaderAvatarUrl();
        assertTrue(url.contains("://f4.bcbits.com/img/") && url.endsWith(".jpg"));
    }

    @Test
    public void testNoArtistProfilePicture() throws ExtractionException {
        assertEquals("", bandcamp.getStreamExtractor("https://powertothequeerkids.bandcamp.com/track/human-nature").getUploaderAvatarUrl());
    }

    @Test
    public void testAudioStream() {
        assertTrue(extractor.getAudioStreams().get(0).getUrl().contains("bcbits.com/stream"));
        assertEquals(1, extractor.getAudioStreams().size());
    }

    @Test(expected = ParsingException.class)
    public void testInvalidUrl() throws ExtractionException {
        bandcamp.getStreamExtractor("https://bandcamp.com");
    }

    @Test
    public void testCategory() throws ExtractionException, IOException {
        StreamExtractor se = bandcamp.getStreamExtractor("https://npet.bandcamp.com/track/track-1");
        se.fetchPage();
        assertEquals("acoustic", se.getCategory());
    }

    @Test
    public void testLicense() throws ExtractionException, IOException {
        StreamExtractor se = bandcamp.getStreamExtractor("https://npet.bandcamp.com/track/track-1");
        se.fetchPage();
        assertEquals("CC BY 3.0", se.getLicence());
    }


}
