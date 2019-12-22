package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.Extractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampRadioStreamExtractor;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampStreamExtractor;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ServiceList.bandcamp;

public class BandcampRadioStreamExtractorTest {

    @BeforeClass
    public static void setUp() {
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    public void testGettingCorrectStreamExtractor() throws ExtractionException {
        assertTrue(bandcamp.getStreamExtractor("https://bandcamp.com/?show=3") instanceof BandcampRadioStreamExtractor);
        assertFalse(bandcamp.getStreamExtractor("https://zachbenson.bandcamp.com/track/deflated") instanceof BandcampRadioStreamExtractor);
    }

    @Test
    public void testExtracting() throws ExtractionException, IOException {
        BandcampRadioStreamExtractor e = (BandcampRadioStreamExtractor) bandcamp.getStreamExtractor("https://bandcamp.com/?show=230");
        e.fetchPage();
        assertEquals("Sound Movements", e.getName());
        assertEquals("Andrew Jervis", e.getUploaderName());
    }
}
