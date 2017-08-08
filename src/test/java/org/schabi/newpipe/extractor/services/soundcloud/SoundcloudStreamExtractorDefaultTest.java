package org.schabi.newpipe.extractor.services.soundcloud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemCollector;
import org.schabi.newpipe.extractor.stream.StreamType;

/**
 * Test for {@link StreamExtractor}
 */
public class SoundcloudStreamExtractorDefaultTest {
    private StreamExtractor extractor;

    @Before
    public void setUp() throws Exception {
        NewPipe.init(Downloader.getInstance());
        extractor = SoundCloud.getService().getStreamExtractor("https://soundcloud.com/liluzivert/do-what-i-want-produced-by-maaly-raw-don-cannon");
    }

    @Test
    public void testGetInvalidTimeStamp() throws ParsingException {
        assertTrue(Integer.toString(extractor.getTimeStamp()),
                extractor.getTimeStamp() <= 0);
    }

    @Test
    public void testGetValidTimeStamp() throws IOException, ExtractionException {
        StreamExtractor extractor = SoundCloud.getService().getStreamExtractor("https://soundcloud.com/liluzivert/do-what-i-want-produced-by-maaly-raw-don-cannon#t=69");
        assertEquals(Integer.toString(extractor.getTimeStamp()), "69");
    }

    @Test
    public void testGetTitle() throws ParsingException {
        assertEquals(extractor.getTitle(), "Do What I Want [Produced By Maaly Raw + Don Cannon]");
    }

    @Test
    public void testGetDescription() throws ParsingException {
        assertEquals(extractor.getDescription(), "The Perfect LUV Tape®️");
    }

    @Test
    public void testGetUploader() throws ParsingException {
        assertEquals(extractor.getUploader(), "LIL UZI VERT");
    }

    @Test
    public void testGetLength() throws ParsingException {
        assertEquals(extractor.getLength(), 175);
    }

    @Test
    public void testGetViewCount() throws ParsingException {
        assertTrue(Long.toString(extractor.getViewCount()),
                extractor.getViewCount() > 44227978);
    }

    @Test
    public void testGetUploadDate() throws ParsingException {
        assertEquals(extractor.getUploadDate(), "2016-07-31");
    }

    @Test
    public void testGetUserUrl() throws ParsingException {
        assertEquals(extractor.getUserUrl(), "http://soundcloud.com/liluzivert");
    }

    @Test
    public void testGetThumbnailUrl() throws ParsingException {
        assertEquals(extractor.getThumbnailUrl(), "https://i1.sndcdn.com/artworks-000174195399-iw6seg-large.jpg");
    }

    @Test
    public void testGetUploaderThumbnailUrl() throws ParsingException {
        assertEquals(extractor.getUploaderThumbnailUrl(), "https://a1.sndcdn.com/images/default_avatar_large.png");
    }

    @Test
    public void testGetAudioStreams() throws IOException, ExtractionException {
        assertTrue(!extractor.getAudioStreams().isEmpty());
    }

    @Test
    public void testStreamType() throws ParsingException {
        assertTrue(extractor.getStreamType() == StreamType.AUDIO_STREAM);
    }

    @Test
    public void testGetRelatedVideos() throws ExtractionException, IOException {
        StreamInfoItemCollector relatedVideos = extractor.getRelatedVideos();
        assertFalse(relatedVideos.getItemList().isEmpty());
        assertTrue(relatedVideos.getErrors().isEmpty());
    }
}
