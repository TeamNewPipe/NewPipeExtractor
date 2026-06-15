package org.schabi.newpipe.extractor.services.youtube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.stream.AudioTrackType;

import java.io.IOException;

public class YoutubeParsingHelperTest implements InitYoutubeTest {

    @Test
    void testIsHardcodedClientVersionValid() throws IOException, ExtractionException {
        assertTrue(YoutubeParsingHelper.isHardcodedClientVersionValid(),
                "Hardcoded client version is not valid anymore");
    }

    @Test
    void testIsHardcodedYoutubeMusicClientVersionValid() throws IOException, ExtractionException {
        assertTrue(YoutubeParsingHelper.isHardcodedYoutubeMusicClientVersionValid(),
                "Hardcoded YouTube Music client version is not valid anymore");
    }

    @Test
    void testParseDurationString() throws ParsingException {
        assertEquals(1162567, YoutubeParsingHelper.parseDurationString("12:34:56:07"));
        assertEquals(4445767, YoutubeParsingHelper.parseDurationString("1,234:56:07"));
        assertEquals(754, YoutubeParsingHelper.parseDurationString("12:34 "));
    }

    @Test
    void testConvertFromGoogleCacheUrl() {
        assertEquals("https://mohfw.gov.in/",
                YoutubeParsingHelper.extractCachedUrlIfNeeded("https://webcache.googleusercontent.com/search?q=cache:https://mohfw.gov.in/"));
        assertEquals("https://www.infektionsschutz.de/coronavirus-sars-cov-2.html",
                YoutubeParsingHelper.extractCachedUrlIfNeeded("https://www.infektionsschutz.de/coronavirus-sars-cov-2.html"));
    }

    @Test
    void extractAudioTrackType() {
        final String originalXtags =  "ChEKBWFjb250EghvcmlnaW5hbAoNCgRsYW5nEgVlbi1VUw";
        final String dubbedXtags = "ChQKBWFjb250EgtkdWJiZWQtYXV0bwoKCgRsYW5nEgJhcg";
        final String descriptiveXtags = "ChQKBWFjb250EgtkZXNjcmlwdGl2ZQ";
        final String noTrackXtags = null;

        assertEquals(AudioTrackType.ORIGINAL, YoutubeParsingHelper.extractAudioTrackType(originalXtags));
        assertEquals(AudioTrackType.DUBBED, YoutubeParsingHelper.extractAudioTrackType(dubbedXtags));
        assertEquals(AudioTrackType.DESCRIPTIVE, YoutubeParsingHelper.extractAudioTrackType(descriptiveXtags));
        assertNull(YoutubeParsingHelper.extractAudioTrackType(noTrackXtags));
    }
}
