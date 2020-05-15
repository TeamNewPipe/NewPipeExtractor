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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

public class YoutubeStreamExtractorLivestreamTest {
    private static YoutubeStreamExtractor extractor;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = (YoutubeStreamExtractor) YouTube
                .getStreamExtractor("https://www.youtube.com/watch?v=5qap5aO4i9A");
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
        assertFalse(extractor.getDescription().getContent().isEmpty());
    }

    @Test
    public void testGetFullLinksInDescription() throws ParsingException {
        assertTrue(extractor.getDescription().getContent().contains("https://bit.ly/chilledcow-playlists"));
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
        assertTrue(extractor.getDashMpdUrl().startsWith("https://manifest.googlevideo.com/api/manifest/dash/"));
    }

    @Test
    public void testGetRelatedVideos() throws ExtractionException {
        StreamInfoItemsCollector relatedVideos = extractor.getRelatedStreams();
        Utils.printErrors(relatedVideos.getErrors());
        assertFalse(relatedVideos.getItems().isEmpty());
        assertTrue(relatedVideos.getErrors().isEmpty());
    }

    @Test
    public void testGetSubtitlesListDefault() {
        assertTrue(extractor.getSubtitlesDefault().isEmpty());
    }

    @Test
    public void testGetSubtitlesList() {
        assertTrue(extractor.getSubtitles(MediaFormat.TTML).isEmpty());
    }
}
