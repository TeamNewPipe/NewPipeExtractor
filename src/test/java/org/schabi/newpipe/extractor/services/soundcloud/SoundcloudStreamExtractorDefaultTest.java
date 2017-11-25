package org.schabi.newpipe.extractor.services.soundcloud;

import com.grack.nanojson.JsonParserException;
import org.junit.Before;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Subtitles;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemCollector;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.SubtitlesFormat;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;

/**
 * Test for {@link StreamExtractor}
 */
public class SoundcloudStreamExtractorDefaultTest {
    private SoundcloudStreamExtractor extractor;

    @Before
    public void setUp() throws Exception {
        NewPipe.init(Downloader.getInstance());
        extractor = (SoundcloudStreamExtractor) SoundCloud.getService().getStreamExtractor("https://soundcloud.com/liluzivert/do-what-i-want-produced-by-maaly-raw-don-cannon");
    }

    @Test
    public void testGetInvalidTimeStamp() throws ParsingException {
        assertTrue(extractor.getTimeStamp() + "",
                extractor.getTimeStamp() <= 0);
    }

    @Test
    public void testGetValidTimeStamp() throws IOException, ExtractionException {
        StreamExtractor extractor = SoundCloud.getService().getStreamExtractor("https://soundcloud.com/liluzivert/do-what-i-want-produced-by-maaly-raw-don-cannon#t=69");
        assertEquals(extractor.getTimeStamp() + "", "69");
    }

    @Test
    public void testGetTitle() throws ParsingException {
        assertEquals(extractor.getName(), "Do What I Want [Produced By Maaly Raw + Don Cannon]");
    }

    @Test
    public void testGetDescription() throws ParsingException {
        assertEquals(extractor.getDescription(), "The Perfect LUV Tape®️");
    }

    @Test
    public void testGetUploaderName() throws ParsingException {
        assertEquals(extractor.getUploaderName(), "LIL UZI VERT");
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
    public void testGetUploaderUrl() throws ParsingException {
        assertEquals(extractor.getUploaderUrl(), "http://soundcloud.com/liluzivert");
    }

    @Test
    public void testGetThumbnailUrl() throws ParsingException {
        assertTrue(extractor.getThumbnailUrl(), extractor.getThumbnailUrl().contains("https://"));
    }

    @Test
    public void testGetUploaderAvatarUrl() throws ParsingException {
        assertTrue(extractor.getUploaderAvatarUrl(), extractor.getUploaderAvatarUrl().contains("https://"));
    }

    @Test
    public void testGetAudioStreams() throws IOException, ExtractionException {
        assertFalse(extractor.getAudioStreams().isEmpty());
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

    @Test
    public void testGetSubtitlesListDefault() throws IOException, ExtractionException {
        // Video (/view?v=YQHsXMglC9A) set in the setUp() method has no captions => null
        assertTrue(extractor.getSubtitlesDefault() == null);
    }

    @Test
    public void testGetSubtitlesList() throws IOException, ExtractionException {
        // Video (/view?v=YQHsXMglC9A) set in the setUp() method has no captions => null
        assertTrue(extractor.getSubtitles(SubtitlesFormat.VTT) == null);
    }
}
