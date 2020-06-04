package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.Extractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampRadioStreamExtractor;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampStreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamExtractor;

import java.io.IOException;
import java.util.Calendar;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ServiceList.Bandcamp;

public class BandcampRadioStreamExtractorTest {

    private static StreamExtractor e;

    private static final String SHOW_URL = "https://bandcamp.com/?show=230";

    @BeforeClass
    public static void setUp() throws IOException, ExtractionException {
        NewPipe.init(DownloaderTestImpl.getInstance());
        e = Bandcamp.getStreamExtractor(SHOW_URL);
        e.fetchPage();
    }

    @Test
    public void testGettingCorrectStreamExtractor() throws ExtractionException {
        assertTrue(Bandcamp.getStreamExtractor("https://bandcamp.com/?show=3") instanceof BandcampRadioStreamExtractor);
        assertFalse(Bandcamp.getStreamExtractor("https://zachbenson.bandcamp.com/track/deflated") instanceof BandcampRadioStreamExtractor);
    }

    @Test
    public void testGetName() throws ParsingException {
        assertEquals("Sound Movements", e.getName());
    }

    @Test
    public void testGetUploaderUrl() {
    }

    @Test
    public void testGetUrl() throws ParsingException {
        assertEquals(SHOW_URL, e.getUrl());
    }

    @Test
    public void testGetUploaderName() throws ParsingException {
        assertEquals("Andrew Jervis", e.getUploaderName());
    }

    @Test
    public void testGetTextualUploadDate() throws ParsingException {
        assertEquals("16 May 2017", e.getTextualUploadDate());
    }

    @Test
    public void testUploadDate() throws ParsingException {
        assertEquals(136, e.getUploadDate().date().get(Calendar.DAY_OF_YEAR));
    }

    @Test
    public void testGetThumbnailUrl() throws ParsingException {
        assertTrue(e.getThumbnailUrl().contains("bcbits.com/img"));
    }

    @Test
    public void testGetUploaderAvatarUrl() throws ParsingException {
        assertTrue(e.getUploaderAvatarUrl().contains("bandcamp-button"));

    }

    @Test
    public void testGetDescription() throws ParsingException {
        assertEquals("Featuring special guests Nick Hakim and Elbows, plus fresh cuts from Eddie Palmieri, KRS One, Ladi6, and Moonchild.", e.getDescription().getContent());

    }

    @Test
    public void testGetLength() throws ParsingException {
        assertEquals(5619, e.getLength());
    }

    @Test
    public void testGetAudioStreams() throws ExtractionException, IOException {
        assertEquals(2, e.getAudioStreams().size());
    }

    @Test
    public void testGetLicence() throws ParsingException {
        assertEquals("", e.getLicence());
    }

    @Test
    public void testGetCategory() throws ParsingException {
        assertEquals("", e.getCategory());
    }

    @Test
    public void testGetTags() throws ParsingException {
        assertEquals(0, e.getTags().size());
    }
}
