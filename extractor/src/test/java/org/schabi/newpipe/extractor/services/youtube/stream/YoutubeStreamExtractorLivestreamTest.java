package org.schabi.newpipe.extractor.services.youtube.stream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

public class YoutubeStreamExtractorLivestreamTest {
    private static YoutubeStreamExtractor extractor;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = (YoutubeStreamExtractor) YouTube
                .getStreamExtractor("https://www.youtube.com/watch?v=EcEMX-63PKY");
        extractor.fetchPage();
    }

    @Test
    public void testGetInvalidTimeStamp() throws ParsingException {
        assertTrue(extractor.getTimeStamp() + "",
                extractor.getTimeStamp() <= 0);
    }

    @Test
    public void testGetTitle() throws ParsingException {
        assertFalse(extractor.getName().isEmpty());
    }

    @Test
    public void testGetDescription() throws ParsingException {
        assertNotNull(extractor.getDescription());
        assertFalse(extractor.getDescription().isEmpty());
    }

    @Test
    public void testGetFullLinksInDescription() throws ParsingException {
        assertTrue(extractor.getDescription().contains("https://www.instagram.com/nathalie.baraton/"));
        assertFalse(extractor.getDescription().contains("https://www.instagram.com/nathalie.ba..."));
    }

    @Test
    public void testGetUploaderName() throws ParsingException {
        assertNotNull(extractor.getUploaderName());
        assertFalse(extractor.getUploaderName().isEmpty());
    }


    @Test
    public void testGetLength() throws ParsingException {
        assertEquals(0, extractor.getLength());
    }

    @Test
    public void testGetViewCount() throws ParsingException {
        long count = extractor.getViewCount();
        assertTrue(Long.toString(count), count > -1);
    }

    @Test
    public void testGetUploadDate() throws ParsingException {
        assertNull(extractor.getUploadDate());
        assertNull(extractor.getTextualUploadDate());
    }

    @Test
    public void testGetUploaderUrl() throws ParsingException {
        assertEquals("https://www.youtube.com/channel/UCSJ4gkVC6NrvII8umztf0Ow", extractor.getUploaderUrl());
    }

    @Test
    public void testGetThumbnailUrl() throws ParsingException {
        assertIsSecureUrl(extractor.getThumbnailUrl());
    }

    @Test
    public void testGetUploaderAvatarUrl() throws ParsingException {
        assertIsSecureUrl(extractor.getUploaderAvatarUrl());
    }

    @Test
    public void testGetAudioStreams() throws ExtractionException {
        assertFalse(extractor.getAudioStreams().isEmpty());
    }

    @Test
    public void testGetVideoStreams() throws ExtractionException {
        for (VideoStream s : extractor.getVideoStreams()) {
            assertIsSecureUrl(s.url);
            assertTrue(s.resolution.length() > 0);
            assertTrue(Integer.toString(s.getFormatId()),
                    0 <= s.getFormatId() && s.getFormatId() <= 0x100);
        }
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
    public void testGetRelatedVideos() throws ExtractionException, IOException {
        StreamInfoItemsCollector relatedVideos = extractor.getRelatedStreams();
        Utils.printErrors(relatedVideos.getErrors());
        assertFalse(relatedVideos.getItems().isEmpty());
        assertTrue(relatedVideos.getErrors().isEmpty());
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
