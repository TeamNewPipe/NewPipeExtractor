package org.schabi.newpipe.extractor.services.youtube;

import com.grack.nanojson.JsonParserException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.SubtitlesFormat;
import org.schabi.newpipe.extractor.stream.VideoStream;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

/**
 * Test for {@link YoutubeStreamUrlIdHandler}
 */
public class YoutubeStreamExtractorRestrictedTest {
    public static final String HTTPS = "https://";
    private YoutubeStreamExtractor extractor;

    @Before
    public void setUp() throws Exception {
        NewPipe.init(Downloader.getInstance());
        extractor = (YoutubeStreamExtractor) YouTube.getService()
                .getStreamExtractor("https://www.youtube.com/watch?v=i6JTvzrpBy0");
    }

    @Test
    public void testGetInvalidTimeStamp() throws ParsingException {
        assertTrue(extractor.getTimeStamp() + "", extractor.getTimeStamp() <= 0);
    }

    @Test
    public void testGetValidTimeStamp() throws IOException, ExtractionException {
        StreamExtractor extractor = YouTube.getService().getStreamExtractor("https://youtu.be/FmG385_uUys?t=174");
        assertEquals(extractor.getTimeStamp() + "", "174");
    }

    @Test
    public void testGetAgeLimit() throws ParsingException {
        assertTrue(extractor.getAgeLimit() == 18);
    }

    @Test
    public void testGetName() throws ParsingException {
        assertNotNull("name is null", extractor.getName());
        assertFalse("name is empty", extractor.getName().isEmpty());
    }

    @Test
    public void testGetDescription() throws ParsingException {
        assertNotNull(extractor.getDescription());
        assertFalse(extractor.getDescription().isEmpty());
    }

    @Test
    public void testGetUploaderName() throws ParsingException {
        assertNotNull(extractor.getUploaderName());
        assertFalse(extractor.getUploaderName().isEmpty());
    }

    @Ignore // Currently there is no way get the length from restricted videos
    @Test
    public void testGetLength() throws ParsingException {
        assertTrue(extractor.getLength() > 0);
    }

    @Test
    public void testGetViews() throws ParsingException {
        assertTrue(extractor.getViewCount() > 0);
    }

    @Test
    public void testGetUploadDate() throws ParsingException {
        assertTrue(extractor.getUploadDate().length() > 0);
    }

    @Test
    public void testGetThumbnailUrl() throws ParsingException {
        assertTrue(extractor.getThumbnailUrl(),
                extractor.getThumbnailUrl().contains(HTTPS));
    }

    @Test
    public void testGetUploaderAvatarUrl() throws ParsingException {
        assertTrue(extractor.getUploaderAvatarUrl(),
                extractor.getUploaderAvatarUrl().contains(HTTPS));
    }

    // FIXME: 25.11.17 Are there no streams or are they not listed?
    @Ignore
    @Test
    public void testGetAudioStreams() throws IOException, ExtractionException {
        // audio streams are not always necessary
        assertFalse(extractor.getAudioStreams().isEmpty());
    }

    @Test
    public void testGetVideoStreams() throws IOException, ExtractionException {
        for (VideoStream s : extractor.getVideoStreams()) {
            assertTrue(s.getUrl(),
                    s.getUrl().contains(HTTPS));
            assertTrue(s.resolution.length() > 0);
            assertTrue(Integer.toString(s.getFormatId()),
                    0 <= s.getFormatId() && s.getFormatId() <= 4);
        }
    }


    @Test
    public void testGetSubtitlesListDefault() throws IOException, ExtractionException {
        // Video (/view?v=YQHsXMglC9A) set in the setUp() method has no captions => null
        assertNull(extractor.getSubtitlesDefault());
    }

    @Test
    public void testGetSubtitlesList() throws IOException, ExtractionException {
        // Video (/view?v=YQHsXMglC9A) set in the setUp() method has no captions => null
        assertNull(extractor.getSubtitles(SubtitlesFormat.VTT));
    }
}
