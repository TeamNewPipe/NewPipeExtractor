package org.schabi.newpipe.extractor.services.youtube.stream;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.VideoStream;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

/**
 * Test for {@link YoutubeStreamLinkHandlerFactory}
 */
public class YoutubeStreamExtractorControversialTest {
    private static YoutubeStreamExtractor extractor;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = (YoutubeStreamExtractor) YouTube
                .getStreamExtractor("https://www.youtube.com/watch?v=T4XJQO3qol8");
        extractor.fetchPage();
    }

    @Test
    public void testGetInvalidTimeStamp() throws ParsingException {
        assertTrue(extractor.getTimeStamp() + "", extractor.getTimeStamp() <= 0);
    }

    @Test
    public void testGetValidTimeStamp() throws IOException, ExtractionException {
        StreamExtractor extractor = YouTube.getStreamExtractor("https://youtu.be/FmG385_uUys?t=174");
        assertEquals(extractor.getTimeStamp() + "", "174");
    }

    @Test
    @Ignore
    public void testGetAgeLimit() throws ParsingException {
        assertEquals(18, extractor.getAgeLimit());
    }

    @Test
    public void testGetName() throws ParsingException {
        assertNotNull("name is null", extractor.getName());
        assertFalse("name is empty", extractor.getName().isEmpty());
    }

    @Test
    public void testGetDescription() throws ParsingException {
        assertNotNull(extractor.getDescription());
        assertFalse(extractor.getDescription().getContent().isEmpty());
    }

    @Test
    public void testGetUploaderName() throws ParsingException {
        assertNotNull(extractor.getUploaderName());
        assertFalse(extractor.getUploaderName().isEmpty());
    }

    @Test
    public void testGetLength() throws ParsingException {
        assertEquals(219, extractor.getLength());
    }

    @Test
    public void testGetViews() throws ParsingException {
        assertTrue(extractor.getViewCount() > 0);
    }

    @Test
    public void testGetTextualUploadDate() throws ParsingException {
        assertEquals("2010-09-09", extractor.getTextualUploadDate());
    }

    @Test
    public void testGetUploadDate() throws ParsingException, ParseException {
        final Calendar instance = Calendar.getInstance();
        instance.setTime(new SimpleDateFormat("yyyy-MM-dd").parse("2010-09-09"));
        assertEquals(instance, requireNonNull(extractor.getUploadDate()).date());
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
    public void testGetAudioStreams() throws IOException, ExtractionException {
        // audio streams are not always necessary
        assertFalse(extractor.getAudioStreams().isEmpty());
    }

    @Test
    public void testGetVideoStreams() throws IOException, ExtractionException {
        List<VideoStream> streams = new ArrayList<>();
        streams.addAll(extractor.getVideoStreams());
        streams.addAll(extractor.getVideoOnlyStreams());
        assertTrue(streams.size() > 0);
    }

    @Test
    public void testGetSubtitlesListDefault() throws IOException, ExtractionException {
        // Video (/view?v=T4XJQO3qol8) set in the setUp() method has at least auto-generated (English) captions
        assertFalse(extractor.getSubtitlesDefault().isEmpty());
    }

    @Test
    public void testGetSubtitlesList() throws IOException, ExtractionException {
        // Video (/view?v=T4XJQO3qol8) set in the setUp() method has at least auto-generated (English) captions
        assertFalse(extractor.getSubtitles(MediaFormat.TTML).isEmpty());
    }
}
