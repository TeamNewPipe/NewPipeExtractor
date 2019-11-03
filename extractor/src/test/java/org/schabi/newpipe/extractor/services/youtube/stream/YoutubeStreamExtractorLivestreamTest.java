package org.schabi.newpipe.extractor.services.youtube.stream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.schabi.newpipe.extractor.utils.Localization;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

public class YoutubeStreamExtractorLivestreamTest
        extends YoutubeStreamExtractorBaseTest {

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(Downloader.getInstance(), new Localization("GB", "en"));
        extractor = (YoutubeStreamExtractor) YouTube
                .getStreamExtractor("https://www.youtube.com/watch?v=EcEMX-63PKY");
        extractor.fetchPage();
    }

    @Test
    public void testGetFullLinksInDescription() throws ParsingException {
        assertTrue(extractor.getDescription().contains("https://www.instagram.com/nathalie.baraton/"));
        assertFalse(extractor.getDescription().contains("https://www.instagram.com/nathalie.ba..."));
    }

    @Test
    public void testGetLength() throws ParsingException {
        assertEquals(0, extractor.getLength());
    }

    @Test
    public void testGetViewCount() throws ParsingException {
        long count = extractor.getViewCount();
        assertTrue(Long.toString(count), count >= 7148995);
    }

    @Test
    public void testGetUploaderUrl() throws ParsingException {
        assertEquals("https://www.youtube.com/channel/UCSJ4gkVC6NrvII8umztf0Ow", extractor.getUploaderUrl());
    }

    @Test
    public void testStreamType() throws ParsingException {
        assertSame(extractor.getStreamType(), StreamType.LIVE_STREAM);
    }

    @Test
    public void testGetDashMpd() throws ParsingException {
        // we dont expect this particular video to have a DASH file. For this purpouse we use a different test class.
        assertTrue(extractor.getDashMpdUrl(), extractor.getDashMpdUrl().isEmpty());
    }

    @Test
    public void testGetSubtitlesListDefault() throws IOException, ExtractionException {
        assertTrue(extractor.getSubtitlesDefault().isEmpty());
    }

    @Test
    public void testGetSubtitlesList() throws IOException, ExtractionException {
        assertTrue(extractor.getSubtitles(MediaFormat.TTML).isEmpty());
    }
}
