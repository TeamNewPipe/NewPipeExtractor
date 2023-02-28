package org.schabi.newpipe.extractor.services.bandcamp;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.schabi.newpipe.extractor.ServiceList.Bandcamp;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.PaidContentException;

public class BandcampPaidStreamExtractorTest {

    @BeforeAll
    public static void setUp() {
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    public void testPaidTrack() throws ExtractionException {
        final var extractor = Bandcamp.getStreamExtractor("https://radicaldreamland.bandcamp.com/track/hackmud-continuous-mix");
        assertThrows(PaidContentException.class, extractor::fetchPage);
    }
}
