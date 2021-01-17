package org.schabi.newpipe.extractor.services.youtube;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.downloader.DownloaderFactory;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class YoutubeParsingHelperTest {

    private static final String RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/";

    @BeforeClass
    public static void setUp() throws IOException {
        YoutubeParsingHelper.resetClientVersionAndKey();
        NewPipe.init(new DownloaderFactory().getDownloader(RESOURCE_PATH + "youtubeParsingHelper"));
    }

    @Test
    public void testIsHardcodedClientVersionValid() throws IOException, ExtractionException {
        assertTrue("Hardcoded client version is not valid anymore",
                YoutubeParsingHelper.isHardcodedClientVersionValid());
    }

    @Test
    public void testAreHardcodedYoutubeMusicKeysValid() throws IOException, ExtractionException {
        assertTrue("Hardcoded YouTube Music keys are not valid anymore",
                YoutubeParsingHelper.areHardcodedYoutubeMusicKeysValid());
    }

    @Test
    public void testParseDurationString() throws ParsingException {
        assertEquals(1162567, YoutubeParsingHelper.parseDurationString("12:34:56:07"));
        assertEquals(4445767, YoutubeParsingHelper.parseDurationString("1,234:56:07"));
        assertEquals(754, YoutubeParsingHelper.parseDurationString("12:34 "));
    }

    @Test
    public void testConvertFromGoogleCacheUrl() throws ParsingException {
        assertEquals("https://mohfw.gov.in/",
                YoutubeParsingHelper.extractCachedUrlIfNeeded("https://webcache.googleusercontent.com/search?q=cache:https://mohfw.gov.in/"));
        assertEquals("https://www.infektionsschutz.de/coronavirus-sars-cov-2.html",
                YoutubeParsingHelper.extractCachedUrlIfNeeded("https://www.infektionsschutz.de/coronavirus-sars-cov-2.html"));
    }
}
