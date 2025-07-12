package org.schabi.newpipe.extractor.services.bandcamp;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.schabi.newpipe.extractor.ServiceList.Bandcamp;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.InitNewPipeTest;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.PaidContentException;
import org.schabi.newpipe.extractor.stream.StreamExtractor;

public class BandcampPaidStreamExtractorTest implements InitNewPipeTest {

    @Test
    public void testPaidTrack() throws ExtractionException {
        final StreamExtractor extractor = Bandcamp.getStreamExtractor(
            "https://radicaldreamland.bandcamp.com/track/hackmud-continuous-mix");
        assertThrows(PaidContentException.class, extractor::fetchPage);
    }
}
